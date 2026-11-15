/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.demo.descriptors

import kotlin.math.PI
import kotlin.math.floor

private const val TwoPi: Double = 2.0 * PI
private const val InvTwoPi: Double = 1.0 / TwoPi
private const val HalfPi: Double = PI / 2.0

/**
 * Pure-Kotlin sine approximation - compiles to plain Wasm instructions on the wasmJs target.
 *
 * [kotlin.math.sin] is imported from the JS host on Kotlin/Wasm (Wasm has no instruction for
 * transcendental functions), which costs a boundary crossing per call. This implementation uses
 * only multiplications, additions and [floor] (a native `f64.floor` instruction), so it stays
 * inside the Wasm sandbox.
 *
 * Range reduction to [-PI/2, PI/2] plus a degree-15 Taylor polynomial.
 * Maximum absolute error: ~1e-11 (verified in FastSinTest) - sufficient for painting,
 * NOT correctly rounded like [kotlin.math.sin].
 */
internal fun fastSin(x: Double): Double {
  //Wrap to [-PI, PI]
  var reduced = x - TwoPi * floor(x * InvTwoPi + 0.5)

  //Fold to [-PI/2, PI/2] using sin(PI - r) == sin(r)
  reduced = when {
    reduced > HalfPi -> PI - reduced
    reduced < -HalfPi -> -PI - reduced
    else -> reduced
  }

  val reducedSquared = reduced * reduced
  return reduced * (1.0 + reducedSquared * (-1.6666666666666666e-1 + reducedSquared * (8.333333333333333e-3 + reducedSquared * (-1.984126984126984e-4 + reducedSquared * (2.7557319223985893e-6 + reducedSquared * (-2.505210838544172e-8 + reducedSquared * (1.6059043836821613e-10 + reducedSquared * -7.647163731819816e-13)))))))
}
