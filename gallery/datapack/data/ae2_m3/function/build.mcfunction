# SPDX-License-Identifier: LGPL-3.0-only
# Exact AE2 S1 plus ATM 1.2.0 M4/M5 cumulative review fixture.
# Persist one count per complete build invocation so rebuilds are detectable.
scoreboard objectives add ae2m3run dummy
scoreboard players add #m3f_builds ae2m3run 1
scoreboard objectives add ae2s1run dummy
scoreboard players add #s1_builds ae2s1run 1
function ae2_m3:clear
time set noon
weather clear
gamerule doDaylightCycle false
gamerule doWeatherCycle false
fill 208 99 192 263 99 239 minecraft:smooth_stone replace
fill 208 99 242 239 99 249 minecraft:smooth_stone replace
fill 240 98 242 263 98 249 minecraft:smooth_stone replace
fill 240 99 242 263 99 249 minecraft:air replace
fill 240 98 260 279 98 267 minecraft:smooth_stone replace
fill 240 99 260 279 99 267 minecraft:air replace
fill 208 97 288 279 97 307 minecraft:smooth_stone replace
fill 208 98 288 279 104 307 minecraft:air replace
fill 296 97 260 319 97 299 minecraft:smooth_stone replace
fill 296 98 260 319 105 299 minecraft:air replace
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

# ae2-m1-01 stone-control
setblock 210 100 226 minecraft:stone replace

# ae2-m1-02 glass-to-energy-acceptor-fallback
setblock 216 100 226 ae2:cable_bus replace
data merge block 216 100 226 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 217 100 226 ae2:energy_acceptor replace

# ae2-m1-03 dense-smart-to-controller-fallback
setblock 222 100 226 ae2:cable_bus replace
data merge block 222 100 226 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 223 100 226 ae2:controller replace

# ae2-m1-04 all-colors-glass-dominoes
setblock 210 100 194 ae2:cable_bus replace
data merge block 210 100 194 {hasRedstone:2,cable:{id:"ae2:white_glass_cable"}}
setblock 211 100 194 ae2:cable_bus replace
data merge block 211 100 194 {hasRedstone:2,cable:{id:"ae2:white_glass_cable"}}
setblock 213 100 194 ae2:cable_bus replace
data merge block 213 100 194 {hasRedstone:2,cable:{id:"ae2:light_gray_glass_cable"}}
setblock 214 100 194 ae2:cable_bus replace
data merge block 214 100 194 {hasRedstone:2,cable:{id:"ae2:light_gray_glass_cable"}}
setblock 216 100 194 ae2:cable_bus replace
data merge block 216 100 194 {hasRedstone:2,cable:{id:"ae2:gray_glass_cable"}}
setblock 217 100 194 ae2:cable_bus replace
data merge block 217 100 194 {hasRedstone:2,cable:{id:"ae2:gray_glass_cable"}}
setblock 219 100 194 ae2:cable_bus replace
data merge block 219 100 194 {hasRedstone:2,cable:{id:"ae2:black_glass_cable"}}
setblock 220 100 194 ae2:cable_bus replace
data merge block 220 100 194 {hasRedstone:2,cable:{id:"ae2:black_glass_cable"}}
setblock 222 100 194 ae2:cable_bus replace
data merge block 222 100 194 {hasRedstone:2,cable:{id:"ae2:lime_glass_cable"}}
setblock 223 100 194 ae2:cable_bus replace
data merge block 223 100 194 {hasRedstone:2,cable:{id:"ae2:lime_glass_cable"}}
setblock 225 100 194 ae2:cable_bus replace
data merge block 225 100 194 {hasRedstone:2,cable:{id:"ae2:yellow_glass_cable"}}
setblock 226 100 194 ae2:cable_bus replace
data merge block 226 100 194 {hasRedstone:2,cable:{id:"ae2:yellow_glass_cable"}}
setblock 228 100 194 ae2:cable_bus replace
data merge block 228 100 194 {hasRedstone:2,cable:{id:"ae2:orange_glass_cable"}}
setblock 229 100 194 ae2:cable_bus replace
data merge block 229 100 194 {hasRedstone:2,cable:{id:"ae2:orange_glass_cable"}}
setblock 231 100 194 ae2:cable_bus replace
data merge block 231 100 194 {hasRedstone:2,cable:{id:"ae2:brown_glass_cable"}}
setblock 232 100 194 ae2:cable_bus replace
data merge block 232 100 194 {hasRedstone:2,cable:{id:"ae2:brown_glass_cable"}}
setblock 234 100 194 ae2:cable_bus replace
data merge block 234 100 194 {hasRedstone:2,cable:{id:"ae2:red_glass_cable"}}
setblock 235 100 194 ae2:cable_bus replace
data merge block 235 100 194 {hasRedstone:2,cable:{id:"ae2:red_glass_cable"}}
setblock 237 100 194 ae2:cable_bus replace
data merge block 237 100 194 {hasRedstone:2,cable:{id:"ae2:pink_glass_cable"}}
setblock 238 100 194 ae2:cable_bus replace
data merge block 238 100 194 {hasRedstone:2,cable:{id:"ae2:pink_glass_cable"}}
setblock 240 100 194 ae2:cable_bus replace
data merge block 240 100 194 {hasRedstone:2,cable:{id:"ae2:magenta_glass_cable"}}
setblock 241 100 194 ae2:cable_bus replace
data merge block 241 100 194 {hasRedstone:2,cable:{id:"ae2:magenta_glass_cable"}}
setblock 243 100 194 ae2:cable_bus replace
data merge block 243 100 194 {hasRedstone:2,cable:{id:"ae2:purple_glass_cable"}}
setblock 244 100 194 ae2:cable_bus replace
data merge block 244 100 194 {hasRedstone:2,cable:{id:"ae2:purple_glass_cable"}}
setblock 246 100 194 ae2:cable_bus replace
data merge block 246 100 194 {hasRedstone:2,cable:{id:"ae2:blue_glass_cable"}}
setblock 247 100 194 ae2:cable_bus replace
data merge block 247 100 194 {hasRedstone:2,cable:{id:"ae2:blue_glass_cable"}}
setblock 249 100 194 ae2:cable_bus replace
data merge block 249 100 194 {hasRedstone:2,cable:{id:"ae2:light_blue_glass_cable"}}
setblock 250 100 194 ae2:cable_bus replace
data merge block 250 100 194 {hasRedstone:2,cable:{id:"ae2:light_blue_glass_cable"}}
setblock 252 100 194 ae2:cable_bus replace
data merge block 252 100 194 {hasRedstone:2,cable:{id:"ae2:cyan_glass_cable"}}
setblock 253 100 194 ae2:cable_bus replace
data merge block 253 100 194 {hasRedstone:2,cable:{id:"ae2:cyan_glass_cable"}}
setblock 255 100 194 ae2:cable_bus replace
data merge block 255 100 194 {hasRedstone:2,cable:{id:"ae2:green_glass_cable"}}
setblock 256 100 194 ae2:cable_bus replace
data merge block 256 100 194 {hasRedstone:2,cable:{id:"ae2:green_glass_cable"}}
setblock 258 100 194 ae2:cable_bus replace
data merge block 258 100 194 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 259 100 194 ae2:cable_bus replace
data merge block 259 100 194 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}

# ae2-m1-05 all-colors-covered-dominoes
setblock 210 100 198 ae2:cable_bus replace
data merge block 210 100 198 {hasRedstone:2,cable:{id:"ae2:white_covered_cable"}}
setblock 211 100 198 ae2:cable_bus replace
data merge block 211 100 198 {hasRedstone:2,cable:{id:"ae2:white_covered_cable"}}
setblock 213 100 198 ae2:cable_bus replace
data merge block 213 100 198 {hasRedstone:2,cable:{id:"ae2:light_gray_covered_cable"}}
setblock 214 100 198 ae2:cable_bus replace
data merge block 214 100 198 {hasRedstone:2,cable:{id:"ae2:light_gray_covered_cable"}}
setblock 216 100 198 ae2:cable_bus replace
data merge block 216 100 198 {hasRedstone:2,cable:{id:"ae2:gray_covered_cable"}}
setblock 217 100 198 ae2:cable_bus replace
data merge block 217 100 198 {hasRedstone:2,cable:{id:"ae2:gray_covered_cable"}}
setblock 219 100 198 ae2:cable_bus replace
data merge block 219 100 198 {hasRedstone:2,cable:{id:"ae2:black_covered_cable"}}
setblock 220 100 198 ae2:cable_bus replace
data merge block 220 100 198 {hasRedstone:2,cable:{id:"ae2:black_covered_cable"}}
setblock 222 100 198 ae2:cable_bus replace
data merge block 222 100 198 {hasRedstone:2,cable:{id:"ae2:lime_covered_cable"}}
setblock 223 100 198 ae2:cable_bus replace
data merge block 223 100 198 {hasRedstone:2,cable:{id:"ae2:lime_covered_cable"}}
setblock 225 100 198 ae2:cable_bus replace
data merge block 225 100 198 {hasRedstone:2,cable:{id:"ae2:yellow_covered_cable"}}
setblock 226 100 198 ae2:cable_bus replace
data merge block 226 100 198 {hasRedstone:2,cable:{id:"ae2:yellow_covered_cable"}}
setblock 228 100 198 ae2:cable_bus replace
data merge block 228 100 198 {hasRedstone:2,cable:{id:"ae2:orange_covered_cable"}}
setblock 229 100 198 ae2:cable_bus replace
data merge block 229 100 198 {hasRedstone:2,cable:{id:"ae2:orange_covered_cable"}}
setblock 231 100 198 ae2:cable_bus replace
data merge block 231 100 198 {hasRedstone:2,cable:{id:"ae2:brown_covered_cable"}}
setblock 232 100 198 ae2:cable_bus replace
data merge block 232 100 198 {hasRedstone:2,cable:{id:"ae2:brown_covered_cable"}}
setblock 234 100 198 ae2:cable_bus replace
data merge block 234 100 198 {hasRedstone:2,cable:{id:"ae2:red_covered_cable"}}
setblock 235 100 198 ae2:cable_bus replace
data merge block 235 100 198 {hasRedstone:2,cable:{id:"ae2:red_covered_cable"}}
setblock 237 100 198 ae2:cable_bus replace
data merge block 237 100 198 {hasRedstone:2,cable:{id:"ae2:pink_covered_cable"}}
setblock 238 100 198 ae2:cable_bus replace
data merge block 238 100 198 {hasRedstone:2,cable:{id:"ae2:pink_covered_cable"}}
setblock 240 100 198 ae2:cable_bus replace
data merge block 240 100 198 {hasRedstone:2,cable:{id:"ae2:magenta_covered_cable"}}
setblock 241 100 198 ae2:cable_bus replace
data merge block 241 100 198 {hasRedstone:2,cable:{id:"ae2:magenta_covered_cable"}}
setblock 243 100 198 ae2:cable_bus replace
data merge block 243 100 198 {hasRedstone:2,cable:{id:"ae2:purple_covered_cable"}}
setblock 244 100 198 ae2:cable_bus replace
data merge block 244 100 198 {hasRedstone:2,cable:{id:"ae2:purple_covered_cable"}}
setblock 246 100 198 ae2:cable_bus replace
data merge block 246 100 198 {hasRedstone:2,cable:{id:"ae2:blue_covered_cable"}}
setblock 247 100 198 ae2:cable_bus replace
data merge block 247 100 198 {hasRedstone:2,cable:{id:"ae2:blue_covered_cable"}}
setblock 249 100 198 ae2:cable_bus replace
data merge block 249 100 198 {hasRedstone:2,cable:{id:"ae2:light_blue_covered_cable"}}
setblock 250 100 198 ae2:cable_bus replace
data merge block 250 100 198 {hasRedstone:2,cable:{id:"ae2:light_blue_covered_cable"}}
setblock 252 100 198 ae2:cable_bus replace
data merge block 252 100 198 {hasRedstone:2,cable:{id:"ae2:cyan_covered_cable"}}
setblock 253 100 198 ae2:cable_bus replace
data merge block 253 100 198 {hasRedstone:2,cable:{id:"ae2:cyan_covered_cable"}}
setblock 255 100 198 ae2:cable_bus replace
data merge block 255 100 198 {hasRedstone:2,cable:{id:"ae2:green_covered_cable"}}
setblock 256 100 198 ae2:cable_bus replace
data merge block 256 100 198 {hasRedstone:2,cable:{id:"ae2:green_covered_cable"}}
setblock 258 100 198 ae2:cable_bus replace
data merge block 258 100 198 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 259 100 198 ae2:cable_bus replace
data merge block 259 100 198 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}

# ae2-m1-06 all-colors-smart-dominoes
setblock 210 100 202 ae2:cable_bus replace
data merge block 210 100 202 {hasRedstone:2,cable:{id:"ae2:white_smart_cable"}}
setblock 211 100 202 ae2:cable_bus replace
data merge block 211 100 202 {hasRedstone:2,cable:{id:"ae2:white_smart_cable"}}
setblock 213 100 202 ae2:cable_bus replace
data merge block 213 100 202 {hasRedstone:2,cable:{id:"ae2:light_gray_smart_cable"}}
setblock 214 100 202 ae2:cable_bus replace
data merge block 214 100 202 {hasRedstone:2,cable:{id:"ae2:light_gray_smart_cable"}}
setblock 216 100 202 ae2:cable_bus replace
data merge block 216 100 202 {hasRedstone:2,cable:{id:"ae2:gray_smart_cable"}}
setblock 217 100 202 ae2:cable_bus replace
data merge block 217 100 202 {hasRedstone:2,cable:{id:"ae2:gray_smart_cable"}}
setblock 219 100 202 ae2:cable_bus replace
data merge block 219 100 202 {hasRedstone:2,cable:{id:"ae2:black_smart_cable"}}
setblock 220 100 202 ae2:cable_bus replace
data merge block 220 100 202 {hasRedstone:2,cable:{id:"ae2:black_smart_cable"}}
setblock 222 100 202 ae2:cable_bus replace
data merge block 222 100 202 {hasRedstone:2,cable:{id:"ae2:lime_smart_cable"}}
setblock 223 100 202 ae2:cable_bus replace
data merge block 223 100 202 {hasRedstone:2,cable:{id:"ae2:lime_smart_cable"}}
setblock 225 100 202 ae2:cable_bus replace
data merge block 225 100 202 {hasRedstone:2,cable:{id:"ae2:yellow_smart_cable"}}
setblock 226 100 202 ae2:cable_bus replace
data merge block 226 100 202 {hasRedstone:2,cable:{id:"ae2:yellow_smart_cable"}}
setblock 228 100 202 ae2:cable_bus replace
data merge block 228 100 202 {hasRedstone:2,cable:{id:"ae2:orange_smart_cable"}}
setblock 229 100 202 ae2:cable_bus replace
data merge block 229 100 202 {hasRedstone:2,cable:{id:"ae2:orange_smart_cable"}}
setblock 231 100 202 ae2:cable_bus replace
data merge block 231 100 202 {hasRedstone:2,cable:{id:"ae2:brown_smart_cable"}}
setblock 232 100 202 ae2:cable_bus replace
data merge block 232 100 202 {hasRedstone:2,cable:{id:"ae2:brown_smart_cable"}}
setblock 234 100 202 ae2:cable_bus replace
data merge block 234 100 202 {hasRedstone:2,cable:{id:"ae2:red_smart_cable"}}
setblock 235 100 202 ae2:cable_bus replace
data merge block 235 100 202 {hasRedstone:2,cable:{id:"ae2:red_smart_cable"}}
setblock 237 100 202 ae2:cable_bus replace
data merge block 237 100 202 {hasRedstone:2,cable:{id:"ae2:pink_smart_cable"}}
setblock 238 100 202 ae2:cable_bus replace
data merge block 238 100 202 {hasRedstone:2,cable:{id:"ae2:pink_smart_cable"}}
setblock 240 100 202 ae2:cable_bus replace
data merge block 240 100 202 {hasRedstone:2,cable:{id:"ae2:magenta_smart_cable"}}
setblock 241 100 202 ae2:cable_bus replace
data merge block 241 100 202 {hasRedstone:2,cable:{id:"ae2:magenta_smart_cable"}}
setblock 243 100 202 ae2:cable_bus replace
data merge block 243 100 202 {hasRedstone:2,cable:{id:"ae2:purple_smart_cable"}}
setblock 244 100 202 ae2:cable_bus replace
data merge block 244 100 202 {hasRedstone:2,cable:{id:"ae2:purple_smart_cable"}}
setblock 246 100 202 ae2:cable_bus replace
data merge block 246 100 202 {hasRedstone:2,cable:{id:"ae2:blue_smart_cable"}}
setblock 247 100 202 ae2:cable_bus replace
data merge block 247 100 202 {hasRedstone:2,cable:{id:"ae2:blue_smart_cable"}}
setblock 249 100 202 ae2:cable_bus replace
data merge block 249 100 202 {hasRedstone:2,cable:{id:"ae2:light_blue_smart_cable"}}
setblock 250 100 202 ae2:cable_bus replace
data merge block 250 100 202 {hasRedstone:2,cable:{id:"ae2:light_blue_smart_cable"}}
setblock 252 100 202 ae2:cable_bus replace
data merge block 252 100 202 {hasRedstone:2,cable:{id:"ae2:cyan_smart_cable"}}
setblock 253 100 202 ae2:cable_bus replace
data merge block 253 100 202 {hasRedstone:2,cable:{id:"ae2:cyan_smart_cable"}}
setblock 255 100 202 ae2:cable_bus replace
data merge block 255 100 202 {hasRedstone:2,cable:{id:"ae2:green_smart_cable"}}
setblock 256 100 202 ae2:cable_bus replace
data merge block 256 100 202 {hasRedstone:2,cable:{id:"ae2:green_smart_cable"}}
setblock 258 100 202 ae2:cable_bus replace
data merge block 258 100 202 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 259 100 202 ae2:cable_bus replace
data merge block 259 100 202 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}

# ae2-m1-07 all-colors-dense_covered-dominoes
setblock 210 100 206 ae2:cable_bus replace
data merge block 210 100 206 {hasRedstone:2,cable:{id:"ae2:white_covered_dense_cable"}}
setblock 211 100 206 ae2:cable_bus replace
data merge block 211 100 206 {hasRedstone:2,cable:{id:"ae2:white_covered_dense_cable"}}
setblock 213 100 206 ae2:cable_bus replace
data merge block 213 100 206 {hasRedstone:2,cable:{id:"ae2:light_gray_covered_dense_cable"}}
setblock 214 100 206 ae2:cable_bus replace
data merge block 214 100 206 {hasRedstone:2,cable:{id:"ae2:light_gray_covered_dense_cable"}}
setblock 216 100 206 ae2:cable_bus replace
data merge block 216 100 206 {hasRedstone:2,cable:{id:"ae2:gray_covered_dense_cable"}}
setblock 217 100 206 ae2:cable_bus replace
data merge block 217 100 206 {hasRedstone:2,cable:{id:"ae2:gray_covered_dense_cable"}}
setblock 219 100 206 ae2:cable_bus replace
data merge block 219 100 206 {hasRedstone:2,cable:{id:"ae2:black_covered_dense_cable"}}
setblock 220 100 206 ae2:cable_bus replace
data merge block 220 100 206 {hasRedstone:2,cable:{id:"ae2:black_covered_dense_cable"}}
setblock 222 100 206 ae2:cable_bus replace
data merge block 222 100 206 {hasRedstone:2,cable:{id:"ae2:lime_covered_dense_cable"}}
setblock 223 100 206 ae2:cable_bus replace
data merge block 223 100 206 {hasRedstone:2,cable:{id:"ae2:lime_covered_dense_cable"}}
setblock 225 100 206 ae2:cable_bus replace
data merge block 225 100 206 {hasRedstone:2,cable:{id:"ae2:yellow_covered_dense_cable"}}
setblock 226 100 206 ae2:cable_bus replace
data merge block 226 100 206 {hasRedstone:2,cable:{id:"ae2:yellow_covered_dense_cable"}}
setblock 228 100 206 ae2:cable_bus replace
data merge block 228 100 206 {hasRedstone:2,cable:{id:"ae2:orange_covered_dense_cable"}}
setblock 229 100 206 ae2:cable_bus replace
data merge block 229 100 206 {hasRedstone:2,cable:{id:"ae2:orange_covered_dense_cable"}}
setblock 231 100 206 ae2:cable_bus replace
data merge block 231 100 206 {hasRedstone:2,cable:{id:"ae2:brown_covered_dense_cable"}}
setblock 232 100 206 ae2:cable_bus replace
data merge block 232 100 206 {hasRedstone:2,cable:{id:"ae2:brown_covered_dense_cable"}}
setblock 234 100 206 ae2:cable_bus replace
data merge block 234 100 206 {hasRedstone:2,cable:{id:"ae2:red_covered_dense_cable"}}
setblock 235 100 206 ae2:cable_bus replace
data merge block 235 100 206 {hasRedstone:2,cable:{id:"ae2:red_covered_dense_cable"}}
setblock 237 100 206 ae2:cable_bus replace
data merge block 237 100 206 {hasRedstone:2,cable:{id:"ae2:pink_covered_dense_cable"}}
setblock 238 100 206 ae2:cable_bus replace
data merge block 238 100 206 {hasRedstone:2,cable:{id:"ae2:pink_covered_dense_cable"}}
setblock 240 100 206 ae2:cable_bus replace
data merge block 240 100 206 {hasRedstone:2,cable:{id:"ae2:magenta_covered_dense_cable"}}
setblock 241 100 206 ae2:cable_bus replace
data merge block 241 100 206 {hasRedstone:2,cable:{id:"ae2:magenta_covered_dense_cable"}}
setblock 243 100 206 ae2:cable_bus replace
data merge block 243 100 206 {hasRedstone:2,cable:{id:"ae2:purple_covered_dense_cable"}}
setblock 244 100 206 ae2:cable_bus replace
data merge block 244 100 206 {hasRedstone:2,cable:{id:"ae2:purple_covered_dense_cable"}}
setblock 246 100 206 ae2:cable_bus replace
data merge block 246 100 206 {hasRedstone:2,cable:{id:"ae2:blue_covered_dense_cable"}}
setblock 247 100 206 ae2:cable_bus replace
data merge block 247 100 206 {hasRedstone:2,cable:{id:"ae2:blue_covered_dense_cable"}}
setblock 249 100 206 ae2:cable_bus replace
data merge block 249 100 206 {hasRedstone:2,cable:{id:"ae2:light_blue_covered_dense_cable"}}
setblock 250 100 206 ae2:cable_bus replace
data merge block 250 100 206 {hasRedstone:2,cable:{id:"ae2:light_blue_covered_dense_cable"}}
setblock 252 100 206 ae2:cable_bus replace
data merge block 252 100 206 {hasRedstone:2,cable:{id:"ae2:cyan_covered_dense_cable"}}
setblock 253 100 206 ae2:cable_bus replace
data merge block 253 100 206 {hasRedstone:2,cable:{id:"ae2:cyan_covered_dense_cable"}}
setblock 255 100 206 ae2:cable_bus replace
data merge block 255 100 206 {hasRedstone:2,cable:{id:"ae2:green_covered_dense_cable"}}
setblock 256 100 206 ae2:cable_bus replace
data merge block 256 100 206 {hasRedstone:2,cable:{id:"ae2:green_covered_dense_cable"}}
setblock 258 100 206 ae2:cable_bus replace
data merge block 258 100 206 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}
setblock 259 100 206 ae2:cable_bus replace
data merge block 259 100 206 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}

