# SPDX-License-Identifier: LGPL-3.0-only
# Wait for stable S1 and exact M4/M5 persisted review states; require two consecutive exact checks.
scoreboard objectives add ae2m3s dummy
scoreboard players add #attempts ae2m3s 1
scoreboard players set #failures ae2m3s 0

# ae2-m3d-01 isolated-storage-catalog
execute unless block 297 100 261 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 301 100 261 ae2:4k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 261 ae2:16k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 309 100 261 ae2:64k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 313 100 261 ae2:256k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1

# ae2-m3d-02 unit-plus-1k-storage
execute unless block 297 100 265 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 298 100 265 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1

# ae2-m3d-03 accelerator-plus-1k-storage
execute unless block 302 100 265 ae2:crafting_accelerator[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 303 100 265 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1

# ae2-m3d-04 unit-storage-accelerator-line
execute unless block 307 100 265 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 265 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 309 100 265 ae2:crafting_accelerator[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1

# ae2-m3d-05 two-by-two-plane
execute unless block 312 100 264 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 313 100 264 ae2:crafting_accelerator[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 312 100 265 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 313 100 265 ae2:4k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1

# ae2-m3d-06 powered-two-by-two-by-two-all-eight
execute unless block 297 100 269 ae2:crafting_unit[formed=true,powered=true] run scoreboard players add #failures ae2m3s 1
execute unless block 298 100 269 ae2:crafting_accelerator[formed=true,powered=true] run scoreboard players add #failures ae2m3s 1
execute unless block 297 100 270 ae2:1k_crafting_storage[formed=true,powered=true] run scoreboard players add #failures ae2m3s 1
execute unless block 298 100 270 ae2:4k_crafting_storage[formed=true,powered=true] run scoreboard players add #failures ae2m3s 1
execute unless block 297 101 269 ae2:16k_crafting_storage[formed=true,powered=true] run scoreboard players add #failures ae2m3s 1
execute unless block 298 101 269 ae2:64k_crafting_storage[formed=true,powered=true] run scoreboard players add #failures ae2m3s 1
execute unless block 297 101 270 ae2:256k_crafting_storage[formed=true,powered=true] run scoreboard players add #failures ae2m3s 1
execute unless block 298 101 270 ae2:crafting_monitor[formed=true,powered=true,facing=up,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 298 101 270 {paintedColor:16b} run scoreboard players add #failures ae2m3s 1
execute unless block 297 98 269 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 297 99 269 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 297 99 269 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}} run scoreboard players add #failures ae2m3s 1

# ae2-m3d-07 unpowered-three-by-three-by-three-hard-culling
execute unless block 304 100 269 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 269 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 306 100 269 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 304 100 270 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 270 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 306 100 270 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 304 100 271 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 271 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 306 100 271 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 304 101 269 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 305 101 269 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 306 101 269 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 304 101 270 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 305 101 270 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 306 101 270 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 304 101 271 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 305 101 271 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 306 101 271 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 304 102 269 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 305 102 269 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 306 102 269 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 304 102 270 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 305 102 270 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 306 102 270 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 304 102 271 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 305 102 271 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 306 102 271 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1

# ae2-m3d-08 monitor-paint-orientation-catalog
execute unless block 298 100 276 ae2:crafting_monitor[formed=true,powered=false,facing=south,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 298 100 276 {paintedColor:0b} run scoreboard players add #failures ae2m3s 1
execute unless block 298 100 275 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 303 100 276 ae2:crafting_monitor[formed=true,powered=false,facing=north,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 303 100 276 {paintedColor:1b} run scoreboard players add #failures ae2m3s 1
execute unless block 303 100 277 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 276 ae2:crafting_monitor[formed=true,powered=false,facing=east,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 276 {paintedColor:2b} run scoreboard players add #failures ae2m3s 1
execute unless block 307 100 276 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 313 100 276 ae2:crafting_monitor[formed=true,powered=false,facing=west,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 313 100 276 {paintedColor:3b} run scoreboard players add #failures ae2m3s 1
execute unless block 314 100 276 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 298 100 281 ae2:crafting_monitor[formed=true,powered=false,facing=up,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 298 100 281 {paintedColor:4b} run scoreboard players add #failures ae2m3s 1
execute unless block 298 99 281 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 303 100 281 ae2:crafting_monitor[formed=true,powered=false,facing=down,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 303 100 281 {paintedColor:5b} run scoreboard players add #failures ae2m3s 1
execute unless block 303 101 281 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 281 ae2:crafting_monitor[formed=true,powered=false,facing=south,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 281 {paintedColor:6b} run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 280 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 313 100 281 ae2:crafting_monitor[formed=true,powered=false,facing=north,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 313 100 281 {paintedColor:7b} run scoreboard players add #failures ae2m3s 1
execute unless block 313 100 282 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 298 100 286 ae2:crafting_monitor[formed=true,powered=false,facing=east,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 298 100 286 {paintedColor:8b} run scoreboard players add #failures ae2m3s 1
execute unless block 297 100 286 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 303 100 286 ae2:crafting_monitor[formed=true,powered=false,facing=west,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 303 100 286 {paintedColor:9b} run scoreboard players add #failures ae2m3s 1
execute unless block 304 100 286 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 286 ae2:crafting_monitor[formed=true,powered=false,facing=up,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 286 {paintedColor:10b} run scoreboard players add #failures ae2m3s 1
execute unless block 308 99 286 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 313 100 286 ae2:crafting_monitor[formed=true,powered=false,facing=down,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 313 100 286 {paintedColor:11b} run scoreboard players add #failures ae2m3s 1
execute unless block 313 101 286 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 298 100 291 ae2:crafting_monitor[formed=true,powered=false,facing=south,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 298 100 291 {paintedColor:12b} run scoreboard players add #failures ae2m3s 1
execute unless block 298 100 290 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 303 100 291 ae2:crafting_monitor[formed=true,powered=false,facing=north,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 303 100 291 {paintedColor:13b} run scoreboard players add #failures ae2m3s 1
execute unless block 303 100 292 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 291 ae2:crafting_monitor[formed=true,powered=false,facing=east,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 291 {paintedColor:14b} run scoreboard players add #failures ae2m3s 1
execute unless block 307 100 291 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 313 100 291 ae2:crafting_monitor[formed=true,powered=false,facing=west,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 313 100 291 {paintedColor:15b} run scoreboard players add #failures ae2m3s 1
execute unless block 314 100 291 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 298 100 296 ae2:crafting_monitor[formed=true,powered=false,facing=up,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 298 100 296 {paintedColor:16b} run scoreboard players add #failures ae2m3s 1
execute unless block 298 99 296 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1

# ae2-m3d-09 compatible-extension-atomic-fallback
execute unless block 318 100 261 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 317 100 261 megacells:mega_crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless block 319 100 261 expandedae:exp_crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1

# ae2-m3e-01 formed-unpowered-xz-chunk-boundary
execute unless block 286 100 270 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 286 100 270 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 287 100 270 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 270 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 288 100 270 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 288 100 270 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 286 100 271 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 286 100 271 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 287 100 271 ae2:quantum_link[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 271 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 288 100 271 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 288 100 271 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 286 100 272 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 286 100 272 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 287 100 272 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 272 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 288 100 272 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 288 100 272 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1

# ae2-m3e-02 formed-unpowered-xy
execute unless block 282 100 276 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 100 276 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 283 100 276 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 283 100 276 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 284 100 276 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 284 100 276 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 282 101 276 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 101 276 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 283 101 276 ae2:quantum_link[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 283 101 276 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 284 101 276 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 284 101 276 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 282 102 276 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 102 276 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 283 102 276 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 283 102 276 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 284 102 276 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 284 102 276 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1

# ae2-m3e-03 formed-unpowered-yz
execute unless block 290 100 270 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 270 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 290 101 270 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 290 101 270 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 290 102 270 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 290 102 270 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 290 100 271 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 271 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 290 101 271 ae2:quantum_link[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 290 101 271 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 290 102 271 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 290 102 271 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 290 100 272 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 272 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 290 101 272 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 290 101 272 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 290 102 272 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 290 102 272 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1

# ae2-m3f-01 non-lumen-palette-faces-and-layering
execute unless block 282 100 209 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 100 209 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 282 100 209 {dots:[B;1b,-120b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 286 100 209 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 286 100 209 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 286 100 209 {dots:[B;1b,-120b,8b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 290 100 209 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 209 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 209 {dots:[B;1b,-120b,16b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 294 100 209 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 100 209 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 294 100 209 {dots:[B;1b,-120b,24b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 298 100 209 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 298 100 209 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 298 100 209 {dots:[B;1b,-120b,32b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 302 100 209 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 209 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 209 {dots:[B;1b,-120b,40b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 306 100 209 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 306 100 209 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 306 100 209 {dots:[B;1b,-120b,48b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 310 100 209 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 310 100 209 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 310 100 209 {dots:[B;1b,-120b,56b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 282 100 213 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 100 213 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 282 100 213 {dots:[B;1b,-120b,64b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 286 100 213 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 286 100 213 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 286 100 213 {dots:[B;1b,-120b,72b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 290 100 213 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 213 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 213 {dots:[B;1b,-120b,80b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 294 100 213 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 100 213 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 294 100 213 {dots:[B;1b,-120b,88b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 298 100 213 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 298 100 213 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 298 100 213 {dots:[B;1b,-120b,96b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 302 100 213 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 213 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 213 {dots:[B;1b,-120b,104b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 306 100 213 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 306 100 213 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 306 100 213 {dots:[B;1b,-120b,112b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 310 100 213 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 310 100 213 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 310 100 213 {dots:[B;1b,-120b,120b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 282 100 217 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 100 217 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 282 100 217 {dots:[B;1b,-87b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 288 100 217 ae2:paint[facing=down,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 288 100 217 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 288 100 217 {dots:[B;1b,-86b,9b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 294 100 217 ae2:paint[facing=south,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 100 217 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 294 100 217 {dots:[B;1b,-85b,18b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 300 100 217 ae2:paint[facing=north,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 300 100 217 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 300 100 217 {dots:[B;1b,-84b,27b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 306 100 217 ae2:paint[facing=east,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 306 100 217 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 306 100 217 {dots:[B;1b,-83b,36b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 312 100 217 ae2:paint[facing=west,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 312 100 217 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 312 100 217 {dots:[B;1b,-82b,45b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 318 100 217 ae2:paint[facing=up,light_level=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 318 100 217 {id:"ae2:paint"} run scoreboard players add #failures ae2m3s 1
execute unless data block 318 100 217 {dots:[B;3b,-120b,32b,-103b,80b,-86b,120b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]} run scoreboard players add #failures ae2m3s 1
execute unless block 282 99 209 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 286 99 209 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 290 99 209 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 294 99 209 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 298 99 209 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 302 99 209 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 306 99 209 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 310 99 209 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 282 99 213 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 286 99 213 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 290 99 213 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 294 99 213 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 298 99 213 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 302 99 213 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 306 99 213 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 310 99 213 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 282 99 217 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 288 101 217 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 294 100 216 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 300 100 218 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 217 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 313 100 217 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1
execute unless block 318 99 217 minecraft:smooth_stone run scoreboard players add #failures ae2m3s 1

# ae2-m3f-02 closed-sky-stone-chest-facings
execute unless block 282 100 222 ae2:sky_stone_chest[facing=south,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 100 222 {id:"ae2:sky_chest"} run scoreboard players add #failures ae2m3s 1
execute unless block 288 100 222 ae2:sky_stone_chest[facing=west,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 288 100 222 {id:"ae2:sky_chest"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 100 222 ae2:sky_stone_chest[facing=north,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 100 222 {id:"ae2:sky_chest"} run scoreboard players add #failures ae2m3s 1
execute unless block 300 100 222 ae2:sky_stone_chest[facing=east,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 300 100 222 {id:"ae2:sky_chest"} run scoreboard players add #failures ae2m3s 1
execute unless block 282 100 226 ae2:smooth_sky_stone_chest[facing=south,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 100 226 {id:"ae2:sky_chest"} run scoreboard players add #failures ae2m3s 1
execute unless block 288 100 226 ae2:smooth_sky_stone_chest[facing=west,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 288 100 226 {id:"ae2:sky_chest"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 100 226 ae2:smooth_sky_stone_chest[facing=north,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 100 226 {id:"ae2:sky_chest"} run scoreboard players add #failures ae2m3s 1
execute unless block 300 100 226 ae2:smooth_sky_stone_chest[facing=east,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 300 100 226 {id:"ae2:sky_chest"} run scoreboard players add #failures ae2m3s 1

# ae2-m3f-03 neutral-crank-six-facings
execute unless block 306 100 222 ae2:crank[facing=down] run scoreboard players add #failures ae2m3s 1
execute unless data block 306 100 222 {id:"ae2:crank"} run scoreboard players add #failures ae2m3s 1
execute unless block 312 100 222 ae2:crank[facing=up] run scoreboard players add #failures ae2m3s 1
execute unless data block 312 100 222 {id:"ae2:crank"} run scoreboard players add #failures ae2m3s 1
execute unless block 318 100 222 ae2:crank[facing=north] run scoreboard players add #failures ae2m3s 1
execute unless data block 318 100 222 {id:"ae2:crank"} run scoreboard players add #failures ae2m3s 1
execute unless block 306 100 226 ae2:crank[facing=south] run scoreboard players add #failures ae2m3s 1
execute unless data block 306 100 226 {id:"ae2:crank"} run scoreboard players add #failures ae2m3s 1
execute unless block 312 100 226 ae2:crank[facing=west] run scoreboard players add #failures ae2m3s 1
execute unless data block 312 100 226 {id:"ae2:crank"} run scoreboard players add #failures ae2m3s 1
execute unless block 318 100 226 ae2:crank[facing=east] run scoreboard players add #failures ae2m3s 1
execute unless data block 318 100 226 {id:"ae2:crank"} run scoreboard players add #failures ae2m3s 1
execute unless block 306 101 222 ae2:charger[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 306 101 222 {id:"ae2:charger"} run scoreboard players add #failures ae2m3s 1
execute unless block 312 99 222 ae2:charger[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 312 99 222 {id:"ae2:charger"} run scoreboard players add #failures ae2m3s 1
execute unless block 318 100 223 ae2:charger[facing=east,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 318 100 223 {id:"ae2:charger"} run scoreboard players add #failures ae2m3s 1
execute unless block 306 100 225 ae2:charger[facing=east,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 306 100 225 {id:"ae2:charger"} run scoreboard players add #failures ae2m3s 1
execute unless block 313 100 226 ae2:charger[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 313 100 226 {id:"ae2:charger"} run scoreboard players add #failures ae2m3s 1
execute unless block 317 100 226 ae2:charger[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 317 100 226 {id:"ae2:charger"} run scoreboard players add #failures ae2m3s 1

# ae2-m3f-04 neutral-inscriber-all-facing-spin-states
execute unless block 282 98 229 ae2:inscriber[facing=down,spin=0,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 98 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 287 98 229 ae2:inscriber[facing=down,spin=1,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 287 98 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 292 98 229 ae2:inscriber[facing=down,spin=2,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 292 98 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 297 98 229 ae2:inscriber[facing=down,spin=3,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 297 98 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 302 98 229 ae2:inscriber[facing=up,spin=0,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 302 98 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 307 98 229 ae2:inscriber[facing=up,spin=1,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 307 98 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 312 98 229 ae2:inscriber[facing=up,spin=2,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 312 98 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 317 98 229 ae2:inscriber[facing=up,spin=3,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 317 98 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 282 102 229 ae2:inscriber[facing=north,spin=0,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 102 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 287 102 229 ae2:inscriber[facing=north,spin=1,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 287 102 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 292 102 229 ae2:inscriber[facing=north,spin=2,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 292 102 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 297 102 229 ae2:inscriber[facing=north,spin=3,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 297 102 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 302 102 229 ae2:inscriber[facing=south,spin=0,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 302 102 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 307 102 229 ae2:inscriber[facing=south,spin=1,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 307 102 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 312 102 229 ae2:inscriber[facing=south,spin=2,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 312 102 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 317 102 229 ae2:inscriber[facing=south,spin=3,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 317 102 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 282 106 229 ae2:inscriber[facing=west,spin=0,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 106 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 287 106 229 ae2:inscriber[facing=west,spin=1,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 287 106 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 292 106 229 ae2:inscriber[facing=west,spin=2,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 292 106 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 297 106 229 ae2:inscriber[facing=west,spin=3,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 297 106 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 302 106 229 ae2:inscriber[facing=east,spin=0,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 302 106 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 307 106 229 ae2:inscriber[facing=east,spin=1,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 307 106 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 312 106 229 ae2:inscriber[facing=east,spin=2,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 312 106 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 317 106 229 ae2:inscriber[facing=east,spin=3,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 317 106 229 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1

# ae2-m3f-05 spatial-pylon-isolated-and-three-axis-lines
execute unless block 282 104 208 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 104 208 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 286 104 208 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 286 104 208 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 287 104 208 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 287 104 208 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 288 104 208 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 288 104 208 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 102 208 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 102 208 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 103 208 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 103 208 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 104 208 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 104 208 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 300 104 208 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 300 104 208 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 300 104 209 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 300 104 209 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 300 104 210 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 300 104 210 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1

# ae2-m3f-06 spatial-pylon-perpendicular-component-unformed
execute unless block 310 104 214 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 310 104 214 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 311 104 214 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 311 104 214 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 310 104 215 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 310 104 215 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1

# ae2-m3f-07 spatial-pylon-branched-component-unformed
execute unless block 316 103 214 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 316 103 214 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 315 103 214 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 315 103 214 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 317 103 214 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 317 103 214 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 316 104 214 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 316 104 214 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1

# ae2-s1-01 all-native-parts-installed-down
execute unless block 209 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 209 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:quartz_fiber"},facadeDown:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 212 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 212 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:toggle_bus"},facadeDown:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 215 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 215 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:inverted_toggle_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 218 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 218 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 221 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 221 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:monitor",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 224 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 224 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:semi_dark_monitor",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 227 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 227 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:dark_monitor",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 230 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 230 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:storage_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 233 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 233 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:import_bus"},facadeDown:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 236 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 236 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:export_bus"},facadeDown:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 239 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 239 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:level_emitter"},facadeDown:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 242 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 242 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:energy_level_emitter"}} run scoreboard players add #failures ae2m3s 1
execute unless block 245 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 245 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 248 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 248 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 251 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 251 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:pattern_encoding_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 254 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:crafting_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 257 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 257 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:terminal",spin:0b},facadeDown:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 260 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 260 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:storage_monitor",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 263 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 263 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:conversion_monitor",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 266 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 266 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:cable_pattern_provider"}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 269 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:cable_interface"}} run scoreboard players add #failures ae2m3s 1
execute unless block 272 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 272 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:pattern_access_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 275 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 275 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:cable_energy_acceptor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 278 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 278 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:me_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 281 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 281 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:redstone_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 284 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 284 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:item_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 287 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:fluid_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 290 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:fe_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 293 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 293 100 313 {cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:light_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-02 all-native-parts-installed-up
execute unless block 296 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 296 100 313 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:quartz_fiber"}} run scoreboard players add #failures ae2m3s 1
execute unless block 299 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 299 100 313 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:toggle_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 302 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 313 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:inverted_toggle_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 305 100 313 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 313 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:monitor",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 311 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 311 100 313 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:semi_dark_monitor",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 314 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 314 100 313 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:dark_monitor",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 317 100 313 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 317 100 313 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:storage_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 209 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 209 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:import_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 212 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 212 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:export_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 215 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 215 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:level_emitter"}} run scoreboard players add #failures ae2m3s 1
execute unless block 218 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 218 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:energy_level_emitter"}} run scoreboard players add #failures ae2m3s 1
execute unless block 221 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 221 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 224 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 224 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 227 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 227 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:pattern_encoding_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 230 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 230 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:crafting_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 233 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 233 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 236 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 236 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:storage_monitor",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 239 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 239 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:conversion_monitor",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 242 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 242 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:cable_pattern_provider"}} run scoreboard players add #failures ae2m3s 1
execute unless block 245 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 245 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:cable_interface"}} run scoreboard players add #failures ae2m3s 1
execute unless block 248 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 248 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:pattern_access_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 251 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 251 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:cable_energy_acceptor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 254 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:me_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 257 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 257 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:redstone_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 260 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 260 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:item_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 263 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 263 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:fluid_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 266 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 266 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:fe_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 269 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:light_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-03 all-native-parts-installed-north
execute unless block 272 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 272 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:quartz_fiber"}} run scoreboard players add #failures ae2m3s 1
execute unless block 275 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 275 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:toggle_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 278 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 278 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:inverted_toggle_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 281 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 281 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 284 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 284 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:monitor",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 287 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:semi_dark_monitor",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 290 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:dark_monitor",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 293 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 293 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:storage_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 296 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 296 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:import_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 299 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 299 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:export_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 302 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:level_emitter"}} run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 305 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:energy_level_emitter"}} run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 311 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 311 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 314 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 314 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:pattern_encoding_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 317 100 318 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 317 100 318 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:crafting_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 209 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 209 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 212 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 212 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:storage_monitor",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 215 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 215 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:conversion_monitor",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 218 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 218 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:cable_pattern_provider"}} run scoreboard players add #failures ae2m3s 1
execute unless block 221 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 221 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:cable_interface"}} run scoreboard players add #failures ae2m3s 1
execute unless block 224 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 224 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:pattern_access_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 227 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 227 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:cable_energy_acceptor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 230 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 230 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:me_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 233 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 233 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:redstone_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 236 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 236 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:item_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 239 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 239 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:fluid_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 242 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 242 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:fe_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 245 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 245 100 323 {cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:light_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-04 all-native-parts-installed-south
execute unless block 248 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 248 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:quartz_fiber"}} run scoreboard players add #failures ae2m3s 1
execute unless block 251 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 251 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:toggle_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 254 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:inverted_toggle_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 257 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 257 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 260 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 260 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:monitor",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 263 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 263 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:semi_dark_monitor",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 266 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 266 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:dark_monitor",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 269 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:storage_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 272 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 272 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:import_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 275 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 275 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:export_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 278 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 278 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:level_emitter"}} run scoreboard players add #failures ae2m3s 1
execute unless block 281 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 281 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:energy_level_emitter"}} run scoreboard players add #failures ae2m3s 1
execute unless block 284 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 284 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 287 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 290 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:pattern_encoding_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 293 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 293 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:crafting_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 296 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 296 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 299 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 299 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:storage_monitor",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 302 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:conversion_monitor",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 305 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:cable_pattern_provider"}} run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:cable_interface"}} run scoreboard players add #failures ae2m3s 1
execute unless block 311 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 311 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:pattern_access_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 314 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 314 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:cable_energy_acceptor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 317 100 323 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 317 100 323 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:me_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 209 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 209 100 328 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:redstone_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 212 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 212 100 328 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:item_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 215 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 215 100 328 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:fluid_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 218 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 218 100 328 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:fe_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 221 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 221 100 328 {cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:light_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-05 all-native-parts-installed-west
execute unless block 224 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 224 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:quartz_fiber"}} run scoreboard players add #failures ae2m3s 1
execute unless block 227 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 227 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:toggle_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 230 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 230 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:inverted_toggle_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 233 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 233 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 236 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 236 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:monitor",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 239 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 239 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:semi_dark_monitor",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 242 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 242 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:dark_monitor",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 245 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 245 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:storage_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 248 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 248 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:import_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 251 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 251 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:export_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 254 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:level_emitter"}} run scoreboard players add #failures ae2m3s 1
execute unless block 257 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 257 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:energy_level_emitter"}} run scoreboard players add #failures ae2m3s 1
execute unless block 260 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 260 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 263 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 263 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 266 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 266 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:pattern_encoding_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 269 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:crafting_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 272 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 272 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 275 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 275 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:storage_monitor",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 278 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 278 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:conversion_monitor",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 281 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 281 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:cable_pattern_provider"}} run scoreboard players add #failures ae2m3s 1
execute unless block 284 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 284 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:cable_interface"}} run scoreboard players add #failures ae2m3s 1
execute unless block 287 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:pattern_access_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 290 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:cable_energy_acceptor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 293 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 293 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:me_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 296 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 296 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:redstone_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 299 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 299 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:item_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 302 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:fluid_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 305 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:fe_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 328 {cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:light_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-06 all-native-parts-installed-east
execute unless block 311 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 311 100 328 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:quartz_fiber"}} run scoreboard players add #failures ae2m3s 1
execute unless block 314 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 314 100 328 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:toggle_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 317 100 328 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 317 100 328 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:inverted_toggle_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 209 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 209 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 212 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 212 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:monitor",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 215 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 215 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:semi_dark_monitor",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 218 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 218 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:dark_monitor",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 221 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 221 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:storage_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 224 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 224 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:import_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 227 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 227 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:export_bus"}} run scoreboard players add #failures ae2m3s 1
execute unless block 230 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 230 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:level_emitter"}} run scoreboard players add #failures ae2m3s 1
execute unless block 233 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 233 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:energy_level_emitter"}} run scoreboard players add #failures ae2m3s 1
execute unless block 236 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 236 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 239 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 239 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 242 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 242 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:pattern_encoding_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 245 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 245 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:crafting_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 248 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 248 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 251 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 251 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:storage_monitor",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 254 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:conversion_monitor",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 257 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 257 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:cable_pattern_provider"}} run scoreboard players add #failures ae2m3s 1
execute unless block 260 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 260 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:cable_interface"}} run scoreboard players add #failures ae2m3s 1
execute unless block 263 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 263 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:pattern_access_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 266 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 266 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:cable_energy_acceptor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 269 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:me_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 272 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 272 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:redstone_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 275 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 275 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:item_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 278 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 278 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:fluid_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 281 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 281 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:fe_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 284 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 284 100 333 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:light_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-07 annihilation-plane-all-sixteen-masks
execute unless block 287 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 290 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 293 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 293 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 296 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 296 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 299 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 299 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 302 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 305 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 311 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 311 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:glass"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 314 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 314 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 317 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 317 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 209 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 209 100 338 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 212 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 212 100 338 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 215 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 215 100 338 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 218 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 218 100 338 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 221 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 221 100 338 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 289 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 289 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 293 100 334 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 293 100 334 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 296 100 334 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 296 100 334 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 295 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 295 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 300 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 300 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 303 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 303 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 301 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 301 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 306 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 306 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 334 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 305 100 334 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 309 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 309 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 334 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 334 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 307 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 307 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 311 100 332 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 311 100 332 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 314 100 332 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 314 100 332 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 313 100 333 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 313 100 333 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 317 100 332 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 317 100 332 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 317 100 334 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 317 100 334 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 209 100 337 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 209 100 337 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 209 100 339 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 209 100 339 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 208 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 208 100 338 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 212 100 337 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 212 100 337 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 213 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 213 100 338 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 215 100 337 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 215 100 337 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 216 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 216 100 338 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 214 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 214 100 338 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 218 100 337 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 218 100 337 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 219 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 219 100 338 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 218 100 339 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 218 100 339 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 221 100 337 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 221 100 337 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 222 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 222 100 338 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 221 100 339 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 221 100 339 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 220 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 220 100 338 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-08 formation-plane-all-sixteen-masks
execute unless block 224 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 224 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 227 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 227 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 230 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 230 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 233 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 233 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 236 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 236 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 239 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 239 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 242 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 242 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 245 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 245 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 248 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 248 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 251 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 251 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 254 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 257 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 257 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 260 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 260 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 263 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 263 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 266 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 266 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 269 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 228 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 228 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 230 99 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 230 99 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 233 99 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 233 99 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 234 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 234 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 235 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 235 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 238 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 238 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 240 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 240 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 241 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 241 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 242 99 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 242 99 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 244 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 244 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 245 99 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 245 99 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 246 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 246 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 248 101 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 248 101 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 251 101 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 251 101 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 252 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 252 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 101 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 254 101 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 99 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 254 99 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 257 101 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 257 101 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 257 99 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 257 99 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 258 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 258 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 260 101 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 260 101 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 259 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 259 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 263 101 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 263 101 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 262 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 262 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 264 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 264 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 266 101 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 266 101 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 265 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 265 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 266 99 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 266 99 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 101 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 269 101 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 268 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 268 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 99 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 269 99 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 270 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 270 100 338 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-09 all-p2p-types-frequency-0
execute unless block 272 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 272 100 338 {cable:{id:"ae2:fluix_smart_cable"},down:{id:"ae2:me_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 275 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 275 100 338 {cable:{id:"ae2:fluix_smart_cable"},up:{id:"ae2:redstone_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 278 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 278 100 338 {cable:{id:"ae2:fluix_smart_cable"},north:{id:"ae2:item_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 281 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 281 100 338 {cable:{id:"ae2:fluix_smart_cable"},south:{id:"ae2:fluid_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 284 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 284 100 338 {cable:{id:"ae2:fluix_smart_cable"},west:{id:"ae2:fe_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1
execute unless block 287 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 338 {cable:{id:"ae2:fluix_smart_cable"},east:{id:"ae2:light_p2p_tunnel",freq:0s}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-10 all-p2p-types-frequency-4660
execute unless block 290 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 338 {cable:{id:"ae2:fluix_smart_cable"},down:{id:"ae2:me_p2p_tunnel",freq:4660s}} run scoreboard players add #failures ae2m3s 1
execute unless block 293 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 293 100 338 {cable:{id:"ae2:fluix_smart_cable"},up:{id:"ae2:redstone_p2p_tunnel",freq:4660s}} run scoreboard players add #failures ae2m3s 1
execute unless block 296 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 296 100 338 {cable:{id:"ae2:fluix_smart_cable"},north:{id:"ae2:item_p2p_tunnel",freq:4660s}} run scoreboard players add #failures ae2m3s 1
execute unless block 299 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 299 100 338 {cable:{id:"ae2:fluix_smart_cable"},south:{id:"ae2:fluid_p2p_tunnel",freq:4660s}} run scoreboard players add #failures ae2m3s 1
execute unless block 302 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 338 {cable:{id:"ae2:fluix_smart_cable"},west:{id:"ae2:fe_p2p_tunnel",freq:4660s}} run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 305 100 338 {cable:{id:"ae2:fluix_smart_cable"},east:{id:"ae2:light_p2p_tunnel",freq:4660s}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-11 all-p2p-types-frequency-65535
execute unless block 308 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 338 {cable:{id:"ae2:fluix_smart_cable"},down:{id:"ae2:me_p2p_tunnel",freq:-1s}} run scoreboard players add #failures ae2m3s 1
execute unless block 311 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 311 100 338 {cable:{id:"ae2:fluix_smart_cable"},up:{id:"ae2:redstone_p2p_tunnel",freq:-1s}} run scoreboard players add #failures ae2m3s 1
execute unless block 314 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 314 100 338 {cable:{id:"ae2:fluix_smart_cable"},north:{id:"ae2:item_p2p_tunnel",freq:-1s}} run scoreboard players add #failures ae2m3s 1
execute unless block 317 100 338 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 317 100 338 {cable:{id:"ae2:fluix_smart_cable"},south:{id:"ae2:fluid_p2p_tunnel",freq:-1s}} run scoreboard players add #failures ae2m3s 1
execute unless block 209 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 209 100 343 {cable:{id:"ae2:fluix_smart_cable"},west:{id:"ae2:fe_p2p_tunnel",freq:-1s}} run scoreboard players add #failures ae2m3s 1
execute unless block 212 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 212 100 343 {cable:{id:"ae2:fluix_smart_cable"},east:{id:"ae2:light_p2p_tunnel",freq:-1s}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-12 dense-anchor-legality-and-persistent-spin-control
execute unless block 215 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 215 100 343 {cable:{id:"ae2:fluix_smart_dense_cable"},down:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 218 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 218 100 343 {cable:{id:"ae2:fluix_smart_dense_cable"},up:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 221 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 221 100 343 {cable:{id:"ae2:fluix_smart_dense_cable"},north:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 224 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 224 100 343 {cable:{id:"ae2:fluix_smart_dense_cable"},south:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 227 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 227 100 343 {cable:{id:"ae2:fluix_smart_dense_cable"},west:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 230 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 230 100 343 {cable:{id:"ae2:fluix_smart_dense_cable"},east:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 233 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 233 100 343 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:monitor",spin:4b}} run scoreboard players add #failures ae2m3s 1
execute unless block 236 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 236 100 343 {cable:{id:"ae2:fluix_smart_dense_cable"}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-13 part-and-facade-only-buses
execute unless block 239 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 239 100 343 {down:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 242 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 242 100 343 {up:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 245 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 245 100 343 {north:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 248 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 248 100 343 {south:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 251 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 251 100 343 {west:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 254 100 343 {east:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 257 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 257 100 343 {down:{id:"ae2:cable_anchor"},up:{id:"ae2:cable_anchor"},north:{id:"ae2:cable_anchor"},south:{id:"ae2:cable_anchor"},west:{id:"ae2:cable_anchor"},east:{id:"ae2:cable_anchor"}} run scoreboard players add #failures ae2m3s 1
execute unless block 260 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 260 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-14 facade-mask-00-through-10
execute unless block 263 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 263 100 343 {cable:{id:"ae2:fluix_covered_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 266 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 266 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:quartz_glass"}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 269 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"ae2:quartz_vibrant_glass"}} run scoreboard players add #failures ae2m3s 1
execute unless block 272 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 272 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:controller",Properties:{state:"offline",type:"block"}},facadeUp:{Name:"ae2:controller",Properties:{state:"offline",type:"block"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 275 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 275 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"ae2:1k_crafting_storage",Properties:{formed:"false",powered:"false"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 278 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 278 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:quartz_glass"},facadeNorth:{Name:"ae2:quartz_vibrant_glass"}} run scoreboard players add #failures ae2m3s 1
execute unless block 281 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 281 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"ae2:4k_crafting_storage",Properties:{formed:"false",powered:"false"}},facadeNorth:{Name:"ae2:4k_crafting_storage",Properties:{formed:"false",powered:"false"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 284 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 284 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:16k_crafting_storage",Properties:{formed:"false",powered:"false"}},facadeUp:{Name:"ae2:16k_crafting_storage",Properties:{formed:"false",powered:"false"}},facadeNorth:{Name:"ae2:16k_crafting_storage",Properties:{formed:"false",powered:"false"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 287 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeSouth:{Name:"ae2:64k_crafting_storage",Properties:{formed:"false",powered:"false"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 290 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:256k_crafting_storage",Properties:{formed:"false",powered:"false"}},facadeSouth:{Name:"ae2:256k_crafting_storage",Properties:{formed:"false",powered:"false"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 293 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 293 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"ae2:crafting_monitor",Properties:{facing:"north",formed:"false",powered:"false",spin:"0"}},facadeSouth:{Name:"ae2:crafting_monitor",Properties:{facing:"north",formed:"false",powered:"false",spin:"0"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 101 343 ae2:quartz_vibrant_glass[] run scoreboard players add #failures ae2m3s 1

# ae2-s1-15 facade-mask-11-through-21
execute unless block 296 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 296 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:crafting_unit",Properties:{formed:"false",powered:"false"}},facadeUp:{Name:"ae2:crafting_unit",Properties:{formed:"false",powered:"false"}},facadeSouth:{Name:"ae2:crafting_unit",Properties:{formed:"false",powered:"false"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 299 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 299 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"ae2:crafting_accelerator",Properties:{formed:"false",powered:"false"}},facadeSouth:{Name:"ae2:crafting_accelerator",Properties:{formed:"false",powered:"false"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 302 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:chiseled_bookshelf",Properties:{facing:"north",slot_0_occupied:"false",slot_1_occupied:"false",slot_2_occupied:"false",slot_3_occupied:"false",slot_4_occupied:"false",slot_5_occupied:"false"}},facadeNorth:{Name:"minecraft:chiseled_bookshelf",Properties:{facing:"north",slot_0_occupied:"false",slot_1_occupied:"false",slot_2_occupied:"false",slot_3_occupied:"false",slot_4_occupied:"false",slot_5_occupied:"false"}},facadeSouth:{Name:"minecraft:chiseled_bookshelf",Properties:{facing:"north",slot_0_occupied:"false",slot_1_occupied:"false",slot_2_occupied:"false",slot_3_occupied:"false",slot_4_occupied:"false",slot_5_occupied:"false"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 305 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:furnace",Properties:{facing:"north",lit:"false"}},facadeNorth:{Name:"minecraft:furnace",Properties:{facing:"north",lit:"false"}},facadeSouth:{Name:"minecraft:furnace",Properties:{facing:"north",lit:"false"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"ae2:crafting_monitor",Properties:{facing:"east",formed:"true",powered:"true",spin:"3"}},facadeSouth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 311 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 311 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeWest:{Name:"minecraft:soul_sand"}} run scoreboard players add #failures ae2m3s 1
execute unless block 314 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 314 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:honey_block"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 317 100 343 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 317 100 343 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:honey_block"},facadeWest:{Name:"minecraft:soul_sand"}} run scoreboard players add #failures ae2m3s 1
execute unless block 209 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 209 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 212 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 212 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 215 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 215 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-16 facade-mask-22-through-32
execute unless block 218 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 218 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 221 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 221 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 224 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 224 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 227 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 227 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 230 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 230 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 233 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 233 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 236 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 236 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 239 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 239 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 242 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 242 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 245 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 245 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 248 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 248 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-17 facade-mask-33-through-43
execute unless block 251 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 251 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 254 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 257 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 257 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 260 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 260 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 263 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 263 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 266 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 266 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 269 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 272 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 272 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 275 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 275 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 278 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 278 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 281 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 281 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-18 facade-mask-44-through-53
execute unless block 284 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 284 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 287 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 290 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 293 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 293 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 296 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 296 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 299 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 299 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 302 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 305 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 308 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 311 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 311 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-19 facade-mask-54-through-63
execute unless block 314 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 314 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 317 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 317 100 348 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 209 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 209 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 212 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 212 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 215 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 215 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 218 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 218 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 221 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 221 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 224 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 224 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 227 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 227 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 230 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 230 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:quartz_glass"},facadeUp:{Name:"ae2:quartz_glass"},facadeNorth:{Name:"ae2:quartz_glass"},facadeSouth:{Name:"ae2:quartz_glass"},facadeWest:{Name:"ae2:quartz_glass"},facadeEast:{Name:"ae2:quartz_glass"}} run scoreboard players add #failures ae2m3s 1
execute unless block 230 99 353 ae2:quartz_glass[] run scoreboard players add #failures ae2m3s 1
execute unless block 230 101 353 ae2:quartz_glass[] run scoreboard players add #failures ae2m3s 1
execute unless block 230 100 352 ae2:quartz_glass[] run scoreboard players add #failures ae2m3s 1
execute unless block 230 100 354 ae2:quartz_glass[] run scoreboard players add #failures ae2m3s 1
execute unless block 229 100 353 ae2:quartz_glass[] run scoreboard players add #failures ae2m3s 1
execute unless block 231 100 353 ae2:quartz_glass[] run scoreboard players add #failures ae2m3s 1

# ae2-s1-20 transparent-facade-six-faces
execute unless block 233 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 233 100 353 {cable:{id:"ae2:fluix_glass_cable"},facadeDown:{Name:"minecraft:glass"}} run scoreboard players add #failures ae2m3s 1
execute unless block 236 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 236 100 353 {cable:{id:"ae2:fluix_glass_cable"},facadeUp:{Name:"minecraft:glass"}} run scoreboard players add #failures ae2m3s 1
execute unless block 239 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 239 100 353 {cable:{id:"ae2:fluix_glass_cable"},facadeNorth:{Name:"minecraft:glass"}} run scoreboard players add #failures ae2m3s 1
execute unless block 242 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 242 100 353 {cable:{id:"ae2:fluix_glass_cable"},facadeSouth:{Name:"minecraft:glass"}} run scoreboard players add #failures ae2m3s 1
execute unless block 245 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 245 100 353 {cable:{id:"ae2:fluix_glass_cable"},facadeWest:{Name:"minecraft:glass"}} run scoreboard players add #failures ae2m3s 1
execute unless block 248 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 248 100 353 {cable:{id:"ae2:fluix_glass_cable"},facadeEast:{Name:"minecraft:glass"}} run scoreboard players add #failures ae2m3s 1
execute unless block 236 101 353 minecraft:glass[] run scoreboard players add #failures ae2m3s 1

# ae2-s1-21 stateful-facade-materials
execute unless block 251 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 251 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:oak_log",Properties:{axis:"x"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 254 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:oak_log",Properties:{axis:"y"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 257 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 257 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:oak_log",Properties:{axis:"z"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 260 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 260 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeSouth:{Name:"minecraft:magma_block"}} run scoreboard players add #failures ae2m3s 1
execute unless block 263 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 263 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeWest:{Name:"minecraft:oak_leaves",Properties:{distance:"1",persistent:"true",waterlogged:"false"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 101 353 minecraft:oak_log[axis=y] run scoreboard players add #failures ae2m3s 1

# ae2-s1-22 facade-stilts-clipping-and-part-coexistence
execute unless block 266 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 266 100 353 {cable:{id:"ae2:fluix_covered_cable"},facadeSouth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 269 100 353 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:cable_anchor"},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 272 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 272 100 353 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"ae2:terminal",spin:2b},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 275 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 275 100 353 {cable:{id:"ae2:fluix_smart_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 278 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 278 100 353 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"ae2:cable_anchor"},facadeUp:{Name:"minecraft:glass"},facadeWest:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-23 native-endpoints-profile-order-01-through-09
execute unless block 281 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 281 100 353 {cable:{id:"ae2:fluix_glass_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 284 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 284 100 353 {cable:{id:"ae2:fluix_smart_cable"},north:{id:"ae2:cable_anchor"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 287 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 353 {cable:{id:"ae2:fluix_smart_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 290 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 290 100 353 {cable:{id:"ae2:fluix_smart_dense_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 293 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 293 100 353 {cable:{id:"ae2:fluix_glass_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 296 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 296 100 353 {cable:{id:"ae2:fluix_covered_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 299 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 299 100 353 {cable:{id:"ae2:fluix_smart_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 302 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 302 100 353 {cable:{id:"ae2:fluix_smart_dense_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 305 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 305 100 353 {cable:{id:"ae2:fluix_smart_dense_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 282 100 353 ae2:inscriber[facing=east,spin=0,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 282 100 353 {id:"ae2:inscriber"} run scoreboard players add #failures ae2m3s 1
execute unless block 283 100 353 ae2:wireless_access_point[facing=west,state=off,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 283 100 353 {id:"ae2:wireless_access_point"} run scoreboard players add #failures ae2m3s 1
execute unless block 285 100 353 ae2:wireless_access_point[facing=east,state=off,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 285 100 353 {id:"ae2:wireless_access_point"} run scoreboard players add #failures ae2m3s 1
execute unless block 288 100 353 ae2:charger[facing=east,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 288 100 353 {id:"ae2:charger"} run scoreboard players add #failures ae2m3s 1
execute unless block 291 98 352 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 291 98 352 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 291 98 353 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 291 98 353 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 291 98 354 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 291 98 354 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 291 99 352 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 291 99 352 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 291 99 353 ae2:quantum_link[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 291 99 353 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 291 99 354 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 291 99 354 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 291 100 352 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 291 100 352 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 291 100 353 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 291 100 353 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 291 100 354 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 291 100 354 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 99 352 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 99 352 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 99 353 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 99 353 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 99 354 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 99 354 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 100 352 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 100 352 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 100 353 ae2:quantum_link[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 100 353 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 100 354 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 100 354 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 101 352 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 101 352 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 101 353 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 101 353 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 294 101 354 ae2:quantum_ring[formed=true,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 294 101 354 {id:"ae2:quantum_ring"} run scoreboard players add #failures ae2m3s 1
execute unless block 297 99 353 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 297 99 353 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 297 100 353 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 297 100 353 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 297 101 353 ae2:spatial_pylon[powered_on=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 297 101 353 {id:"ae2:spatial_pylon"} run scoreboard players add #failures ae2m3s 1
execute unless block 300 100 353 ae2:spatial_io_port[facing=north,powered=false,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 300 100 353 {id:"ae2:spatial_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 303 100 353 ae2:spatial_anchor[facing=north,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 303 100 353 {id:"ae2:spatial_anchor"} run scoreboard players add #failures ae2m3s 1
execute unless block 304 100 353 ae2:controller[state=offline,type=block] run scoreboard players add #failures ae2m3s 1
execute unless data block 304 100 353 {id:"ae2:controller"} run scoreboard players add #failures ae2m3s 1
execute unless block 306 100 353 ae2:controller[state=offline,type=block] run scoreboard players add #failures ae2m3s 1
execute unless data block 306 100 353 {id:"ae2:controller"} run scoreboard players add #failures ae2m3s 1

# ae2-s1-24 native-endpoints-profile-order-10-through-12
execute unless block 308 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 308 100 353 {cable:{id:"ae2:fluix_glass_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 311 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 311 100 353 {cable:{id:"ae2:fluix_covered_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 314 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 314 100 353 {cable:{id:"ae2:fluix_smart_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 309 100 353 ae2:drive[facing=east,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 309 100 353 {id:"ae2:drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 312 100 353 ae2:chest[facing=north,lights_on=false,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 312 100 353 {id:"ae2:chest"} run scoreboard players add #failures ae2m3s 1
execute unless block 315 100 353 ae2:interface[] run scoreboard players add #failures ae2m3s 1
execute unless data block 315 100 353 {id:"ae2:interface"} run scoreboard players add #failures ae2m3s 1

# ae2-s1-25 native-endpoints-profile-order-13-through-30
execute unless block 317 100 353 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 317 100 353 {cable:{id:"ae2:fluix_glass_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 209 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 209 100 358 {cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 212 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 212 100 358 {cable:{id:"ae2:fluix_smart_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 215 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 215 100 358 {cable:{id:"ae2:fluix_smart_dense_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 218 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 218 100 358 {cable:{id:"ae2:fluix_glass_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 221 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 221 100 358 {cable:{id:"ae2:fluix_covered_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 224 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 224 100 358 {cable:{id:"ae2:fluix_smart_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 227 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 227 100 358 {cable:{id:"ae2:fluix_smart_dense_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 230 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 230 100 358 {cable:{id:"ae2:fluix_glass_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 233 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 233 100 358 {cable:{id:"ae2:fluix_covered_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 236 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 236 100 358 {cable:{id:"ae2:fluix_smart_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 239 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 239 100 358 {cable:{id:"ae2:fluix_smart_dense_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 242 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 242 100 358 {cable:{id:"ae2:fluix_glass_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 245 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 245 100 358 {cable:{id:"ae2:fluix_covered_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 248 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 248 100 358 {cable:{id:"ae2:fluix_smart_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 251 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 251 100 358 {cable:{id:"ae2:fluix_smart_dense_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 254 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 254 100 358 {cable:{id:"ae2:fluix_glass_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 257 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 257 100 358 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 318 100 353 ae2:io_port[facing=north,powered=false,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 318 100 353 {id:"ae2:io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 208 100 358 ae2:energy_acceptor[] run scoreboard players add #failures ae2m3s 1
execute unless data block 208 100 358 {id:"ae2:energy_acceptor"} run scoreboard players add #failures ae2m3s 1
execute unless block 210 100 358 ae2:energy_acceptor[] run scoreboard players add #failures ae2m3s 1
execute unless data block 210 100 358 {id:"ae2:energy_acceptor"} run scoreboard players add #failures ae2m3s 1
execute unless block 213 100 358 ae2:crystal_resonance_generator[facing=east,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 213 100 358 {id:"ae2:crystal_resonance_generator"} run scoreboard players add #failures ae2m3s 1
execute unless block 216 100 358 ae2:vibration_chamber[active=false,facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 216 100 358 {id:"ae2:vibration_chamber"} run scoreboard players add #failures ae2m3s 1
execute unless block 219 100 358 ae2:growth_accelerator[facing=east,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 219 100 358 {id:"ae2:growth_accelerator"} run scoreboard players add #failures ae2m3s 1
execute unless block 222 100 358 ae2:energy_cell[fullness=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 222 100 358 {id:"ae2:energy_cell"} run scoreboard players add #failures ae2m3s 1
execute unless block 225 100 358 ae2:dense_energy_cell[fullness=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 225 100 358 {id:"ae2:dense_energy_cell"} run scoreboard players add #failures ae2m3s 1
execute unless block 228 100 358 ae2:creative_energy_cell[] run scoreboard players add #failures ae2m3s 1
execute unless data block 228 100 358 {id:"ae2:creative_energy_cell"} run scoreboard players add #failures ae2m3s 1
execute unless block 231 100 358 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 231 100 358 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 231 101 358 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 231 101 358 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 234 100 358 ae2:crafting_accelerator[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 234 100 358 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 234 101 358 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 234 101 358 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 237 100 358 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 237 100 358 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 240 100 358 ae2:4k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 240 100 358 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 240 101 358 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 240 101 358 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 243 100 358 ae2:16k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 243 100 358 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 243 101 358 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 243 101 358 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 246 100 358 ae2:64k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 246 100 358 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 246 101 358 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 246 101 358 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 249 100 358 ae2:256k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 249 100 358 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 249 101 358 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 249 101 358 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 252 100 358 ae2:crafting_monitor[facing=east,formed=true,powered=false,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 252 100 358 {id:"ae2:crafting_monitor"} run scoreboard players add #failures ae2m3s 1
execute unless block 252 101 358 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 252 101 358 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 255 100 358 ae2:pattern_provider[push_direction=east] run scoreboard players add #failures ae2m3s 1
execute unless data block 255 100 358 {id:"ae2:pattern_provider"} run scoreboard players add #failures ae2m3s 1
execute unless block 256 100 358 ae2:molecular_assembler[powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 256 100 358 {id:"ae2:molecular_assembler"} run scoreboard players add #failures ae2m3s 1
execute unless block 258 100 358 ae2:molecular_assembler[powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 258 100 358 {id:"ae2:molecular_assembler"} run scoreboard players add #failures ae2m3s 1

# ae2-s1-26 persistent-invalid-reporting-spin-controls
execute unless block 260 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 260 100 358 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:semi_dark_monitor",spin:4b}} run scoreboard players add #failures ae2m3s 1
execute unless block 263 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 263 100 358 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:terminal",spin:0b},south:{id:"ae2:terminal",spin:4b}} run scoreboard players add #failures ae2m3s 1
execute unless block 266 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 266 100 358 {cable:{id:"ae2:fluix_smart_cable"},south:{id:"ae2:dark_monitor",spin:4b}} run scoreboard players add #failures ae2m3s 1
execute unless block 269 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 269 100 358 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:pattern_encoding_terminal",spin:4b}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-27 persistent-facade-and-spin-fallback-controls
execute unless block 272 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 272 100 358 {cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:oak_stairs",Properties:{facing:"east",half:"bottom",shape:"straight",waterlogged:"false"}}} run scoreboard players add #failures ae2m3s 1
execute unless block 275 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 275 100 358 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"ae2:crafting_terminal",spin:4b}} run scoreboard players add #failures ae2m3s 1
execute unless block 278 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 278 100 358 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:storage_monitor",spin:4b}} run scoreboard players add #failures ae2m3s 1

# ae2-s1-28 disconnected-endpoint-and-whole-bus-controls
execute unless block 281 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 281 100 358 {cable:{id:"ae2:fluix_smart_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 284 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 284 100 358 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:terminal",spin:3b},facadeNorth:{Name:"minecraft:stone"}} run scoreboard players add #failures ae2m3s 1
execute unless block 287 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 287 100 358 {cable:{id:"ae2:fluix_smart_cable"}} run scoreboard players add #failures ae2m3s 1
execute unless block 282 100 358 minecraft:stone run scoreboard players add #failures ae2m3s 1
execute unless block 284 100 359 ae2:wireless_access_point[facing=north,state=off,waterlogged=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 284 100 359 {id:"ae2:wireless_access_point"} run scoreboard players add #failures ae2m3s 1
execute unless block 288 100 358 expandedae:exp_io_port[facing=north,powered=false,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 288 100 358 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1

# ae2-m45-01 AppliedFlux generic blocks, face part, and all twenty Drive-cell identities
execute unless block 336 100 312 appflux:charged_redstone_block run scoreboard players add #failures ae2m3s 1
execute unless block 338 100 312 appflux:flux_accessor run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 312 {id:"appflux:flux_accessor"} run scoreboard players add #failures ae2m3s 1
execute unless block 336 100 316 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 316 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"appflux:part_flux_accessor",fast:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 338 100 316 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 316 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"appflux:part_flux_accessor",fast:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 340 100 316 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 316 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"appflux:part_flux_accessor",fast:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 342 100 316 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 342 100 316 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"appflux:part_flux_accessor",fast:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 344 100 316 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 316 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"appflux:part_flux_accessor",fast:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 346 100 316 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 316 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"appflux:part_flux_accessor",fast:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 336 100 320 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 {id:"ae2:drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 336 100 320 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 {inv:{item0:{id:"appflux:fe_1k_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 336 100 320 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 {inv:{item1:{id:"appflux:fe_1k_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 336 100 320 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 {inv:{item2:{id:"appflux:fe_4k_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 336 100 320 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 {inv:{item3:{id:"appflux:fe_4k_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 336 100 320 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 {inv:{item4:{id:"appflux:fe_16k_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 336 100 320 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 {inv:{item5:{id:"appflux:fe_16k_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 336 100 320 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 {inv:{item6:{id:"appflux:fe_64k_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 336 100 320 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 inv.item7 run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 {inv:{item7:{id:"appflux:fe_64k_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 336 100 320 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 inv.item8 run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 {inv:{item8:{id:"appflux:fe_256k_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 336 100 320 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 inv.item9 run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 320 {inv:{item9:{id:"appflux:fe_256k_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 336 100 320 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute if data block 336 100 320 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless block 338 100 320 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 {id:"ae2:drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 338 100 320 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 {inv:{item0:{id:"appflux:fe_1m_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 338 100 320 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 {inv:{item1:{id:"appflux:fe_1m_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 338 100 320 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 {inv:{item2:{id:"appflux:fe_4m_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 338 100 320 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 {inv:{item3:{id:"appflux:fe_4m_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 338 100 320 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 {inv:{item4:{id:"appflux:fe_16m_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 338 100 320 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 {inv:{item5:{id:"appflux:fe_16m_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 338 100 320 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 {inv:{item6:{id:"appflux:fe_64m_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 338 100 320 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 inv.item7 run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 {inv:{item7:{id:"appflux:fe_64m_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 338 100 320 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 inv.item8 run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 {inv:{item8:{id:"appflux:fe_256m_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 338 100 320 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 inv.item9 run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 320 {inv:{item9:{id:"appflux:fe_256m_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 338 100 320 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute if data block 338 100 320 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless block 340 100 320 extendedae:ex_drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {id:"extendedae:ex_drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 340 100 320 extendedae:ex_drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item0:{id:"appflux:fe_1k_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item1:{id:"appflux:fe_1k_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item2:{id:"appflux:fe_4k_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item3:{id:"appflux:fe_4k_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item4:{id:"appflux:fe_16k_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item5:{id:"appflux:fe_16k_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item6:{id:"appflux:fe_64k_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item7 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item7:{id:"appflux:fe_64k_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item8 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item8:{id:"appflux:fe_256k_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item9 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item9:{id:"appflux:fe_256k_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item10:{id:"appflux:fe_1m_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item10.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item11 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item11:{id:"appflux:fe_1m_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item11.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item12 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item12:{id:"appflux:fe_4m_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item12.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item13 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item13:{id:"appflux:fe_4m_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item13.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item14 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item14:{id:"appflux:fe_16m_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item14.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item15 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item15:{id:"appflux:fe_16m_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item15.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item16 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item16:{id:"appflux:fe_64m_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item16.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item17 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item17:{id:"appflux:fe_64m_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item17.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item18 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item18:{id:"appflux:fe_256m_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item18.components run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 inv.item19 run scoreboard players add #failures ae2m3s 1
execute unless data block 340 100 320 {inv:{item19:{id:"appflux:fe_256m_portable_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item19.components run scoreboard players add #failures ae2m3s 1
execute if data block 340 100 320 inv.item20 run scoreboard players add #failures ae2m3s 1

# ae2-m45-02 ME Requester 12 stable idle-derived block orientations and 24 terminal orientations
execute unless block 368 100 312 merequester:requester[active=false,facing=north] run scoreboard players add #failures ae2m3s 1
execute unless data block 368 100 312 {id:"merequester:requester"} run scoreboard players add #failures ae2m3s 1
execute unless block 370 100 312 merequester:requester[active=false,facing=east] run scoreboard players add #failures ae2m3s 1
execute unless data block 370 100 312 {id:"merequester:requester"} run scoreboard players add #failures ae2m3s 1
execute unless block 372 100 312 merequester:requester[active=false,facing=south] run scoreboard players add #failures ae2m3s 1
execute unless data block 372 100 312 {id:"merequester:requester"} run scoreboard players add #failures ae2m3s 1
execute unless block 374 100 312 merequester:requester[active=false,facing=west] run scoreboard players add #failures ae2m3s 1
execute unless data block 374 100 312 {id:"merequester:requester"} run scoreboard players add #failures ae2m3s 1
execute unless block 376 100 312 merequester:requester[active=false,facing=up,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 376 100 312 {id:"merequester:requester"} run scoreboard players add #failures ae2m3s 1
execute unless block 378 100 312 merequester:requester[active=false,facing=up,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 378 100 312 {id:"merequester:requester"} run scoreboard players add #failures ae2m3s 1
execute unless block 380 100 312 merequester:requester[active=false,facing=up,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 380 100 312 {id:"merequester:requester"} run scoreboard players add #failures ae2m3s 1
execute unless block 382 100 312 merequester:requester[active=false,facing=up,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 382 100 312 {id:"merequester:requester"} run scoreboard players add #failures ae2m3s 1
execute unless block 384 100 312 merequester:requester[active=false,facing=down,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 384 100 312 {id:"merequester:requester"} run scoreboard players add #failures ae2m3s 1
execute unless block 386 100 312 merequester:requester[active=false,facing=down,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 386 100 312 {id:"merequester:requester"} run scoreboard players add #failures ae2m3s 1
execute unless block 388 100 312 merequester:requester[active=false,facing=down,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 388 100 312 {id:"merequester:requester"} run scoreboard players add #failures ae2m3s 1
execute unless block 390 100 312 merequester:requester[active=false,facing=down,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 390 100 312 {id:"merequester:requester"} run scoreboard players add #failures ae2m3s 1
execute unless block 368 100 320 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 368 100 320 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"merequester:requester_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 370 100 320 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 370 100 320 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"merequester:requester_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 372 100 320 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 372 100 320 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"merequester:requester_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 374 100 320 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 374 100 320 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"merequester:requester_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 376 100 320 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 376 100 320 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"merequester:requester_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 378 100 320 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 378 100 320 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"merequester:requester_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 380 100 320 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 380 100 320 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"merequester:requester_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 382 100 320 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 382 100 320 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"merequester:requester_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 384 100 320 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 384 100 320 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"merequester:requester_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 386 100 320 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 386 100 320 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"merequester:requester_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 388 100 320 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 388 100 320 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"merequester:requester_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 390 100 320 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 390 100 320 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"merequester:requester_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 368 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 368 100 322 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"merequester:requester_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 370 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 370 100 322 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"merequester:requester_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 372 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 372 100 322 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"merequester:requester_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 374 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 374 100 322 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"merequester:requester_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 376 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 376 100 322 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"merequester:requester_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 378 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 378 100 322 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"merequester:requester_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 380 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 380 100 322 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"merequester:requester_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 382 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 382 100 322 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"merequester:requester_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 384 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 384 100 322 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"merequester:requester_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 386 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 386 100 322 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"merequester:requester_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 388 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 388 100 322 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"merequester:requester_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 390 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 390 100 322 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"merequester:requester_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1

# ae2-m45-03 Expanded AE complete crafting, IO-port Z rotations, parts, and fallback controls
execute unless block 416 100 312 expandedae:exp_crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 100 312 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 100 312 expandedae:exp_crafting_accelerator_2[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 100 312 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 100 312 expandedae:exp_crafting_accelerator_4[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 100 312 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 100 313 expandedae:exp_crafting_accelerator_8[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 100 313 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 100 313 expandedae:exp_crafting_accelerator_16[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 100 313 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 100 313 expandedae:exp_crafting_accelerator_32[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 100 313 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 100 314 expandedae:exp_crafting_accelerator_64[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 100 314 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 100 314 expandedae:exp_crafting_accelerator_128[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 100 314 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 100 314 expandedae:exp_crafting_accelerator_256[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 100 314 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 101 312 expandedae:exp_crafting_accelerator_512[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 101 312 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 101 312 expandedae:exp_crafting_accelerator_1k[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 101 312 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 101 312 expandedae:exp_crafting_accelerator_2k[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 101 312 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 101 313 expandedae:exp_crafting_accelerator_4k[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 101 313 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 101 313 expandedae:exp_crafting_accelerator_8k[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 101 313 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 101 314 expandedae:exp_crafting_accelerator_16k[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 101 314 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 101 314 expandedae:exp_crafting_accelerator_32k[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 101 314 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 101 314 expandedae:exp_crafting_accelerator_64k[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 101 314 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 102 312 expandedae:exp_crafting_accelerator_128k[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 102 312 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 102 312 expandedae:exp_crafting_accelerator_256k[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 102 312 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 102 312 expandedae:exp_crafting_accelerator_512k[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 102 312 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 102 313 expandedae:exp_crafting_accelerator_1m[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 102 313 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 464 100 312 expandedae:exp_crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 464 100 312 {id:"expandedae:exp_cpus"} run scoreboard players add #failures ae2m3s 1
execute unless block 424 100 312 expandedae:exp_io_port[facing=down,powered=false,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 424 100 312 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 426 100 312 expandedae:exp_io_port[facing=down,powered=false,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 426 100 312 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 428 100 312 expandedae:exp_io_port[facing=down,powered=false,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 428 100 312 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 430 100 312 expandedae:exp_io_port[facing=down,powered=false,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 430 100 312 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 432 100 312 expandedae:exp_io_port[facing=down,powered=true,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 432 100 312 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 434 100 312 expandedae:exp_io_port[facing=down,powered=true,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 434 100 312 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 436 100 312 expandedae:exp_io_port[facing=down,powered=true,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 436 100 312 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 438 100 312 expandedae:exp_io_port[facing=down,powered=true,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 438 100 312 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 440 100 312 expandedae:exp_io_port[facing=up,powered=false,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 440 100 312 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 442 100 312 expandedae:exp_io_port[facing=up,powered=false,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 442 100 312 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 444 100 312 expandedae:exp_io_port[facing=up,powered=false,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 444 100 312 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 446 100 312 expandedae:exp_io_port[facing=up,powered=false,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 446 100 312 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 424 100 314 expandedae:exp_io_port[facing=up,powered=true,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 424 100 314 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 426 100 314 expandedae:exp_io_port[facing=up,powered=true,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 426 100 314 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 428 100 314 expandedae:exp_io_port[facing=up,powered=true,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 428 100 314 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 430 100 314 expandedae:exp_io_port[facing=up,powered=true,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 430 100 314 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 432 100 314 expandedae:exp_io_port[facing=north,powered=false,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 432 100 314 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 434 100 314 expandedae:exp_io_port[facing=north,powered=false,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 434 100 314 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 436 100 314 expandedae:exp_io_port[facing=north,powered=false,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 436 100 314 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 438 100 314 expandedae:exp_io_port[facing=north,powered=false,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 438 100 314 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 440 100 314 expandedae:exp_io_port[facing=north,powered=true,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 440 100 314 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 442 100 314 expandedae:exp_io_port[facing=north,powered=true,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 442 100 314 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 444 100 314 expandedae:exp_io_port[facing=north,powered=true,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 444 100 314 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 446 100 314 expandedae:exp_io_port[facing=north,powered=true,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 446 100 314 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 424 100 316 expandedae:exp_io_port[facing=south,powered=false,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 424 100 316 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 426 100 316 expandedae:exp_io_port[facing=south,powered=false,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 426 100 316 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 428 100 316 expandedae:exp_io_port[facing=south,powered=false,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 428 100 316 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 430 100 316 expandedae:exp_io_port[facing=south,powered=false,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 430 100 316 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 432 100 316 expandedae:exp_io_port[facing=south,powered=true,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 432 100 316 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 434 100 316 expandedae:exp_io_port[facing=south,powered=true,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 434 100 316 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 436 100 316 expandedae:exp_io_port[facing=south,powered=true,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 436 100 316 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 438 100 316 expandedae:exp_io_port[facing=south,powered=true,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 438 100 316 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 440 100 316 expandedae:exp_io_port[facing=west,powered=false,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 440 100 316 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 442 100 316 expandedae:exp_io_port[facing=west,powered=false,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 442 100 316 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 444 100 316 expandedae:exp_io_port[facing=west,powered=false,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 444 100 316 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 446 100 316 expandedae:exp_io_port[facing=west,powered=false,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 446 100 316 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 424 100 318 expandedae:exp_io_port[facing=west,powered=true,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 424 100 318 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 426 100 318 expandedae:exp_io_port[facing=west,powered=true,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 426 100 318 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 428 100 318 expandedae:exp_io_port[facing=west,powered=true,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 428 100 318 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 430 100 318 expandedae:exp_io_port[facing=west,powered=true,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 430 100 318 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 432 100 318 expandedae:exp_io_port[facing=east,powered=false,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 432 100 318 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 434 100 318 expandedae:exp_io_port[facing=east,powered=false,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 434 100 318 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 436 100 318 expandedae:exp_io_port[facing=east,powered=false,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 436 100 318 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 438 100 318 expandedae:exp_io_port[facing=east,powered=false,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 438 100 318 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 440 100 318 expandedae:exp_io_port[facing=east,powered=true,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 440 100 318 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 442 100 318 expandedae:exp_io_port[facing=east,powered=true,spin=1] run scoreboard players add #failures ae2m3s 1
execute unless data block 442 100 318 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 444 100 318 expandedae:exp_io_port[facing=east,powered=true,spin=2] run scoreboard players add #failures ae2m3s 1
execute unless data block 444 100 318 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 446 100 318 expandedae:exp_io_port[facing=east,powered=true,spin=3] run scoreboard players add #failures ae2m3s 1
execute unless data block 446 100 318 {id:"expandedae:exp_io_port"} run scoreboard players add #failures ae2m3s 1
execute unless block 424 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 424 100 322 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"expandedae:exp_pattern_provider_part"}} run scoreboard players add #failures ae2m3s 1
execute unless block 426 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 426 100 322 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"expandedae:exp_pattern_provider_part"}} run scoreboard players add #failures ae2m3s 1
execute unless block 428 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 428 100 322 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"expandedae:exp_pattern_provider_part"}} run scoreboard players add #failures ae2m3s 1
execute unless block 430 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 430 100 322 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"expandedae:exp_pattern_provider_part"}} run scoreboard players add #failures ae2m3s 1
execute unless block 432 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 432 100 322 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"expandedae:exp_pattern_provider_part"}} run scoreboard players add #failures ae2m3s 1
execute unless block 434 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 434 100 322 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"expandedae:exp_pattern_provider_part"}} run scoreboard players add #failures ae2m3s 1
execute unless block 436 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 436 100 322 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"expandedae:exp_encoding_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 438 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 438 100 322 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"expandedae:exp_encoding_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 440 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 440 100 322 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"expandedae:exp_encoding_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 442 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 442 100 322 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"expandedae:exp_encoding_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 444 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 444 100 322 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"expandedae:exp_encoding_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 446 100 322 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 446 100 322 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"expandedae:exp_encoding_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 424 100 324 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 424 100 324 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"expandedae:exp_encoding_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 426 100 324 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 426 100 324 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"expandedae:exp_encoding_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 428 100 324 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 428 100 324 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"expandedae:exp_encoding_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 430 100 324 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 430 100 324 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"expandedae:exp_encoding_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 432 100 324 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 432 100 324 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"expandedae:exp_encoding_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 434 100 324 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 434 100 324 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"expandedae:exp_encoding_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 436 100 324 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 436 100 324 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"expandedae:exp_encoding_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 438 100 324 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 438 100 324 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"expandedae:exp_encoding_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 440 100 324 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 440 100 324 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"expandedae:exp_encoding_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 442 100 324 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 442 100 324 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"expandedae:exp_encoding_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 444 100 324 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 444 100 324 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"expandedae:exp_encoding_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 446 100 324 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 446 100 324 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"expandedae:exp_encoding_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 424 100 326 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 424 100 326 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"expandedae:exp_encoding_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 426 100 326 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 426 100 326 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"expandedae:exp_encoding_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 428 100 326 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 428 100 326 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"expandedae:exp_encoding_terminal",spin:0b}} run scoreboard players add #failures ae2m3s 1
execute unless block 430 100 326 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 430 100 326 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"expandedae:exp_encoding_terminal",spin:1b}} run scoreboard players add #failures ae2m3s 1
execute unless block 432 100 326 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 432 100 326 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"expandedae:exp_encoding_terminal",spin:2b}} run scoreboard players add #failures ae2m3s 1
execute unless block 434 100 326 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 434 100 326 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"expandedae:exp_encoding_terminal",spin:3b}} run scoreboard players add #failures ae2m3s 1
execute unless block 424 100 330 expandedae:exp_pattern_provider run scoreboard players add #failures ae2m3s 1
execute unless data block 424 100 330 {id:"expandedae:exp_pattern_provider"} run scoreboard players add #failures ae2m3s 1
execute unless block 424 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 424 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 424 100 334 {color:"WHITE"} run scoreboard players add #failures ae2m3s 1
execute unless block 426 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 426 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 426 100 334 {color:"LIGHT_GRAY"} run scoreboard players add #failures ae2m3s 1
execute unless block 428 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 428 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 428 100 334 {color:"GRAY"} run scoreboard players add #failures ae2m3s 1
execute unless block 430 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 430 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 430 100 334 {color:"BLACK"} run scoreboard players add #failures ae2m3s 1
execute unless block 432 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 432 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 432 100 334 {color:"LIME"} run scoreboard players add #failures ae2m3s 1
execute unless block 434 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 434 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 434 100 334 {color:"YELLOW"} run scoreboard players add #failures ae2m3s 1
execute unless block 436 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 436 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 436 100 334 {color:"ORANGE"} run scoreboard players add #failures ae2m3s 1
execute unless block 438 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 438 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 438 100 334 {color:"BROWN"} run scoreboard players add #failures ae2m3s 1
execute unless block 440 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 440 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 440 100 334 {color:"RED"} run scoreboard players add #failures ae2m3s 1
execute unless block 442 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 442 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 442 100 334 {color:"PINK"} run scoreboard players add #failures ae2m3s 1
execute unless block 444 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 444 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 444 100 334 {color:"MAGENTA"} run scoreboard players add #failures ae2m3s 1
execute unless block 446 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 446 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 446 100 334 {color:"PURPLE"} run scoreboard players add #failures ae2m3s 1
execute unless block 448 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 448 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 448 100 334 {color:"BLUE"} run scoreboard players add #failures ae2m3s 1
execute unless block 450 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 450 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 450 100 334 {color:"LIGHT_BLUE"} run scoreboard players add #failures ae2m3s 1
execute unless block 452 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 452 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 452 100 334 {color:"CYAN"} run scoreboard players add #failures ae2m3s 1
execute unless block 454 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 454 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 454 100 334 {color:"GREEN"} run scoreboard players add #failures ae2m3s 1
execute unless block 456 100 334 expandedae:colorable_drive run scoreboard players add #failures ae2m3s 1
execute unless data block 456 100 334 {id:"expandedae:colorable_drive"} run scoreboard players add #failures ae2m3s 1
execute unless data block 456 100 334 {color:"TRANSPARENT"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 101 313 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 101 313 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 102 313 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 102 313 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 102 313 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 102 313 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 102 314 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 102 314 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 102 314 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 102 314 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 102 314 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 102 314 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 466 100 312 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 466 100 312 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 464 100 313 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 464 100 313 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 465 100 313 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 465 100 313 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 466 100 313 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 466 100 313 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 464 100 314 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 464 100 314 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 465 100 314 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 465 100 314 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 466 100 314 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 466 100 314 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 464 101 312 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 464 101 312 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 465 101 312 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 465 101 312 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 466 101 312 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 466 101 312 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 464 101 313 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 464 101 313 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 465 101 313 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 465 101 313 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 466 101 313 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 466 101 313 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 464 101 314 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 464 101 314 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 465 101 314 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 465 101 314 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 466 101 314 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 466 101 314 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 464 102 312 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 464 102 312 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 465 102 312 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 465 102 312 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 466 102 312 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 466 102 312 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 464 102 313 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 464 102 313 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 465 102 313 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 465 102 313 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 466 102 313 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 466 102 313 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 464 102 314 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 464 102 314 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 465 102 314 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 465 102 314 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 466 102 314 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 466 102 314 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 432 99 312 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 434 99 312 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 436 99 312 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 438 99 312 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 424 99 314 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 426 99 314 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 428 99 314 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 430 99 314 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 440 99 314 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 442 99 314 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 444 99 314 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 446 99 314 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 432 99 316 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 434 99 316 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 436 99 316 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 438 99 316 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 424 99 318 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 426 99 318 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 428 99 318 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 430 99 318 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 440 99 318 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 442 99 318 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 444 99 318 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1
execute unless block 446 99 318 ae2:creative_energy_cell run scoreboard players add #failures ae2m3s 1

# ae2-m45-04 MEGA Cells crafting, parts, Cell Dock, and all Drive-cell identities
execute unless block 336 100 344 megacells:mega_crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 344 {id:"megacells:mega_crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 337 100 344 megacells:mega_crafting_accelerator[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 337 100 344 {id:"megacells:mega_crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 338 100 344 megacells:1m_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 344 {id:"megacells:mega_crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 336 100 345 megacells:4m_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 345 {id:"megacells:mega_crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 337 100 345 megacells:16m_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 337 100 345 {id:"megacells:mega_crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 338 100 345 megacells:64m_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 345 {id:"megacells:mega_crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 336 100 346 megacells:256m_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 336 100 346 {id:"megacells:mega_crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 337 100 346 megacells:mega_crafting_monitor[facing=north,formed=true,powered=false,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 337 100 346 {id:"megacells:mega_crafting_monitor"} run scoreboard players add #failures ae2m3s 1
execute unless block 465 100 312 megacells:mega_crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 465 100 312 {id:"megacells:mega_crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 344 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 344 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:0b,cell:{}}} run scoreboard players add #failures ae2m3s 1
execute unless block 346 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 344 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:bulk_item_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 348 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 344 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:chemical_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 350 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 344 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:chemical_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 352 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 344 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:chemical_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 354 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 344 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:chemical_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 356 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 344 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:chemical_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 358 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 358 100 344 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:experience_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 360 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 360 100 344 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:experience_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 362 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 362 100 344 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:experience_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 364 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 364 100 344 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:experience_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 366 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 366 100 344 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:experience_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 368 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 368 100 344 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:fluid_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 370 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 370 100 344 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:fluid_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 372 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 372 100 344 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:fluid_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 374 100 344 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 374 100 344 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:fluid_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 344 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 346 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:fluid_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 346 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 346 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:item_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 348 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 346 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:item_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 350 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 346 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:item_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 352 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 346 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:item_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 354 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 346 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:item_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 356 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 346 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:mana_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 358 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 358 100 346 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:mana_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 360 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 360 100 346 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:mana_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 362 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 362 100 346 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:mana_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 364 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 364 100 346 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:mana_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 366 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 366 100 346 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_chemical_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 368 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 368 100 346 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_chemical_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 370 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 370 100 346 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_chemical_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 372 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 372 100 346 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_chemical_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 374 100 346 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 374 100 346 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_chemical_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 344 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 348 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_experience_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 346 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 348 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_experience_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 348 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 348 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_experience_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 350 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 348 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_experience_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 352 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 348 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_experience_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 354 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 348 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_fluid_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 356 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 348 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_fluid_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 358 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 358 100 348 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_fluid_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 360 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 360 100 348 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_fluid_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 362 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 362 100 348 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_fluid_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 364 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 364 100 348 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_item_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 366 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 366 100 348 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_item_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 368 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 368 100 348 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_item_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 370 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 370 100 348 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_item_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 372 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 372 100 348 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_item_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 374 100 348 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 374 100 348 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_mana_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 344 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 350 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_mana_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 346 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 350 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_mana_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 348 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 350 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_mana_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 350 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 350 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_mana_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 352 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 350 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_source_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 354 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 350 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_source_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 356 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 350 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_source_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 358 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 358 100 350 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_source_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 360 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 360 100 350 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_source_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 362 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 362 100 350 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:radioactive_chemical_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 364 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 364 100 350 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:soul_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 366 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 366 100 350 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:soul_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 368 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 368 100 350 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:soul_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 370 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 370 100 350 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:soul_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 372 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 372 100 350 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:soul_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 374 100 350 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 374 100 350 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:source_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 344 100 352 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 352 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:source_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 346 100 352 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 352 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:source_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 348 100 352 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 352 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:source_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 350 100 352 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 352 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:source_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 352 100 352 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 352 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:0b,cell:{id:"minecraft:stone",count:1}}} run scoreboard players add #failures ae2m3s 1
execute unless block 344 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 356 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:decompression_module"}} run scoreboard players add #failures ae2m3s 1
execute unless block 346 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 356 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:decompression_module"}} run scoreboard players add #failures ae2m3s 1
execute unless block 348 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 356 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:decompression_module"}} run scoreboard players add #failures ae2m3s 1
execute unless block 350 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 356 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:decompression_module"}} run scoreboard players add #failures ae2m3s 1
execute unless block 352 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 356 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:decompression_module"}} run scoreboard players add #failures ae2m3s 1
execute unless block 354 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 356 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:decompression_module"}} run scoreboard players add #failures ae2m3s 1
execute unless block 356 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 356 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cable_mega_interface"}} run scoreboard players add #failures ae2m3s 1
execute unless block 358 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 358 100 356 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cable_mega_interface"}} run scoreboard players add #failures ae2m3s 1
execute unless block 360 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 360 100 356 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cable_mega_interface"}} run scoreboard players add #failures ae2m3s 1
execute unless block 362 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 362 100 356 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cable_mega_interface"}} run scoreboard players add #failures ae2m3s 1
execute unless block 364 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 364 100 356 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cable_mega_interface"}} run scoreboard players add #failures ae2m3s 1
execute unless block 366 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 366 100 356 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cable_mega_interface"}} run scoreboard players add #failures ae2m3s 1
execute unless block 368 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 368 100 356 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cable_mega_pattern_provider"}} run scoreboard players add #failures ae2m3s 1
execute unless block 370 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 370 100 356 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cable_mega_pattern_provider"}} run scoreboard players add #failures ae2m3s 1
execute unless block 372 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 372 100 356 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cable_mega_pattern_provider"}} run scoreboard players add #failures ae2m3s 1
execute unless block 374 100 356 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 374 100 356 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cable_mega_pattern_provider"}} run scoreboard players add #failures ae2m3s 1
execute unless block 344 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 358 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cable_mega_pattern_provider"}} run scoreboard players add #failures ae2m3s 1
execute unless block 346 100 358 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 358 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cable_mega_pattern_provider"}} run scoreboard players add #failures ae2m3s 1
execute unless block 344 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 {id:"ae2:drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 344 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 {inv:{item0:{id:"megacells:bulk_item_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 362 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 {inv:{item1:{id:"megacells:chemical_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 362 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 {inv:{item2:{id:"megacells:chemical_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 362 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 {inv:{item3:{id:"megacells:chemical_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 362 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 {inv:{item4:{id:"megacells:chemical_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 362 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 {inv:{item5:{id:"megacells:chemical_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 362 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 {inv:{item6:{id:"megacells:experience_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 362 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 inv.item7 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 {inv:{item7:{id:"megacells:experience_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 362 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 inv.item8 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 {inv:{item8:{id:"megacells:experience_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 362 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 inv.item9 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 362 {inv:{item9:{id:"megacells:experience_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 362 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 362 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless block 346 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 {id:"ae2:drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 346 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 {inv:{item0:{id:"megacells:experience_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 362 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 {inv:{item1:{id:"megacells:fluid_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 362 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 {inv:{item2:{id:"megacells:fluid_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 362 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 {inv:{item3:{id:"megacells:fluid_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 362 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 {inv:{item4:{id:"megacells:fluid_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 362 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 {inv:{item5:{id:"megacells:fluid_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 362 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 {inv:{item6:{id:"megacells:item_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 362 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 inv.item7 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 {inv:{item7:{id:"megacells:item_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 362 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 inv.item8 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 {inv:{item8:{id:"megacells:item_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 362 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 inv.item9 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 362 {inv:{item9:{id:"megacells:item_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 362 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 362 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless block 348 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 {id:"ae2:drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 348 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 {inv:{item0:{id:"megacells:item_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 362 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 {inv:{item1:{id:"megacells:mana_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 362 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 {inv:{item2:{id:"megacells:mana_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 362 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 {inv:{item3:{id:"megacells:mana_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 362 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 {inv:{item4:{id:"megacells:mana_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 362 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 {inv:{item5:{id:"megacells:mana_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 362 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 {inv:{item6:{id:"megacells:portable_chemical_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 362 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 inv.item7 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 {inv:{item7:{id:"megacells:portable_chemical_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 362 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 inv.item8 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 {inv:{item8:{id:"megacells:portable_chemical_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 362 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 inv.item9 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 362 {inv:{item9:{id:"megacells:portable_chemical_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 362 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 362 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless block 350 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 {id:"ae2:drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 350 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 {inv:{item0:{id:"megacells:portable_chemical_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 362 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 {inv:{item1:{id:"megacells:portable_experience_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 362 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 {inv:{item2:{id:"megacells:portable_experience_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 362 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 {inv:{item3:{id:"megacells:portable_experience_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 362 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 {inv:{item4:{id:"megacells:portable_experience_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 362 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 {inv:{item5:{id:"megacells:portable_experience_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 362 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 {inv:{item6:{id:"megacells:portable_fluid_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 362 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 inv.item7 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 {inv:{item7:{id:"megacells:portable_fluid_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 362 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 inv.item8 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 {inv:{item8:{id:"megacells:portable_fluid_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 362 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 inv.item9 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 362 {inv:{item9:{id:"megacells:portable_fluid_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 362 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 362 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless block 352 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 {id:"ae2:drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 352 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 {inv:{item0:{id:"megacells:portable_fluid_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 352 100 362 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 {inv:{item1:{id:"megacells:portable_item_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 352 100 362 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 {inv:{item2:{id:"megacells:portable_item_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 352 100 362 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 {inv:{item3:{id:"megacells:portable_item_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 352 100 362 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 {inv:{item4:{id:"megacells:portable_item_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 352 100 362 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 {inv:{item5:{id:"megacells:portable_item_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 352 100 362 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 {inv:{item6:{id:"megacells:portable_mana_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 352 100 362 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 inv.item7 run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 {inv:{item7:{id:"megacells:portable_mana_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 352 100 362 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 inv.item8 run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 {inv:{item8:{id:"megacells:portable_mana_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 352 100 362 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 inv.item9 run scoreboard players add #failures ae2m3s 1
execute unless data block 352 100 362 {inv:{item9:{id:"megacells:portable_mana_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 352 100 362 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute if data block 352 100 362 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless block 354 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 {id:"ae2:drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 354 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 {inv:{item0:{id:"megacells:portable_mana_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 354 100 362 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 {inv:{item1:{id:"megacells:portable_source_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 354 100 362 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 {inv:{item2:{id:"megacells:portable_source_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 354 100 362 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 {inv:{item3:{id:"megacells:portable_source_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 354 100 362 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 {inv:{item4:{id:"megacells:portable_source_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 354 100 362 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 {inv:{item5:{id:"megacells:portable_source_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 354 100 362 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 {inv:{item6:{id:"megacells:radioactive_chemical_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 354 100 362 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 inv.item7 run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 {inv:{item7:{id:"megacells:soul_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 354 100 362 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 inv.item8 run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 {inv:{item8:{id:"megacells:soul_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 354 100 362 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 inv.item9 run scoreboard players add #failures ae2m3s 1
execute unless data block 354 100 362 {inv:{item9:{id:"megacells:soul_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 354 100 362 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute if data block 354 100 362 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless block 356 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 {id:"ae2:drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 356 100 362 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 {inv:{item0:{id:"megacells:soul_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 {inv:{item1:{id:"megacells:soul_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 {inv:{item2:{id:"megacells:source_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 {inv:{item3:{id:"megacells:source_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 {inv:{item4:{id:"megacells:source_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 {inv:{item5:{id:"megacells:source_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 {inv:{item6:{id:"megacells:source_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 inv.item7 run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item7.id run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item7.count run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 inv.item8 run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item8.id run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item8.count run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 356 100 362 inv.item9 run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item9.id run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item9.count run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute if data block 356 100 362 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless block 344 100 366 extendedae:ex_drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {id:"extendedae:ex_drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 344 100 366 extendedae:ex_drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item0:{id:"megacells:bulk_item_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item1:{id:"megacells:chemical_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item2:{id:"megacells:chemical_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item3:{id:"megacells:chemical_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item4:{id:"megacells:chemical_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item5:{id:"megacells:chemical_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item6:{id:"megacells:experience_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item7 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item7:{id:"megacells:experience_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item8 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item8:{id:"megacells:experience_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item9 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item9:{id:"megacells:experience_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item10:{id:"megacells:experience_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item10.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item11 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item11:{id:"megacells:fluid_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item11.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item12 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item12:{id:"megacells:fluid_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item12.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item13 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item13:{id:"megacells:fluid_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item13.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item14 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item14:{id:"megacells:fluid_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item14.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item15 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item15:{id:"megacells:fluid_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item15.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item16 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item16:{id:"megacells:item_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item16.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item17 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item17:{id:"megacells:item_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item17.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item18 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item18:{id:"megacells:item_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item18.components run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 inv.item19 run scoreboard players add #failures ae2m3s 1
execute unless data block 344 100 366 {inv:{item19:{id:"megacells:item_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item19.components run scoreboard players add #failures ae2m3s 1
execute if data block 344 100 366 inv.item20 run scoreboard players add #failures ae2m3s 1
execute unless block 346 100 366 extendedae:ex_drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {id:"extendedae:ex_drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 346 100 366 extendedae:ex_drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item0:{id:"megacells:item_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item1:{id:"megacells:mana_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item2:{id:"megacells:mana_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item3:{id:"megacells:mana_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item4:{id:"megacells:mana_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item5:{id:"megacells:mana_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item6:{id:"megacells:portable_chemical_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item7 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item7:{id:"megacells:portable_chemical_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item8 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item8:{id:"megacells:portable_chemical_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item9 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item9:{id:"megacells:portable_chemical_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item10:{id:"megacells:portable_chemical_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item10.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item11 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item11:{id:"megacells:portable_experience_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item11.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item12 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item12:{id:"megacells:portable_experience_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item12.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item13 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item13:{id:"megacells:portable_experience_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item13.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item14 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item14:{id:"megacells:portable_experience_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item14.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item15 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item15:{id:"megacells:portable_experience_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item15.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item16 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item16:{id:"megacells:portable_fluid_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item16.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item17 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item17:{id:"megacells:portable_fluid_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item17.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item18 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item18:{id:"megacells:portable_fluid_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item18.components run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 inv.item19 run scoreboard players add #failures ae2m3s 1
execute unless data block 346 100 366 {inv:{item19:{id:"megacells:portable_fluid_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item19.components run scoreboard players add #failures ae2m3s 1
execute if data block 346 100 366 inv.item20 run scoreboard players add #failures ae2m3s 1
execute unless block 348 100 366 extendedae:ex_drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {id:"extendedae:ex_drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 348 100 366 extendedae:ex_drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item0:{id:"megacells:portable_fluid_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item1:{id:"megacells:portable_item_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item2:{id:"megacells:portable_item_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item3:{id:"megacells:portable_item_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item4:{id:"megacells:portable_item_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item5:{id:"megacells:portable_item_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item6:{id:"megacells:portable_mana_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item7 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item7:{id:"megacells:portable_mana_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item8 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item8:{id:"megacells:portable_mana_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item9 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item9:{id:"megacells:portable_mana_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item10 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item10:{id:"megacells:portable_mana_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item10.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item11 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item11:{id:"megacells:portable_source_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item11.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item12 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item12:{id:"megacells:portable_source_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item12.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item13 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item13:{id:"megacells:portable_source_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item13.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item14 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item14:{id:"megacells:portable_source_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item14.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item15 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item15:{id:"megacells:portable_source_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item15.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item16 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item16:{id:"megacells:radioactive_chemical_cell",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item16.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item17 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item17:{id:"megacells:soul_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item17.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item18 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item18:{id:"megacells:soul_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item18.components run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 inv.item19 run scoreboard players add #failures ae2m3s 1
execute unless data block 348 100 366 {inv:{item19:{id:"megacells:soul_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item19.components run scoreboard players add #failures ae2m3s 1
execute if data block 348 100 366 inv.item20 run scoreboard players add #failures ae2m3s 1
execute unless block 350 100 366 extendedae:ex_drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 {id:"extendedae:ex_drive"} run scoreboard players add #failures ae2m3s 1
execute unless block 350 100 366 extendedae:ex_drive[facing=north,spin=0] run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item0 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 {inv:{item0:{id:"megacells:soul_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item0.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item1 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 {inv:{item1:{id:"megacells:soul_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item1.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item2 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 {inv:{item2:{id:"megacells:source_storage_cell_16m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item2.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item3 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 {inv:{item3:{id:"megacells:source_storage_cell_1m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item3.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item4 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 {inv:{item4:{id:"megacells:source_storage_cell_256m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item4.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item5 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 {inv:{item5:{id:"megacells:source_storage_cell_4m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item5.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item6 run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 {inv:{item6:{id:"megacells:source_storage_cell_64m",count:1}}} run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item6.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item7 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item7.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item7.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item7.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item8 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item8.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item8.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item8.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item9 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item9.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item9.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item9.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item10 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item10.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item10.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item10.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item11 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item11.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item11.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item11.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item12 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item12.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item12.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item12.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item13 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item13.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item13.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item13.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item14 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item14.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item14.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item14.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item15 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item15.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item15.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item15.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item16 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item16.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item16.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item16.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item17 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item17.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item17.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item17.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item18 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item18.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item18.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item18.components run scoreboard players add #failures ae2m3s 1
execute unless data block 350 100 366 inv.item19 run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item19.id run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item19.count run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item19.components run scoreboard players add #failures ae2m3s 1
execute if data block 350 100 366 inv.item20 run scoreboard players add #failures ae2m3s 1
execute unless block 338 100 346 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 338 100 346 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 336 101 344 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 336 101 344 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 337 101 344 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 337 101 344 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 338 101 344 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 338 101 344 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 336 101 345 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 336 101 345 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 337 101 345 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 337 101 345 {id:"ae2:crafting_storage"} run scoreboard players add #failures ae2m3s 1
execute unless block 338 101 345 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 338 101 345 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 336 101 346 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 336 101 346 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 337 101 346 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 337 101 346 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 338 101 346 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 338 101 346 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 336 102 344 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 336 102 344 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 337 102 344 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 337 102 344 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 338 102 344 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 338 102 344 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 336 102 345 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 336 102 345 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 337 102 345 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 337 102 345 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 338 102 345 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 338 102 345 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 336 102 346 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 336 102 346 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 337 102 346 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 337 102 346 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1
execute unless block 338 102 346 ae2:crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 338 102 346 {id:"ae2:crafting_unit"} run scoreboard players add #failures ae2m3s 1

# ae2-m45-05 Advanced AE static roles plus a live-proven physical 4x3x3 quantum computer
execute unless block 416 100 370 advanced_ae:quantum_unit[formed=false,light_level=0,multiblocked=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 100 370 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 100 370 advanced_ae:quantum_core[formed=true,light_level=0,multiblocked=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 100 370 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 420 100 370 advanced_ae:quantum_storage_128[formed=false,light_level=0,multiblocked=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 420 100 370 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 422 100 370 advanced_ae:quantum_storage_256[formed=false,light_level=0,multiblocked=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 422 100 370 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 424 100 370 advanced_ae:data_entangler[formed=false,light_level=0,multiblocked=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 424 100 370 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 426 100 370 advanced_ae:quantum_accelerator[formed=false,light_level=0,multiblocked=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 426 100 370 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 428 100 370 advanced_ae:quantum_multi_threader[formed=false,light_level=0,multiblocked=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 428 100 370 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 430 100 370 advanced_ae:quantum_structure[formed=false,light_level=0,multiblocked=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 430 100 370 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 100 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 100 376 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 100 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 100 376 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 100 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 100 376 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 419 100 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 419 100 376 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 100 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 100 377 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 100 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 100 377 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 100 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 100 377 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 419 100 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 419 100 377 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 100 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 100 378 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 100 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 100 378 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 100 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 100 378 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 419 100 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 419 100 378 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 101 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 101 376 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 101 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 101 376 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 101 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 101 376 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 419 101 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 419 101 376 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 101 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 101 377 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 101 377 advanced_ae:quantum_core[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 101 377 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 101 377 advanced_ae:quantum_storage_128[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 101 377 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 419 101 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 419 101 377 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 101 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 101 378 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 101 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 101 378 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 101 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 101 378 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 419 101 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 419 101 378 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 102 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 102 376 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 102 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 102 376 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 102 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 102 376 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 419 102 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 419 102 376 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 102 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 102 377 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 102 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 102 377 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 102 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 102 377 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 419 102 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 419 102 377 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 416 102 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 416 102 378 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 417 102 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 417 102 378 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 418 102 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 418 102 378 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1
execute unless block 419 102 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 419 102 378 {id:"advanced_ae:quantum_core"} run scoreboard players add #failures ae2m3s 1

# ae2-m45-06 Advanced AE quantum-alloy isolated and connected Athena topology
execute unless block 424 100 376 advanced_ae:quantum_alloy_block run scoreboard players add #failures ae2m3s 1
execute unless block 426 100 376 advanced_ae:quantum_alloy_block run scoreboard players add #failures ae2m3s 1
execute unless block 427 100 376 advanced_ae:quantum_alloy_block run scoreboard players add #failures ae2m3s 1
execute unless block 426 100 377 advanced_ae:quantum_alloy_block run scoreboard players add #failures ae2m3s 1
execute unless block 427 100 377 advanced_ae:quantum_alloy_block run scoreboard players add #failures ae2m3s 1
execute unless block 426 101 376 advanced_ae:quantum_alloy_block run scoreboard players add #failures ae2m3s 1
execute unless block 427 101 376 advanced_ae:quantum_alloy_block run scoreboard players add #failures ae2m3s 1
execute unless block 426 101 377 advanced_ae:quantum_alloy_block run scoreboard players add #failures ae2m3s 1
execute unless block 427 101 377 advanced_ae:quantum_alloy_block run scoreboard players add #failures ae2m3s 1

# ae2-m45-07 ExtendedAE static roles plus a live-proven physical 4x3x3 Assembler Matrix
execute unless block 448 100 370 extendedae:assembler_matrix_frame[formed=false,powered=false,shape=block] run scoreboard players add #failures ae2m3s 1
execute unless data block 448 100 370 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 448 100 374 extendedae:assembler_matrix_wall[formed=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 448 100 374 {id:"extendedae:assembler_matrix_wall"} run scoreboard players add #failures ae2m3s 1
execute unless block 450 100 374 extendedae:assembler_matrix_glass[formed=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 450 100 374 {id:"extendedae:assembler_matrix_glass"} run scoreboard players add #failures ae2m3s 1
execute unless block 452 100 374 extendedae:assembler_matrix_pattern[formed=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 452 100 374 {id:"extendedae:assembler_matrix_pattern"} run scoreboard players add #failures ae2m3s 1
execute unless block 454 100 374 extendedae:assembler_matrix_crafter[formed=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 454 100 374 {id:"extendedae:assembler_matrix_crafter"} run scoreboard players add #failures ae2m3s 1
execute unless block 456 100 374 extendedae:assembler_matrix_speed[formed=false,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 456 100 374 {id:"extendedae:assembler_matrix_speed"} run scoreboard players add #failures ae2m3s 1
execute unless block 456 100 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] run scoreboard players add #failures ae2m3s 1
execute unless data block 456 100 378 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 457 100 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] run scoreboard players add #failures ae2m3s 1
execute unless data block 457 100 378 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 458 100 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] run scoreboard players add #failures ae2m3s 1
execute unless data block 458 100 378 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 459 100 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] run scoreboard players add #failures ae2m3s 1
execute unless data block 459 100 378 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 456 100 379 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_z] run scoreboard players add #failures ae2m3s 1
execute unless data block 456 100 379 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 457 100 379 extendedae:assembler_matrix_glass[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 457 100 379 {id:"extendedae:assembler_matrix_glass"} run scoreboard players add #failures ae2m3s 1
execute unless block 458 100 379 extendedae:assembler_matrix_glass[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 458 100 379 {id:"extendedae:assembler_matrix_glass"} run scoreboard players add #failures ae2m3s 1
execute unless block 459 100 379 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_z] run scoreboard players add #failures ae2m3s 1
execute unless data block 459 100 379 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 456 100 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] run scoreboard players add #failures ae2m3s 1
execute unless data block 456 100 380 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 457 100 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] run scoreboard players add #failures ae2m3s 1
execute unless data block 457 100 380 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 458 100 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] run scoreboard players add #failures ae2m3s 1
execute unless data block 458 100 380 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 459 100 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] run scoreboard players add #failures ae2m3s 1
execute unless data block 459 100 380 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 456 101 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_y] run scoreboard players add #failures ae2m3s 1
execute unless data block 456 101 378 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 457 101 378 extendedae:assembler_matrix_glass[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 457 101 378 {id:"extendedae:assembler_matrix_glass"} run scoreboard players add #failures ae2m3s 1
execute unless block 458 101 378 extendedae:assembler_matrix_glass[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 458 101 378 {id:"extendedae:assembler_matrix_glass"} run scoreboard players add #failures ae2m3s 1
execute unless block 459 101 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_y] run scoreboard players add #failures ae2m3s 1
execute unless data block 459 101 378 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 456 101 379 extendedae:assembler_matrix_glass[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 456 101 379 {id:"extendedae:assembler_matrix_glass"} run scoreboard players add #failures ae2m3s 1
execute unless block 457 101 379 extendedae:assembler_matrix_pattern[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 457 101 379 {id:"extendedae:assembler_matrix_pattern"} run scoreboard players add #failures ae2m3s 1
execute unless block 458 101 379 extendedae:assembler_matrix_crafter[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 458 101 379 {id:"extendedae:assembler_matrix_crafter"} run scoreboard players add #failures ae2m3s 1
execute unless block 459 101 379 extendedae:assembler_matrix_glass[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 459 101 379 {id:"extendedae:assembler_matrix_glass"} run scoreboard players add #failures ae2m3s 1
execute unless block 456 101 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_y] run scoreboard players add #failures ae2m3s 1
execute unless data block 456 101 380 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 457 101 380 extendedae:assembler_matrix_glass[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 457 101 380 {id:"extendedae:assembler_matrix_glass"} run scoreboard players add #failures ae2m3s 1
execute unless block 458 101 380 extendedae:assembler_matrix_glass[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 458 101 380 {id:"extendedae:assembler_matrix_glass"} run scoreboard players add #failures ae2m3s 1
execute unless block 459 101 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_y] run scoreboard players add #failures ae2m3s 1
execute unless data block 459 101 380 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 456 102 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] run scoreboard players add #failures ae2m3s 1
execute unless data block 456 102 378 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 457 102 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] run scoreboard players add #failures ae2m3s 1
execute unless data block 457 102 378 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 458 102 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] run scoreboard players add #failures ae2m3s 1
execute unless data block 458 102 378 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 459 102 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] run scoreboard players add #failures ae2m3s 1
execute unless data block 459 102 378 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 456 102 379 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_z] run scoreboard players add #failures ae2m3s 1
execute unless data block 456 102 379 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 457 102 379 extendedae:assembler_matrix_glass[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 457 102 379 {id:"extendedae:assembler_matrix_glass"} run scoreboard players add #failures ae2m3s 1
execute unless block 458 102 379 extendedae:assembler_matrix_glass[formed=true,powered=false] run scoreboard players add #failures ae2m3s 1
execute unless data block 458 102 379 {id:"extendedae:assembler_matrix_glass"} run scoreboard players add #failures ae2m3s 1
execute unless block 459 102 379 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_z] run scoreboard players add #failures ae2m3s 1
execute unless data block 459 102 379 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 456 102 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] run scoreboard players add #failures ae2m3s 1
execute unless data block 456 102 380 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 457 102 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] run scoreboard players add #failures ae2m3s 1
execute unless data block 457 102 380 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 458 102 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] run scoreboard players add #failures ae2m3s 1
execute unless data block 458 102 380 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1
execute unless block 459 102 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] run scoreboard players add #failures ae2m3s 1
execute unless data block 459 102 380 {id:"extendedae:assembler_matrix_frame"} run scoreboard players add #failures ae2m3s 1

# ae2-m45-08 ExtendedAE plane identities, installed-face orbit, and all sixteen masks
execute unless block 480 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 480 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 484 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 484 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 488 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 488 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 492 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 492 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 496 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 496 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 500 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 500 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 504 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 504 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 480 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 480 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 484 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 484 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 488 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 488 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 492 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 492 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 496 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 496 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 500 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 500 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 504 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 504 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 480 100 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 480 100 378 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 484 100 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 484 100 378 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 488 100 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 488 100 378 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 492 100 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 492 100 378 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 496 100 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 496 100 378 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 500 100 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 500 100 378 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 504 100 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 504 100 378 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 480 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 480 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 484 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 484 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 488 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 488 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 492 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 492 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 496 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 496 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 500 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 500 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 504 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 504 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 480 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 480 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 484 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 484 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 488 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 488 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 492 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 492 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 496 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 496 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 500 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 500 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 504 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 504 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 480 100 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 480 100 390 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 484 100 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 484 100 390 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 488 100 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 488 100 390 {cable:{id:"ae2:fluix_covered_cable"},down:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 492 100 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 492 100 390 {cable:{id:"ae2:fluix_covered_cable"},up:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 496 100 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 496 100 390 {cable:{id:"ae2:fluix_covered_cable"},south:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 500 100 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 500 100 390 {cable:{id:"ae2:fluix_covered_cable"},west:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 504 100 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 504 100 390 {cable:{id:"ae2:fluix_covered_cable"},east:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 485 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 485 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 488 99 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 488 99 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 492 99 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 492 99 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 493 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 493 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 495 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 495 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 499 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 499 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 501 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 501 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 503 100 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 503 100 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 504 99 370 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 504 99 370 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 479 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 479 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 480 99 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 480 99 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 481 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 481 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 484 101 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 484 101 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 488 101 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 488 101 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 489 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 489 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 492 101 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 492 101 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 492 99 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 492 99 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 496 101 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 496 101 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 496 99 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 496 99 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 497 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 497 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 500 101 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 500 101 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 499 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 499 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 504 101 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 504 101 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 503 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 503 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 505 100 374 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 505 100 374 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 480 101 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 480 101 378 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 479 100 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 479 100 378 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 480 99 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 480 99 378 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 484 101 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 484 101 378 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 483 100 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 483 100 378 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 484 99 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 484 99 378 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 485 100 378 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 485 100 378 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 485 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 485 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 488 99 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 488 99 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 492 99 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 492 99 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 493 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 493 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 495 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 495 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 499 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 499 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 501 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 501 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 503 100 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 503 100 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 504 99 382 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 504 99 382 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 479 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 479 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 480 99 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 480 99 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 481 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 481 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 484 101 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 484 101 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 488 101 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 488 101 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 489 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 489 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 492 101 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 492 101 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 492 99 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 492 99 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 496 101 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 496 101 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 496 99 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 496 99 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 497 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 497 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 500 101 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 500 101 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 499 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 499 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 504 101 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 504 101 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 503 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 503 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 505 100 386 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 505 100 386 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 480 101 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 480 101 390 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 479 100 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 479 100 390 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 480 99 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 480 99 390 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 484 101 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 484 101 390 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 483 100 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 483 100 390 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 484 99 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 484 99 390 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1
execute unless block 485 100 390 ae2:cable_bus run scoreboard players add #failures ae2m3s 1
execute unless data block 485 100 390 {cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}} run scoreboard players add #failures ae2m3s 1

execute if score #failures ae2m3s matches 0 run scoreboard players add #stable ae2m3s 1
execute unless score #failures ae2m3s matches 0 run scoreboard players set #stable ae2m3s 0
execute if score #stable ae2m3s matches 2.. run save-all flush
execute if score #stable ae2m3s matches 2.. run function ae2_m3:verify
execute unless score #stable ae2m3s matches 2.. if score #attempts ae2m3s matches ..59 run schedule function ae2_m3:settle_check 20t replace
execute unless score #stable ae2m3s matches 2.. if score #attempts ae2m3s matches 60.. run tellraw @a [{"text":"AE2 cumulative review fixture did not reach two consecutive exact structural checks within 60 seconds; no save/verify was accepted. A rewritten physical M5 state is a deliberate fail-closed result.","color":"red"}]
