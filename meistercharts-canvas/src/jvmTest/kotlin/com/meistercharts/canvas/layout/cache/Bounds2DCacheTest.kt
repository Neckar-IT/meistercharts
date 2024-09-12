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
package com.meistercharts.canvas.layout.cache

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import it.neckar.open.unit.number.MayBeNegative
import org.junit.jupiter.api.Test

class Bounds2DCacheTest {
  @Test
  fun testIt() {
    val cache = StringModuleBounds2DCache()
    assertThat(cache.size).isEqualTo(0)

    cache.prepare(3)
    assertThat(cache.size).isEqualTo(3)
    assertThat(cache[0].size).isEqualTo(0)
    assertThat(cache[1].size).isEqualTo(0)
    assertThat(cache[2].size).isEqualTo(0)


    cache[0].prepare(7)
    assertThat(cache[0].size).isEqualTo(7)

    cache[0].x(3, 12.1)
    assertThat(cache[0].x(3)).isEqualTo(12.1)


    //Second rendering
    cache.prepare(3)

    assertThat(cache[0].size).isEqualTo(7)
    assertThat(cache[0].x(3)).isEqualTo(0.0)
  }

  class StringCache {
    val boundsCache = BoundsMultiCache()
    val textCache = StringMultiCache()

  }

  @Test
  fun testApi() {
    val stringLabelBoundsCache = StringModuleBounds2DCache() //2D

    //
    //
    // Layout
    //
    //


    val stringCount = 2
    stringLabelBoundsCache.prepare(stringCount)

    //iterate over first string
    stringLabelBoundsCache[0].prepare(3) //3 modules in string

    stringLabelBoundsCache[0].x(0, 17.1)
    stringLabelBoundsCache[0].y(0, 17.2)
    stringLabelBoundsCache[0].width(0, 4.2)
    stringLabelBoundsCache[0].height(0, 5.2)
    //...


    //iterate over second string
    stringLabelBoundsCache[1].prepare(5) //5 modules in string
    stringLabelBoundsCache[1].x(0, 18.1)
    stringLabelBoundsCache[1].y(0, 18.2)
    //...


    //
    //
    // PAINT
    //
    //

    stringLabelBoundsCache.fastForEach { boundsCacheForSingleString ->
      boundsCacheForSingleString.fastForEachIndexed {
          index: Int,
          x: @Window Double,
          y: @Window Double,
          width: @MayBeNegative @Zoomed Double,
          height: @MayBeNegative @Zoomed Double,
        ->


      }


    }

  }
}