# ae2-m1-08 all-colors-dense_smart-dominoes
setblock 210 100 210 ae2:cable_bus replace
data merge block 210 100 210 {hasRedstone:2,cable:{id:"ae2:white_smart_dense_cable"}}
setblock 211 100 210 ae2:cable_bus replace
data merge block 211 100 210 {hasRedstone:2,cable:{id:"ae2:white_smart_dense_cable"}}
setblock 213 100 210 ae2:cable_bus replace
data merge block 213 100 210 {hasRedstone:2,cable:{id:"ae2:light_gray_smart_dense_cable"}}
setblock 214 100 210 ae2:cable_bus replace
data merge block 214 100 210 {hasRedstone:2,cable:{id:"ae2:light_gray_smart_dense_cable"}}
setblock 216 100 210 ae2:cable_bus replace
data merge block 216 100 210 {hasRedstone:2,cable:{id:"ae2:gray_smart_dense_cable"}}
setblock 217 100 210 ae2:cable_bus replace
data merge block 217 100 210 {hasRedstone:2,cable:{id:"ae2:gray_smart_dense_cable"}}
setblock 219 100 210 ae2:cable_bus replace
data merge block 219 100 210 {hasRedstone:2,cable:{id:"ae2:black_smart_dense_cable"}}
setblock 220 100 210 ae2:cable_bus replace
data merge block 220 100 210 {hasRedstone:2,cable:{id:"ae2:black_smart_dense_cable"}}
setblock 222 100 210 ae2:cable_bus replace
data merge block 222 100 210 {hasRedstone:2,cable:{id:"ae2:lime_smart_dense_cable"}}
setblock 223 100 210 ae2:cable_bus replace
data merge block 223 100 210 {hasRedstone:2,cable:{id:"ae2:lime_smart_dense_cable"}}
setblock 225 100 210 ae2:cable_bus replace
data merge block 225 100 210 {hasRedstone:2,cable:{id:"ae2:yellow_smart_dense_cable"}}
setblock 226 100 210 ae2:cable_bus replace
data merge block 226 100 210 {hasRedstone:2,cable:{id:"ae2:yellow_smart_dense_cable"}}
setblock 228 100 210 ae2:cable_bus replace
data merge block 228 100 210 {hasRedstone:2,cable:{id:"ae2:orange_smart_dense_cable"}}
setblock 229 100 210 ae2:cable_bus replace
data merge block 229 100 210 {hasRedstone:2,cable:{id:"ae2:orange_smart_dense_cable"}}
setblock 231 100 210 ae2:cable_bus replace
data merge block 231 100 210 {hasRedstone:2,cable:{id:"ae2:brown_smart_dense_cable"}}
setblock 232 100 210 ae2:cable_bus replace
data merge block 232 100 210 {hasRedstone:2,cable:{id:"ae2:brown_smart_dense_cable"}}
setblock 234 100 210 ae2:cable_bus replace
data merge block 234 100 210 {hasRedstone:2,cable:{id:"ae2:red_smart_dense_cable"}}
setblock 235 100 210 ae2:cable_bus replace
data merge block 235 100 210 {hasRedstone:2,cable:{id:"ae2:red_smart_dense_cable"}}
setblock 237 100 210 ae2:cable_bus replace
data merge block 237 100 210 {hasRedstone:2,cable:{id:"ae2:pink_smart_dense_cable"}}
setblock 238 100 210 ae2:cable_bus replace
data merge block 238 100 210 {hasRedstone:2,cable:{id:"ae2:pink_smart_dense_cable"}}
setblock 240 100 210 ae2:cable_bus replace
data merge block 240 100 210 {hasRedstone:2,cable:{id:"ae2:magenta_smart_dense_cable"}}
setblock 241 100 210 ae2:cable_bus replace
data merge block 241 100 210 {hasRedstone:2,cable:{id:"ae2:magenta_smart_dense_cable"}}
setblock 243 100 210 ae2:cable_bus replace
data merge block 243 100 210 {hasRedstone:2,cable:{id:"ae2:purple_smart_dense_cable"}}
setblock 244 100 210 ae2:cable_bus replace
data merge block 244 100 210 {hasRedstone:2,cable:{id:"ae2:purple_smart_dense_cable"}}
setblock 246 100 210 ae2:cable_bus replace
data merge block 246 100 210 {hasRedstone:2,cable:{id:"ae2:blue_smart_dense_cable"}}
setblock 247 100 210 ae2:cable_bus replace
data merge block 247 100 210 {hasRedstone:2,cable:{id:"ae2:blue_smart_dense_cable"}}
setblock 249 100 210 ae2:cable_bus replace
data merge block 249 100 210 {hasRedstone:2,cable:{id:"ae2:light_blue_smart_dense_cable"}}
setblock 250 100 210 ae2:cable_bus replace
data merge block 250 100 210 {hasRedstone:2,cable:{id:"ae2:light_blue_smart_dense_cable"}}
setblock 252 100 210 ae2:cable_bus replace
data merge block 252 100 210 {hasRedstone:2,cable:{id:"ae2:cyan_smart_dense_cable"}}
setblock 253 100 210 ae2:cable_bus replace
data merge block 253 100 210 {hasRedstone:2,cable:{id:"ae2:cyan_smart_dense_cable"}}
setblock 255 100 210 ae2:cable_bus replace
data merge block 255 100 210 {hasRedstone:2,cable:{id:"ae2:green_smart_dense_cable"}}
setblock 256 100 210 ae2:cable_bus replace
data merge block 256 100 210 {hasRedstone:2,cable:{id:"ae2:green_smart_dense_cable"}}
setblock 258 100 210 ae2:cable_bus replace
data merge block 258 100 210 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 259 100 210 ae2:cable_bus replace
data merge block 259 100 210 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}

# ae2-m1-09 straight-glass
setblock 210 100 222 ae2:cable_bus replace
data merge block 210 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 211 100 222 ae2:cable_bus replace
data merge block 211 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 212 100 222 ae2:cable_bus replace
data merge block 212 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}

# ae2-m1-10 straight-covered
setblock 217 100 222 ae2:cable_bus replace
data merge block 217 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 218 100 222 ae2:cable_bus replace
data merge block 218 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 219 100 222 ae2:cable_bus replace
data merge block 219 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}

# ae2-m1-11 straight-smart
setblock 224 100 222 ae2:cable_bus replace
data merge block 224 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 225 100 222 ae2:cable_bus replace
data merge block 225 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 226 100 222 ae2:cable_bus replace
data merge block 226 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}

# ae2-m1-12 straight-dense_covered
setblock 231 100 222 ae2:cable_bus replace
data merge block 231 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}
setblock 232 100 222 ae2:cable_bus replace
data merge block 232 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}
setblock 233 100 222 ae2:cable_bus replace
data merge block 233 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}

# ae2-m1-13 straight-dense_smart
setblock 238 100 222 ae2:cable_bus replace
data merge block 238 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 239 100 222 ae2:cable_bus replace
data merge block 239 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 240 100 222 ae2:cable_bus replace
data merge block 240 100 222 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}

# ae2-m1-14 glass-corner
setblock 211 100 234 ae2:cable_bus replace
data merge block 211 100 234 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 212 100 234 ae2:cable_bus replace
data merge block 212 100 234 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 211 100 235 ae2:cable_bus replace
data merge block 211 100 235 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}

# ae2-m1-15 smart-t-junction
setblock 223 100 234 ae2:cable_bus replace
data merge block 223 100 234 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 222 100 234 ae2:cable_bus replace
data merge block 222 100 234 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 224 100 234 ae2:cable_bus replace
data merge block 224 100 234 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 223 100 235 ae2:cable_bus replace
data merge block 223 100 235 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}

# ae2-m1-16 dense-covered-cross
setblock 237 100 234 ae2:cable_bus replace
data merge block 237 100 234 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}
setblock 236 100 234 ae2:cable_bus replace
data merge block 236 100 234 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}
setblock 238 100 234 ae2:cable_bus replace
data merge block 238 100 234 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}
setblock 237 100 233 ae2:cable_bus replace
data merge block 237 100 233 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}
setblock 237 100 235 ae2:cable_bus replace
data merge block 237 100 235 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}

# ae2-m1-17 dense-smart-six-way
setblock 251 102 234 ae2:cable_bus replace
data merge block 251 102 234 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 250 102 234 ae2:cable_bus replace
data merge block 250 102 234 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 252 102 234 ae2:cable_bus replace
data merge block 252 102 234 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 251 102 233 ae2:cable_bus replace
data merge block 251 102 233 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 251 102 235 ae2:cable_bus replace
data merge block 251 102 235 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 251 101 234 ae2:cable_bus replace
data merge block 251 101 234 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 251 103 234 ae2:cable_bus replace
data merge block 251 103 234 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}

# ae2-m1-18 compatible-fluix-glass-to-glass
setblock 210 100 214 ae2:cable_bus replace
data merge block 210 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 211 100 214 ae2:cable_bus replace
data merge block 211 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}

# ae2-m1-19 compatible-fluix-glass-to-covered
setblock 213 100 214 ae2:cable_bus replace
data merge block 213 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 214 100 214 ae2:cable_bus replace
data merge block 214 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}

# ae2-m1-20 compatible-fluix-glass-to-smart
setblock 216 100 214 ae2:cable_bus replace
data merge block 216 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 217 100 214 ae2:cable_bus replace
data merge block 217 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}

# ae2-m1-21 compatible-fluix-glass-to-dense_covered
setblock 219 100 214 ae2:cable_bus replace
data merge block 219 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 220 100 214 ae2:cable_bus replace
data merge block 220 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}

# ae2-m1-22 compatible-fluix-glass-to-dense_smart
setblock 222 100 214 ae2:cable_bus replace
data merge block 222 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 223 100 214 ae2:cable_bus replace
data merge block 223 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}

# ae2-m1-23 compatible-fluix-covered-to-covered
setblock 225 100 214 ae2:cable_bus replace
data merge block 225 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 226 100 214 ae2:cable_bus replace
data merge block 226 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}

# ae2-m1-24 compatible-fluix-covered-to-smart
setblock 228 100 214 ae2:cable_bus replace
data merge block 228 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 229 100 214 ae2:cable_bus replace
data merge block 229 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}

# ae2-m1-25 compatible-fluix-covered-to-dense_covered
setblock 231 100 214 ae2:cable_bus replace
data merge block 231 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 232 100 214 ae2:cable_bus replace
data merge block 232 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}

# ae2-m1-26 compatible-fluix-covered-to-dense_smart
setblock 234 100 214 ae2:cable_bus replace
data merge block 234 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 235 100 214 ae2:cable_bus replace
data merge block 235 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}

# ae2-m1-27 compatible-fluix-smart-to-smart
setblock 237 100 214 ae2:cable_bus replace
data merge block 237 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 238 100 214 ae2:cable_bus replace
data merge block 238 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}

# ae2-m1-28 compatible-fluix-smart-to-dense_covered
setblock 240 100 214 ae2:cable_bus replace
data merge block 240 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 241 100 214 ae2:cable_bus replace
data merge block 241 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}

# ae2-m1-29 compatible-fluix-smart-to-dense_smart
setblock 243 100 214 ae2:cable_bus replace
data merge block 243 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 244 100 214 ae2:cable_bus replace
data merge block 244 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}

# ae2-m1-30 compatible-fluix-dense_covered-to-dense_covered
setblock 246 100 214 ae2:cable_bus replace
data merge block 246 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}
setblock 247 100 214 ae2:cable_bus replace
data merge block 247 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}

# ae2-m1-31 compatible-fluix-dense_covered-to-dense_smart
setblock 249 100 214 ae2:cable_bus replace
data merge block 249 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_covered_dense_cable"}}
setblock 250 100 214 ae2:cable_bus replace
data merge block 250 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}

# ae2-m1-32 compatible-fluix-dense_smart-to-dense_smart
setblock 252 100 214 ae2:cable_bus replace
data merge block 252 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 253 100 214 ae2:cable_bus replace
data merge block 253 100 214 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}

# ae2-m1-33 compatible-red-covered-to-smart
setblock 256 100 214 ae2:cable_bus replace
data merge block 256 100 214 {hasRedstone:2,cable:{id:"ae2:red_covered_cable"}}
setblock 257 100 214 ae2:cable_bus replace
data merge block 257 100 214 {hasRedstone:2,cable:{id:"ae2:red_smart_cable"}}

# ae2-m1-34 incompatible-red-glass-to-blue-glass
setblock 210 100 218 ae2:cable_bus replace
data merge block 210 100 218 {hasRedstone:2,cable:{id:"ae2:red_glass_cable"}}
setblock 211 100 218 ae2:cable_bus replace
data merge block 211 100 218 {hasRedstone:2,cable:{id:"ae2:blue_glass_cable"}}

# ae2-m1-35 incompatible-red-glass-to-blue-covered
setblock 213 100 218 ae2:cable_bus replace
data merge block 213 100 218 {hasRedstone:2,cable:{id:"ae2:red_glass_cable"}}
setblock 214 100 218 ae2:cable_bus replace
data merge block 214 100 218 {hasRedstone:2,cable:{id:"ae2:blue_covered_cable"}}

# ae2-m1-36 incompatible-red-glass-to-blue-smart
setblock 216 100 218 ae2:cable_bus replace
data merge block 216 100 218 {hasRedstone:2,cable:{id:"ae2:red_glass_cable"}}
setblock 217 100 218 ae2:cable_bus replace
data merge block 217 100 218 {hasRedstone:2,cable:{id:"ae2:blue_smart_cable"}}

# ae2-m1-37 incompatible-red-glass-to-blue-dense_covered
setblock 219 100 218 ae2:cable_bus replace
data merge block 219 100 218 {hasRedstone:2,cable:{id:"ae2:red_glass_cable"}}
setblock 220 100 218 ae2:cable_bus replace
data merge block 220 100 218 {hasRedstone:2,cable:{id:"ae2:blue_covered_dense_cable"}}

# ae2-m1-38 incompatible-red-glass-to-blue-dense_smart
setblock 222 100 218 ae2:cable_bus replace
data merge block 222 100 218 {hasRedstone:2,cable:{id:"ae2:red_glass_cable"}}
setblock 223 100 218 ae2:cable_bus replace
data merge block 223 100 218 {hasRedstone:2,cable:{id:"ae2:blue_smart_dense_cable"}}

# ae2-m1-39 incompatible-red-covered-to-blue-covered
setblock 225 100 218 ae2:cable_bus replace
data merge block 225 100 218 {hasRedstone:2,cable:{id:"ae2:red_covered_cable"}}
setblock 226 100 218 ae2:cable_bus replace
data merge block 226 100 218 {hasRedstone:2,cable:{id:"ae2:blue_covered_cable"}}

# ae2-m1-40 incompatible-red-covered-to-blue-smart
setblock 228 100 218 ae2:cable_bus replace
data merge block 228 100 218 {hasRedstone:2,cable:{id:"ae2:red_covered_cable"}}
setblock 229 100 218 ae2:cable_bus replace
data merge block 229 100 218 {hasRedstone:2,cable:{id:"ae2:blue_smart_cable"}}

# ae2-m1-41 incompatible-red-covered-to-blue-dense_covered
setblock 231 100 218 ae2:cable_bus replace
data merge block 231 100 218 {hasRedstone:2,cable:{id:"ae2:red_covered_cable"}}
setblock 232 100 218 ae2:cable_bus replace
data merge block 232 100 218 {hasRedstone:2,cable:{id:"ae2:blue_covered_dense_cable"}}

# ae2-m1-42 incompatible-red-covered-to-blue-dense_smart
setblock 234 100 218 ae2:cable_bus replace
data merge block 234 100 218 {hasRedstone:2,cable:{id:"ae2:red_covered_cable"}}
setblock 235 100 218 ae2:cable_bus replace
data merge block 235 100 218 {hasRedstone:2,cable:{id:"ae2:blue_smart_dense_cable"}}

# ae2-m1-43 incompatible-red-smart-to-blue-smart
setblock 237 100 218 ae2:cable_bus replace
data merge block 237 100 218 {hasRedstone:2,cable:{id:"ae2:red_smart_cable"}}
setblock 238 100 218 ae2:cable_bus replace
data merge block 238 100 218 {hasRedstone:2,cable:{id:"ae2:blue_smart_cable"}}

# ae2-m1-44 incompatible-red-smart-to-blue-dense_covered
setblock 240 100 218 ae2:cable_bus replace
data merge block 240 100 218 {hasRedstone:2,cable:{id:"ae2:red_smart_cable"}}
setblock 241 100 218 ae2:cable_bus replace
data merge block 241 100 218 {hasRedstone:2,cable:{id:"ae2:blue_covered_dense_cable"}}

# ae2-m1-45 incompatible-red-smart-to-blue-dense_smart
setblock 243 100 218 ae2:cable_bus replace
data merge block 243 100 218 {hasRedstone:2,cable:{id:"ae2:red_smart_cable"}}
setblock 244 100 218 ae2:cable_bus replace
data merge block 244 100 218 {hasRedstone:2,cable:{id:"ae2:blue_smart_dense_cable"}}

# ae2-m1-46 incompatible-red-dense_covered-to-blue-dense_covered
setblock 246 100 218 ae2:cable_bus replace
data merge block 246 100 218 {hasRedstone:2,cable:{id:"ae2:red_covered_dense_cable"}}
setblock 247 100 218 ae2:cable_bus replace
data merge block 247 100 218 {hasRedstone:2,cable:{id:"ae2:blue_covered_dense_cable"}}

# ae2-m1-47 incompatible-red-dense_covered-to-blue-dense_smart
setblock 249 100 218 ae2:cable_bus replace
data merge block 249 100 218 {hasRedstone:2,cable:{id:"ae2:red_covered_dense_cable"}}
setblock 250 100 218 ae2:cable_bus replace
data merge block 250 100 218 {hasRedstone:2,cable:{id:"ae2:blue_smart_dense_cable"}}

# ae2-m1-48 incompatible-red-dense_smart-to-blue-dense_smart
setblock 252 100 218 ae2:cable_bus replace
data merge block 252 100 218 {hasRedstone:2,cable:{id:"ae2:red_smart_dense_cable"}}
setblock 253 100 218 ae2:cable_bus replace
data merge block 253 100 218 {hasRedstone:2,cable:{id:"ae2:blue_smart_dense_cable"}}

# ae2-m2-01 terminal-six-faces-spins-zero-one
setblock 210 100 243 ae2:cable_bus replace
data merge block 210 100 243 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:terminal",spin:0b}}
setblock 214 100 243 ae2:cable_bus replace
data merge block 214 100 243 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:terminal",spin:1b}}
setblock 218 100 243 ae2:cable_bus replace
data merge block 218 100 243 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},north:{id:"ae2:terminal",spin:0b}}
setblock 222 100 243 ae2:cable_bus replace
data merge block 222 100 243 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:terminal",spin:1b}}
setblock 226 100 243 ae2:cable_bus replace
data merge block 226 100 243 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"ae2:terminal",spin:0b}}
setblock 230 100 243 ae2:cable_bus replace
data merge block 230 100 243 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},east:{id:"ae2:terminal",spin:1b}}

# ae2-m2-02 multiple-mixed-spin-terminals
setblock 234 100 243 ae2:cable_bus replace
data merge block 234 100 243 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"ae2:terminal",spin:0b},south:{id:"ae2:terminal",spin:1b},east:{id:"ae2:terminal",spin:0b}}

# ae2-m2-03 terminal-disables-straight-simplification
setblock 210 100 246 ae2:cable_bus replace
data merge block 210 100 246 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 211 100 246 ae2:cable_bus replace
data merge block 211 100 246 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:terminal",spin:1b}}
setblock 212 100 246 ae2:cable_bus replace
data merge block 212 100 246 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}

# ae2-m2-04 plain-stone-facade-south
setblock 216 100 246 ae2:cable_bus replace
data merge block 216 100 246 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:terminal",spin:0b},facadeSouth:{Name:"minecraft:stone"}}

# ae2-m2-05 plain-stone-facade-up
setblock 220 100 246 ae2:cable_bus replace
data merge block 220 100 246 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:terminal",spin:1b},facadeUp:{Name:"minecraft:stone"}}

# ae2-m2-06 cable-anchor-face-part-fallback
setblock 210 100 248 ae2:cable_bus replace
data merge block 210 100 248 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:cable_anchor"}}

# ae2-m2-07 unsupported-monitor-part-fallback
setblock 213 100 248 ae2:cable_bus replace
data merge block 213 100 248 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:monitor",spin:0b}}

# ae2-m2-08 terminal-out-of-range-spin-fallback
setblock 216 100 248 ae2:cable_bus replace
data merge block 216 100 248 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:terminal",spin:4b}}

# ae2-m2-09 standalone-terminal-missing-center-fallback
setblock 219 100 248 ae2:cable_bus replace
data merge block 219 100 248 {hasRedstone:2,north:{id:"ae2:terminal",spin:0b}}

# ae2-m2-10 facade-only-fallback
setblock 222 100 248 ae2:cable_bus replace
data merge block 222 100 248 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},facadeSouth:{Name:"minecraft:stone"}}

# ae2-m2-11 glass-facade-fallback
setblock 225 100 248 ae2:cable_bus replace
data merge block 225 100 248 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:terminal",spin:0b},facadeSouth:{Name:"minecraft:glass"}}

# ae2-m2-12 property-bearing-facade-fallback
setblock 228 100 248 ae2:cable_bus replace
data merge block 228 100 248 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:terminal",spin:0b},facadeSouth:{Name:"minecraft:oak_log",Properties:{axis:"y"}}}

# ae2-m2-13 multiple-facades-fallback
setblock 231 100 248 ae2:cable_bus replace
data merge block 231 100 248 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:terminal",spin:0b},south:{id:"ae2:terminal",spin:1b},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"}}

# ae2-m2-14 facade-with-extra-part-fallback
setblock 234 100 248 ae2:cable_bus replace
data merge block 234 100 248 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:terminal",spin:1b},south:{id:"ae2:terminal",spin:0b},facadeSouth:{Name:"minecraft:stone"}}

# ae2-m3-01 drive-facing-down-spins-zero-through-three
setblock 242 100 242 ae2:drive[facing=down,spin=0] replace
data merge block 242 100 242 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 248 100 242 ae2:drive[facing=down,spin=1] replace
data merge block 248 100 242 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 254 100 242 ae2:drive[facing=down,spin=2] replace
data merge block 254 100 242 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 260 100 242 ae2:drive[facing=down,spin=3] replace
data merge block 260 100 242 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}

# ae2-m3-02 drive-facing-up-spins-zero-through-three
setblock 242 100 243 ae2:drive[facing=up,spin=0] replace
data merge block 242 100 243 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 248 100 243 ae2:drive[facing=up,spin=1] replace
data merge block 248 100 243 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 254 100 243 ae2:drive[facing=up,spin=2] replace
data merge block 254 100 243 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 260 100 243 ae2:drive[facing=up,spin=3] replace
data merge block 260 100 243 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}

# ae2-m3-03 drive-facing-north-spins-zero-through-three
setblock 242 100 244 ae2:drive[facing=north,spin=0] replace
data merge block 242 100 244 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 248 100 244 ae2:drive[facing=north,spin=1] replace
data merge block 248 100 244 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 254 100 244 ae2:drive[facing=north,spin=2] replace
data merge block 254 100 244 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 260 100 244 ae2:drive[facing=north,spin=3] replace
data merge block 260 100 244 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}

# ae2-m3-04 drive-facing-south-spins-zero-through-three
setblock 242 100 245 ae2:drive[facing=south,spin=0] replace
data merge block 242 100 245 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 248 100 245 ae2:drive[facing=south,spin=1] replace
data merge block 248 100 245 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 254 100 245 ae2:drive[facing=south,spin=2] replace
data merge block 254 100 245 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 260 100 245 ae2:drive[facing=south,spin=3] replace
data merge block 260 100 245 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}

# ae2-m3-05 drive-facing-west-spins-zero-through-three
setblock 242 100 246 ae2:drive[facing=west,spin=0] replace
data merge block 242 100 246 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 248 100 246 ae2:drive[facing=west,spin=1] replace
data merge block 248 100 246 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 254 100 246 ae2:drive[facing=west,spin=2] replace
data merge block 254 100 246 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 260 100 246 ae2:drive[facing=west,spin=3] replace
data merge block 260 100 246 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}

# ae2-m3-06 drive-facing-east-spins-zero-through-three
setblock 242 100 247 ae2:drive[facing=east,spin=0] replace
data merge block 242 100 247 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 248 100 247 ae2:drive[facing=east,spin=1] replace
data merge block 248 100 247 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 254 100 247 ae2:drive[facing=east,spin=2] replace
data merge block 254 100 247 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 260 100 247 ae2:drive[facing=east,spin=3] replace
data merge block 260 100 247 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}

# ae2-m3-07 drive-empty
setblock 241 100 248 ae2:drive[facing=south,spin=0] replace
data merge block 241 100 248 {inv:{item0:{},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}

# ae2-m3-08 drive-primary-cell-catalog
setblock 244 100 248 ae2:drive[facing=south,spin=0] replace
data merge block 244 100 248 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{id:"ae2:item_storage_cell_4k",count:1},item2:{id:"ae2:item_storage_cell_16k",count:1},item3:{id:"ae2:item_storage_cell_64k",count:1},item4:{id:"ae2:item_storage_cell_256k",count:1},item5:{id:"ae2:fluid_storage_cell_1k",count:1},item6:{id:"ae2:fluid_storage_cell_4k",count:1},item7:{id:"ae2:fluid_storage_cell_16k",count:1},item8:{id:"ae2:fluid_storage_cell_64k",count:1},item9:{id:"ae2:fluid_storage_cell_256k",count:1}}}

