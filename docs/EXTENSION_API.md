# Data-only extension API

`io.github.janguenter.bluemap.ae2.api.Ae2ExtensionRegistry` lets a separate
BlueMap add-on add exact cable-bus part definitions and native AE2 Drive cell
mappings without exposing renderer callbacks or linking this add-on to the
other mod.

## Lifecycle and boundary

- Call `register` during the dependent add-on's normal entrypoint setup,
  before BlueMap creates resource-pack extensions.
- One call atomically registers one route. Duplicate or reserved route, part,
  or item IDs reject the complete call.
- Part and item IDs, plus extension-owned model paths, must use the declared
  owner namespace. Native Drive models must be below
  `<namespace>:block/drive/`.
- The returned `ExtensionRoute` starts `INACTIVE`. Activate it only after the
  owner has validated its exact artifacts and resources.
- Registration freezes when BlueMap begins creating either AE2 resource-pack
  extension. Late registration throws `IllegalStateException`; route state
  remains mutable after the definition freeze.
- `DISABLED` is sticky for the current JVM. A renderer or resource failure is
  isolated to the owning route.
- The registering add-on remains responsible for collecting and baking its
  model and texture resources. This API only contributes immutable render
  definitions and isolated route state.

## Ars Energistique 2.1.1-beta registration

The soft-dependent Ars Energistique add-on can register its exact data once
and retain the returned route handle:

```java
import io.github.janguenter.bluemap.ae2.api.Ae2ExtensionRegistry;
import io.github.janguenter.bluemap.ae2.api.CableBusPartDefinition;
import io.github.janguenter.bluemap.ae2.api.CableBusPartKind;
import io.github.janguenter.bluemap.ae2.api.ExtensionDefinition;
import io.github.janguenter.bluemap.ae2.api.ExtensionRoute;
import io.github.janguenter.bluemap.ae2.api.NativeDriveCellDefinition;

import java.util.List;

ExtensionRoute route = Ae2ExtensionRegistry.register(new ExtensionDefinition(
        "arseng-2.1.1-beta",
        "arseng",
        List.of(
                new CableBusPartDefinition(
                        "arseng:cable_source_acceptor",
                        CableBusPartKind.STATIC,
                        2,
                        2D,
                        14D,
                        List.of("arseng:part/source_acceptor")
                ),
                new CableBusPartDefinition(
                        "arseng:source_p2p_tunnel",
                        CableBusPartKind.P2P,
                        1,
                        2D,
                        14D,
                        List.of(
                                "ae2:part/p2p/p2p_tunnel_status_off",
                                "ae2:part/p2p/p2p_tunnel_frequency",
                                "arseng:part/source_p2p_tunnel"
                        )
                ),
                new CableBusPartDefinition(
                        "arseng:spell_p2p_tunnel",
                        CableBusPartKind.P2P,
                        1,
                        2D,
                        14D,
                        List.of(
                                "ae2:part/p2p/p2p_tunnel_status_off",
                                "ae2:part/p2p/p2p_tunnel_frequency",
                                "arseng:part/spell_p2p_tunnel"
                        )
                )
        ),
        List.of(
                new NativeDriveCellDefinition(
                        "arseng:source_storage_cell_1k",
                        "arseng:block/drive/cells/1k_source_cell"
                ),
                new NativeDriveCellDefinition(
                        "arseng:source_storage_cell_4k",
                        "arseng:block/drive/cells/4k_source_cell"
                ),
                new NativeDriveCellDefinition(
                        "arseng:source_storage_cell_16k",
                        "arseng:block/drive/cells/16k_source_cell"
                ),
                new NativeDriveCellDefinition(
                        "arseng:source_storage_cell_64k",
                        "arseng:block/drive/cells/64k_source_cell"
                ),
                new NativeDriveCellDefinition(
                        "arseng:source_storage_cell_256k",
                        "arseng:block/drive/cells/256k_source_cell"
                )
        )
));
```

After the Ars Energistique add-on's exact three-artifact gate and its resource
checks pass, it calls:

```java
route.activate();
```

If the gate does not pass, leave the route inactive. Call `deactivate()` when
a later non-terminal check no longer passes, or `disable()` for a terminal
failure. The P2P definitions retain AE2's persisted NBT `freq` only when it is
a `Short`; BlueMap interprets that signed storage as an unsigned `0..65535`
frequency for the neutral frequency layer.

The five chassis models may all resolve their face texture to
`arseng:block/source_drive_cell`; the Ars Energistique add-on must include that
texture in its own resource collection.
