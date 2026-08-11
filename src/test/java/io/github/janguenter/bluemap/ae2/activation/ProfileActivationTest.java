/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileActivationTest {

    @Test
    void startsInactiveAndActivatesWithTheExactTypedReason() {
        ProfileActivation activation = new ProfileActivation();

        assertFalse(activation.isActive());
        assertFalse(activation.isDisabled());
        assertEquals(ProfileActivation.Reason.NOT_INSTALLED, activation.snapshot().reason());

        activation.activate();

        assertTrue(activation.isActive());
        assertEquals(ProfileActivation.State.ACTIVE, activation.snapshot().state());
        assertEquals(ProfileActivation.Reason.EXACT_19_2_17, activation.snapshot().reason());
    }

    @Test
    void inactiveReasonsRemainTypedAndRecoverable() {
        ProfileActivation activation = new ProfileActivation();

        activation.inactive(ProfileActivation.Reason.ARTIFACT_NOT_FOUND);
        assertEquals(ProfileActivation.Reason.ARTIFACT_NOT_FOUND, activation.snapshot().reason());

        activation.inactive(ProfileActivation.Reason.MULTIPLE_ARTIFACTS);
        assertEquals(ProfileActivation.Reason.MULTIPLE_ARTIFACTS, activation.snapshot().reason());

        activation.inactive(ProfileActivation.Reason.REQUIRED_RESOURCES_MISMATCH);
        assertEquals(
                ProfileActivation.Reason.REQUIRED_RESOURCES_MISMATCH,
                activation.snapshot().reason()
        );
        assertFalse(activation.isActive());
    }

    @Test
    void operatorDisablementIsTerminalForTheJvmLifetime() {
        ProfileActivation activation = new ProfileActivation();

        activation.disable(ProfileActivation.Reason.OPERATOR_DISABLED);
        activation.activate();
        activation.inactive(ProfileActivation.Reason.ARTIFACT_NOT_FOUND);

        assertTrue(activation.isDisabled());
        assertEquals(ProfileActivation.Reason.OPERATOR_DISABLED, activation.snapshot().reason());
    }

    @Test
    void explicitDisablementRemainsTerminalUnderConcurrentUpdates() throws Exception {
        ProfileActivation activation = new ProfileActivation();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int index = 0; index < 64; index++) {
                int operation = index % 3;
                tasks.add(() -> {
                    if (operation == 0) {
                        activation.activate();
                    } else if (operation == 1) {
                        activation.inactive(ProfileActivation.Reason.ARTIFACT_READ_FAILED);
                    } else {
                        activation.disable(ProfileActivation.Reason.RENDER_CALLBACK_FAILED);
                    }
                    return null;
                });
            }
            executor.invokeAll(tasks).forEach(future -> {
                try {
                    future.get();
                } catch (Exception exception) {
                    throw new AssertionError(exception);
                }
            });
        } finally {
            executor.shutdownNow();
        }

        assertTrue(activation.isDisabled());
        assertEquals(
                ProfileActivation.Reason.RENDER_CALLBACK_FAILED,
                activation.snapshot().reason()
        );
    }

    @Test
    void stateTransitionsRejectSemanticallyInvalidReasons() {
        ProfileActivation activation = new ProfileActivation();

        assertThrows(
                IllegalArgumentException.class,
                () -> activation.inactive(ProfileActivation.Reason.EXACT_19_2_17)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> activation.inactive(ProfileActivation.Reason.OPERATOR_DISABLED)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> activation.disable(ProfileActivation.Reason.EXACT_19_2_17)
        );
    }
}