# ae2-m3-09 drive-portable-cell-catalog
setblock 247 100 248 ae2:drive[facing=south,spin=0] replace
data merge block 247 100 248 {inv:{item0:{id:"ae2:portable_item_cell_1k",count:1},item1:{id:"ae2:portable_item_cell_4k",count:1},item2:{id:"ae2:portable_item_cell_16k",count:1},item3:{id:"ae2:portable_item_cell_64k",count:1},item4:{id:"ae2:portable_item_cell_256k",count:1},item5:{id:"ae2:portable_fluid_cell_1k",count:1},item6:{id:"ae2:portable_fluid_cell_4k",count:1},item7:{id:"ae2:portable_fluid_cell_16k",count:1},item8:{id:"ae2:portable_fluid_cell_64k",count:1},item9:{id:"ae2:portable_fluid_cell_256k",count:1}}}

# ae2-m3-10 drive-special-cell-catalog
setblock 250 100 248 ae2:drive[facing=south,spin=0] replace
data merge block 250 100 248 {inv:{item0:{id:"ae2:creative_storage_cell",count:1},item1:{id:"ae2:matter_cannon",count:1},item2:{id:"ae2:color_applicator",count:1},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}

# ae2-m3-11 drive-sparse-first-last-slots
setblock 253 100 248 ae2:drive[facing=south,spin=0] replace
data merge block 253 100 248 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{id:"ae2:fluid_storage_cell_256k",count:1}}}

# ae2-m3-12 drive-full-mixed-slots
setblock 256 100 248 ae2:drive[facing=south,spin=0] replace
data merge block 256 100 248 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{id:"ae2:fluid_storage_cell_1k",count:1},item2:{id:"ae2:item_storage_cell_4k",count:1},item3:{id:"ae2:fluid_storage_cell_4k",count:1},item4:{id:"ae2:item_storage_cell_16k",count:1},item5:{id:"ae2:fluid_storage_cell_16k",count:1},item6:{id:"ae2:creative_storage_cell",count:1},item7:{id:"ae2:matter_cannon",count:1},item8:{id:"ae2:color_applicator",count:1},item9:{id:"ae2:portable_item_cell_256k",count:1}}}

# ae2-m3-13 drive-components-insensitive
setblock 259 100 248 ae2:drive[facing=south,spin=0] replace
data merge block 259 100 248 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 262 100 248 ae2:drive[facing=south,spin=0] replace
data merge block 262 100 248 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1,components:{"ae2:storage_cell_inv":[{"#t":"ae2:i","#":64L,id:"minecraft:stone"}]}},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}

