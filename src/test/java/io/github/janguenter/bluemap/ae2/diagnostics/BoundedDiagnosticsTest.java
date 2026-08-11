/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.diagnostics;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundedDiagnosticsTest {

    @Test
    void failingDiagnosticSinkIsContained() {
        BoundedDiagnostics diagnostics = new BoundedDiagnostics(
                1,
                (level, key, message) -> {
                    throw new IllegalStateException("injected");
                }
        );

        assertFalse(diagnostics.tryReport(BoundedDiagnostics.Event.RESOURCE_CALLBACK_FAILED));
        assertFalse(diagnostics.tryReport(BoundedDiagnostics.Event.RESOURCE_CALLBACK_FAILED));
    }

    @Test
    void eachFixedEventIsBoundedIndependently() {
        List<Emission> emissions = new ArrayList<>();
        BoundedDiagnostics diagnostics = new BoundedDiagnostics(
                2,
                (level, key, message) -> emissions.add(new Emission(level, key, message))
        );

        assertTrue(diagnostics.tryReport(BoundedDiagnostics.Event.MALFORMED_BLOCK_DATA));
        assertTrue(diagnostics.tryReport(BoundedDiagnostics.Event.MALFORMED_BLOCK_DATA));
        assertFalse(diagnostics.tryReport(BoundedDiagnostics.Event.MALFORMED_BLOCK_DATA));
        assertTrue(diagnostics.tryReport(BoundedDiagnostics.Event.RENDER_FAILED));

        assertEquals(3, emissions.size());
        assertEquals("malformed-block-data", emissions.get(0).key());
        assertEquals("render-failed", emissions.get(2).key());
    }

    @Test
    void concurrentReportsCannotExceedThePerEventCap() throws Exception {
        List<Emission> emissions = Collections.synchronizedList(new ArrayList<>());
        BoundedDiagnostics diagnostics = new BoundedDiagnostics(
                3,
                (level, key, message) -> emissions.add(new Emission(level, key, message))
        );
        ExecutorService executor = Executors.newFixedThreadPool(12);
        try {
            List<Runnable> tasks = new ArrayList<>();
            for (int index = 0; index < 256; index++) {
                tasks.add(() -> diagnostics.tryReport(BoundedDiagnostics.Event.RENDER_FAILED));
            }
            for (Runnable task : tasks) {
                executor.submit(task);
            }
        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS));
        }

        assertEquals(3, emissions.size());
    }

    @Test
    void diagnosticVocabularyContainsNoDynamicContextSurface() {
        assertEquals(
                "unsupported-block-state",
                BoundedDiagnostics.Event.UNSUPPORTED_BLOCK_STATE.key()
        );
        assertEquals(
                "BlueMap AE2 exact 19.2.17 M2 cable-bus profile activated.",
                BoundedDiagnostics.Event.PROFILE_ACTIVATED.message()
        );
        assertEquals(
                "quartz-glass-render-failed",
                BoundedDiagnostics.Event.QUARTZ_GLASS_RENDER_FAILED.key()
        );
        for (BoundedDiagnostics.Event event : BoundedDiagnostics.Event.values()) {
            assertTrue(event.key().matches("[a-z0-9-]+"));
            assertFalse(event.message().contains("outside M0"));
            assertFalse(event.message().contains("\n"));
            assertFalse(event.message().contains("{"));
            assertFalse(event.message().contains("}"));
            assertFalse(event.message().contains("/"));
            assertFalse(event.message().contains("\\"));
        }
    }

    @Test
    void capAndSinkMustBeValid() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BoundedDiagnostics(0, (level, key, message) -> { })
        );
        assertThrows(NullPointerException.class, () -> new BoundedDiagnostics(1, null));
    }

    private record Emission(BoundedDiagnostics.Level level, String key, String message) {
    }
}
