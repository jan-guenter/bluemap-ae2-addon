# SPDX-License-Identifier: LGPL-3.0-only
# Bounded M3d formation/power preprobe; does not invoke the gallery build.
schedule clear ae2_m3:preprobe_check
forceload add 296 268 299 271
forceload add 316 260 319 262
fill 296 97 268 299 102 271 minecraft:air replace
fill 316 97 260 319 102 262 minecraft:air replace
fill 296 97 268 299 97 271 minecraft:smooth_stone replace
fill 316 97 260 319 97 262 minecraft:smooth_stone replace

# ae2-m3d-06 powered-two-by-two-by-two-all-eight
setblock 297 100 269 ae2:crafting_unit replace
setblock 298 100 269 ae2:crafting_accelerator replace
setblock 297 100 270 ae2:1k_crafting_storage replace
setblock 298 100 270 ae2:4k_crafting_storage replace
setblock 297 101 269 ae2:16k_crafting_storage replace
setblock 298 101 269 ae2:64k_crafting_storage replace
setblock 297 101 270 ae2:256k_crafting_storage replace
setblock 298 101 270 ae2:crafting_monitor[facing=up,spin=0] replace
data merge block 298 101 270 {paintedColor:16b}
setblock 297 98 269 ae2:creative_energy_cell replace
setblock 297 99 269 ae2:cable_bus replace
data merge block 297 99 269 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}

# ae2-m3d-09 compatible-extension-atomic-fallback
setblock 318 100 261 ae2:1k_crafting_storage replace
setblock 317 100 261 megacells:mega_crafting_unit replace
setblock 319 100 261 expandedae:exp_crafting_unit replace

scoreboard objectives add ae2m3p dummy
scoreboard players set #attempts ae2m3p 0
scoreboard players set #failures ae2m3p 0
scoreboard players set #stable ae2m3p 0
scoreboard players set #result ae2m3p 0
schedule function ae2_m3:preprobe_check 20t replace
tellraw @a [{"text":"AE2 M3d bounded preprobe scheduled; inspect #result ae2m3p (1=pass, -1=timeout).","color":"aqua"}]