# ae2-m3-14 drive-unknown-extension-cell-fallback
setblock 241 100 249 ae2:drive[facing=south,spin=0] replace
data merge block 241 100 249 {inv:{item0:{id:"megacells:item_storage_cell_1m",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}

# ae2-m3b-01 extended-drive-facing-down-spins-zero-through-three
setblock 242 100 260 extendedae:ex_drive[facing=down,spin=0] replace
data merge block 242 100 260 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 248 100 260 extendedae:ex_drive[facing=down,spin=1] replace
data merge block 248 100 260 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 254 100 260 extendedae:ex_drive[facing=down,spin=2] replace
data merge block 254 100 260 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 260 100 260 extendedae:ex_drive[facing=down,spin=3] replace
data merge block 260 100 260 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-02 extended-drive-facing-up-spins-zero-through-three
setblock 242 100 261 extendedae:ex_drive[facing=up,spin=0] replace
data merge block 242 100 261 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 248 100 261 extendedae:ex_drive[facing=up,spin=1] replace
data merge block 248 100 261 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 254 100 261 extendedae:ex_drive[facing=up,spin=2] replace
data merge block 254 100 261 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 260 100 261 extendedae:ex_drive[facing=up,spin=3] replace
data merge block 260 100 261 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-03 extended-drive-facing-north-spins-zero-through-three
setblock 242 100 262 extendedae:ex_drive[facing=north,spin=0] replace
data merge block 242 100 262 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 248 100 262 extendedae:ex_drive[facing=north,spin=1] replace
data merge block 248 100 262 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 254 100 262 extendedae:ex_drive[facing=north,spin=2] replace
data merge block 254 100 262 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 260 100 262 extendedae:ex_drive[facing=north,spin=3] replace
data merge block 260 100 262 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-04 extended-drive-facing-south-spins-zero-through-three
setblock 242 100 263 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 242 100 263 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 248 100 263 extendedae:ex_drive[facing=south,spin=1] replace
data merge block 248 100 263 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 254 100 263 extendedae:ex_drive[facing=south,spin=2] replace
data merge block 254 100 263 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 260 100 263 extendedae:ex_drive[facing=south,spin=3] replace
data merge block 260 100 263 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-05 extended-drive-facing-west-spins-zero-through-three
setblock 242 100 264 extendedae:ex_drive[facing=west,spin=0] replace
data merge block 242 100 264 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 248 100 264 extendedae:ex_drive[facing=west,spin=1] replace
data merge block 248 100 264 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 254 100 264 extendedae:ex_drive[facing=west,spin=2] replace
data merge block 254 100 264 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 260 100 264 extendedae:ex_drive[facing=west,spin=3] replace
data merge block 260 100 264 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-06 extended-drive-facing-east-spins-zero-through-three
setblock 242 100 265 extendedae:ex_drive[facing=east,spin=0] replace
data merge block 242 100 265 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 248 100 265 extendedae:ex_drive[facing=east,spin=1] replace
data merge block 248 100 265 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 254 100 265 extendedae:ex_drive[facing=east,spin=2] replace
data merge block 254 100 265 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 260 100 265 extendedae:ex_drive[facing=east,spin=3] replace
data merge block 260 100 265 {inv:{item0:{id:"ae2:portable_fluid_cell_16k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-07 extended-drive-empty
setblock 242 100 266 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 242 100 266 {inv:{item0:{},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-08 extended-drive-native-cells-front-and-back
setblock 245 100 266 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 245 100 266 {inv:{item0:{id:"extendedae:infinity_water_cell",count:1},item1:{id:"extendedae:infinity_cobblestone_cell",count:1},item2:{id:"extendedae:void_cell",count:1},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:infinity_water_cell",count:1},item11:{id:"extendedae:infinity_cobblestone_cell",count:1},item12:{id:"extendedae:void_cell",count:1},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-09 extended-drive-sparse-front-back-edge-slots
setblock 248 100 266 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 248 100 266 {inv:{item0:{id:"ae2:portable_fluid_cell_64k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{id:"ae2:portable_fluid_cell_256k",count:1},item10:{id:"extendedae:infinity_cobblestone_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{id:"extendedae:void_cell",count:1}}}

# ae2-m3b-10 extended-drive-full-twenty-slots
setblock 251 100 266 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 251 100 266 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:1},item1:{id:"ae2:item_storage_cell_4k",count:1},item2:{id:"ae2:item_storage_cell_16k",count:1},item3:{id:"ae2:item_storage_cell_64k",count:1},item4:{id:"ae2:item_storage_cell_256k",count:1},item5:{id:"ae2:fluid_storage_cell_1k",count:1},item6:{id:"ae2:fluid_storage_cell_4k",count:1},item7:{id:"ae2:fluid_storage_cell_16k",count:1},item8:{id:"ae2:fluid_storage_cell_64k",count:1},item9:{id:"ae2:fluid_storage_cell_256k",count:1},item10:{id:"ae2:portable_item_cell_1k",count:1},item11:{id:"ae2:portable_item_cell_4k",count:1},item12:{id:"ae2:portable_item_cell_16k",count:1},item13:{id:"ae2:portable_item_cell_64k",count:1},item14:{id:"ae2:portable_item_cell_256k",count:1},item15:{id:"ae2:portable_fluid_cell_1k",count:1},item16:{id:"ae2:portable_fluid_cell_4k",count:1},item17:{id:"ae2:creative_storage_cell",count:1},item18:{id:"ae2:matter_cannon",count:1},item19:{id:"ae2:color_applicator",count:1}}}

# ae2-m3b-11 extended-drive-front-back-mirror
setblock 254 100 266 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 254 100 266 {inv:{item0:{id:"extendedae:void_cell",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 257 100 266 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 257 100 266 {inv:{item0:{},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"extendedae:void_cell",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-12 extended-drive-components-insensitive
setblock 260 100 266 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 260 100 266 {inv:{item0:{id:"extendedae:infinity_water_cell",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"ae2:item_storage_cell_1k",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}
setblock 263 100 266 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 263 100 266 {inv:{item0:{id:"extendedae:infinity_water_cell",count:1,components:{"ae2:storage_cell_inv":[{"#t":"ae2:i","#":64L,id:"minecraft:stone"}]}},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{id:"ae2:item_storage_cell_1k",count:1},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-13 extended-drive-megacells-fallback
setblock 266 100 266 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 266 100 266 {inv:{item0:{id:"megacells:item_storage_cell_1m",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-14 extended-drive-kubejs-cell-fallback
setblock 269 100 266 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 269 100 266 {inv:{item0:{id:"kubejs:lava_cell",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-15 extended-drive-count-two-fallback
setblock 272 100 266 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 272 100 266 {inv:{item0:{id:"ae2:item_storage_cell_1k",count:2},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3b-16 extended-drive-non-cell-item-fallback
setblock 275 100 266 extendedae:ex_drive[facing=south,spin=0] replace
data merge block 275 100 266 {inv:{item0:{id:"minecraft:stone",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{},item10:{},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m3c-01 isolated-ordinary-vibrant-matched-selection
setblock 208 100 288 ae2:quartz_glass replace
setblock 244 100 288 ae2:quartz_vibrant_glass replace

# ae2-m3c-02 center-down-up
setblock 214 100 290 ae2:quartz_glass replace
setblock 214 99 290 ae2:quartz_vibrant_glass replace
setblock 214 101 290 ae2:quartz_glass replace

# ae2-m3c-03 center-north-up
setblock 222 100 290 ae2:quartz_glass replace
setblock 222 100 289 ae2:quartz_vibrant_glass replace
setblock 222 101 290 ae2:quartz_glass replace

# ae2-m3c-04 center-north-west
setblock 230 100 290 ae2:quartz_glass replace
setblock 230 100 289 ae2:quartz_vibrant_glass replace
setblock 229 100 290 ae2:quartz_glass replace

# ae2-m3c-05 center-north-south-west
setblock 238 100 290 ae2:quartz_glass replace
setblock 238 100 289 ae2:quartz_vibrant_glass replace
setblock 238 100 291 ae2:quartz_glass replace
setblock 237 100 290 ae2:quartz_vibrant_glass replace

# ae2-m3c-06 center-east-north-up-west
setblock 250 100 290 ae2:quartz_glass replace
setblock 251 100 290 ae2:quartz_vibrant_glass replace
setblock 250 100 289 ae2:quartz_glass replace
setblock 250 101 290 ae2:quartz_vibrant_glass replace
setblock 249 100 290 ae2:quartz_glass replace

# ae2-m3c-07 diagonal-only-ordinary-vibrant
setblock 258 100 290 ae2:quartz_glass replace
setblock 259 101 290 ae2:quartz_vibrant_glass replace

# ae2-m3c-08 three-by-three-checkerboard-plane
setblock 263 100 289 ae2:quartz_glass replace
setblock 264 100 289 ae2:quartz_vibrant_glass replace
setblock 265 100 289 ae2:quartz_glass replace
setblock 263 100 290 ae2:quartz_vibrant_glass replace
setblock 264 100 290 ae2:quartz_glass replace
setblock 265 100 290 ae2:quartz_vibrant_glass replace
setblock 263 100 291 ae2:quartz_glass replace
setblock 264 100 291 ae2:quartz_vibrant_glass replace
setblock 265 100 291 ae2:quartz_glass replace

# ae2-m3c-09 two-by-two-by-two-checkerboard-cube
setblock 272 100 289 ae2:quartz_glass replace
setblock 273 100 289 ae2:quartz_vibrant_glass replace
setblock 272 100 290 ae2:quartz_vibrant_glass replace
setblock 273 100 290 ae2:quartz_glass replace
setblock 272 101 289 ae2:quartz_vibrant_glass replace
setblock 273 101 289 ae2:quartz_glass replace
setblock 272 101 290 ae2:quartz_glass replace
setblock 273 101 290 ae2:quartz_vibrant_glass replace

# ae2-m3c-10 mixed-six-neighbor-enclosed-plus
setblock 215 101 301 ae2:quartz_glass replace
setblock 215 100 301 ae2:quartz_vibrant_glass replace
setblock 215 102 301 ae2:quartz_glass replace
setblock 215 101 300 ae2:quartz_vibrant_glass replace
setblock 215 101 302 ae2:quartz_glass replace
setblock 214 101 301 ae2:quartz_vibrant_glass replace
setblock 216 101 301 ae2:quartz_glass replace

# ae2-m3c-11 opaque-neighbor-culling
setblock 226 100 301 ae2:quartz_glass replace
setblock 227 100 301 minecraft:stone replace

# ae2-m3d-01 isolated-storage-catalog
setblock 297 100 261 ae2:1k_crafting_storage replace
setblock 301 100 261 ae2:4k_crafting_storage replace
setblock 305 100 261 ae2:16k_crafting_storage replace
setblock 309 100 261 ae2:64k_crafting_storage replace
setblock 313 100 261 ae2:256k_crafting_storage replace

# ae2-m3d-02 unit-plus-1k-storage
setblock 297 100 265 ae2:crafting_unit replace
setblock 298 100 265 ae2:1k_crafting_storage replace

# ae2-m3d-03 accelerator-plus-1k-storage
setblock 302 100 265 ae2:crafting_accelerator replace
setblock 303 100 265 ae2:1k_crafting_storage replace

# ae2-m3d-04 unit-storage-accelerator-line
setblock 307 100 265 ae2:crafting_unit replace
setblock 308 100 265 ae2:1k_crafting_storage replace
setblock 309 100 265 ae2:crafting_accelerator replace

# ae2-m3d-05 two-by-two-plane
setblock 312 100 264 ae2:crafting_unit replace
setblock 313 100 264 ae2:crafting_accelerator replace
setblock 312 100 265 ae2:1k_crafting_storage replace
setblock 313 100 265 ae2:4k_crafting_storage replace

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

# ae2-m3d-07 unpowered-three-by-three-by-three-hard-culling
setblock 304 100 269 ae2:crafting_unit replace
setblock 305 100 269 ae2:crafting_unit replace
setblock 306 100 269 ae2:crafting_unit replace
setblock 304 100 270 ae2:crafting_unit replace
setblock 305 100 270 ae2:crafting_unit replace
setblock 306 100 270 ae2:crafting_unit replace
setblock 304 100 271 ae2:crafting_unit replace
setblock 305 100 271 ae2:crafting_unit replace
setblock 306 100 271 ae2:crafting_unit replace
setblock 304 101 269 ae2:crafting_unit replace
setblock 305 101 269 ae2:crafting_unit replace
setblock 306 101 269 ae2:crafting_unit replace
setblock 304 101 270 ae2:crafting_unit replace
setblock 305 101 270 ae2:crafting_unit replace
setblock 306 101 270 ae2:crafting_unit replace
setblock 304 101 271 ae2:crafting_unit replace
setblock 305 101 271 ae2:crafting_unit replace
setblock 306 101 271 ae2:crafting_unit replace
setblock 304 102 269 ae2:crafting_unit replace
setblock 305 102 269 ae2:1k_crafting_storage replace
setblock 306 102 269 ae2:crafting_unit replace
setblock 304 102 270 ae2:crafting_unit replace
setblock 305 102 270 ae2:crafting_unit replace
setblock 306 102 270 ae2:crafting_unit replace
setblock 304 102 271 ae2:crafting_unit replace
setblock 305 102 271 ae2:crafting_unit replace
setblock 306 102 271 ae2:crafting_unit replace

# ae2-m3d-08 monitor-paint-orientation-catalog
setblock 298 100 276 ae2:crafting_monitor[facing=south,spin=0] replace
data merge block 298 100 276 {paintedColor:0b}
setblock 298 100 275 ae2:1k_crafting_storage replace
setblock 303 100 276 ae2:crafting_monitor[facing=north,spin=1] replace
data merge block 303 100 276 {paintedColor:1b}
setblock 303 100 277 ae2:1k_crafting_storage replace
setblock 308 100 276 ae2:crafting_monitor[facing=east,spin=2] replace
data merge block 308 100 276 {paintedColor:2b}
setblock 307 100 276 ae2:1k_crafting_storage replace
setblock 313 100 276 ae2:crafting_monitor[facing=west,spin=3] replace
data merge block 313 100 276 {paintedColor:3b}
setblock 314 100 276 ae2:1k_crafting_storage replace
setblock 298 100 281 ae2:crafting_monitor[facing=up,spin=0] replace
data merge block 298 100 281 {paintedColor:4b}
setblock 298 99 281 ae2:1k_crafting_storage replace
setblock 303 100 281 ae2:crafting_monitor[facing=down,spin=1] replace
data merge block 303 100 281 {paintedColor:5b}
setblock 303 101 281 ae2:1k_crafting_storage replace
setblock 308 100 281 ae2:crafting_monitor[facing=south,spin=2] replace
data merge block 308 100 281 {paintedColor:6b}
setblock 308 100 280 ae2:1k_crafting_storage replace
setblock 313 100 281 ae2:crafting_monitor[facing=north,spin=3] replace
data merge block 313 100 281 {paintedColor:7b}
setblock 313 100 282 ae2:1k_crafting_storage replace
setblock 298 100 286 ae2:crafting_monitor[facing=east,spin=0] replace
data merge block 298 100 286 {paintedColor:8b}
setblock 297 100 286 ae2:1k_crafting_storage replace
setblock 303 100 286 ae2:crafting_monitor[facing=west,spin=1] replace
data merge block 303 100 286 {paintedColor:9b}
setblock 304 100 286 ae2:1k_crafting_storage replace
setblock 308 100 286 ae2:crafting_monitor[facing=up,spin=2] replace
data merge block 308 100 286 {paintedColor:10b}
setblock 308 99 286 ae2:1k_crafting_storage replace
setblock 313 100 286 ae2:crafting_monitor[facing=down,spin=3] replace
data merge block 313 100 286 {paintedColor:11b}
setblock 313 101 286 ae2:1k_crafting_storage replace
setblock 298 100 291 ae2:crafting_monitor[facing=south,spin=0] replace
data merge block 298 100 291 {paintedColor:12b}
setblock 298 100 290 ae2:1k_crafting_storage replace
setblock 303 100 291 ae2:crafting_monitor[facing=north,spin=1] replace
data merge block 303 100 291 {paintedColor:13b}
setblock 303 100 292 ae2:1k_crafting_storage replace
setblock 308 100 291 ae2:crafting_monitor[facing=east,spin=2] replace
data merge block 308 100 291 {paintedColor:14b}
setblock 307 100 291 ae2:1k_crafting_storage replace
setblock 313 100 291 ae2:crafting_monitor[facing=west,spin=3] replace
data merge block 313 100 291 {paintedColor:15b}
setblock 314 100 291 ae2:1k_crafting_storage replace
setblock 298 100 296 ae2:crafting_monitor[facing=up,spin=0] replace
data merge block 298 100 296 {paintedColor:16b}
setblock 298 99 296 ae2:1k_crafting_storage replace

# ae2-m3d-09 compatible-extension-atomic-fallback
setblock 318 100 261 ae2:1k_crafting_storage replace
setblock 317 100 261 megacells:mega_crafting_unit replace
setblock 319 100 261 expandedae:exp_crafting_unit replace

# ae2-m3e-01 formed-unpowered-xz-chunk-boundary
setblock 286 100 270 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 287 100 270 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 288 100 270 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 286 100 271 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 287 100 271 ae2:quantum_link[formed=false,waterlogged=false] replace
setblock 288 100 271 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 286 100 272 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 287 100 272 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 288 100 272 ae2:quantum_ring[formed=false,waterlogged=false] replace

# ae2-m3e-02 formed-unpowered-xy
setblock 282 100 276 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 283 100 276 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 284 100 276 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 282 101 276 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 283 101 276 ae2:quantum_link[formed=false,waterlogged=false] replace
setblock 284 101 276 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 282 102 276 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 283 102 276 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 284 102 276 ae2:quantum_ring[formed=false,waterlogged=false] replace

# ae2-m3e-03 formed-unpowered-yz
setblock 290 100 270 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 290 101 270 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 290 102 270 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 290 100 271 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 290 101 271 ae2:quantum_link[formed=false,waterlogged=false] replace
setblock 290 102 271 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 290 100 272 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 290 101 272 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 290 102 272 ae2:quantum_ring[formed=false,waterlogged=false] replace

# ae2-m3f-01 non-lumen-palette-faces-and-layering
setblock 282 99 209 minecraft:smooth_stone replace
setblock 286 99 209 minecraft:smooth_stone replace
setblock 290 99 209 minecraft:smooth_stone replace
setblock 294 99 209 minecraft:smooth_stone replace
setblock 298 99 209 minecraft:smooth_stone replace
setblock 302 99 209 minecraft:smooth_stone replace
setblock 306 99 209 minecraft:smooth_stone replace
setblock 310 99 209 minecraft:smooth_stone replace
setblock 282 99 213 minecraft:smooth_stone replace
setblock 286 99 213 minecraft:smooth_stone replace
setblock 290 99 213 minecraft:smooth_stone replace
setblock 294 99 213 minecraft:smooth_stone replace
setblock 298 99 213 minecraft:smooth_stone replace
setblock 302 99 213 minecraft:smooth_stone replace
setblock 306 99 213 minecraft:smooth_stone replace
setblock 310 99 213 minecraft:smooth_stone replace
setblock 282 99 217 minecraft:smooth_stone replace
setblock 288 101 217 minecraft:smooth_stone replace
setblock 294 100 216 minecraft:smooth_stone replace
setblock 300 100 218 minecraft:smooth_stone replace
setblock 305 100 217 minecraft:smooth_stone replace
setblock 313 100 217 minecraft:smooth_stone replace
setblock 318 99 217 minecraft:smooth_stone replace
setblock 282 100 209 ae2:paint[facing=up,light_level=0] replace
data merge block 282 100 209 {dots:[B;1b,-120b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 286 100 209 ae2:paint[facing=up,light_level=0] replace
data merge block 286 100 209 {dots:[B;1b,-120b,8b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 290 100 209 ae2:paint[facing=up,light_level=0] replace
data merge block 290 100 209 {dots:[B;1b,-120b,16b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 294 100 209 ae2:paint[facing=up,light_level=0] replace
data merge block 294 100 209 {dots:[B;1b,-120b,24b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 298 100 209 ae2:paint[facing=up,light_level=0] replace
data merge block 298 100 209 {dots:[B;1b,-120b,32b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 302 100 209 ae2:paint[facing=up,light_level=0] replace
data merge block 302 100 209 {dots:[B;1b,-120b,40b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 306 100 209 ae2:paint[facing=up,light_level=0] replace
data merge block 306 100 209 {dots:[B;1b,-120b,48b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 310 100 209 ae2:paint[facing=up,light_level=0] replace
data merge block 310 100 209 {dots:[B;1b,-120b,56b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 282 100 213 ae2:paint[facing=up,light_level=0] replace
data merge block 282 100 213 {dots:[B;1b,-120b,64b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 286 100 213 ae2:paint[facing=up,light_level=0] replace
data merge block 286 100 213 {dots:[B;1b,-120b,72b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 290 100 213 ae2:paint[facing=up,light_level=0] replace
data merge block 290 100 213 {dots:[B;1b,-120b,80b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 294 100 213 ae2:paint[facing=up,light_level=0] replace
data merge block 294 100 213 {dots:[B;1b,-120b,88b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 298 100 213 ae2:paint[facing=up,light_level=0] replace
data merge block 298 100 213 {dots:[B;1b,-120b,96b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 302 100 213 ae2:paint[facing=up,light_level=0] replace
data merge block 302 100 213 {dots:[B;1b,-120b,104b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 306 100 213 ae2:paint[facing=up,light_level=0] replace
data merge block 306 100 213 {dots:[B;1b,-120b,112b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 310 100 213 ae2:paint[facing=up,light_level=0] replace
data merge block 310 100 213 {dots:[B;1b,-120b,120b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 282 100 217 ae2:paint[facing=up,light_level=0] replace
data merge block 282 100 217 {dots:[B;1b,-87b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 288 100 217 ae2:paint[facing=down,light_level=0] replace
data merge block 288 100 217 {dots:[B;1b,-86b,9b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 294 100 217 ae2:paint[facing=south,light_level=0] replace
data merge block 294 100 217 {dots:[B;1b,-85b,18b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 300 100 217 ae2:paint[facing=north,light_level=0] replace
data merge block 300 100 217 {dots:[B;1b,-84b,27b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 306 100 217 ae2:paint[facing=east,light_level=0] replace
data merge block 306 100 217 {dots:[B;1b,-83b,36b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 312 100 217 ae2:paint[facing=west,light_level=0] replace
data merge block 312 100 217 {dots:[B;1b,-82b,45b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}
setblock 318 100 217 ae2:paint[facing=up,light_level=0] replace
data merge block 318 100 217 {dots:[B;3b,-120b,32b,-103b,80b,-86b,120b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b,0b]}

# ae2-m3f-02 closed-sky-stone-chest-facings
setblock 282 100 222 ae2:sky_stone_chest[facing=south,waterlogged=false] replace
setblock 288 100 222 ae2:sky_stone_chest[facing=west,waterlogged=false] replace
setblock 294 100 222 ae2:sky_stone_chest[facing=north,waterlogged=false] replace
setblock 300 100 222 ae2:sky_stone_chest[facing=east,waterlogged=false] replace
setblock 282 100 226 ae2:smooth_sky_stone_chest[facing=south,waterlogged=false] replace
setblock 288 100 226 ae2:smooth_sky_stone_chest[facing=west,waterlogged=false] replace
setblock 294 100 226 ae2:smooth_sky_stone_chest[facing=north,waterlogged=false] replace
setblock 300 100 226 ae2:smooth_sky_stone_chest[facing=east,waterlogged=false] replace

# ae2-m3f-03 neutral-crank-six-facings
setblock 306 101 222 ae2:charger[facing=north,spin=0] replace
setblock 312 99 222 ae2:charger[facing=north,spin=0] replace
setblock 318 100 223 ae2:charger[facing=east,spin=0] replace
setblock 306 100 225 ae2:charger[facing=east,spin=0] replace
setblock 313 100 226 ae2:charger[facing=north,spin=0] replace
setblock 317 100 226 ae2:charger[facing=north,spin=0] replace
setblock 306 100 222 ae2:crank[facing=down] replace
setblock 312 100 222 ae2:crank[facing=up] replace
setblock 318 100 222 ae2:crank[facing=north] replace
setblock 306 100 226 ae2:crank[facing=south] replace
setblock 312 100 226 ae2:crank[facing=west] replace
setblock 318 100 226 ae2:crank[facing=east] replace

# ae2-m3f-04 neutral-inscriber-all-facing-spin-states
setblock 282 98 229 ae2:inscriber[facing=down,spin=0,waterlogged=false] replace
setblock 287 98 229 ae2:inscriber[facing=down,spin=1,waterlogged=false] replace
setblock 292 98 229 ae2:inscriber[facing=down,spin=2,waterlogged=false] replace
setblock 297 98 229 ae2:inscriber[facing=down,spin=3,waterlogged=false] replace
setblock 302 98 229 ae2:inscriber[facing=up,spin=0,waterlogged=false] replace
setblock 307 98 229 ae2:inscriber[facing=up,spin=1,waterlogged=false] replace
setblock 312 98 229 ae2:inscriber[facing=up,spin=2,waterlogged=false] replace
setblock 317 98 229 ae2:inscriber[facing=up,spin=3,waterlogged=false] replace
setblock 282 102 229 ae2:inscriber[facing=north,spin=0,waterlogged=false] replace
setblock 287 102 229 ae2:inscriber[facing=north,spin=1,waterlogged=false] replace
setblock 292 102 229 ae2:inscriber[facing=north,spin=2,waterlogged=false] replace
setblock 297 102 229 ae2:inscriber[facing=north,spin=3,waterlogged=false] replace
setblock 302 102 229 ae2:inscriber[facing=south,spin=0,waterlogged=false] replace
setblock 307 102 229 ae2:inscriber[facing=south,spin=1,waterlogged=false] replace
setblock 312 102 229 ae2:inscriber[facing=south,spin=2,waterlogged=false] replace
setblock 317 102 229 ae2:inscriber[facing=south,spin=3,waterlogged=false] replace
setblock 282 106 229 ae2:inscriber[facing=west,spin=0,waterlogged=false] replace
setblock 287 106 229 ae2:inscriber[facing=west,spin=1,waterlogged=false] replace
setblock 292 106 229 ae2:inscriber[facing=west,spin=2,waterlogged=false] replace
setblock 297 106 229 ae2:inscriber[facing=west,spin=3,waterlogged=false] replace
setblock 302 106 229 ae2:inscriber[facing=east,spin=0,waterlogged=false] replace
setblock 307 106 229 ae2:inscriber[facing=east,spin=1,waterlogged=false] replace
setblock 312 106 229 ae2:inscriber[facing=east,spin=2,waterlogged=false] replace
setblock 317 106 229 ae2:inscriber[facing=east,spin=3,waterlogged=false] replace

# ae2-m3f-05 spatial-pylon-isolated-and-three-axis-lines
setblock 282 104 208 ae2:spatial_pylon[powered_on=false] replace
setblock 286 104 208 ae2:spatial_pylon[powered_on=false] replace
setblock 287 104 208 ae2:spatial_pylon[powered_on=false] replace
setblock 288 104 208 ae2:spatial_pylon[powered_on=false] replace
setblock 294 102 208 ae2:spatial_pylon[powered_on=false] replace
setblock 294 103 208 ae2:spatial_pylon[powered_on=false] replace
setblock 294 104 208 ae2:spatial_pylon[powered_on=false] replace
setblock 300 104 208 ae2:spatial_pylon[powered_on=false] replace
setblock 300 104 209 ae2:spatial_pylon[powered_on=false] replace
setblock 300 104 210 ae2:spatial_pylon[powered_on=false] replace

# ae2-m3f-06 spatial-pylon-perpendicular-component-unformed
setblock 310 104 214 ae2:spatial_pylon[powered_on=false] replace
setblock 311 104 214 ae2:spatial_pylon[powered_on=false] replace
setblock 310 104 215 ae2:spatial_pylon[powered_on=false] replace

# ae2-m3f-07 spatial-pylon-branched-component-unformed
setblock 316 103 214 ae2:spatial_pylon[powered_on=false] replace
setblock 315 103 214 ae2:spatial_pylon[powered_on=false] replace
setblock 317 103 214 ae2:spatial_pylon[powered_on=false] replace
setblock 316 104 214 ae2:spatial_pylon[powered_on=false] replace

# ae2-s1-01 all-native-parts-installed-down
setblock 209 100 313 ae2:cable_bus replace
data merge block 209 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:quartz_fiber"},facadeDown:{Name:"minecraft:stone"}}
setblock 212 100 313 ae2:cable_bus replace
data merge block 212 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:toggle_bus"},facadeDown:{Name:"minecraft:stone"}}
setblock 215 100 313 ae2:cable_bus replace
data merge block 215 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:inverted_toggle_bus"}}
setblock 218 100 313 ae2:cable_bus replace
data merge block 218 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:cable_anchor"}}
setblock 221 100 313 ae2:cable_bus replace
data merge block 221 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:monitor",spin:0b}}
setblock 224 100 313 ae2:cable_bus replace
data merge block 224 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:semi_dark_monitor",spin:0b}}
setblock 227 100 313 ae2:cable_bus replace
data merge block 227 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:dark_monitor",spin:0b}}
setblock 230 100 313 ae2:cable_bus replace
data merge block 230 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:storage_bus"}}
setblock 233 100 313 ae2:cable_bus replace
data merge block 233 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:import_bus"},facadeDown:{Name:"minecraft:stone"}}
setblock 236 100 313 ae2:cable_bus replace
data merge block 236 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:export_bus"},facadeDown:{Name:"minecraft:stone"}}
setblock 239 100 313 ae2:cable_bus replace
data merge block 239 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:level_emitter"},facadeDown:{Name:"minecraft:stone"}}
setblock 242 100 313 ae2:cable_bus replace
data merge block 242 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:energy_level_emitter"}}
setblock 245 100 313 ae2:cable_bus replace
data merge block 245 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:annihilation_plane"}}
setblock 248 100 313 ae2:cable_bus replace
data merge block 248 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:formation_plane"}}
setblock 251 100 313 ae2:cable_bus replace
data merge block 251 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:pattern_encoding_terminal",spin:0b}}
setblock 254 100 313 ae2:cable_bus replace
data merge block 254 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:crafting_terminal",spin:0b}}
setblock 257 100 313 ae2:cable_bus replace
data merge block 257 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:terminal",spin:0b},facadeDown:{Name:"minecraft:stone"}}
setblock 260 100 313 ae2:cable_bus replace
data merge block 260 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:storage_monitor",spin:0b}}
setblock 263 100 313 ae2:cable_bus replace
data merge block 263 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:conversion_monitor",spin:0b}}
setblock 266 100 313 ae2:cable_bus replace
data merge block 266 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:cable_pattern_provider"}}
setblock 269 100 313 ae2:cable_bus replace
data merge block 269 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:cable_interface"}}
setblock 272 100 313 ae2:cable_bus replace
data merge block 272 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:pattern_access_terminal",spin:0b}}
setblock 275 100 313 ae2:cable_bus replace
data merge block 275 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:cable_energy_acceptor"}}
setblock 278 100 313 ae2:cable_bus replace
data merge block 278 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:me_p2p_tunnel",freq:0s}}
setblock 281 100 313 ae2:cable_bus replace
data merge block 281 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:redstone_p2p_tunnel",freq:0s}}
setblock 284 100 313 ae2:cable_bus replace
data merge block 284 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:item_p2p_tunnel",freq:0s}}
setblock 287 100 313 ae2:cable_bus replace
data merge block 287 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:fluid_p2p_tunnel",freq:0s}}
setblock 290 100 313 ae2:cable_bus replace
data merge block 290 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:fe_p2p_tunnel",freq:0s}}
setblock 293 100 313 ae2:cable_bus replace
data merge block 293 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},down:{id:"ae2:light_p2p_tunnel",freq:0s}}

# ae2-s1-02 all-native-parts-installed-up
setblock 296 100 313 ae2:cable_bus replace
data merge block 296 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:quartz_fiber"}}
setblock 299 100 313 ae2:cable_bus replace
data merge block 299 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:toggle_bus"}}
setblock 302 100 313 ae2:cable_bus replace
data merge block 302 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:inverted_toggle_bus"}}
setblock 305 100 313 ae2:cable_bus replace
data merge block 305 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:cable_anchor"}}
setblock 308 100 313 ae2:cable_bus replace
data merge block 308 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:monitor",spin:1b}}
setblock 311 100 313 ae2:cable_bus replace
data merge block 311 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:semi_dark_monitor",spin:1b}}
setblock 314 100 313 ae2:cable_bus replace
data merge block 314 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:dark_monitor",spin:1b}}
setblock 317 100 313 ae2:cable_bus replace
data merge block 317 100 313 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:storage_bus"}}
setblock 209 100 318 ae2:cable_bus replace
data merge block 209 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:import_bus"}}
setblock 212 100 318 ae2:cable_bus replace
data merge block 212 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:export_bus"}}
setblock 215 100 318 ae2:cable_bus replace
data merge block 215 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:level_emitter"}}
setblock 218 100 318 ae2:cable_bus replace
data merge block 218 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:energy_level_emitter"}}
setblock 221 100 318 ae2:cable_bus replace
data merge block 221 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 224 100 318 ae2:cable_bus replace
data merge block 224 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:formation_plane"}}
setblock 227 100 318 ae2:cable_bus replace
data merge block 227 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:pattern_encoding_terminal",spin:1b}}
setblock 230 100 318 ae2:cable_bus replace
data merge block 230 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:crafting_terminal",spin:1b}}
setblock 233 100 318 ae2:cable_bus replace
data merge block 233 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:terminal",spin:1b}}
setblock 236 100 318 ae2:cable_bus replace
data merge block 236 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:storage_monitor",spin:1b}}
setblock 239 100 318 ae2:cable_bus replace
data merge block 239 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:conversion_monitor",spin:1b}}
setblock 242 100 318 ae2:cable_bus replace
data merge block 242 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:cable_pattern_provider"}}
setblock 245 100 318 ae2:cable_bus replace
data merge block 245 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:cable_interface"}}
setblock 248 100 318 ae2:cable_bus replace
data merge block 248 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:pattern_access_terminal",spin:1b}}
setblock 251 100 318 ae2:cable_bus replace
data merge block 251 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:cable_energy_acceptor"}}
setblock 254 100 318 ae2:cable_bus replace
data merge block 254 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:me_p2p_tunnel",freq:0s}}
setblock 257 100 318 ae2:cable_bus replace
data merge block 257 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:redstone_p2p_tunnel",freq:0s}}
setblock 260 100 318 ae2:cable_bus replace
data merge block 260 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:item_p2p_tunnel",freq:0s}}
setblock 263 100 318 ae2:cable_bus replace
data merge block 263 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:fluid_p2p_tunnel",freq:0s}}
setblock 266 100 318 ae2:cable_bus replace
data merge block 266 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:fe_p2p_tunnel",freq:0s}}
setblock 269 100 318 ae2:cable_bus replace
data merge block 269 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:light_p2p_tunnel",freq:0s}}

# ae2-s1-03 all-native-parts-installed-north
setblock 272 100 318 ae2:cable_bus replace
data merge block 272 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:quartz_fiber"}}
setblock 275 100 318 ae2:cable_bus replace
data merge block 275 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:toggle_bus"}}
setblock 278 100 318 ae2:cable_bus replace
data merge block 278 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:inverted_toggle_bus"}}
setblock 281 100 318 ae2:cable_bus replace
data merge block 281 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:cable_anchor"}}
setblock 284 100 318 ae2:cable_bus replace
data merge block 284 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:monitor",spin:2b}}
setblock 287 100 318 ae2:cable_bus replace
data merge block 287 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:semi_dark_monitor",spin:2b}}
setblock 290 100 318 ae2:cable_bus replace
data merge block 290 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:dark_monitor",spin:2b}}
setblock 293 100 318 ae2:cable_bus replace
data merge block 293 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:storage_bus"}}
setblock 296 100 318 ae2:cable_bus replace
data merge block 296 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:import_bus"}}
setblock 299 100 318 ae2:cable_bus replace
data merge block 299 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:export_bus"}}
setblock 302 100 318 ae2:cable_bus replace
data merge block 302 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:level_emitter"}}
setblock 305 100 318 ae2:cable_bus replace
data merge block 305 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:energy_level_emitter"}}
setblock 308 100 318 ae2:cable_bus replace
data merge block 308 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:annihilation_plane"}}
setblock 311 100 318 ae2:cable_bus replace
data merge block 311 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:formation_plane"}}
setblock 314 100 318 ae2:cable_bus replace
data merge block 314 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:pattern_encoding_terminal",spin:2b}}
setblock 317 100 318 ae2:cable_bus replace
data merge block 317 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:crafting_terminal",spin:2b}}
setblock 209 100 323 ae2:cable_bus replace
data merge block 209 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:terminal",spin:2b}}
setblock 212 100 323 ae2:cable_bus replace
data merge block 212 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:storage_monitor",spin:2b}}
setblock 215 100 323 ae2:cable_bus replace
data merge block 215 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:conversion_monitor",spin:2b}}
setblock 218 100 323 ae2:cable_bus replace
data merge block 218 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:cable_pattern_provider"}}
setblock 221 100 323 ae2:cable_bus replace
data merge block 221 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:cable_interface"}}
setblock 224 100 323 ae2:cable_bus replace
data merge block 224 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:pattern_access_terminal",spin:2b}}
setblock 227 100 323 ae2:cable_bus replace
data merge block 227 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:cable_energy_acceptor"}}
setblock 230 100 323 ae2:cable_bus replace
data merge block 230 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:me_p2p_tunnel",freq:0s}}
setblock 233 100 323 ae2:cable_bus replace
data merge block 233 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:redstone_p2p_tunnel",freq:0s}}
setblock 236 100 323 ae2:cable_bus replace
data merge block 236 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:item_p2p_tunnel",freq:0s}}
setblock 239 100 323 ae2:cable_bus replace
data merge block 239 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:fluid_p2p_tunnel",freq:0s}}
setblock 242 100 323 ae2:cable_bus replace
data merge block 242 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:fe_p2p_tunnel",freq:0s}}
setblock 245 100 323 ae2:cable_bus replace
data merge block 245 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},north:{id:"ae2:light_p2p_tunnel",freq:0s}}

# ae2-s1-04 all-native-parts-installed-south
setblock 248 100 323 ae2:cable_bus replace
data merge block 248 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:quartz_fiber"}}
setblock 251 100 323 ae2:cable_bus replace
data merge block 251 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:toggle_bus"}}
setblock 254 100 323 ae2:cable_bus replace
data merge block 254 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:inverted_toggle_bus"}}
setblock 257 100 323 ae2:cable_bus replace
data merge block 257 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:cable_anchor"}}
setblock 260 100 323 ae2:cable_bus replace
data merge block 260 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:monitor",spin:3b}}
setblock 263 100 323 ae2:cable_bus replace
data merge block 263 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:semi_dark_monitor",spin:3b}}
setblock 266 100 323 ae2:cable_bus replace
data merge block 266 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:dark_monitor",spin:3b}}
setblock 269 100 323 ae2:cable_bus replace
data merge block 269 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:storage_bus"}}
setblock 272 100 323 ae2:cable_bus replace
data merge block 272 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:import_bus"}}
setblock 275 100 323 ae2:cable_bus replace
data merge block 275 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:export_bus"}}
setblock 278 100 323 ae2:cable_bus replace
data merge block 278 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:level_emitter"}}
setblock 281 100 323 ae2:cable_bus replace
data merge block 281 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:energy_level_emitter"}}
setblock 284 100 323 ae2:cable_bus replace
data merge block 284 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:annihilation_plane"}}
setblock 287 100 323 ae2:cable_bus replace
data merge block 287 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:formation_plane"}}
setblock 290 100 323 ae2:cable_bus replace
data merge block 290 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:pattern_encoding_terminal",spin:3b}}
setblock 293 100 323 ae2:cable_bus replace
data merge block 293 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:crafting_terminal",spin:3b}}
setblock 296 100 323 ae2:cable_bus replace
data merge block 296 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:terminal",spin:3b}}
setblock 299 100 323 ae2:cable_bus replace
data merge block 299 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:storage_monitor",spin:3b}}
setblock 302 100 323 ae2:cable_bus replace
data merge block 302 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:conversion_monitor",spin:3b}}
setblock 305 100 323 ae2:cable_bus replace
data merge block 305 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:cable_pattern_provider"}}
setblock 308 100 323 ae2:cable_bus replace
data merge block 308 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:cable_interface"}}
setblock 311 100 323 ae2:cable_bus replace
data merge block 311 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:pattern_access_terminal",spin:3b}}
setblock 314 100 323 ae2:cable_bus replace
data merge block 314 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:cable_energy_acceptor"}}
setblock 317 100 323 ae2:cable_bus replace
data merge block 317 100 323 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:me_p2p_tunnel",freq:0s}}
setblock 209 100 328 ae2:cable_bus replace
data merge block 209 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:redstone_p2p_tunnel",freq:0s}}
setblock 212 100 328 ae2:cable_bus replace
data merge block 212 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:item_p2p_tunnel",freq:0s}}
setblock 215 100 328 ae2:cable_bus replace
data merge block 215 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:fluid_p2p_tunnel",freq:0s}}
setblock 218 100 328 ae2:cable_bus replace
data merge block 218 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:fe_p2p_tunnel",freq:0s}}
setblock 221 100 328 ae2:cable_bus replace
data merge block 221 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},south:{id:"ae2:light_p2p_tunnel",freq:0s}}

# ae2-s1-05 all-native-parts-installed-west
setblock 224 100 328 ae2:cable_bus replace
data merge block 224 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:quartz_fiber"}}
setblock 227 100 328 ae2:cable_bus replace
data merge block 227 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:toggle_bus"}}
setblock 230 100 328 ae2:cable_bus replace
data merge block 230 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:inverted_toggle_bus"}}
setblock 233 100 328 ae2:cable_bus replace
data merge block 233 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:cable_anchor"}}
setblock 236 100 328 ae2:cable_bus replace
data merge block 236 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:monitor",spin:0b}}
setblock 239 100 328 ae2:cable_bus replace
data merge block 239 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:semi_dark_monitor",spin:0b}}
setblock 242 100 328 ae2:cable_bus replace
data merge block 242 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:dark_monitor",spin:0b}}
setblock 245 100 328 ae2:cable_bus replace
data merge block 245 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:storage_bus"}}
setblock 248 100 328 ae2:cable_bus replace
data merge block 248 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:import_bus"}}
setblock 251 100 328 ae2:cable_bus replace
data merge block 251 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:export_bus"}}
setblock 254 100 328 ae2:cable_bus replace
data merge block 254 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:level_emitter"}}
setblock 257 100 328 ae2:cable_bus replace
data merge block 257 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:energy_level_emitter"}}
setblock 260 100 328 ae2:cable_bus replace
data merge block 260 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:annihilation_plane"}}
setblock 263 100 328 ae2:cable_bus replace
data merge block 263 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:formation_plane"}}
setblock 266 100 328 ae2:cable_bus replace
data merge block 266 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:pattern_encoding_terminal",spin:0b}}
setblock 269 100 328 ae2:cable_bus replace
data merge block 269 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:crafting_terminal",spin:0b}}
setblock 272 100 328 ae2:cable_bus replace
data merge block 272 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:terminal",spin:0b}}
setblock 275 100 328 ae2:cable_bus replace
data merge block 275 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:storage_monitor",spin:0b}}
setblock 278 100 328 ae2:cable_bus replace
data merge block 278 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:conversion_monitor",spin:0b}}
setblock 281 100 328 ae2:cable_bus replace
data merge block 281 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:cable_pattern_provider"}}
setblock 284 100 328 ae2:cable_bus replace
data merge block 284 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:cable_interface"}}
setblock 287 100 328 ae2:cable_bus replace
data merge block 287 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:pattern_access_terminal",spin:0b}}
setblock 290 100 328 ae2:cable_bus replace
data merge block 290 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:cable_energy_acceptor"}}
setblock 293 100 328 ae2:cable_bus replace
data merge block 293 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:me_p2p_tunnel",freq:0s}}
setblock 296 100 328 ae2:cable_bus replace
data merge block 296 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:redstone_p2p_tunnel",freq:0s}}
setblock 299 100 328 ae2:cable_bus replace
data merge block 299 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:item_p2p_tunnel",freq:0s}}
setblock 302 100 328 ae2:cable_bus replace
data merge block 302 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:fluid_p2p_tunnel",freq:0s}}
setblock 305 100 328 ae2:cable_bus replace
data merge block 305 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:fe_p2p_tunnel",freq:0s}}
setblock 308 100 328 ae2:cable_bus replace
data merge block 308 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},west:{id:"ae2:light_p2p_tunnel",freq:0s}}

# ae2-s1-06 all-native-parts-installed-east
setblock 311 100 328 ae2:cable_bus replace
data merge block 311 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:quartz_fiber"}}
setblock 314 100 328 ae2:cable_bus replace
data merge block 314 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:toggle_bus"}}
setblock 317 100 328 ae2:cable_bus replace
data merge block 317 100 328 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:inverted_toggle_bus"}}
setblock 209 100 333 ae2:cable_bus replace
data merge block 209 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:cable_anchor"}}
setblock 212 100 333 ae2:cable_bus replace
data merge block 212 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:monitor",spin:1b}}
setblock 215 100 333 ae2:cable_bus replace
data merge block 215 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:semi_dark_monitor",spin:1b}}
setblock 218 100 333 ae2:cable_bus replace
data merge block 218 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:dark_monitor",spin:1b}}
setblock 221 100 333 ae2:cable_bus replace
data merge block 221 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:storage_bus"}}
setblock 224 100 333 ae2:cable_bus replace
data merge block 224 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:import_bus"}}
setblock 227 100 333 ae2:cable_bus replace
data merge block 227 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:export_bus"}}
setblock 230 100 333 ae2:cable_bus replace
data merge block 230 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:level_emitter"}}
setblock 233 100 333 ae2:cable_bus replace
data merge block 233 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:energy_level_emitter"}}
setblock 236 100 333 ae2:cable_bus replace
data merge block 236 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:annihilation_plane"}}
setblock 239 100 333 ae2:cable_bus replace
data merge block 239 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:formation_plane"}}
setblock 242 100 333 ae2:cable_bus replace
data merge block 242 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:pattern_encoding_terminal",spin:1b}}
setblock 245 100 333 ae2:cable_bus replace
data merge block 245 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:crafting_terminal",spin:1b}}
setblock 248 100 333 ae2:cable_bus replace
data merge block 248 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:terminal",spin:1b}}
setblock 251 100 333 ae2:cable_bus replace
data merge block 251 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:storage_monitor",spin:1b}}
setblock 254 100 333 ae2:cable_bus replace
data merge block 254 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:conversion_monitor",spin:1b}}
setblock 257 100 333 ae2:cable_bus replace
data merge block 257 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:cable_pattern_provider"}}
setblock 260 100 333 ae2:cable_bus replace
data merge block 260 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:cable_interface"}}
setblock 263 100 333 ae2:cable_bus replace
data merge block 263 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:pattern_access_terminal",spin:1b}}
setblock 266 100 333 ae2:cable_bus replace
data merge block 266 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:cable_energy_acceptor"}}
setblock 269 100 333 ae2:cable_bus replace
data merge block 269 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:me_p2p_tunnel",freq:0s}}
setblock 272 100 333 ae2:cable_bus replace
data merge block 272 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:redstone_p2p_tunnel",freq:0s}}
setblock 275 100 333 ae2:cable_bus replace
data merge block 275 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:item_p2p_tunnel",freq:0s}}
setblock 278 100 333 ae2:cable_bus replace
data merge block 278 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:fluid_p2p_tunnel",freq:0s}}
setblock 281 100 333 ae2:cable_bus replace
data merge block 281 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:fe_p2p_tunnel",freq:0s}}
setblock 284 100 333 ae2:cable_bus replace
data merge block 284 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:light_p2p_tunnel",freq:0s}}

# ae2-s1-07 annihilation-plane-all-sixteen-masks
setblock 289 100 333 ae2:cable_bus replace
data merge block 289 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 293 100 334 ae2:cable_bus replace
data merge block 293 100 334 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 296 100 334 ae2:cable_bus replace
data merge block 296 100 334 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 295 100 333 ae2:cable_bus replace
data merge block 295 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 300 100 333 ae2:cable_bus replace
data merge block 300 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 303 100 333 ae2:cable_bus replace
data merge block 303 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 301 100 333 ae2:cable_bus replace
data merge block 301 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 306 100 333 ae2:cable_bus replace
data merge block 306 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 305 100 334 ae2:cable_bus replace
data merge block 305 100 334 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 309 100 333 ae2:cable_bus replace
data merge block 309 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 308 100 334 ae2:cable_bus replace
data merge block 308 100 334 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 307 100 333 ae2:cable_bus replace
data merge block 307 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 311 100 332 ae2:cable_bus replace
data merge block 311 100 332 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 314 100 332 ae2:cable_bus replace
data merge block 314 100 332 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 313 100 333 ae2:cable_bus replace
data merge block 313 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 317 100 332 ae2:cable_bus replace
data merge block 317 100 332 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 317 100 334 ae2:cable_bus replace
data merge block 317 100 334 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 209 100 337 ae2:cable_bus replace
data merge block 209 100 337 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 209 100 339 ae2:cable_bus replace
data merge block 209 100 339 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 208 100 338 ae2:cable_bus replace
data merge block 208 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 212 100 337 ae2:cable_bus replace
data merge block 212 100 337 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 213 100 338 ae2:cable_bus replace
data merge block 213 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 215 100 337 ae2:cable_bus replace
data merge block 215 100 337 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 216 100 338 ae2:cable_bus replace
data merge block 216 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 214 100 338 ae2:cable_bus replace
data merge block 214 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 218 100 337 ae2:cable_bus replace
data merge block 218 100 337 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 219 100 338 ae2:cable_bus replace
data merge block 219 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 218 100 339 ae2:cable_bus replace
data merge block 218 100 339 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 221 100 337 ae2:cable_bus replace
data merge block 221 100 337 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 222 100 338 ae2:cable_bus replace
data merge block 222 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 221 100 339 ae2:cable_bus replace
data merge block 221 100 339 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 220 100 338 ae2:cable_bus replace
data merge block 220 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"}}
setblock 287 100 333 ae2:cable_bus replace
data merge block 287 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 290 100 333 ae2:cable_bus replace
data merge block 290 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 293 100 333 ae2:cable_bus replace
data merge block 293 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 296 100 333 ae2:cable_bus replace
data merge block 296 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 299 100 333 ae2:cable_bus replace
data merge block 299 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 302 100 333 ae2:cable_bus replace
data merge block 302 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 305 100 333 ae2:cable_bus replace
data merge block 305 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 308 100 333 ae2:cable_bus replace
data merge block 308 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 311 100 333 ae2:cable_bus replace
data merge block 311 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:glass"},facadeNorth:{Name:"minecraft:stone"}}
setblock 314 100 333 ae2:cable_bus replace
data merge block 314 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 317 100 333 ae2:cable_bus replace
data merge block 317 100 333 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 209 100 338 ae2:cable_bus replace
data merge block 209 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 212 100 338 ae2:cable_bus replace
data merge block 212 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 215 100 338 ae2:cable_bus replace
data merge block 215 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 218 100 338 ae2:cable_bus replace
data merge block 218 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}
setblock 221 100 338 ae2:cable_bus replace
data merge block 221 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:annihilation_plane"},facadeUp:{Name:"minecraft:stone"}}

# ae2-s1-08 formation-plane-all-sixteen-masks
setblock 228 100 338 ae2:cable_bus replace
data merge block 228 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 230 99 338 ae2:cable_bus replace
data merge block 230 99 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 233 99 338 ae2:cable_bus replace
data merge block 233 99 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 234 100 338 ae2:cable_bus replace
data merge block 234 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 235 100 338 ae2:cable_bus replace
data merge block 235 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 238 100 338 ae2:cable_bus replace
data merge block 238 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 240 100 338 ae2:cable_bus replace
data merge block 240 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 241 100 338 ae2:cable_bus replace
data merge block 241 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 242 99 338 ae2:cable_bus replace
data merge block 242 99 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 244 100 338 ae2:cable_bus replace
data merge block 244 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 245 99 338 ae2:cable_bus replace
data merge block 245 99 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 246 100 338 ae2:cable_bus replace
data merge block 246 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 248 101 338 ae2:cable_bus replace
data merge block 248 101 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 251 101 338 ae2:cable_bus replace
data merge block 251 101 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 252 100 338 ae2:cable_bus replace
data merge block 252 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 254 101 338 ae2:cable_bus replace
data merge block 254 101 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 254 99 338 ae2:cable_bus replace
data merge block 254 99 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 257 101 338 ae2:cable_bus replace
data merge block 257 101 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 257 99 338 ae2:cable_bus replace
data merge block 257 99 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 258 100 338 ae2:cable_bus replace
data merge block 258 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 260 101 338 ae2:cable_bus replace
data merge block 260 101 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 259 100 338 ae2:cable_bus replace
data merge block 259 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 263 101 338 ae2:cable_bus replace
data merge block 263 101 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 262 100 338 ae2:cable_bus replace
data merge block 262 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 264 100 338 ae2:cable_bus replace
data merge block 264 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 266 101 338 ae2:cable_bus replace
data merge block 266 101 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 265 100 338 ae2:cable_bus replace
data merge block 265 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 266 99 338 ae2:cable_bus replace
data merge block 266 99 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 269 101 338 ae2:cable_bus replace
data merge block 269 101 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 268 100 338 ae2:cable_bus replace
data merge block 268 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 269 99 338 ae2:cable_bus replace
data merge block 269 99 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 270 100 338 ae2:cable_bus replace
data merge block 270 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"}}
setblock 224 100 338 ae2:cable_bus replace
data merge block 224 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 227 100 338 ae2:cable_bus replace
data merge block 227 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 230 100 338 ae2:cable_bus replace
data merge block 230 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 233 100 338 ae2:cable_bus replace
data merge block 233 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 236 100 338 ae2:cable_bus replace
data merge block 236 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 239 100 338 ae2:cable_bus replace
data merge block 239 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 242 100 338 ae2:cable_bus replace
data merge block 242 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 245 100 338 ae2:cable_bus replace
data merge block 245 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 248 100 338 ae2:cable_bus replace
data merge block 248 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 251 100 338 ae2:cable_bus replace
data merge block 251 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 254 100 338 ae2:cable_bus replace
data merge block 254 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 257 100 338 ae2:cable_bus replace
data merge block 257 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 260 100 338 ae2:cable_bus replace
data merge block 260 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 263 100 338 ae2:cable_bus replace
data merge block 263 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 266 100 338 ae2:cable_bus replace
data merge block 266 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}
setblock 269 100 338 ae2:cable_bus replace
data merge block 269 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:formation_plane"},facadeNorth:{Name:"minecraft:stone"}}

# ae2-s1-09 all-p2p-types-frequency-0
setblock 272 100 338 ae2:cable_bus replace
data merge block 272 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},down:{id:"ae2:me_p2p_tunnel",freq:0s}}
setblock 275 100 338 ae2:cable_bus replace
data merge block 275 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},up:{id:"ae2:redstone_p2p_tunnel",freq:0s}}
setblock 278 100 338 ae2:cable_bus replace
data merge block 278 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},north:{id:"ae2:item_p2p_tunnel",freq:0s}}
setblock 281 100 338 ae2:cable_bus replace
data merge block 281 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},south:{id:"ae2:fluid_p2p_tunnel",freq:0s}}
setblock 284 100 338 ae2:cable_bus replace
data merge block 284 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},west:{id:"ae2:fe_p2p_tunnel",freq:0s}}
setblock 287 100 338 ae2:cable_bus replace
data merge block 287 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},east:{id:"ae2:light_p2p_tunnel",freq:0s}}

# ae2-s1-10 all-p2p-types-frequency-4660
setblock 290 100 338 ae2:cable_bus replace
data merge block 290 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},down:{id:"ae2:me_p2p_tunnel",freq:4660s}}
setblock 293 100 338 ae2:cable_bus replace
data merge block 293 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},up:{id:"ae2:redstone_p2p_tunnel",freq:4660s}}
setblock 296 100 338 ae2:cable_bus replace
data merge block 296 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},north:{id:"ae2:item_p2p_tunnel",freq:4660s}}
setblock 299 100 338 ae2:cable_bus replace
data merge block 299 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},south:{id:"ae2:fluid_p2p_tunnel",freq:4660s}}
setblock 302 100 338 ae2:cable_bus replace
data merge block 302 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},west:{id:"ae2:fe_p2p_tunnel",freq:4660s}}
setblock 305 100 338 ae2:cable_bus replace
data merge block 305 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},east:{id:"ae2:light_p2p_tunnel",freq:4660s}}

# ae2-s1-11 all-p2p-types-frequency-65535
setblock 308 100 338 ae2:cable_bus replace
data merge block 308 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},down:{id:"ae2:me_p2p_tunnel",freq:-1s}}
setblock 311 100 338 ae2:cable_bus replace
data merge block 311 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},up:{id:"ae2:redstone_p2p_tunnel",freq:-1s}}
setblock 314 100 338 ae2:cable_bus replace
data merge block 314 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},north:{id:"ae2:item_p2p_tunnel",freq:-1s}}
setblock 317 100 338 ae2:cable_bus replace
data merge block 317 100 338 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},south:{id:"ae2:fluid_p2p_tunnel",freq:-1s}}
setblock 209 100 343 ae2:cable_bus replace
data merge block 209 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},west:{id:"ae2:fe_p2p_tunnel",freq:-1s}}
setblock 212 100 343 ae2:cable_bus replace
data merge block 212 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},east:{id:"ae2:light_p2p_tunnel",freq:-1s}}

# ae2-s1-12 dense-anchor-legality-and-persistent-spin-control
setblock 215 100 343 ae2:cable_bus replace
data merge block 215 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"},down:{id:"ae2:cable_anchor"}}
setblock 218 100 343 ae2:cable_bus replace
data merge block 218 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"},up:{id:"ae2:cable_anchor"}}
setblock 221 100 343 ae2:cable_bus replace
data merge block 221 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"},north:{id:"ae2:cable_anchor"}}
setblock 224 100 343 ae2:cable_bus replace
data merge block 224 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"},south:{id:"ae2:cable_anchor"}}
setblock 227 100 343 ae2:cable_bus replace
data merge block 227 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"},west:{id:"ae2:cable_anchor"}}
setblock 230 100 343 ae2:cable_bus replace
data merge block 230 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"},east:{id:"ae2:cable_anchor"}}
setblock 233 100 343 ae2:cable_bus replace
data merge block 233 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:monitor",spin:4b}}
setblock 236 100 343 ae2:cable_bus replace
data merge block 236 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}

# ae2-s1-13 part-and-facade-only-buses
setblock 239 100 343 ae2:cable_bus replace
data merge block 239 100 343 {hasRedstone:2,down:{id:"ae2:cable_anchor"}}
setblock 242 100 343 ae2:cable_bus replace
data merge block 242 100 343 {hasRedstone:2,up:{id:"ae2:cable_anchor"}}
setblock 245 100 343 ae2:cable_bus replace
data merge block 245 100 343 {hasRedstone:2,north:{id:"ae2:cable_anchor"}}
setblock 248 100 343 ae2:cable_bus replace
data merge block 248 100 343 {hasRedstone:2,south:{id:"ae2:cable_anchor"}}
setblock 251 100 343 ae2:cable_bus replace
data merge block 251 100 343 {hasRedstone:2,west:{id:"ae2:cable_anchor"}}
setblock 254 100 343 ae2:cable_bus replace
data merge block 254 100 343 {hasRedstone:2,east:{id:"ae2:cable_anchor"}}
setblock 257 100 343 ae2:cable_bus replace
data merge block 257 100 343 {hasRedstone:2,down:{id:"ae2:cable_anchor"},up:{id:"ae2:cable_anchor"},north:{id:"ae2:cable_anchor"},south:{id:"ae2:cable_anchor"},west:{id:"ae2:cable_anchor"},east:{id:"ae2:cable_anchor"}}
setblock 260 100 343 ae2:cable_bus replace
data merge block 260 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"}}

# ae2-s1-14 facade-mask-00-through-10
setblock 269 101 343 ae2:quartz_vibrant_glass replace
setblock 263 100 343 ae2:cable_bus replace
data merge block 263 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 266 100 343 ae2:cable_bus replace
data merge block 266 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:quartz_glass"}}
setblock 269 100 343 ae2:cable_bus replace
data merge block 269 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"ae2:quartz_vibrant_glass"}}
setblock 272 100 343 ae2:cable_bus replace
data merge block 272 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:controller",Properties:{state:"offline",type:"block"}},facadeUp:{Name:"ae2:controller",Properties:{state:"offline",type:"block"}}}
setblock 275 100 343 ae2:cable_bus replace
data merge block 275 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"ae2:1k_crafting_storage",Properties:{formed:"false",powered:"false"}}}
setblock 278 100 343 ae2:cable_bus replace
data merge block 278 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:quartz_glass"},facadeNorth:{Name:"ae2:quartz_vibrant_glass"}}
setblock 281 100 343 ae2:cable_bus replace
data merge block 281 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"ae2:4k_crafting_storage",Properties:{formed:"false",powered:"false"}},facadeNorth:{Name:"ae2:4k_crafting_storage",Properties:{formed:"false",powered:"false"}}}
setblock 284 100 343 ae2:cable_bus replace
data merge block 284 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:16k_crafting_storage",Properties:{formed:"false",powered:"false"}},facadeUp:{Name:"ae2:16k_crafting_storage",Properties:{formed:"false",powered:"false"}},facadeNorth:{Name:"ae2:16k_crafting_storage",Properties:{formed:"false",powered:"false"}}}
setblock 287 100 343 ae2:cable_bus replace
data merge block 287 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeSouth:{Name:"ae2:64k_crafting_storage",Properties:{formed:"false",powered:"false"}}}
setblock 290 100 343 ae2:cable_bus replace
data merge block 290 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:256k_crafting_storage",Properties:{formed:"false",powered:"false"}},facadeSouth:{Name:"ae2:256k_crafting_storage",Properties:{formed:"false",powered:"false"}}}
setblock 293 100 343 ae2:cable_bus replace
data merge block 293 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"ae2:crafting_monitor",Properties:{facing:"north",formed:"false",powered:"false",spin:"0"}},facadeSouth:{Name:"ae2:crafting_monitor",Properties:{facing:"north",formed:"false",powered:"false",spin:"0"}}}

# ae2-s1-15 facade-mask-11-through-21
setblock 296 100 343 ae2:cable_bus replace
data merge block 296 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:crafting_unit",Properties:{formed:"false",powered:"false"}},facadeUp:{Name:"ae2:crafting_unit",Properties:{formed:"false",powered:"false"}},facadeSouth:{Name:"ae2:crafting_unit",Properties:{formed:"false",powered:"false"}}}
setblock 299 100 343 ae2:cable_bus replace
data merge block 299 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"ae2:crafting_accelerator",Properties:{formed:"false",powered:"false"}},facadeSouth:{Name:"ae2:crafting_accelerator",Properties:{formed:"false",powered:"false"}}}
setblock 302 100 343 ae2:cable_bus replace
data merge block 302 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:chiseled_bookshelf",Properties:{facing:"north",slot_0_occupied:"false",slot_1_occupied:"false",slot_2_occupied:"false",slot_3_occupied:"false",slot_4_occupied:"false",slot_5_occupied:"false"}},facadeNorth:{Name:"minecraft:chiseled_bookshelf",Properties:{facing:"north",slot_0_occupied:"false",slot_1_occupied:"false",slot_2_occupied:"false",slot_3_occupied:"false",slot_4_occupied:"false",slot_5_occupied:"false"}},facadeSouth:{Name:"minecraft:chiseled_bookshelf",Properties:{facing:"north",slot_0_occupied:"false",slot_1_occupied:"false",slot_2_occupied:"false",slot_3_occupied:"false",slot_4_occupied:"false",slot_5_occupied:"false"}}}
setblock 305 100 343 ae2:cable_bus replace
data merge block 305 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:furnace",Properties:{facing:"north",lit:"false"}},facadeNorth:{Name:"minecraft:furnace",Properties:{facing:"north",lit:"false"}},facadeSouth:{Name:"minecraft:furnace",Properties:{facing:"north",lit:"false"}}}
setblock 308 100 343 ae2:cable_bus replace
data merge block 308 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"ae2:crafting_monitor",Properties:{facing:"east",formed:"true",powered:"true",spin:"3"}},facadeSouth:{Name:"minecraft:stone"}}
setblock 311 100 343 ae2:cable_bus replace
data merge block 311 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeWest:{Name:"minecraft:soul_sand"}}
setblock 314 100 343 ae2:cable_bus replace
data merge block 314 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:honey_block"},facadeWest:{Name:"minecraft:stone"}}
setblock 317 100 343 ae2:cable_bus replace
data merge block 317 100 343 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:honey_block"},facadeWest:{Name:"minecraft:soul_sand"}}
setblock 209 100 348 ae2:cable_bus replace
data merge block 209 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}
setblock 212 100 348 ae2:cable_bus replace
data merge block 212 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}
setblock 215 100 348 ae2:cable_bus replace
data merge block 215 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}

# ae2-s1-16 facade-mask-22-through-32
setblock 218 100 348 ae2:cable_bus replace
data merge block 218 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}
setblock 221 100 348 ae2:cable_bus replace
data merge block 221 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}
setblock 224 100 348 ae2:cable_bus replace
data merge block 224 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}
setblock 227 100 348 ae2:cable_bus replace
data merge block 227 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}
setblock 230 100 348 ae2:cable_bus replace
data merge block 230 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}
setblock 233 100 348 ae2:cable_bus replace
data merge block 233 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}
setblock 236 100 348 ae2:cable_bus replace
data merge block 236 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}
setblock 239 100 348 ae2:cable_bus replace
data merge block 239 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}
setblock 242 100 348 ae2:cable_bus replace
data merge block 242 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}
setblock 245 100 348 ae2:cable_bus replace
data merge block 245 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"}}
setblock 248 100 348 ae2:cable_bus replace
data merge block 248 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeEast:{Name:"minecraft:stone"}}

