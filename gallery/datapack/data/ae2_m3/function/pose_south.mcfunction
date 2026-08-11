# SPDX-License-Identifier: LGPL-3.0-only
# Fixed south observation pose; no teleport if the deck is unsafe.
execute if block 221 107 254 minecraft:stone_bricks if block 221 108 254 minecraft:air if block 221 109 254 minecraft:air run teleport @s 221.5 108 254.5 180 14
execute unless block 221 107 254 minecraft:stone_bricks run tellraw @s [{"text":"Observation pose refused: deck floor is missing.","color":"red"}]
execute unless block 221 108 254 minecraft:air run tellraw @s [{"text":"Observation pose refused: foot space is blocked.","color":"red"}]
execute unless block 221 109 254 minecraft:air run tellraw @s [{"text":"Observation pose refused: head space is blocked.","color":"red"}]
