# SPDX-License-Identifier: LGPL-3.0-only
# Opt-in only: four scheduled 8x4x8 covered-dense lattices.
forceload add 256 176
forceload add 272 176
forceload add 272 192
forceload add 272 240
forceload add 288 176
forceload add 288 192
forceload add 288 240
forceload add 304 176
forceload add 304 192
forceload add 304 240
function ae2_m3:dense/clear
fill 272 95 192 279 95 199 minecraft:smooth_stone replace
fill 304 95 192 311 95 199 minecraft:smooth_stone replace
fill 272 95 232 279 95 239 minecraft:smooth_stone replace
fill 304 95 232 311 95 239 minecraft:smooth_stone replace
schedule function ae2_m3:dense/batch_1 1t replace
tellraw @a [{"text":"Scheduled the optional AE2 M1 regression dense fixture in four batches.","color":"aqua"}]
