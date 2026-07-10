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
import org.junit.jupiter.api.Test

class HighWaterMarkTrimmerTest {
  @Test
  fun testNoTrimUntilWindowFull() {
    val trimmer = HighWaterMarkTrimmer(windowSize = 4, slackFactor = 0.5)

    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(0), demand = 2, currentCapacity = 100)).isNull()
    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(1), demand = 2, currentCapacity = 100)).isNull()
    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(2), demand = 2, currentCapacity = 100)).isNull()

    //4th loop is the window boundary: peak = 2, target = ceil(2 * 1.5) = 3
    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(3), demand = 2, currentCapacity = 100)).isEqualTo(3)
  }

  @Test
  fun testUsesWindowPeakNotLastDemand() {
    val trimmer = HighWaterMarkTrimmer(windowSize = 3, slackFactor = 0.0)

    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(0), demand = 10, currentCapacity = 100)).isNull()
    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(1), demand = 2, currentCapacity = 100)).isNull()

    //Boundary: peak = max(10, 2, 4) = 10, slack 0 -> target = 10
    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(2), demand = 4, currentCapacity = 100)).isEqualTo(10)
  }

  @Test
  fun testNoTrimWhenCapacityWithinTarget() {
    val trimmer = HighWaterMarkTrimmer(windowSize = 2, slackFactor = 0.5)

    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(0), demand = 8, currentCapacity = 12)).isNull()

    //Boundary: peak = 8, target = ceil(8 * 1.5) = 12, capacity 12 is not > 12 -> no trim
    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(1), demand = 8, currentCapacity = 12)).isNull()
  }

  @Test
  fun testSameLoopIndexIsIdempotent() {
    val trimmer = HighWaterMarkTrimmer(windowSize = 1, slackFactor = 0.0)

    //windowSize 1: every new loop is a boundary
    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(0), demand = 5, currentCapacity = 100)).isEqualTo(5)
    //Same loop again: no epoch advance, no double-count
    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(0), demand = 5, currentCapacity = 100)).isNull()
  }

  @Test
  fun testWindowPeakResetsAfterBoundary() {
    val trimmer = HighWaterMarkTrimmer(windowSize = 2, slackFactor = 0.0)

    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(0), demand = 10, currentCapacity = 100)).isNull()
    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(1), demand = 10, currentCapacity = 100)).isEqualTo(10)

    //New window: the old peak of 10 must not leak into it
    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(2), demand = 3, currentCapacity = 100)).isNull()
    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(3), demand = 3, currentCapacity = 100)).isEqualTo(3)
  }

  @Test
  fun testLoopIndexOverflowIsTreatedAsNewLoop() {
    val trimmer = HighWaterMarkTrimmer(windowSize = 1, slackFactor = 0.0)

    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(Int.MAX_VALUE), demand = 5, currentCapacity = 100)).isEqualTo(5)
    //Overflow from Int.MAX_VALUE to 0 is a different value, hence a new loop
    assertThat(trimmer.pollTrimTarget(PaintingLoopIndex(0), demand = 7, currentCapacity = 100)).isEqualTo(7)
  }

  @Test
  fun testRejectsInvalidConfiguration() {
    assertFailure { HighWaterMarkTrimmer(windowSize = 0) }.isInstanceOf(IllegalArgumentException::class)
    assertFailure { HighWaterMarkTrimmer(slackFactor = -0.1) }.isInstanceOf(IllegalArgumentException::class)
  }
}
