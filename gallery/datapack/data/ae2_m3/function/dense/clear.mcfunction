# SPDX-License-Identifier: LGPL-3.0-only
# Clear only the four disjoint optional dense-fixture owned volumes.
fill 271 95 191 280 100 200 minecraft:air replace
fill 303 95 191 312 100 200 minecraft:air replace
fill 271 95 231 280 100 240 minecraft:air replace
fill 303 95 231 312 100 240 minecraft:air replace
tellraw @a [{"text":"Cleared the optional AE2 M1 regression dense fixture.","color":"yellow"}]
