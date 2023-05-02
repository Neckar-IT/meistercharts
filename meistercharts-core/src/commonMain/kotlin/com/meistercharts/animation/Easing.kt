/**
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
package com.meistercharts.animation

import it.neckar.open.unit.other.pct
import kotlin.math.PI
import kotlin.math.pow

/**
 * Supports easing
 *
 * [easings.net](https://easings.net/)
 * for explanations
 *
 * License: GNU General Public License v3.0
 */
fun interface Easing {
  /**
   * Returns the "eased" value for a given linear input.
   *
   * The values for linear must be in the range from 0.0 to 1.0.
   */
  operator fun invoke(linear: @pct Double): @pct Double


  companion object {
    /**
     * The constant that is used to calculate bouncing
     */
    const val bounce10: Double = 1.7015401988668

    fun cubic(function: (t: Double, b: Double, c: Double, d: Double) -> Double): Easing {
      return Easing { function(it, 0.0, 1.0, 1.0) }
    }

    /**
     * Combines two [Easing]s. One for the first 50% the second one for the remaining 50%.
     */
    fun combine(start: Easing, end: Easing): Easing =
      Easing {
        if (it < 0.5) {
          //the first 50% are calculated using the start easing method
          0.5 * start(it * 2.0)
        } else {
          //the second 50% are calculated using the end easing method
          0.5 * end((it - 0.5) * 2.0) + 0.5
        }
      }

    operator fun invoke(function: (Double) -> Double): Easing = Easing { linear -> function(linear) }


    val inElastic: Easing = Easing {
      if (it == 0.0 || it == 1.0) it else {
        val p = 0.3
        val s = p / 4.0
        val inv = it - 1
        -1.0 * 2.0.pow(10.0 * inv) * sin((inv - s) * (2.0 * PI) / p)
      }
    }

    val outElastic: Easing = Easing {
      if (it == 0.0 || it == 1.0) it else {
        val p = 0.3
        val s = p / 4.0
        2.0.pow(-10.0 * it) * sin((it - s) * (2.0 * PI) / p) + 1
      }
    }

    val outBounce: Easing = Easing {
      val s = 7.5625
      val p = 2.75
      when {
        it < (1.0 / p) -> s * it.pow(2.0)
        it < (2.0 / p) -> s * (it - 1.5 / p).pow(2.0) + 0.75
        it < 2.5 / p   -> s * (it - 2.25 / p).pow(2.0) + 0.9375
        else           -> s * (it - 2.625 / p).pow(2.0) + 0.984375
      }
    }

    /**
     * Linear - just returns the same value as the input
     */
    val linear: Easing get() = Easing { it }

    /**
     * Use the sin to ease in
     */
    val sin: Easing = Easing { kotlin.math.sin(it) }

    val smooth: Easing get() = Easing { it * it * (3 - 2 * it) }

    val incoming: Easing = Easing { it * it * it }
    val out: Easing = Easing { val inv = it - 1.0; inv * inv * inv + 1 }

    val inOut: Easing = combine(incoming, out)
    val outIn: Easing = combine(out, incoming)

    val inBack: Easing = Easing { it.pow(2.0) * ((bounce10 + 1.0) * it - bounce10) }
    val outBack: Easing = Easing { val inv = it - 1.0; inv.pow(2.0) * ((bounce10 + 1.0) * inv + bounce10) + 1.0 }

    val inOutBack: Easing = combine(inBack, outBack)
    val outInBack: Easing = combine(outBack, inBack)

    val inOutElastic: Easing = combine(inElastic, outElastic)
    val outInElastic: Easing = combine(outElastic, inElastic)

    val inBounce: Easing = Easing { 1.0 - outBounce(1.0 - it) }
    val inOutBounce: Easing = combine(inBounce, outBounce)
    val outInBounce: Easing = combine(outBounce, inBounce)

    val inQuad: Easing = Easing { 1.0 * it * it }
    val outQuad: Easing = Easing { -1.0 * it * (it - 2) }
    val inOutQuad: Easing = Easing { val t = it * 2.0; if (t < 1) (1.0 / 2 * t * t) else (-1.0 / 2 * ((t - 1) * ((t - 1) - 2) - 1)) }

    val availableEasings: List<Easing> = listOf(
      inElastic,
      outElastic,
      outBounce,
      sin,
      smooth,
      incoming,
      out,
      inOut,
      outIn,
      inBack,
      outBack,
      inOutBack,
      outInBack,
      inOutElastic,
      outInElastic,
      inBounce,
      inOutBounce,
      outInBounce,
      inQuad,
      outQuad,
      inOutQuad,
      linear,
    )
  }
}