# ae2-s1-17 facade-mask-33-through-43
setblock 251 100 348 ae2:cable_bus replace
data merge block 251 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 254 100 348 ae2:cable_bus replace
data merge block 254 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 257 100 348 ae2:cable_bus replace
data merge block 257 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 260 100 348 ae2:cable_bus replace
data merge block 260 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 263 100 348 ae2:cable_bus replace
data merge block 263 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 266 100 348 ae2:cable_bus replace
data merge block 266 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 269 100 348 ae2:cable_bus replace
data merge block 269 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 272 100 348 ae2:cable_bus replace
data merge block 272 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 275 100 348 ae2:cable_bus replace
data merge block 275 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 278 100 348 ae2:cable_bus replace
data merge block 278 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 281 100 348 ae2:cable_bus replace
data merge block 281 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}

# ae2-s1-18 facade-mask-44-through-53
setblock 284 100 348 ae2:cable_bus replace
data merge block 284 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 287 100 348 ae2:cable_bus replace
data merge block 287 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 290 100 348 ae2:cable_bus replace
data merge block 290 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 293 100 348 ae2:cable_bus replace
data merge block 293 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 296 100 348 ae2:cable_bus replace
data merge block 296 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 299 100 348 ae2:cable_bus replace
data merge block 299 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 302 100 348 ae2:cable_bus replace
data merge block 302 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 305 100 348 ae2:cable_bus replace
data merge block 305 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 308 100 348 ae2:cable_bus replace
data merge block 308 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 311 100 348 ae2:cable_bus replace
data merge block 311 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}

# ae2-s1-19 facade-mask-54-through-63
setblock 230 99 353 ae2:quartz_glass replace
setblock 230 101 353 ae2:quartz_glass replace
setblock 230 100 352 ae2:quartz_glass replace
setblock 230 100 354 ae2:quartz_glass replace
setblock 229 100 353 ae2:quartz_glass replace
setblock 231 100 353 ae2:quartz_glass replace
setblock 314 100 348 ae2:cable_bus replace
data merge block 314 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 317 100 348 ae2:cable_bus replace
data merge block 317 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 209 100 353 ae2:cable_bus replace
data merge block 209 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 212 100 353 ae2:cable_bus replace
data merge block 212 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 215 100 353 ae2:cable_bus replace
data merge block 215 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 218 100 353 ae2:cable_bus replace
data merge block 218 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeUp:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 221 100 353 ae2:cable_bus replace
data merge block 221 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 224 100 353 ae2:cable_bus replace
data merge block 224 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 227 100 353 ae2:cable_bus replace
data merge block 227 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeSouth:{Name:"minecraft:stone"},facadeWest:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 230 100 353 ae2:cable_bus replace
data merge block 230 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"ae2:quartz_glass"},facadeUp:{Name:"ae2:quartz_glass"},facadeNorth:{Name:"ae2:quartz_glass"},facadeSouth:{Name:"ae2:quartz_glass"},facadeWest:{Name:"ae2:quartz_glass"},facadeEast:{Name:"ae2:quartz_glass"}}

# ae2-s1-20 transparent-facade-six-faces
setblock 236 101 353 minecraft:glass replace
setblock 233 100 353 ae2:cable_bus replace
data merge block 233 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},facadeDown:{Name:"minecraft:glass"}}
setblock 236 100 353 ae2:cable_bus replace
data merge block 236 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},facadeUp:{Name:"minecraft:glass"}}
setblock 239 100 353 ae2:cable_bus replace
data merge block 239 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},facadeNorth:{Name:"minecraft:glass"}}
setblock 242 100 353 ae2:cable_bus replace
data merge block 242 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},facadeSouth:{Name:"minecraft:glass"}}
setblock 245 100 353 ae2:cable_bus replace
data merge block 245 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},facadeWest:{Name:"minecraft:glass"}}
setblock 248 100 353 ae2:cable_bus replace
data merge block 248 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},facadeEast:{Name:"minecraft:glass"}}

# ae2-s1-21 stateful-facade-materials
setblock 254 101 353 minecraft:oak_log[axis=y] replace
setblock 251 100 353 ae2:cable_bus replace
data merge block 251 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeDown:{Name:"minecraft:oak_log",Properties:{axis:"x"}}}
setblock 254 100 353 ae2:cable_bus replace
data merge block 254 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:oak_log",Properties:{axis:"y"}}}
setblock 257 100 353 ae2:cable_bus replace
data merge block 257 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:oak_log",Properties:{axis:"z"}}}
setblock 260 100 353 ae2:cable_bus replace
data merge block 260 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeSouth:{Name:"minecraft:magma_block"}}
setblock 263 100 353 ae2:cable_bus replace
data merge block 263 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeWest:{Name:"minecraft:oak_leaves",Properties:{distance:"1",persistent:"true",waterlogged:"false"}}}

# ae2-s1-22 facade-stilts-clipping-and-part-coexistence
setblock 266 100 353 ae2:cable_bus replace
data merge block 266 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeSouth:{Name:"minecraft:stone"}}
setblock 269 100 353 ae2:cable_bus replace
data merge block 269 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:cable_anchor"},facadeNorth:{Name:"minecraft:stone"}}
setblock 272 100 353 ae2:cable_bus replace
data merge block 272 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"ae2:terminal",spin:2b},facadeEast:{Name:"minecraft:stone"}}
setblock 275 100 353 ae2:cable_bus replace
data merge block 275 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},facadeUp:{Name:"minecraft:stone"},facadeNorth:{Name:"minecraft:stone"},facadeEast:{Name:"minecraft:stone"}}
setblock 278 100 353 ae2:cable_bus replace
data merge block 278 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"ae2:cable_anchor"},facadeUp:{Name:"minecraft:glass"},facadeWest:{Name:"minecraft:stone"}}

