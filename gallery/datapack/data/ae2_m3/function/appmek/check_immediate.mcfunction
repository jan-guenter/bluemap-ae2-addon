# SPDX-License-Identifier: LGPL-3.0-only
# Exact Applied Mekanistics immediate retained-state checkpoint.
scoreboard objectives add ae2amrun dummy
scoreboard players set #failures ae2amrun 0
# ae2-appmek-01 All ten Applied Mekanistics cells in one native Drive
execute unless block 528 100 312 ae2:drive[facing=north,spin=0] run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 inv run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 inv.item0 run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 {inv:{item0:{id:"appmek:chemical_storage_cell_1k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 312 inv.item0.components run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 inv.item1 run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 {inv:{item1:{id:"appmek:portable_chemical_cell_1k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 312 inv.item1.components run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 inv.item2 run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 {inv:{item2:{id:"appmek:chemical_storage_cell_4k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 312 inv.item2.components run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 inv.item3 run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 {inv:{item3:{id:"appmek:portable_chemical_cell_4k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 312 inv.item3.components run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 inv.item4 run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 {inv:{item4:{id:"appmek:chemical_storage_cell_16k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 312 inv.item4.components run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 inv.item5 run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 {inv:{item5:{id:"appmek:portable_chemical_cell_16k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 312 inv.item5.components run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 inv.item6 run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 {inv:{item6:{id:"appmek:chemical_storage_cell_64k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 312 inv.item6.components run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 inv.item7 run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 {inv:{item7:{id:"appmek:portable_chemical_cell_64k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 312 inv.item7.components run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 inv.item8 run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 {inv:{item8:{id:"appmek:chemical_storage_cell_256k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 312 inv.item8.components run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 inv.item9 run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 {inv:{item9:{id:"appmek:portable_chemical_cell_256k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 312 inv.item9.components run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 312 inv.item10 run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 312 {id:"ae2:drive"} run scoreboard players add #failures ae2amrun 1

# ae2-appmek-02 Representative native Drive facing and spin controls
execute unless block 532 100 312 ae2:drive[facing=up,spin=1] run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 inv run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 inv.item0 run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 {inv:{item0:{id:"appmek:chemical_storage_cell_1k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item0.components run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 inv.item1 run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item1.id run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item1.count run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item1.components run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 inv.item2 run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item2.id run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item2.count run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item2.components run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 inv.item3 run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item3.id run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item3.count run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item3.components run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 inv.item4 run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item4.id run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item4.count run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item4.components run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 inv.item5 run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item5.id run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item5.count run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item5.components run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 inv.item6 run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item6.id run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item6.count run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item6.components run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 inv.item7 run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item7.id run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item7.count run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item7.components run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 inv.item8 run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item8.id run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item8.count run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item8.components run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 inv.item9 run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item9.id run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item9.count run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item9.components run scoreboard players add #failures ae2amrun 1
execute if data block 532 100 312 inv.item10 run scoreboard players add #failures ae2amrun 1
execute unless data block 532 100 312 {id:"ae2:drive"} run scoreboard players add #failures ae2amrun 1
execute unless block 536 100 312 ae2:drive[facing=east,spin=2] run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 inv run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 inv.item0 run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item0.id run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item0.count run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item0.components run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 inv.item1 run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item1.id run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item1.count run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item1.components run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 inv.item2 run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item2.id run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item2.count run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item2.components run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 inv.item3 run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item3.id run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item3.count run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item3.components run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 inv.item4 run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 {inv:{item4:{id:"appmek:chemical_storage_cell_16k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item4.components run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 inv.item5 run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item5.id run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item5.count run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item5.components run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 inv.item6 run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item6.id run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item6.count run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item6.components run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 inv.item7 run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item7.id run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item7.count run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item7.components run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 inv.item8 run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item8.id run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item8.count run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item8.components run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 inv.item9 run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item9.id run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item9.count run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item9.components run scoreboard players add #failures ae2amrun 1
execute if data block 536 100 312 inv.item10 run scoreboard players add #failures ae2amrun 1
execute unless data block 536 100 312 {id:"ae2:drive"} run scoreboard players add #failures ae2amrun 1
execute unless block 540 100 312 ae2:drive[facing=down,spin=3] run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 inv run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 inv.item0 run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item0.id run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item0.count run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item0.components run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 inv.item1 run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item1.id run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item1.count run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item1.components run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 inv.item2 run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item2.id run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item2.count run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item2.components run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 inv.item3 run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item3.id run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item3.count run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item3.components run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 inv.item4 run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item4.id run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item4.count run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item4.components run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 inv.item5 run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item5.id run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item5.count run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item5.components run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 inv.item6 run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item6.id run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item6.count run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item6.components run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 inv.item7 run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item7.id run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item7.count run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item7.components run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 inv.item8 run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item8.id run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item8.count run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item8.components run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 inv.item9 run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 {inv:{item9:{id:"appmek:portable_chemical_cell_256k",count:1}}} run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item9.components run scoreboard players add #failures ae2amrun 1
execute if data block 540 100 312 inv.item10 run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 312 {id:"ae2:drive"} run scoreboard players add #failures ae2amrun 1

# ae2-appmek-03 Storage-bus visual seams against exact Mekanism targets
execute unless block 528 100 318 ae2:cable_bus run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 318 {id:"ae2:cable_bus"} run scoreboard players add #failures ae2amrun 1
execute unless data block 528 100 318 {cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:storage_bus"}} run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 318 down run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 318 facadeDown run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 318 up run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 318 facadeUp run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 318 north run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 318 facadeNorth run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 318 south run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 318 facadeSouth run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 318 west run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 318 facadeWest run scoreboard players add #failures ae2amrun 1
execute if data block 528 100 318 facadeEast run scoreboard players add #failures ae2amrun 1
execute unless block 534 100 318 ae2:cable_bus run scoreboard players add #failures ae2amrun 1
execute unless data block 534 100 318 {id:"ae2:cable_bus"} run scoreboard players add #failures ae2amrun 1
execute unless data block 534 100 318 {cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:storage_bus"}} run scoreboard players add #failures ae2amrun 1
execute if data block 534 100 318 down run scoreboard players add #failures ae2amrun 1
execute if data block 534 100 318 facadeDown run scoreboard players add #failures ae2amrun 1
execute if data block 534 100 318 facadeUp run scoreboard players add #failures ae2amrun 1
execute if data block 534 100 318 north run scoreboard players add #failures ae2amrun 1
execute if data block 534 100 318 facadeNorth run scoreboard players add #failures ae2amrun 1
execute if data block 534 100 318 south run scoreboard players add #failures ae2amrun 1
execute if data block 534 100 318 facadeSouth run scoreboard players add #failures ae2amrun 1
execute if data block 534 100 318 west run scoreboard players add #failures ae2amrun 1
execute if data block 534 100 318 facadeWest run scoreboard players add #failures ae2amrun 1
execute if data block 534 100 318 east run scoreboard players add #failures ae2amrun 1
execute if data block 534 100 318 facadeEast run scoreboard players add #failures ae2amrun 1
execute unless block 529 100 318 mekanism:qio_dashboard[active=false,facing=west] run scoreboard players add #failures ae2amrun 1
execute unless data block 529 100 318 {id:"mekanism:qio_dashboard"} run scoreboard players add #failures ae2amrun 1
execute unless block 534 101 318 mekanism:radioactive_waste_barrel[facing=north] run scoreboard players add #failures ae2amrun 1
execute unless data block 534 101 318 {id:"mekanism:radioactive_waste_barrel"} run scoreboard players add #failures ae2amrun 1

# ae2-appmek-04 Pressurized-tube acceptor seam against the full-block ME Interface
execute unless block 540 100 318 mekanism:basic_pressurized_tube run scoreboard players add #failures ae2amrun 1
execute unless data block 540 100 318 {id:"mekanism:basic_pressurized_tube",connections:0b,acceptors:32b,connection:[I;0,0,0,0,0,0]} run scoreboard players add #failures ae2amrun 1
execute unless block 541 100 318 ae2:interface run scoreboard players add #failures ae2amrun 1
execute unless data block 541 100 318 {id:"ae2:interface"} run scoreboard players add #failures ae2amrun 1

scoreboard players set #appmek_immediate ae2amrun -1
execute if score #failures ae2amrun matches 0 run scoreboard players set #appmek_immediate ae2amrun 1
