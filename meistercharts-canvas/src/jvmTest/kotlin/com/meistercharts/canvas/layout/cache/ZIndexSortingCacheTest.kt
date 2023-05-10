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

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.ZIndex
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.kotlin.lang.fastFor
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class ZIndexSortingCacheTest {
  @Disabled
  @Test
  fun testMemory() {
    val zIndices = doubleArrayOf(7.0, 2.0, 3.0, 1.0)
    val cache = ZIndexSortingCache()
    cache.prepare(4)

    zIndices.fastForEachIndexed { index, value ->
      cache[index].index = index
      cache[index].zIndex = ZIndex(value)
    }

    assertThat(cache.size).isEqualTo(4)
    assertThat(cache[0].index).isEqualTo(0)
    assertThat(cache[0].zIndex).isEqualToZIndex(7.0)

    Thread.sleep(10_000)

    measureTimeMillis {
      1_000_000_000.fastFor {
        cache.sortByZIndex() //507 ms
        //cache.values.sortWith(ZIndexSortingCache.Entry.byZIndex) //138
      }
    }.also {
      println("Took $it ms")
    }
  }

  @Test
  fun testJvm() {
    val zIndices = doubleArrayOf(7.0, 2.0, 3.0, 1.0)

    val cache = ZIndexSortingCache()

    assertThat(cache.size).isEqualTo(0)

    assertThat(cache.size).isEqualTo(0)
    cache.prepare(4)

    zIndices.fastForEachIndexed { index, value ->
      cache[index].index = index
      cache[index].zIndex = ZIndex(value)
    }

    assertThat(cache.size).isEqualTo(4)
    assertThat(cache[0].index).isEqualTo(0)
    assertThat(cache[0].zIndex).isEqualToZIndex(7.0)

    cache.sortByZIndex()

    assertThat(cache.size).isEqualTo(4)
    assertThat(cache[0]).isEqualTo(3, 1.0)
    assertThat(cache[1]).isEqualTo(1, 2.0)
    assertThat(cache[2]).isEqualTo(2, 3.0)
    assertThat(cache[3]).isEqualTo(0, 7.0)

    cache.reset()
    assertThat(cache.size).isEqualTo(4)
    assertThat(cache[0]).isEqualTo(-1, 0.0)
    assertThat(cache[1]).isEqualTo(-1, 0.0)
    assertThat(cache[2]).isEqualTo(-1, 0.0)
    assertThat(cache[3]).isEqualTo(-1, 0.0)
  }
}

fun Assert<ZIndex>.isEqualToZIndex(expectedValue: Double): Unit = given {
  if (it.value == expectedValue) {
    return
  }

  assertThat(it.toString()).isEqualTo(ZIndex(expectedValue).toString())
}

fun Assert<ZIndexSortingCache.Entry>.isEqualTo(expectedIndex: Int, expectedZIndex: Double): Unit = given {
  if (it.index == expectedIndex && it.zIndex.value == expectedZIndex) {
    return
  }

  assertThat(it.index, "Index").isEqualTo(expectedIndex)
  assertThat(it.zIndex, "Z-Index").isEqualTo(ZIndex(expectedZIndex).toString())
}