# ae2-s1-23 native-endpoints-profile-order-01-through-09
setblock 282 100 353 ae2:inscriber[facing=east,spin=0,waterlogged=false] replace
setblock 283 100 353 ae2:wireless_access_point[facing=west,state=off,waterlogged=false] replace
setblock 285 100 353 ae2:wireless_access_point[facing=east,state=off,waterlogged=false] replace
setblock 288 100 353 ae2:charger[facing=east,spin=0] replace
setblock 291 98 352 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 291 98 353 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 291 98 354 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 291 99 352 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 291 99 353 ae2:quantum_link[formed=false,waterlogged=false] replace
setblock 291 99 354 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 291 100 352 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 291 100 353 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 291 100 354 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 294 99 352 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 294 99 353 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 294 99 354 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 294 100 352 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 294 100 353 ae2:quantum_link[formed=false,waterlogged=false] replace
setblock 294 100 354 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 294 101 352 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 294 101 353 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 294 101 354 ae2:quantum_ring[formed=false,waterlogged=false] replace
setblock 297 99 353 ae2:spatial_pylon[powered_on=false] replace
setblock 297 100 353 ae2:spatial_pylon[powered_on=false] replace
setblock 297 101 353 ae2:spatial_pylon[powered_on=false] replace
setblock 300 100 353 ae2:spatial_io_port[facing=north,powered=false,spin=0] replace
setblock 303 100 353 ae2:spatial_anchor[facing=north,powered=false] replace
setblock 304 100 353 ae2:controller[state=offline,type=block] replace
setblock 306 100 353 ae2:controller[state=offline,type=block] replace
setblock 281 100 353 ae2:cable_bus replace
data merge block 281 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 284 100 353 ae2:cable_bus replace
data merge block 284 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},north:{id:"ae2:cable_anchor"},facadeUp:{Name:"minecraft:stone"}}
setblock 287 100 353 ae2:cable_bus replace
data merge block 287 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 290 100 353 ae2:cable_bus replace
data merge block 290 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 293 100 353 ae2:cable_bus replace
data merge block 293 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 296 100 353 ae2:cable_bus replace
data merge block 296 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 299 100 353 ae2:cable_bus replace
data merge block 299 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 302 100 353 ae2:cable_bus replace
data merge block 302 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 305 100 353 ae2:cable_bus replace
data merge block 305 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}

# ae2-s1-24 native-endpoints-profile-order-10-through-12
setblock 309 100 353 ae2:drive[facing=east,spin=0] replace
setblock 312 100 353 ae2:chest[facing=north,lights_on=false,spin=0] replace
setblock 315 100 353 ae2:interface replace
setblock 308 100 353 ae2:cable_bus replace
data merge block 308 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 311 100 353 ae2:cable_bus replace
data merge block 311 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 314 100 353 ae2:cable_bus replace
data merge block 314 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}

# ae2-s1-25 native-endpoints-profile-order-13-through-30
setblock 318 100 353 ae2:io_port[facing=north,powered=false,spin=0] replace
setblock 208 100 358 ae2:energy_acceptor replace
setblock 210 100 358 ae2:energy_acceptor replace
setblock 213 100 358 ae2:crystal_resonance_generator[facing=east,waterlogged=false] replace
setblock 216 100 358 ae2:vibration_chamber[active=false,facing=north,spin=0] replace
setblock 219 100 358 ae2:growth_accelerator[facing=east,powered=false] replace
setblock 222 100 358 ae2:energy_cell[fullness=0] replace
setblock 225 100 358 ae2:dense_energy_cell[fullness=0] replace
setblock 228 100 358 ae2:creative_energy_cell replace
setblock 231 100 358 ae2:crafting_unit[formed=false,powered=false] replace
setblock 231 101 358 ae2:1k_crafting_storage[formed=false,powered=false] replace
setblock 234 100 358 ae2:crafting_accelerator[formed=false,powered=false] replace
setblock 234 101 358 ae2:1k_crafting_storage[formed=false,powered=false] replace
setblock 237 100 358 ae2:1k_crafting_storage[formed=false,powered=false] replace
setblock 240 100 358 ae2:4k_crafting_storage[formed=false,powered=false] replace
setblock 240 101 358 ae2:1k_crafting_storage[formed=false,powered=false] replace
setblock 243 100 358 ae2:16k_crafting_storage[formed=false,powered=false] replace
setblock 243 101 358 ae2:1k_crafting_storage[formed=false,powered=false] replace
setblock 246 100 358 ae2:64k_crafting_storage[formed=false,powered=false] replace
setblock 246 101 358 ae2:1k_crafting_storage[formed=false,powered=false] replace
setblock 249 100 358 ae2:256k_crafting_storage[formed=false,powered=false] replace
setblock 249 101 358 ae2:1k_crafting_storage[formed=false,powered=false] replace
setblock 252 100 358 ae2:crafting_monitor[facing=east,formed=false,powered=false,spin=0] replace
setblock 252 101 358 ae2:1k_crafting_storage[formed=false,powered=false] replace
setblock 255 100 358 ae2:pattern_provider[push_direction=east] replace
setblock 256 100 358 ae2:molecular_assembler[powered=false] replace
setblock 258 100 358 ae2:molecular_assembler[powered=false] replace
setblock 317 100 353 ae2:cable_bus replace
data merge block 317 100 353 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 209 100 358 ae2:cable_bus replace
data merge block 209 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeUp:{Name:"minecraft:stone"}}
setblock 212 100 358 ae2:cable_bus replace
data merge block 212 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 215 100 358 ae2:cable_bus replace
data merge block 215 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 218 100 358 ae2:cable_bus replace
data merge block 218 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 221 100 358 ae2:cable_bus replace
data merge block 221 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 224 100 358 ae2:cable_bus replace
data merge block 224 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 227 100 358 ae2:cable_bus replace
data merge block 227 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 230 100 358 ae2:cable_bus replace
data merge block 230 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 233 100 358 ae2:cable_bus replace
data merge block 233 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 236 100 358 ae2:cable_bus replace
data merge block 236 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 239 100 358 ae2:cable_bus replace
data merge block 239 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 242 100 358 ae2:cable_bus replace
data merge block 242 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 245 100 358 ae2:cable_bus replace
data merge block 245 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"}}
setblock 248 100 358 ae2:cable_bus replace
data merge block 248 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 251 100 358 ae2:cable_bus replace
data merge block 251 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_smart_dense_cable"}}
setblock 254 100 358 ae2:cable_bus replace
data merge block 254 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}
setblock 257 100 358 ae2:cable_bus replace
data merge block 257 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:terminal",spin:0b}}

# ae2-s1-26 persistent-invalid-reporting-spin-controls
setblock 260 100 358 ae2:cable_bus replace
data merge block 260 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:semi_dark_monitor",spin:4b}}
setblock 263 100 358 ae2:cable_bus replace
data merge block 263 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:terminal",spin:0b},south:{id:"ae2:terminal",spin:4b}}
setblock 266 100 358 ae2:cable_bus replace
data merge block 266 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"},south:{id:"ae2:dark_monitor",spin:4b}}
setblock 269 100 358 ae2:cable_bus replace
data merge block 269 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:pattern_encoding_terminal",spin:4b}}

# ae2-s1-27 persistent-facade-and-spin-fallback-controls
setblock 272 100 358 ae2:cable_bus replace
data merge block 272 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},facadeNorth:{Name:"minecraft:oak_stairs",Properties:{facing:"east",half:"bottom",shape:"straight",waterlogged:"false"}}}
setblock 275 100 358 ae2:cable_bus replace
data merge block 275 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"ae2:crafting_terminal",spin:4b}}
setblock 278 100 358 ae2:cable_bus replace
data merge block 278 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"ae2:storage_monitor",spin:4b}}

# ae2-s1-28 disconnected-endpoint-and-whole-bus-controls
setblock 282 100 358 minecraft:stone replace
setblock 284 100 359 ae2:wireless_access_point[facing=north,state=off,waterlogged=false] replace
setblock 288 100 358 expandedae:exp_io_port[facing=north,powered=false,spin=0] replace
setblock 281 100 358 ae2:cable_bus replace
data merge block 281 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}
setblock 284 100 358 ae2:cable_bus replace
data merge block 284 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"ae2:terminal",spin:3b},facadeNorth:{Name:"minecraft:stone"}}
setblock 287 100 358 ae2:cable_bus replace
data merge block 287 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_smart_cable"}}

# ATM 1.2.0 M4/M5 cumulative review fixtures.
scoreboard objectives add ae2m45run dummy
scoreboard players add #m45_builds ae2m45run 1
# ae2-m45-01 AppliedFlux generic blocks, face part, and all twenty Drive-cell identities
setblock 336 100 312 appflux:charged_redstone_block replace
setblock 338 100 312 appflux:flux_accessor replace
setblock 336 100 316 ae2:cable_bus replace
data merge block 336 100 316 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"appflux:part_flux_accessor",fast:0b}}
setblock 338 100 316 ae2:cable_bus replace
data merge block 338 100 316 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"appflux:part_flux_accessor",fast:0b}}
setblock 340 100 316 ae2:cable_bus replace
data merge block 340 100 316 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"appflux:part_flux_accessor",fast:0b}}
setblock 342 100 316 ae2:cable_bus replace
data merge block 342 100 316 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"appflux:part_flux_accessor",fast:0b}}
setblock 344 100 316 ae2:cable_bus replace
data merge block 344 100 316 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"appflux:part_flux_accessor",fast:0b}}
setblock 346 100 316 ae2:cable_bus replace
data merge block 346 100 316 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"appflux:part_flux_accessor",fast:0b}}
setblock 336 100 320 ae2:drive[facing=north,spin=0] replace
data merge block 336 100 320 {inv:{item0:{id:"appflux:fe_1k_cell",count:1},item1:{id:"appflux:fe_1k_portable_cell",count:1},item2:{id:"appflux:fe_4k_cell",count:1},item3:{id:"appflux:fe_4k_portable_cell",count:1},item4:{id:"appflux:fe_16k_cell",count:1},item5:{id:"appflux:fe_16k_portable_cell",count:1},item6:{id:"appflux:fe_64k_cell",count:1},item7:{id:"appflux:fe_64k_portable_cell",count:1},item8:{id:"appflux:fe_256k_cell",count:1},item9:{id:"appflux:fe_256k_portable_cell",count:1}}}
setblock 338 100 320 ae2:drive[facing=north,spin=0] replace
data merge block 338 100 320 {inv:{item0:{id:"appflux:fe_1m_cell",count:1},item1:{id:"appflux:fe_1m_portable_cell",count:1},item2:{id:"appflux:fe_4m_cell",count:1},item3:{id:"appflux:fe_4m_portable_cell",count:1},item4:{id:"appflux:fe_16m_cell",count:1},item5:{id:"appflux:fe_16m_portable_cell",count:1},item6:{id:"appflux:fe_64m_cell",count:1},item7:{id:"appflux:fe_64m_portable_cell",count:1},item8:{id:"appflux:fe_256m_cell",count:1},item9:{id:"appflux:fe_256m_portable_cell",count:1}}}
setblock 340 100 320 extendedae:ex_drive[facing=north,spin=0] replace
data merge block 340 100 320 {inv:{item0:{id:"appflux:fe_1k_cell",count:1},item1:{id:"appflux:fe_1k_portable_cell",count:1},item2:{id:"appflux:fe_4k_cell",count:1},item3:{id:"appflux:fe_4k_portable_cell",count:1},item4:{id:"appflux:fe_16k_cell",count:1},item5:{id:"appflux:fe_16k_portable_cell",count:1},item6:{id:"appflux:fe_64k_cell",count:1},item7:{id:"appflux:fe_64k_portable_cell",count:1},item8:{id:"appflux:fe_256k_cell",count:1},item9:{id:"appflux:fe_256k_portable_cell",count:1},item10:{id:"appflux:fe_1m_cell",count:1},item11:{id:"appflux:fe_1m_portable_cell",count:1},item12:{id:"appflux:fe_4m_cell",count:1},item13:{id:"appflux:fe_4m_portable_cell",count:1},item14:{id:"appflux:fe_16m_cell",count:1},item15:{id:"appflux:fe_16m_portable_cell",count:1},item16:{id:"appflux:fe_64m_cell",count:1},item17:{id:"appflux:fe_64m_portable_cell",count:1},item18:{id:"appflux:fe_256m_cell",count:1},item19:{id:"appflux:fe_256m_portable_cell",count:1}}}

# ae2-m45-02 ME Requester 12 stable idle-derived block orientations and 24 terminal orientations
setblock 368 100 312 merequester:requester[active=false,facing=north] replace
setblock 370 100 312 merequester:requester[active=false,facing=east] replace
setblock 372 100 312 merequester:requester[active=false,facing=south] replace
setblock 374 100 312 merequester:requester[active=false,facing=west] replace
setblock 376 100 312 merequester:requester[active=false,facing=up,spin=0] replace
setblock 378 100 312 merequester:requester[active=false,facing=up,spin=1] replace
setblock 380 100 312 merequester:requester[active=false,facing=up,spin=2] replace
setblock 382 100 312 merequester:requester[active=false,facing=up,spin=3] replace
setblock 384 100 312 merequester:requester[active=false,facing=down,spin=0] replace
setblock 386 100 312 merequester:requester[active=false,facing=down,spin=1] replace
setblock 388 100 312 merequester:requester[active=false,facing=down,spin=2] replace
setblock 390 100 312 merequester:requester[active=false,facing=down,spin=3] replace
setblock 368 100 320 ae2:cable_bus replace
data merge block 368 100 320 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"merequester:requester_terminal",spin:0b}}
setblock 370 100 320 ae2:cable_bus replace
data merge block 370 100 320 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"merequester:requester_terminal",spin:1b}}
setblock 372 100 320 ae2:cable_bus replace
data merge block 372 100 320 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"merequester:requester_terminal",spin:2b}}
setblock 374 100 320 ae2:cable_bus replace
data merge block 374 100 320 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"merequester:requester_terminal",spin:3b}}
setblock 376 100 320 ae2:cable_bus replace
data merge block 376 100 320 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"merequester:requester_terminal",spin:0b}}
setblock 378 100 320 ae2:cable_bus replace
data merge block 378 100 320 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"merequester:requester_terminal",spin:1b}}
setblock 380 100 320 ae2:cable_bus replace
data merge block 380 100 320 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"merequester:requester_terminal",spin:2b}}
setblock 382 100 320 ae2:cable_bus replace
data merge block 382 100 320 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"merequester:requester_terminal",spin:3b}}
setblock 384 100 320 ae2:cable_bus replace
data merge block 384 100 320 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"merequester:requester_terminal",spin:0b}}
setblock 386 100 320 ae2:cable_bus replace
data merge block 386 100 320 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"merequester:requester_terminal",spin:1b}}
setblock 388 100 320 ae2:cable_bus replace
data merge block 388 100 320 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"merequester:requester_terminal",spin:2b}}
setblock 390 100 320 ae2:cable_bus replace
data merge block 390 100 320 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"merequester:requester_terminal",spin:3b}}
setblock 368 100 322 ae2:cable_bus replace
data merge block 368 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"merequester:requester_terminal",spin:0b}}
setblock 370 100 322 ae2:cable_bus replace
data merge block 370 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"merequester:requester_terminal",spin:1b}}
setblock 372 100 322 ae2:cable_bus replace
data merge block 372 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"merequester:requester_terminal",spin:2b}}
setblock 374 100 322 ae2:cable_bus replace
data merge block 374 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"merequester:requester_terminal",spin:3b}}
setblock 376 100 322 ae2:cable_bus replace
data merge block 376 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"merequester:requester_terminal",spin:0b}}
setblock 378 100 322 ae2:cable_bus replace
data merge block 378 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"merequester:requester_terminal",spin:1b}}
setblock 380 100 322 ae2:cable_bus replace
data merge block 380 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"merequester:requester_terminal",spin:2b}}
setblock 382 100 322 ae2:cable_bus replace
data merge block 382 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"merequester:requester_terminal",spin:3b}}
setblock 384 100 322 ae2:cable_bus replace
data merge block 384 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"merequester:requester_terminal",spin:0b}}
setblock 386 100 322 ae2:cable_bus replace
data merge block 386 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"merequester:requester_terminal",spin:1b}}
setblock 388 100 322 ae2:cable_bus replace
data merge block 388 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"merequester:requester_terminal",spin:2b}}
setblock 390 100 322 ae2:cable_bus replace
data merge block 390 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"merequester:requester_terminal",spin:3b}}

