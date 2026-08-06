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
package com.meistercharts.canvas.layout.buffer

import assertk.*
import assertk.assertions.*
import com.meistercharts.loop.PaintingLoopIndex
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import it.neckar.open.test.utils.isNaN
import org.junit.jupiter.api.Test

class MappedLayoutBufferTest {
  @Test
  fun testApi() {
    val buffer = MappedLayoutBuffer<ReferenceEntryDataSeriesIndex, MyLayoutVariable> {
      MyLayoutVariable()
    }

    buffer.clear()
    assertThat(buffer.values).hasSize(0)

    val index7 = ReferenceEntryDataSeriesIndex(7)
    val index8 = ReferenceEntryDataSeriesIndex(8)

    assertThat(buffer.get(index7).xLocation).isNaN()
    assertThat(buffer.values).hasSize(1)
    assertThat(buffer.get(index8).xLocation).isNaN()
    assertThat(buffer.values).hasSize(2)

    buffer.get(index7).xLocation = 7.7
    buffer.get(index8).xLocation = 8.8

    assertThat(buffer.get(index7).xLocation).isEqualTo(7.7)
    assertThat(buffer.get(index8).xLocation).isEqualTo(8.8)

    buffer.clear()

    assertThat(buffer.get(index7).xLocation).isNaN()
    assertThat(buffer.get(index8).xLocation).isNaN()
  }

  @Test
  fun testSimulatePaintSameIndices() {
    var callCount = 0

    val buffer = MappedLayoutBuffer<ReferenceEntryDataSeriesIndex, MyLayoutVariable> {
      callCount++
      MyLayoutVariable()
    }

    assertThat(callCount).isEqualTo(0)

    val index7 = ReferenceEntryDataSeriesIndex(7)
    val index8 = ReferenceEntryDataSeriesIndex(8)

    //
    //First paint
    //
    buffer.resetIfNewLoopIndex(PaintingLoopIndex(0))

    //simulate layout
    buffer.get(index7).xLocation = 7.7
    buffer.get(index8).xLocation = 8.8
    assertThat(callCount).isEqualTo(2)

    //simulate paint
    assertThat(buffer.get(index7).xLocation).isEqualTo(7.7)
    assertThat(buffer.get(index8).xLocation).isEqualTo(8.8)


    //Second repaint
    buffer.resetIfNewLoopIndex(PaintingLoopIndex(1))
    assertThat(buffer.get(index7).xLocation).isNaN()
    assertThat(buffer.get(index8).xLocation).isNaN()

    assertThat(callCount).isEqualTo(2)

    //simulate layout
    buffer.get(index7).xLocation = 7.7
    buffer.get(index8).xLocation = 8.8
    assertThat(callCount).isEqualTo(2)

    //simulate paint
    assertThat(buffer.get(index7).xLocation).isEqualTo(7.7)
    assertThat(buffer.get(index8).xLocation).isEqualTo(8.8)
  }

  @Test
  fun testTrimsStaleKeysAfterSustainedDecline() {
    var factoryCallCount = 0

    val buffer = MappedLayoutBuffer<ReferenceEntryDataSeriesIndex, MyLayoutVariable>(
      HighWaterMarkTrimmer(windowSize = 2, slackFactor = 0.0)
    ) {
      factoryCallCount++
      MyLayoutVariable()
    }

    val allKeys: List<ReferenceEntryDataSeriesIndex> = (1..5).map { ReferenceEntryDataSeriesIndex(it) }
    val activeKeys: List<ReferenceEntryDataSeriesIndex> = (1..2).map { ReferenceEntryDataSeriesIndex(it) }

    //Loop 0: five active keys fill the stock
    buffer.resetIfNewLoopIndex(PaintingLoopIndex(0))
    allKeys.forEach { buffer.get(it) }
    assertThat(buffer.objectsStock).hasSize(5)
    assertThat(factoryCallCount).isEqualTo(5)

    //Loop 1: demand drops to two keys - the window boundary here still records a peak of 5
    buffer.resetIfNewLoopIndex(PaintingLoopIndex(1))
    activeKeys.forEach { buffer.get(it) }
    assertThat(buffer.objectsStock).hasSize(5)

    //Loop 2: still two keys - a fresh window starts with peak 2
    buffer.resetIfNewLoopIndex(PaintingLoopIndex(2))
    activeKeys.forEach { buffer.get(it) }
    assertThat(buffer.objectsStock).hasSize(5)

    //Loop 3: window boundary with peak 2 - stale keys 3,4,5 are evicted, active keys survive
    buffer.resetIfNewLoopIndex(PaintingLoopIndex(3))
    assertThat(buffer.objectsStock).hasSize(2)
    assertThat(buffer.objectsStock.keys).containsOnly(*activeKeys.toTypedArray())
    //No key was recreated up to here: the stock keys were reused since loop 0
    assertThat(factoryCallCount).isEqualTo(5)

    //An evicted key is recreated (in reset state) on the next request; a surviving key is not
    assertThat(buffer.get(ReferenceEntryDataSeriesIndex(3)).xLocation).isNaN()
    assertThat(factoryCallCount).isEqualTo(6)
    assertThat(buffer.get(ReferenceEntryDataSeriesIndex(1)).xLocation).isNaN()
    assertThat(factoryCallCount).isEqualTo(6)
  }

  @Test
  fun testKeepsStockWhileDemandStaysHigh() {
    val buffer = MappedLayoutBuffer<ReferenceEntryDataSeriesIndex, MyLayoutVariable>(
      HighWaterMarkTrimmer(windowSize = 2, slackFactor = 0.0)
    ) {
      MyLayoutVariable()
    }

    val allKeys: List<ReferenceEntryDataSeriesIndex> = (1..5).map { ReferenceEntryDataSeriesIndex(it) }

    //All keys stay active across several window boundaries - nothing must be evicted
    (0..5).forEach { loopIndex ->
      buffer.resetIfNewLoopIndex(PaintingLoopIndex(loopIndex))
      allKeys.forEach { buffer.get(it) }
      assertThat(buffer.objectsStock).hasSize(5)
    }
  }
}

class MyLayoutVariable : LayoutVariable {
  var xLocation: Double = Double.NaN

  override fun reset() {
    xLocation = Double.NaN
  }
}
