# SPDX-License-Identifier: LGPL-3.0-only
# Clear only the disjoint cumulative main gallery-owned volumes.
schedule clear ae2_m3:settle_check
schedule clear ae2_m3:appmek/check_20t
schedule clear ae2_m3:appmek/check_100t
fill 208 99 192 263 104 239 minecraft:air replace
fill 208 99 242 239 104 249 minecraft:air replace
fill 240 98 242 263 104 249 minecraft:air replace
fill 240 98 260 279 104 267 minecraft:air replace
fill 208 97 288 279 104 307 minecraft:air replace
fill 296 97 260 319 105 299 minecraft:air replace
fill 281 97 269 294 105 278 minecraft:air replace
fill 280 96 208 319 106 230 minecraft:air replace
fill 208 96 312 263 110 339 minecraft:air replace
fill 264 96 312 319 110 339 minecraft:air replace
fill 208 96 340 263 110 367 minecraft:air replace
fill 264 96 340 319 110 367 minecraft:air replace
fill 336 96 312 379 110 341 minecraft:air replace
fill 380 96 312 423 110 341 minecraft:air replace
fill 424 96 312 467 110 341 minecraft:air replace
fill 468 96 312 511 110 341 minecraft:air replace
fill 336 96 342 379 110 371 minecraft:air replace
fill 380 96 342 423 110 371 minecraft:air replace
fill 424 96 342 467 110 371 minecraft:air replace
fill 468 96 342 511 110 371 minecraft:air replace
fill 336 96 372 379 110 401 minecraft:air replace
fill 380 96 372 423 110 401 minecraft:air replace
fill 424 96 372 467 110 401 minecraft:air replace
fill 468 96 372 511 110 401 minecraft:air replace
fill 336 96 402 379 110 431 minecraft:air replace
fill 380 96 402 423 110 431 minecraft:air replace
fill 424 96 402 467 110 431 minecraft:air replace
fill 468 96 402 511 110 431 minecraft:air replace
fill 528 96 312 559 110 327 minecraft:air replace
fill 214 106 251 228 110 257 minecraft:air replace
fill 255 99 255 258 102 257 minecraft:air replace
tellraw @a [{"text":"Cleared the bounded cumulative AE2 review-gallery volumes.","color":"yellow"}]