# ae2-m45-03 Expanded AE complete crafting, IO-port Z rotations, parts, and fallback controls
setblock 417 101 313 ae2:1k_crafting_storage[formed=false,powered=false] replace
setblock 417 102 313 ae2:crafting_unit[formed=false,powered=false] replace
setblock 418 102 313 ae2:crafting_unit[formed=false,powered=false] replace
setblock 416 102 314 ae2:crafting_unit[formed=false,powered=false] replace
setblock 417 102 314 ae2:crafting_unit[formed=false,powered=false] replace
setblock 418 102 314 ae2:crafting_unit[formed=false,powered=false] replace
setblock 466 100 312 ae2:crafting_unit[formed=false,powered=false] replace
setblock 464 100 313 ae2:crafting_unit[formed=false,powered=false] replace
setblock 465 100 313 ae2:crafting_unit[formed=false,powered=false] replace
setblock 466 100 313 ae2:crafting_unit[formed=false,powered=false] replace
setblock 464 100 314 ae2:crafting_unit[formed=false,powered=false] replace
setblock 465 100 314 ae2:crafting_unit[formed=false,powered=false] replace
setblock 466 100 314 ae2:crafting_unit[formed=false,powered=false] replace
setblock 464 101 312 ae2:crafting_unit[formed=false,powered=false] replace
setblock 465 101 312 ae2:crafting_unit[formed=false,powered=false] replace
setblock 466 101 312 ae2:crafting_unit[formed=false,powered=false] replace
setblock 464 101 313 ae2:crafting_unit[formed=false,powered=false] replace
setblock 465 101 313 ae2:1k_crafting_storage[formed=false,powered=false] replace
setblock 466 101 313 ae2:crafting_unit[formed=false,powered=false] replace
setblock 464 101 314 ae2:crafting_unit[formed=false,powered=false] replace
setblock 465 101 314 ae2:crafting_unit[formed=false,powered=false] replace
setblock 466 101 314 ae2:crafting_unit[formed=false,powered=false] replace
setblock 464 102 312 ae2:crafting_unit[formed=false,powered=false] replace
setblock 465 102 312 ae2:crafting_unit[formed=false,powered=false] replace
setblock 466 102 312 ae2:crafting_unit[formed=false,powered=false] replace
setblock 464 102 313 ae2:crafting_unit[formed=false,powered=false] replace
setblock 465 102 313 ae2:crafting_unit[formed=false,powered=false] replace
setblock 466 102 313 ae2:crafting_unit[formed=false,powered=false] replace
setblock 464 102 314 ae2:crafting_unit[formed=false,powered=false] replace
setblock 465 102 314 ae2:crafting_unit[formed=false,powered=false] replace
setblock 466 102 314 ae2:crafting_unit[formed=false,powered=false] replace
setblock 432 99 312 ae2:creative_energy_cell replace
setblock 434 99 312 ae2:creative_energy_cell replace
setblock 436 99 312 ae2:creative_energy_cell replace
setblock 438 99 312 ae2:creative_energy_cell replace
setblock 424 99 314 ae2:creative_energy_cell replace
setblock 426 99 314 ae2:creative_energy_cell replace
setblock 428 99 314 ae2:creative_energy_cell replace
setblock 430 99 314 ae2:creative_energy_cell replace
setblock 440 99 314 ae2:creative_energy_cell replace
setblock 442 99 314 ae2:creative_energy_cell replace
setblock 444 99 314 ae2:creative_energy_cell replace
setblock 446 99 314 ae2:creative_energy_cell replace
setblock 432 99 316 ae2:creative_energy_cell replace
setblock 434 99 316 ae2:creative_energy_cell replace
setblock 436 99 316 ae2:creative_energy_cell replace
setblock 438 99 316 ae2:creative_energy_cell replace
setblock 424 99 318 ae2:creative_energy_cell replace
setblock 426 99 318 ae2:creative_energy_cell replace
setblock 428 99 318 ae2:creative_energy_cell replace
setblock 430 99 318 ae2:creative_energy_cell replace
setblock 440 99 318 ae2:creative_energy_cell replace
setblock 442 99 318 ae2:creative_energy_cell replace
setblock 444 99 318 ae2:creative_energy_cell replace
setblock 446 99 318 ae2:creative_energy_cell replace
setblock 416 100 312 expandedae:exp_crafting_unit[formed=false,powered=false] replace
setblock 417 100 312 expandedae:exp_crafting_accelerator_2[formed=false,powered=false] replace
setblock 418 100 312 expandedae:exp_crafting_accelerator_4[formed=false,powered=false] replace
setblock 416 100 313 expandedae:exp_crafting_accelerator_8[formed=false,powered=false] replace
setblock 417 100 313 expandedae:exp_crafting_accelerator_16[formed=false,powered=false] replace
setblock 418 100 313 expandedae:exp_crafting_accelerator_32[formed=false,powered=false] replace
setblock 416 100 314 expandedae:exp_crafting_accelerator_64[formed=false,powered=false] replace
setblock 417 100 314 expandedae:exp_crafting_accelerator_128[formed=false,powered=false] replace
setblock 418 100 314 expandedae:exp_crafting_accelerator_256[formed=false,powered=false] replace
setblock 416 101 312 expandedae:exp_crafting_accelerator_512[formed=false,powered=false] replace
setblock 417 101 312 expandedae:exp_crafting_accelerator_1k[formed=false,powered=false] replace
setblock 418 101 312 expandedae:exp_crafting_accelerator_2k[formed=false,powered=false] replace
setblock 416 101 313 expandedae:exp_crafting_accelerator_4k[formed=false,powered=false] replace
setblock 418 101 313 expandedae:exp_crafting_accelerator_8k[formed=false,powered=false] replace
setblock 416 101 314 expandedae:exp_crafting_accelerator_16k[formed=false,powered=false] replace
setblock 417 101 314 expandedae:exp_crafting_accelerator_32k[formed=false,powered=false] replace
setblock 418 101 314 expandedae:exp_crafting_accelerator_64k[formed=false,powered=false] replace
setblock 416 102 312 expandedae:exp_crafting_accelerator_128k[formed=false,powered=false] replace
setblock 417 102 312 expandedae:exp_crafting_accelerator_256k[formed=false,powered=false] replace
setblock 418 102 312 expandedae:exp_crafting_accelerator_512k[formed=false,powered=false] replace
setblock 416 102 313 expandedae:exp_crafting_accelerator_1m[formed=false,powered=false] replace
setblock 464 100 312 expandedae:exp_crafting_unit[formed=false,powered=false] replace
setblock 424 100 312 expandedae:exp_io_port[facing=down,powered=false,spin=0] replace
setblock 426 100 312 expandedae:exp_io_port[facing=down,powered=false,spin=1] replace
setblock 428 100 312 expandedae:exp_io_port[facing=down,powered=false,spin=2] replace
setblock 430 100 312 expandedae:exp_io_port[facing=down,powered=false,spin=3] replace
setblock 432 100 312 expandedae:exp_io_port[facing=down,powered=true,spin=0] replace
setblock 434 100 312 expandedae:exp_io_port[facing=down,powered=true,spin=1] replace
setblock 436 100 312 expandedae:exp_io_port[facing=down,powered=true,spin=2] replace
setblock 438 100 312 expandedae:exp_io_port[facing=down,powered=true,spin=3] replace
setblock 440 100 312 expandedae:exp_io_port[facing=up,powered=false,spin=0] replace
setblock 442 100 312 expandedae:exp_io_port[facing=up,powered=false,spin=1] replace
setblock 444 100 312 expandedae:exp_io_port[facing=up,powered=false,spin=2] replace
setblock 446 100 312 expandedae:exp_io_port[facing=up,powered=false,spin=3] replace
setblock 424 100 314 expandedae:exp_io_port[facing=up,powered=true,spin=0] replace
setblock 426 100 314 expandedae:exp_io_port[facing=up,powered=true,spin=1] replace
setblock 428 100 314 expandedae:exp_io_port[facing=up,powered=true,spin=2] replace
setblock 430 100 314 expandedae:exp_io_port[facing=up,powered=true,spin=3] replace
setblock 432 100 314 expandedae:exp_io_port[facing=north,powered=false,spin=0] replace
setblock 434 100 314 expandedae:exp_io_port[facing=north,powered=false,spin=1] replace
setblock 436 100 314 expandedae:exp_io_port[facing=north,powered=false,spin=2] replace
setblock 438 100 314 expandedae:exp_io_port[facing=north,powered=false,spin=3] replace
setblock 440 100 314 expandedae:exp_io_port[facing=north,powered=true,spin=0] replace
setblock 442 100 314 expandedae:exp_io_port[facing=north,powered=true,spin=1] replace
setblock 444 100 314 expandedae:exp_io_port[facing=north,powered=true,spin=2] replace
setblock 446 100 314 expandedae:exp_io_port[facing=north,powered=true,spin=3] replace
setblock 424 100 316 expandedae:exp_io_port[facing=south,powered=false,spin=0] replace
setblock 426 100 316 expandedae:exp_io_port[facing=south,powered=false,spin=1] replace
setblock 428 100 316 expandedae:exp_io_port[facing=south,powered=false,spin=2] replace
setblock 430 100 316 expandedae:exp_io_port[facing=south,powered=false,spin=3] replace
setblock 432 100 316 expandedae:exp_io_port[facing=south,powered=true,spin=0] replace
setblock 434 100 316 expandedae:exp_io_port[facing=south,powered=true,spin=1] replace
setblock 436 100 316 expandedae:exp_io_port[facing=south,powered=true,spin=2] replace
setblock 438 100 316 expandedae:exp_io_port[facing=south,powered=true,spin=3] replace
setblock 440 100 316 expandedae:exp_io_port[facing=west,powered=false,spin=0] replace
setblock 442 100 316 expandedae:exp_io_port[facing=west,powered=false,spin=1] replace
setblock 444 100 316 expandedae:exp_io_port[facing=west,powered=false,spin=2] replace
setblock 446 100 316 expandedae:exp_io_port[facing=west,powered=false,spin=3] replace
setblock 424 100 318 expandedae:exp_io_port[facing=west,powered=true,spin=0] replace
setblock 426 100 318 expandedae:exp_io_port[facing=west,powered=true,spin=1] replace
setblock 428 100 318 expandedae:exp_io_port[facing=west,powered=true,spin=2] replace
setblock 430 100 318 expandedae:exp_io_port[facing=west,powered=true,spin=3] replace
setblock 432 100 318 expandedae:exp_io_port[facing=east,powered=false,spin=0] replace
setblock 434 100 318 expandedae:exp_io_port[facing=east,powered=false,spin=1] replace
setblock 436 100 318 expandedae:exp_io_port[facing=east,powered=false,spin=2] replace
setblock 438 100 318 expandedae:exp_io_port[facing=east,powered=false,spin=3] replace
setblock 440 100 318 expandedae:exp_io_port[facing=east,powered=true,spin=0] replace
setblock 442 100 318 expandedae:exp_io_port[facing=east,powered=true,spin=1] replace
setblock 444 100 318 expandedae:exp_io_port[facing=east,powered=true,spin=2] replace
setblock 446 100 318 expandedae:exp_io_port[facing=east,powered=true,spin=3] replace
setblock 424 100 322 ae2:cable_bus replace
data merge block 424 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"expandedae:exp_pattern_provider_part"}}
setblock 426 100 322 ae2:cable_bus replace
data merge block 426 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"expandedae:exp_pattern_provider_part"}}
setblock 428 100 322 ae2:cable_bus replace
data merge block 428 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"expandedae:exp_pattern_provider_part"}}
setblock 430 100 322 ae2:cable_bus replace
data merge block 430 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"expandedae:exp_pattern_provider_part"}}
setblock 432 100 322 ae2:cable_bus replace
data merge block 432 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"expandedae:exp_pattern_provider_part"}}
setblock 434 100 322 ae2:cable_bus replace
data merge block 434 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"expandedae:exp_pattern_provider_part"}}
setblock 436 100 322 ae2:cable_bus replace
data merge block 436 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"expandedae:exp_encoding_terminal",spin:0b}}
setblock 438 100 322 ae2:cable_bus replace
data merge block 438 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"expandedae:exp_encoding_terminal",spin:1b}}
setblock 440 100 322 ae2:cable_bus replace
data merge block 440 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"expandedae:exp_encoding_terminal",spin:2b}}
setblock 442 100 322 ae2:cable_bus replace
data merge block 442 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"expandedae:exp_encoding_terminal",spin:3b}}
setblock 444 100 322 ae2:cable_bus replace
data merge block 444 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"expandedae:exp_encoding_terminal",spin:0b}}
setblock 446 100 322 ae2:cable_bus replace
data merge block 446 100 322 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"expandedae:exp_encoding_terminal",spin:1b}}
setblock 424 100 324 ae2:cable_bus replace
data merge block 424 100 324 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"expandedae:exp_encoding_terminal",spin:2b}}
setblock 426 100 324 ae2:cable_bus replace
data merge block 426 100 324 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"expandedae:exp_encoding_terminal",spin:3b}}
setblock 428 100 324 ae2:cable_bus replace
data merge block 428 100 324 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"expandedae:exp_encoding_terminal",spin:0b}}
setblock 430 100 324 ae2:cable_bus replace
data merge block 430 100 324 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"expandedae:exp_encoding_terminal",spin:1b}}
setblock 432 100 324 ae2:cable_bus replace
data merge block 432 100 324 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"expandedae:exp_encoding_terminal",spin:2b}}
setblock 434 100 324 ae2:cable_bus replace
data merge block 434 100 324 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"expandedae:exp_encoding_terminal",spin:3b}}
setblock 436 100 324 ae2:cable_bus replace
data merge block 436 100 324 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"expandedae:exp_encoding_terminal",spin:0b}}
setblock 438 100 324 ae2:cable_bus replace
data merge block 438 100 324 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"expandedae:exp_encoding_terminal",spin:1b}}
setblock 440 100 324 ae2:cable_bus replace
data merge block 440 100 324 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"expandedae:exp_encoding_terminal",spin:2b}}
setblock 442 100 324 ae2:cable_bus replace
data merge block 442 100 324 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"expandedae:exp_encoding_terminal",spin:3b}}
setblock 444 100 324 ae2:cable_bus replace
data merge block 444 100 324 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"expandedae:exp_encoding_terminal",spin:0b}}
setblock 446 100 324 ae2:cable_bus replace
data merge block 446 100 324 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"expandedae:exp_encoding_terminal",spin:1b}}
setblock 424 100 326 ae2:cable_bus replace
data merge block 424 100 326 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"expandedae:exp_encoding_terminal",spin:2b}}
setblock 426 100 326 ae2:cable_bus replace
data merge block 426 100 326 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"expandedae:exp_encoding_terminal",spin:3b}}
setblock 428 100 326 ae2:cable_bus replace
data merge block 428 100 326 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"expandedae:exp_encoding_terminal",spin:0b}}
setblock 430 100 326 ae2:cable_bus replace
data merge block 430 100 326 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"expandedae:exp_encoding_terminal",spin:1b}}
setblock 432 100 326 ae2:cable_bus replace
data merge block 432 100 326 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"expandedae:exp_encoding_terminal",spin:2b}}
setblock 434 100 326 ae2:cable_bus replace
data merge block 434 100 326 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"expandedae:exp_encoding_terminal",spin:3b}}
setblock 424 100 330 expandedae:exp_pattern_provider replace
setblock 424 100 334 expandedae:colorable_drive replace
data merge block 424 100 334 {color:"WHITE"}
setblock 426 100 334 expandedae:colorable_drive replace
data merge block 426 100 334 {color:"LIGHT_GRAY"}
setblock 428 100 334 expandedae:colorable_drive replace
data merge block 428 100 334 {color:"GRAY"}
setblock 430 100 334 expandedae:colorable_drive replace
data merge block 430 100 334 {color:"BLACK"}
setblock 432 100 334 expandedae:colorable_drive replace
data merge block 432 100 334 {color:"LIME"}
setblock 434 100 334 expandedae:colorable_drive replace
data merge block 434 100 334 {color:"YELLOW"}
setblock 436 100 334 expandedae:colorable_drive replace
data merge block 436 100 334 {color:"ORANGE"}
setblock 438 100 334 expandedae:colorable_drive replace
data merge block 438 100 334 {color:"BROWN"}
setblock 440 100 334 expandedae:colorable_drive replace
data merge block 440 100 334 {color:"RED"}
setblock 442 100 334 expandedae:colorable_drive replace
data merge block 442 100 334 {color:"PINK"}
setblock 444 100 334 expandedae:colorable_drive replace
data merge block 444 100 334 {color:"MAGENTA"}
setblock 446 100 334 expandedae:colorable_drive replace
data merge block 446 100 334 {color:"PURPLE"}
setblock 448 100 334 expandedae:colorable_drive replace
data merge block 448 100 334 {color:"BLUE"}
setblock 450 100 334 expandedae:colorable_drive replace
data merge block 450 100 334 {color:"LIGHT_BLUE"}
setblock 452 100 334 expandedae:colorable_drive replace
data merge block 452 100 334 {color:"CYAN"}
setblock 454 100 334 expandedae:colorable_drive replace
data merge block 454 100 334 {color:"GREEN"}
setblock 456 100 334 expandedae:colorable_drive replace
data merge block 456 100 334 {color:"TRANSPARENT"}

# ae2-m45-04 MEGA Cells crafting, parts, Cell Dock, and all Drive-cell identities
setblock 338 100 346 ae2:crafting_unit[formed=false,powered=false] replace
setblock 336 101 344 ae2:crafting_unit[formed=false,powered=false] replace
setblock 337 101 344 ae2:crafting_unit[formed=false,powered=false] replace
setblock 338 101 344 ae2:crafting_unit[formed=false,powered=false] replace
setblock 336 101 345 ae2:crafting_unit[formed=false,powered=false] replace
setblock 337 101 345 ae2:1k_crafting_storage[formed=false,powered=false] replace
setblock 338 101 345 ae2:crafting_unit[formed=false,powered=false] replace
setblock 336 101 346 ae2:crafting_unit[formed=false,powered=false] replace
setblock 337 101 346 ae2:crafting_unit[formed=false,powered=false] replace
setblock 338 101 346 ae2:crafting_unit[formed=false,powered=false] replace
setblock 336 102 344 ae2:crafting_unit[formed=false,powered=false] replace
setblock 337 102 344 ae2:crafting_unit[formed=false,powered=false] replace
setblock 338 102 344 ae2:crafting_unit[formed=false,powered=false] replace
setblock 336 102 345 ae2:crafting_unit[formed=false,powered=false] replace
setblock 337 102 345 ae2:crafting_unit[formed=false,powered=false] replace
setblock 338 102 345 ae2:crafting_unit[formed=false,powered=false] replace
setblock 336 102 346 ae2:crafting_unit[formed=false,powered=false] replace
setblock 337 102 346 ae2:crafting_unit[formed=false,powered=false] replace
setblock 338 102 346 ae2:crafting_unit[formed=false,powered=false] replace
setblock 336 100 344 megacells:mega_crafting_unit[formed=false,powered=false] replace
setblock 337 100 344 megacells:mega_crafting_accelerator[formed=false,powered=false] replace
setblock 338 100 344 megacells:1m_crafting_storage[formed=false,powered=false] replace
setblock 336 100 345 megacells:4m_crafting_storage[formed=false,powered=false] replace
setblock 337 100 345 megacells:16m_crafting_storage[formed=false,powered=false] replace
setblock 338 100 345 megacells:64m_crafting_storage[formed=false,powered=false] replace
setblock 336 100 346 megacells:256m_crafting_storage[formed=false,powered=false] replace
setblock 337 100 346 megacells:mega_crafting_monitor[facing=north,formed=false,powered=false,spin=0] replace
setblock 465 100 312 megacells:mega_crafting_unit[formed=false,powered=false] replace
setblock 344 100 344 ae2:cable_bus replace
data merge block 344 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:0b,cell:{}}}
setblock 346 100 344 ae2:cable_bus replace
data merge block 346 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:bulk_item_cell",count:1}}}
setblock 348 100 344 ae2:cable_bus replace
data merge block 348 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:chemical_storage_cell_16m",count:1}}}
setblock 350 100 344 ae2:cable_bus replace
data merge block 350 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:chemical_storage_cell_1m",count:1}}}
setblock 352 100 344 ae2:cable_bus replace
data merge block 352 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:chemical_storage_cell_256m",count:1}}}
setblock 354 100 344 ae2:cable_bus replace
data merge block 354 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:chemical_storage_cell_4m",count:1}}}
setblock 356 100 344 ae2:cable_bus replace
data merge block 356 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:chemical_storage_cell_64m",count:1}}}
setblock 358 100 344 ae2:cable_bus replace
data merge block 358 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:experience_storage_cell_16m",count:1}}}
setblock 360 100 344 ae2:cable_bus replace
data merge block 360 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:experience_storage_cell_1m",count:1}}}
setblock 362 100 344 ae2:cable_bus replace
data merge block 362 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:experience_storage_cell_256m",count:1}}}
setblock 364 100 344 ae2:cable_bus replace
data merge block 364 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:experience_storage_cell_4m",count:1}}}
setblock 366 100 344 ae2:cable_bus replace
data merge block 366 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:experience_storage_cell_64m",count:1}}}
setblock 368 100 344 ae2:cable_bus replace
data merge block 368 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:fluid_storage_cell_16m",count:1}}}
setblock 370 100 344 ae2:cable_bus replace
data merge block 370 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:fluid_storage_cell_1m",count:1}}}
setblock 372 100 344 ae2:cable_bus replace
data merge block 372 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:fluid_storage_cell_256m",count:1}}}
setblock 374 100 344 ae2:cable_bus replace
data merge block 374 100 344 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:fluid_storage_cell_4m",count:1}}}
setblock 344 100 346 ae2:cable_bus replace
data merge block 344 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:fluid_storage_cell_64m",count:1}}}
setblock 346 100 346 ae2:cable_bus replace
data merge block 346 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:item_storage_cell_16m",count:1}}}
setblock 348 100 346 ae2:cable_bus replace
data merge block 348 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:item_storage_cell_1m",count:1}}}
setblock 350 100 346 ae2:cable_bus replace
data merge block 350 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:item_storage_cell_256m",count:1}}}
setblock 352 100 346 ae2:cable_bus replace
data merge block 352 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:item_storage_cell_4m",count:1}}}
setblock 354 100 346 ae2:cable_bus replace
data merge block 354 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:item_storage_cell_64m",count:1}}}
setblock 356 100 346 ae2:cable_bus replace
data merge block 356 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:mana_storage_cell_16m",count:1}}}
setblock 358 100 346 ae2:cable_bus replace
data merge block 358 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:mana_storage_cell_1m",count:1}}}
setblock 360 100 346 ae2:cable_bus replace
data merge block 360 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:mana_storage_cell_256m",count:1}}}
setblock 362 100 346 ae2:cable_bus replace
data merge block 362 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:mana_storage_cell_4m",count:1}}}
setblock 364 100 346 ae2:cable_bus replace
data merge block 364 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:mana_storage_cell_64m",count:1}}}
setblock 366 100 346 ae2:cable_bus replace
data merge block 366 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_chemical_cell_16m",count:1}}}
setblock 368 100 346 ae2:cable_bus replace
data merge block 368 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_chemical_cell_1m",count:1}}}
setblock 370 100 346 ae2:cable_bus replace
data merge block 370 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_chemical_cell_256m",count:1}}}
setblock 372 100 346 ae2:cable_bus replace
data merge block 372 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_chemical_cell_4m",count:1}}}
setblock 374 100 346 ae2:cable_bus replace
data merge block 374 100 346 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_chemical_cell_64m",count:1}}}
setblock 344 100 348 ae2:cable_bus replace
data merge block 344 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_experience_cell_16m",count:1}}}
setblock 346 100 348 ae2:cable_bus replace
data merge block 346 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_experience_cell_1m",count:1}}}
setblock 348 100 348 ae2:cable_bus replace
data merge block 348 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_experience_cell_256m",count:1}}}
setblock 350 100 348 ae2:cable_bus replace
data merge block 350 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_experience_cell_4m",count:1}}}
setblock 352 100 348 ae2:cable_bus replace
data merge block 352 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_experience_cell_64m",count:1}}}
setblock 354 100 348 ae2:cable_bus replace
data merge block 354 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_fluid_cell_16m",count:1}}}
setblock 356 100 348 ae2:cable_bus replace
data merge block 356 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_fluid_cell_1m",count:1}}}
setblock 358 100 348 ae2:cable_bus replace
data merge block 358 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_fluid_cell_256m",count:1}}}
setblock 360 100 348 ae2:cable_bus replace
data merge block 360 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_fluid_cell_4m",count:1}}}
setblock 362 100 348 ae2:cable_bus replace
data merge block 362 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_fluid_cell_64m",count:1}}}
setblock 364 100 348 ae2:cable_bus replace
data merge block 364 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_item_cell_16m",count:1}}}
setblock 366 100 348 ae2:cable_bus replace
data merge block 366 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_item_cell_1m",count:1}}}
setblock 368 100 348 ae2:cable_bus replace
data merge block 368 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_item_cell_256m",count:1}}}
setblock 370 100 348 ae2:cable_bus replace
data merge block 370 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_item_cell_4m",count:1}}}
setblock 372 100 348 ae2:cable_bus replace
data merge block 372 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_item_cell_64m",count:1}}}
setblock 374 100 348 ae2:cable_bus replace
data merge block 374 100 348 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_mana_cell_16m",count:1}}}
setblock 344 100 350 ae2:cable_bus replace
data merge block 344 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_mana_cell_1m",count:1}}}
setblock 346 100 350 ae2:cable_bus replace
data merge block 346 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_mana_cell_256m",count:1}}}
setblock 348 100 350 ae2:cable_bus replace
data merge block 348 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_mana_cell_4m",count:1}}}
setblock 350 100 350 ae2:cable_bus replace
data merge block 350 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_mana_cell_64m",count:1}}}
setblock 352 100 350 ae2:cable_bus replace
data merge block 352 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_source_cell_16m",count:1}}}
setblock 354 100 350 ae2:cable_bus replace
data merge block 354 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:portable_source_cell_1m",count:1}}}
setblock 356 100 350 ae2:cable_bus replace
data merge block 356 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:portable_source_cell_256m",count:1}}}
setblock 358 100 350 ae2:cable_bus replace
data merge block 358 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:portable_source_cell_4m",count:1}}}
setblock 360 100 350 ae2:cable_bus replace
data merge block 360 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:portable_source_cell_64m",count:1}}}
setblock 362 100 350 ae2:cable_bus replace
data merge block 362 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:radioactive_chemical_cell",count:1}}}
setblock 364 100 350 ae2:cable_bus replace
data merge block 364 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:soul_storage_cell_16m",count:1}}}
setblock 366 100 350 ae2:cable_bus replace
data merge block 366 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:soul_storage_cell_1m",count:1}}}
setblock 368 100 350 ae2:cable_bus replace
data merge block 368 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:soul_storage_cell_256m",count:1}}}
setblock 370 100 350 ae2:cable_bus replace
data merge block 370 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:soul_storage_cell_4m",count:1}}}
setblock 372 100 350 ae2:cable_bus replace
data merge block 372 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:soul_storage_cell_64m",count:1}}}
setblock 374 100 350 ae2:cable_bus replace
data merge block 374 100 350 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:source_storage_cell_16m",count:1}}}
setblock 344 100 352 ae2:cable_bus replace
data merge block 344 100 352 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:3b,cell:{id:"megacells:source_storage_cell_1m",count:1}}}
setblock 346 100 352 ae2:cable_bus replace
data merge block 346 100 352 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cell_dock",spin:0b,cell:{id:"megacells:source_storage_cell_256m",count:1}}}
setblock 348 100 352 ae2:cable_bus replace
data merge block 348 100 352 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cell_dock",spin:1b,cell:{id:"megacells:source_storage_cell_4m",count:1}}}
setblock 350 100 352 ae2:cable_bus replace
data merge block 350 100 352 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cell_dock",spin:2b,cell:{id:"megacells:source_storage_cell_64m",count:1}}}
setblock 352 100 352 ae2:cable_bus replace
data merge block 352 100 352 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cell_dock",spin:0b,cell:{id:"minecraft:stone",count:1}}}
setblock 344 100 356 ae2:cable_bus replace
data merge block 344 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:decompression_module"}}
setblock 346 100 356 ae2:cable_bus replace
data merge block 346 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:decompression_module"}}
setblock 348 100 356 ae2:cable_bus replace
data merge block 348 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:decompression_module"}}
setblock 350 100 356 ae2:cable_bus replace
data merge block 350 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:decompression_module"}}
setblock 352 100 356 ae2:cable_bus replace
data merge block 352 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:decompression_module"}}
setblock 354 100 356 ae2:cable_bus replace
data merge block 354 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:decompression_module"}}
setblock 356 100 356 ae2:cable_bus replace
data merge block 356 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cable_mega_interface"}}
setblock 358 100 356 ae2:cable_bus replace
data merge block 358 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cable_mega_interface"}}
setblock 360 100 356 ae2:cable_bus replace
data merge block 360 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cable_mega_interface"}}
setblock 362 100 356 ae2:cable_bus replace
data merge block 362 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cable_mega_interface"}}
setblock 364 100 356 ae2:cable_bus replace
data merge block 364 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cable_mega_interface"}}
setblock 366 100 356 ae2:cable_bus replace
data merge block 366 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cable_mega_interface"}}
setblock 368 100 356 ae2:cable_bus replace
data merge block 368 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"megacells:cable_mega_pattern_provider"}}
setblock 370 100 356 ae2:cable_bus replace
data merge block 370 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"megacells:cable_mega_pattern_provider"}}
setblock 372 100 356 ae2:cable_bus replace
data merge block 372 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"megacells:cable_mega_pattern_provider"}}
setblock 374 100 356 ae2:cable_bus replace
data merge block 374 100 356 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"megacells:cable_mega_pattern_provider"}}
setblock 344 100 358 ae2:cable_bus replace
data merge block 344 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"megacells:cable_mega_pattern_provider"}}
setblock 346 100 358 ae2:cable_bus replace
data merge block 346 100 358 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"megacells:cable_mega_pattern_provider"}}
setblock 344 100 362 ae2:drive[facing=north,spin=0] replace
data merge block 344 100 362 {inv:{item0:{id:"megacells:bulk_item_cell",count:1},item1:{id:"megacells:chemical_storage_cell_16m",count:1},item2:{id:"megacells:chemical_storage_cell_1m",count:1},item3:{id:"megacells:chemical_storage_cell_256m",count:1},item4:{id:"megacells:chemical_storage_cell_4m",count:1},item5:{id:"megacells:chemical_storage_cell_64m",count:1},item6:{id:"megacells:experience_storage_cell_16m",count:1},item7:{id:"megacells:experience_storage_cell_1m",count:1},item8:{id:"megacells:experience_storage_cell_256m",count:1},item9:{id:"megacells:experience_storage_cell_4m",count:1}}}
setblock 346 100 362 ae2:drive[facing=north,spin=0] replace
data merge block 346 100 362 {inv:{item0:{id:"megacells:experience_storage_cell_64m",count:1},item1:{id:"megacells:fluid_storage_cell_16m",count:1},item2:{id:"megacells:fluid_storage_cell_1m",count:1},item3:{id:"megacells:fluid_storage_cell_256m",count:1},item4:{id:"megacells:fluid_storage_cell_4m",count:1},item5:{id:"megacells:fluid_storage_cell_64m",count:1},item6:{id:"megacells:item_storage_cell_16m",count:1},item7:{id:"megacells:item_storage_cell_1m",count:1},item8:{id:"megacells:item_storage_cell_256m",count:1},item9:{id:"megacells:item_storage_cell_4m",count:1}}}
setblock 348 100 362 ae2:drive[facing=north,spin=0] replace
data merge block 348 100 362 {inv:{item0:{id:"megacells:item_storage_cell_64m",count:1},item1:{id:"megacells:mana_storage_cell_16m",count:1},item2:{id:"megacells:mana_storage_cell_1m",count:1},item3:{id:"megacells:mana_storage_cell_256m",count:1},item4:{id:"megacells:mana_storage_cell_4m",count:1},item5:{id:"megacells:mana_storage_cell_64m",count:1},item6:{id:"megacells:portable_chemical_cell_16m",count:1},item7:{id:"megacells:portable_chemical_cell_1m",count:1},item8:{id:"megacells:portable_chemical_cell_256m",count:1},item9:{id:"megacells:portable_chemical_cell_4m",count:1}}}
setblock 350 100 362 ae2:drive[facing=north,spin=0] replace
data merge block 350 100 362 {inv:{item0:{id:"megacells:portable_chemical_cell_64m",count:1},item1:{id:"megacells:portable_experience_cell_16m",count:1},item2:{id:"megacells:portable_experience_cell_1m",count:1},item3:{id:"megacells:portable_experience_cell_256m",count:1},item4:{id:"megacells:portable_experience_cell_4m",count:1},item5:{id:"megacells:portable_experience_cell_64m",count:1},item6:{id:"megacells:portable_fluid_cell_16m",count:1},item7:{id:"megacells:portable_fluid_cell_1m",count:1},item8:{id:"megacells:portable_fluid_cell_256m",count:1},item9:{id:"megacells:portable_fluid_cell_4m",count:1}}}
setblock 352 100 362 ae2:drive[facing=north,spin=0] replace
data merge block 352 100 362 {inv:{item0:{id:"megacells:portable_fluid_cell_64m",count:1},item1:{id:"megacells:portable_item_cell_16m",count:1},item2:{id:"megacells:portable_item_cell_1m",count:1},item3:{id:"megacells:portable_item_cell_256m",count:1},item4:{id:"megacells:portable_item_cell_4m",count:1},item5:{id:"megacells:portable_item_cell_64m",count:1},item6:{id:"megacells:portable_mana_cell_16m",count:1},item7:{id:"megacells:portable_mana_cell_1m",count:1},item8:{id:"megacells:portable_mana_cell_256m",count:1},item9:{id:"megacells:portable_mana_cell_4m",count:1}}}
setblock 354 100 362 ae2:drive[facing=north,spin=0] replace
data merge block 354 100 362 {inv:{item0:{id:"megacells:portable_mana_cell_64m",count:1},item1:{id:"megacells:portable_source_cell_16m",count:1},item2:{id:"megacells:portable_source_cell_1m",count:1},item3:{id:"megacells:portable_source_cell_256m",count:1},item4:{id:"megacells:portable_source_cell_4m",count:1},item5:{id:"megacells:portable_source_cell_64m",count:1},item6:{id:"megacells:radioactive_chemical_cell",count:1},item7:{id:"megacells:soul_storage_cell_16m",count:1},item8:{id:"megacells:soul_storage_cell_1m",count:1},item9:{id:"megacells:soul_storage_cell_256m",count:1}}}
setblock 356 100 362 ae2:drive[facing=north,spin=0] replace
data merge block 356 100 362 {inv:{item0:{id:"megacells:soul_storage_cell_4m",count:1},item1:{id:"megacells:soul_storage_cell_64m",count:1},item2:{id:"megacells:source_storage_cell_16m",count:1},item3:{id:"megacells:source_storage_cell_1m",count:1},item4:{id:"megacells:source_storage_cell_256m",count:1},item5:{id:"megacells:source_storage_cell_4m",count:1},item6:{id:"megacells:source_storage_cell_64m",count:1},item7:{},item8:{},item9:{}}}
setblock 344 100 366 extendedae:ex_drive[facing=north,spin=0] replace
data merge block 344 100 366 {inv:{item0:{id:"megacells:bulk_item_cell",count:1},item1:{id:"megacells:chemical_storage_cell_16m",count:1},item2:{id:"megacells:chemical_storage_cell_1m",count:1},item3:{id:"megacells:chemical_storage_cell_256m",count:1},item4:{id:"megacells:chemical_storage_cell_4m",count:1},item5:{id:"megacells:chemical_storage_cell_64m",count:1},item6:{id:"megacells:experience_storage_cell_16m",count:1},item7:{id:"megacells:experience_storage_cell_1m",count:1},item8:{id:"megacells:experience_storage_cell_256m",count:1},item9:{id:"megacells:experience_storage_cell_4m",count:1},item10:{id:"megacells:experience_storage_cell_64m",count:1},item11:{id:"megacells:fluid_storage_cell_16m",count:1},item12:{id:"megacells:fluid_storage_cell_1m",count:1},item13:{id:"megacells:fluid_storage_cell_256m",count:1},item14:{id:"megacells:fluid_storage_cell_4m",count:1},item15:{id:"megacells:fluid_storage_cell_64m",count:1},item16:{id:"megacells:item_storage_cell_16m",count:1},item17:{id:"megacells:item_storage_cell_1m",count:1},item18:{id:"megacells:item_storage_cell_256m",count:1},item19:{id:"megacells:item_storage_cell_4m",count:1}}}
setblock 346 100 366 extendedae:ex_drive[facing=north,spin=0] replace
data merge block 346 100 366 {inv:{item0:{id:"megacells:item_storage_cell_64m",count:1},item1:{id:"megacells:mana_storage_cell_16m",count:1},item2:{id:"megacells:mana_storage_cell_1m",count:1},item3:{id:"megacells:mana_storage_cell_256m",count:1},item4:{id:"megacells:mana_storage_cell_4m",count:1},item5:{id:"megacells:mana_storage_cell_64m",count:1},item6:{id:"megacells:portable_chemical_cell_16m",count:1},item7:{id:"megacells:portable_chemical_cell_1m",count:1},item8:{id:"megacells:portable_chemical_cell_256m",count:1},item9:{id:"megacells:portable_chemical_cell_4m",count:1},item10:{id:"megacells:portable_chemical_cell_64m",count:1},item11:{id:"megacells:portable_experience_cell_16m",count:1},item12:{id:"megacells:portable_experience_cell_1m",count:1},item13:{id:"megacells:portable_experience_cell_256m",count:1},item14:{id:"megacells:portable_experience_cell_4m",count:1},item15:{id:"megacells:portable_experience_cell_64m",count:1},item16:{id:"megacells:portable_fluid_cell_16m",count:1},item17:{id:"megacells:portable_fluid_cell_1m",count:1},item18:{id:"megacells:portable_fluid_cell_256m",count:1},item19:{id:"megacells:portable_fluid_cell_4m",count:1}}}
setblock 348 100 366 extendedae:ex_drive[facing=north,spin=0] replace
data merge block 348 100 366 {inv:{item0:{id:"megacells:portable_fluid_cell_64m",count:1},item1:{id:"megacells:portable_item_cell_16m",count:1},item2:{id:"megacells:portable_item_cell_1m",count:1},item3:{id:"megacells:portable_item_cell_256m",count:1},item4:{id:"megacells:portable_item_cell_4m",count:1},item5:{id:"megacells:portable_item_cell_64m",count:1},item6:{id:"megacells:portable_mana_cell_16m",count:1},item7:{id:"megacells:portable_mana_cell_1m",count:1},item8:{id:"megacells:portable_mana_cell_256m",count:1},item9:{id:"megacells:portable_mana_cell_4m",count:1},item10:{id:"megacells:portable_mana_cell_64m",count:1},item11:{id:"megacells:portable_source_cell_16m",count:1},item12:{id:"megacells:portable_source_cell_1m",count:1},item13:{id:"megacells:portable_source_cell_256m",count:1},item14:{id:"megacells:portable_source_cell_4m",count:1},item15:{id:"megacells:portable_source_cell_64m",count:1},item16:{id:"megacells:radioactive_chemical_cell",count:1},item17:{id:"megacells:soul_storage_cell_16m",count:1},item18:{id:"megacells:soul_storage_cell_1m",count:1},item19:{id:"megacells:soul_storage_cell_256m",count:1}}}
setblock 350 100 366 extendedae:ex_drive[facing=north,spin=0] replace
data merge block 350 100 366 {inv:{item0:{id:"megacells:soul_storage_cell_4m",count:1},item1:{id:"megacells:soul_storage_cell_64m",count:1},item2:{id:"megacells:source_storage_cell_16m",count:1},item3:{id:"megacells:source_storage_cell_1m",count:1},item4:{id:"megacells:source_storage_cell_256m",count:1},item5:{id:"megacells:source_storage_cell_4m",count:1},item6:{id:"megacells:source_storage_cell_64m",count:1},item7:{},item8:{},item9:{},item10:{},item11:{},item12:{},item13:{},item14:{},item15:{},item16:{},item17:{},item18:{},item19:{}}}

