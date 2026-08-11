# SPDX-License-Identifier: LGPL-3.0-only
# Require two consecutive exact checks without persisting fake formed/powered state.
scoreboard objectives add ae2m3p dummy
scoreboard players add #attempts ae2m3p 1
scoreboard players set #failures ae2m3p 0

# ae2-m3d-06 powered-two-by-two-by-two-all-eight
execute unless block 297 100 269 ae2:crafting_unit[formed=true,powered=true] run scoreboard players add #failures ae2m3p 1
execute unless block 298 100 269 ae2:crafting_accelerator[formed=true,powered=true] run scoreboard players add #failures ae2m3p 1
execute unless block 297 100 270 ae2:1k_crafting_storage[formed=true,powered=true] run scoreboard players add #failures ae2m3p 1
execute unless block 298 100 270 ae2:4k_crafting_storage[formed=true,powered=true] run scoreboard players add #failures ae2m3p 1
execute unless block 297 101 269 ae2:16k_crafting_storage[formed=true,powered=true] run scoreboard players add #failures ae2m3p 1
execute unless block 298 101 269 ae2:64k_crafting_storage[formed=true,powered=true] run scoreboard players add #failures ae2m3p 1
execute unless block 297 101 270 ae2:256k_crafting_storage[formed=true,powered=true] run scoreboard players add #failures ae2m3p 1
execute unless block 298 101 270 ae2:crafting_monitor[formed=true,powered=true,facing=up,spin=0] run scoreboard players add #failures ae2m3p 1
execute unless data block 298 101 270 {paintedColor:16b} run scoreboard players add #failures ae2m3p 1
execute unless block 297 98 269 ae2:creative_energy_cell run scoreboard players add #failures ae2m3p 1
execute unless block 297 99 269 ae2:cable_bus run scoreboard players add #failures ae2m3p 1
execute unless data block 297 99 269 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}} run scoreboard players add #failures ae2m3p 1

# ae2-m3d-09 compatible-extension-atomic-fallback
execute unless block 318 100 261 ae2:1k_crafting_storage[formed=true,powered=false] run scoreboard players add #failures ae2m3p 1
execute unless block 317 100 261 megacells:mega_crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3p 1
execute unless block 319 100 261 expandedae:exp_crafting_unit[formed=true,powered=false] run scoreboard players add #failures ae2m3p 1

execute if score #failures ae2m3p matches 0 run scoreboard players add #stable ae2m3p 1
execute unless score #failures ae2m3p matches 0 run scoreboard players set #stable ae2m3p 0
execute if score #stable ae2m3p matches 2.. run scoreboard players set #result ae2m3p 1
execute if score #stable ae2m3p matches 2.. run tellraw @a [{"text":"AE2 M3d bounded preprobe passed two consecutive formed/powered checks.","color":"green"}]
execute unless score #stable ae2m3p matches 2.. if score #attempts ae2m3p matches ..59 run schedule function ae2_m3:preprobe_check 20t replace
execute unless score #stable ae2m3p matches 2.. if score #attempts ae2m3p matches 60.. run scoreboard players set #result ae2m3p -1
execute unless score #stable ae2m3p matches 2.. if score #attempts ae2m3p matches 60.. run tellraw @a [{"text":"AE2 M3d bounded preprobe timed out before two consecutive exact checks.","color":"red"}]