# ae2-m45-05 Advanced AE static roles plus a live-proven physical 4x3x3 quantum computer
setblock 416 100 370 advanced_ae:quantum_unit[formed=false,light_level=0,multiblocked=false,powered=false] replace
setblock 418 100 370 advanced_ae:quantum_core[formed=true,light_level=0,multiblocked=false,powered=false] replace
setblock 420 100 370 advanced_ae:quantum_storage_128[formed=false,light_level=0,multiblocked=false,powered=false] replace
setblock 422 100 370 advanced_ae:quantum_storage_256[formed=false,light_level=0,multiblocked=false,powered=false] replace
setblock 424 100 370 advanced_ae:data_entangler[formed=false,light_level=0,multiblocked=false,powered=false] replace
setblock 426 100 370 advanced_ae:quantum_accelerator[formed=false,light_level=0,multiblocked=false,powered=false] replace
setblock 428 100 370 advanced_ae:quantum_multi_threader[formed=false,light_level=0,multiblocked=false,powered=false] replace
setblock 430 100 370 advanced_ae:quantum_structure[formed=false,light_level=0,multiblocked=false,powered=false] replace
setblock 416 100 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 417 100 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 418 100 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 419 100 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 416 100 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 417 100 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 418 100 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 419 100 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 416 100 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 417 100 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 418 100 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 419 100 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 416 101 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 417 101 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 418 101 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 419 101 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 416 101 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 417 101 377 advanced_ae:quantum_core[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 418 101 377 advanced_ae:quantum_storage_128[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 419 101 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 416 101 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 417 101 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 418 101 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 419 101 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 416 102 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 417 102 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 418 102 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 419 102 376 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 416 102 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 417 102 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 418 102 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 419 102 377 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 416 102 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 417 102 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 418 102 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace
setblock 419 102 378 advanced_ae:quantum_structure[formed=true,light_level=0,multiblocked=true,powered=false] replace

# ae2-m45-06 Advanced AE quantum-alloy isolated and connected Athena topology
setblock 424 100 376 advanced_ae:quantum_alloy_block replace
setblock 426 100 376 advanced_ae:quantum_alloy_block replace
setblock 427 100 376 advanced_ae:quantum_alloy_block replace
setblock 426 100 377 advanced_ae:quantum_alloy_block replace
setblock 427 100 377 advanced_ae:quantum_alloy_block replace
setblock 426 101 376 advanced_ae:quantum_alloy_block replace
setblock 427 101 376 advanced_ae:quantum_alloy_block replace
setblock 426 101 377 advanced_ae:quantum_alloy_block replace
setblock 427 101 377 advanced_ae:quantum_alloy_block replace

# ae2-m45-07 ExtendedAE static roles plus a live-proven physical 4x3x3 Assembler Matrix
setblock 448 100 370 extendedae:assembler_matrix_frame[formed=false,powered=false,shape=block] replace
setblock 448 100 374 extendedae:assembler_matrix_wall[formed=false,powered=false] replace
setblock 450 100 374 extendedae:assembler_matrix_glass[formed=false,powered=false] replace
setblock 452 100 374 extendedae:assembler_matrix_pattern[formed=false,powered=false] replace
setblock 454 100 374 extendedae:assembler_matrix_crafter[formed=false,powered=false] replace
setblock 456 100 374 extendedae:assembler_matrix_speed[formed=false,powered=false] replace
setblock 456 100 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] replace
setblock 457 100 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] replace
setblock 458 100 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] replace
setblock 459 100 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] replace
setblock 456 100 379 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_z] replace
setblock 457 100 379 extendedae:assembler_matrix_glass[formed=true,powered=false] replace
setblock 458 100 379 extendedae:assembler_matrix_glass[formed=true,powered=false] replace
setblock 459 100 379 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_z] replace
setblock 456 100 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] replace
setblock 457 100 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] replace
setblock 458 100 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] replace
setblock 459 100 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] replace
setblock 456 101 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_y] replace
setblock 457 101 378 extendedae:assembler_matrix_glass[formed=true,powered=false] replace
setblock 458 101 378 extendedae:assembler_matrix_glass[formed=true,powered=false] replace
setblock 459 101 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_y] replace
setblock 456 101 379 extendedae:assembler_matrix_glass[formed=true,powered=false] replace
setblock 457 101 379 extendedae:assembler_matrix_pattern[formed=true,powered=false] replace
setblock 458 101 379 extendedae:assembler_matrix_crafter[formed=true,powered=false] replace
setblock 459 101 379 extendedae:assembler_matrix_glass[formed=true,powered=false] replace
setblock 456 101 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_y] replace
setblock 457 101 380 extendedae:assembler_matrix_glass[formed=true,powered=false] replace
setblock 458 101 380 extendedae:assembler_matrix_glass[formed=true,powered=false] replace
setblock 459 101 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_y] replace
setblock 456 102 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] replace
setblock 457 102 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] replace
setblock 458 102 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] replace
setblock 459 102 378 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] replace
setblock 456 102 379 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_z] replace
setblock 457 102 379 extendedae:assembler_matrix_glass[formed=true,powered=false] replace
setblock 458 102 379 extendedae:assembler_matrix_glass[formed=true,powered=false] replace
setblock 459 102 379 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_z] replace
setblock 456 102 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] replace
setblock 457 102 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] replace
setblock 458 102 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=column_x] replace
setblock 459 102 380 extendedae:assembler_matrix_frame[formed=true,powered=false,shape=block] replace

# ae2-m45-08 ExtendedAE plane identities, installed-face orbit, and all sixteen masks
setblock 485 100 370 ae2:cable_bus replace
data merge block 485 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 488 99 370 ae2:cable_bus replace
data merge block 488 99 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 492 99 370 ae2:cable_bus replace
data merge block 492 99 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 493 100 370 ae2:cable_bus replace
data merge block 493 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 495 100 370 ae2:cable_bus replace
data merge block 495 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 499 100 370 ae2:cable_bus replace
data merge block 499 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 501 100 370 ae2:cable_bus replace
data merge block 501 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 503 100 370 ae2:cable_bus replace
data merge block 503 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 504 99 370 ae2:cable_bus replace
data merge block 504 99 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 479 100 374 ae2:cable_bus replace
data merge block 479 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 480 99 374 ae2:cable_bus replace
data merge block 480 99 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 481 100 374 ae2:cable_bus replace
data merge block 481 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 484 101 374 ae2:cable_bus replace
data merge block 484 101 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 488 101 374 ae2:cable_bus replace
data merge block 488 101 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 489 100 374 ae2:cable_bus replace
data merge block 489 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 492 101 374 ae2:cable_bus replace
data merge block 492 101 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 492 99 374 ae2:cable_bus replace
data merge block 492 99 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 496 101 374 ae2:cable_bus replace
data merge block 496 101 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 496 99 374 ae2:cable_bus replace
data merge block 496 99 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 497 100 374 ae2:cable_bus replace
data merge block 497 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 500 101 374 ae2:cable_bus replace
data merge block 500 101 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 499 100 374 ae2:cable_bus replace
data merge block 499 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 504 101 374 ae2:cable_bus replace
data merge block 504 101 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 503 100 374 ae2:cable_bus replace
data merge block 503 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 505 100 374 ae2:cable_bus replace
data merge block 505 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 480 101 378 ae2:cable_bus replace
data merge block 480 101 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 479 100 378 ae2:cable_bus replace
data merge block 479 100 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 480 99 378 ae2:cable_bus replace
data merge block 480 99 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 484 101 378 ae2:cable_bus replace
data merge block 484 101 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 483 100 378 ae2:cable_bus replace
data merge block 483 100 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 484 99 378 ae2:cable_bus replace
data merge block 484 99 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 485 100 378 ae2:cable_bus replace
data merge block 485 100 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 485 100 382 ae2:cable_bus replace
data merge block 485 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 488 99 382 ae2:cable_bus replace
data merge block 488 99 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 492 99 382 ae2:cable_bus replace
data merge block 492 99 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 493 100 382 ae2:cable_bus replace
data merge block 493 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 495 100 382 ae2:cable_bus replace
data merge block 495 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 499 100 382 ae2:cable_bus replace
data merge block 499 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 501 100 382 ae2:cable_bus replace
data merge block 501 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 503 100 382 ae2:cable_bus replace
data merge block 503 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 504 99 382 ae2:cable_bus replace
data merge block 504 99 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 479 100 386 ae2:cable_bus replace
data merge block 479 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 480 99 386 ae2:cable_bus replace
data merge block 480 99 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 481 100 386 ae2:cable_bus replace
data merge block 481 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 484 101 386 ae2:cable_bus replace
data merge block 484 101 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 488 101 386 ae2:cable_bus replace
data merge block 488 101 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 489 100 386 ae2:cable_bus replace
data merge block 489 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 492 101 386 ae2:cable_bus replace
data merge block 492 101 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 492 99 386 ae2:cable_bus replace
data merge block 492 99 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 496 101 386 ae2:cable_bus replace
data merge block 496 101 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 496 99 386 ae2:cable_bus replace
data merge block 496 99 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 497 100 386 ae2:cable_bus replace
data merge block 497 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 500 101 386 ae2:cable_bus replace
data merge block 500 101 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 499 100 386 ae2:cable_bus replace
data merge block 499 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 504 101 386 ae2:cable_bus replace
data merge block 504 101 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 503 100 386 ae2:cable_bus replace
data merge block 503 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 505 100 386 ae2:cable_bus replace
data merge block 505 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 480 101 390 ae2:cable_bus replace
data merge block 480 101 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 479 100 390 ae2:cable_bus replace
data merge block 479 100 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 480 99 390 ae2:cable_bus replace
data merge block 480 99 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 484 101 390 ae2:cable_bus replace
data merge block 484 101 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 483 100 390 ae2:cable_bus replace
data merge block 483 100 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 484 99 390 ae2:cable_bus replace
data merge block 484 99 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 485 100 390 ae2:cable_bus replace
data merge block 485 100 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 480 100 370 ae2:cable_bus replace
data merge block 480 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 484 100 370 ae2:cable_bus replace
data merge block 484 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 488 100 370 ae2:cable_bus replace
data merge block 488 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 492 100 370 ae2:cable_bus replace
data merge block 492 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 496 100 370 ae2:cable_bus replace
data merge block 496 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 500 100 370 ae2:cable_bus replace
data merge block 500 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 504 100 370 ae2:cable_bus replace
data merge block 504 100 370 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 480 100 374 ae2:cable_bus replace
data merge block 480 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 484 100 374 ae2:cable_bus replace
data merge block 484 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 488 100 374 ae2:cable_bus replace
data merge block 488 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 492 100 374 ae2:cable_bus replace
data merge block 492 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 496 100 374 ae2:cable_bus replace
data merge block 496 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 500 100 374 ae2:cable_bus replace
data merge block 500 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 504 100 374 ae2:cable_bus replace
data merge block 504 100 374 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 480 100 378 ae2:cable_bus replace
data merge block 480 100 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 484 100 378 ae2:cable_bus replace
data merge block 484 100 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:active_formation_plane"}}
setblock 488 100 378 ae2:cable_bus replace
data merge block 488 100 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"extendedae:active_formation_plane"}}
setblock 492 100 378 ae2:cable_bus replace
data merge block 492 100 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"extendedae:active_formation_plane"}}
setblock 496 100 378 ae2:cable_bus replace
data merge block 496 100 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"extendedae:active_formation_plane"}}
setblock 500 100 378 ae2:cable_bus replace
data merge block 500 100 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"extendedae:active_formation_plane"}}
setblock 504 100 378 ae2:cable_bus replace
data merge block 504 100 378 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"extendedae:active_formation_plane"}}
setblock 480 100 382 ae2:cable_bus replace
data merge block 480 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 484 100 382 ae2:cable_bus replace
data merge block 484 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 488 100 382 ae2:cable_bus replace
data merge block 488 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 492 100 382 ae2:cable_bus replace
data merge block 492 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 496 100 382 ae2:cable_bus replace
data merge block 496 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 500 100 382 ae2:cable_bus replace
data merge block 500 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 504 100 382 ae2:cable_bus replace
data merge block 504 100 382 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 480 100 386 ae2:cable_bus replace
data merge block 480 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 484 100 386 ae2:cable_bus replace
data merge block 484 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 488 100 386 ae2:cable_bus replace
data merge block 488 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 492 100 386 ae2:cable_bus replace
data merge block 492 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 496 100 386 ae2:cable_bus replace
data merge block 496 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 500 100 386 ae2:cable_bus replace
data merge block 500 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 504 100 386 ae2:cable_bus replace
data merge block 504 100 386 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 480 100 390 ae2:cable_bus replace
data merge block 480 100 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 484 100 390 ae2:cable_bus replace
data merge block 484 100 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},north:{id:"extendedae:smart_annihilation_plane"}}
setblock 488 100 390 ae2:cable_bus replace
data merge block 488 100 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},down:{id:"extendedae:smart_annihilation_plane"}}
setblock 492 100 390 ae2:cable_bus replace
data merge block 492 100 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},up:{id:"extendedae:smart_annihilation_plane"}}
setblock 496 100 390 ae2:cable_bus replace
data merge block 496 100 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},south:{id:"extendedae:smart_annihilation_plane"}}
setblock 500 100 390 ae2:cable_bus replace
data merge block 500 100 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},west:{id:"extendedae:smart_annihilation_plane"}}
setblock 504 100 390 ae2:cable_bus replace
data merge block 504 100 390 {hasRedstone:2,cable:{id:"ae2:fluix_covered_cable"},east:{id:"extendedae:smart_annihilation_plane"}}

# Applied Mekanistics schema-12 exact extension fixtures.
scoreboard objectives add ae2amrun dummy
scoreboard players add #appmek_builds ae2amrun 1
scoreboard players set #appmek_immediate ae2amrun 0
scoreboard players set #appmek_20t ae2amrun 0
scoreboard players set #appmek_100t ae2amrun 0
# ae2-appmek-01 All ten Applied Mekanistics cells in one native Drive
setblock 528 100 312 ae2:drive[facing=north,spin=0] replace
data merge block 528 100 312 {inv:{item0:{id:"appmek:chemical_storage_cell_1k",count:1},item1:{id:"appmek:portable_chemical_cell_1k",count:1},item2:{id:"appmek:chemical_storage_cell_4k",count:1},item3:{id:"appmek:portable_chemical_cell_4k",count:1},item4:{id:"appmek:chemical_storage_cell_16k",count:1},item5:{id:"appmek:portable_chemical_cell_16k",count:1},item6:{id:"appmek:chemical_storage_cell_64k",count:1},item7:{id:"appmek:portable_chemical_cell_64k",count:1},item8:{id:"appmek:chemical_storage_cell_256k",count:1},item9:{id:"appmek:portable_chemical_cell_256k",count:1}}}

# ae2-appmek-02 Representative native Drive facing and spin controls
setblock 532 100 312 ae2:drive[facing=up,spin=1] replace
data merge block 532 100 312 {inv:{item0:{id:"appmek:chemical_storage_cell_1k",count:1},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 536 100 312 ae2:drive[facing=east,spin=2] replace
data merge block 536 100 312 {inv:{item0:{},item1:{},item2:{},item3:{},item4:{id:"appmek:chemical_storage_cell_16k",count:1},item5:{},item6:{},item7:{},item8:{},item9:{}}}
setblock 540 100 312 ae2:drive[facing=down,spin=3] replace
data merge block 540 100 312 {inv:{item0:{},item1:{},item2:{},item3:{},item4:{},item5:{},item6:{},item7:{},item8:{},item9:{id:"appmek:portable_chemical_cell_256k",count:1}}}

# ae2-appmek-03 Storage-bus visual seams against exact Mekanism targets
setblock 529 100 318 mekanism:qio_dashboard[active=false,facing=west] replace
setblock 534 101 318 mekanism:radioactive_waste_barrel[facing=north] replace
setblock 528 100 318 ae2:cable_bus replace
data merge block 528 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},east:{id:"ae2:storage_bus"}}
setblock 534 100 318 ae2:cable_bus replace
data merge block 534 100 318 {hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"},up:{id:"ae2:storage_bus"}}

# ae2-appmek-04 Pressurized-tube acceptor seam against the full-block ME Interface
setblock 540 100 318 mekanism:basic_pressurized_tube replace
setblock 541 100 318 ae2:interface replace

function ae2_m3:appmek/check_immediate
schedule function ae2_m3:appmek/check_20t 20t replace
schedule function ae2_m3:appmek/check_100t 100t replace

# Read/capture controller compatibility sentinel.
setblock 256 99 256 minecraft:stone replace
setblock 256 100 256 framedblocks:framed_cube[alt=false,glowing=false,propagates_skylight=false,reinforced=false,solid=true,solid_bg=false] replace
data merge block 256 100 256 {camo:{type:"framedblocks:block",state:{Name:"minecraft:stone"}},glowing:0b,intangible:0b,reinforced:0b,updated:3b}
setblock 257 100 256 minecraft:air replace
setblock 258 100 256 minecraft:stone replace

# South observation deck and guarded pose volume.
fill 215 107 252 227 107 256 minecraft:stone_bricks replace
fill 215 108 252 227 109 256 minecraft:air replace

scoreboard objectives add ae2m3s dummy
scoreboard players set #attempts ae2m3s 0
scoreboard players set #stable ae2m3s 0
schedule function ae2_m3:settle_check 20t replace
tellraw @a [{"text":"Built cumulative AE2 ATM 1.2.0 review gallery: 162 cases, 1373 anchors; waiting for two consecutive exact checks.","color":"aqua"}]
