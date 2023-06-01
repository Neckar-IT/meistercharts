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
package com.meistercharts.algorithms

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.tile.SubIndex
import com.meistercharts.algorithms.tile.TileIndex
import org.junit.jupiter.api.Test

class InternalCalculationsTest {
  @Test
  fun testcalculateTileOriginXMax() {
    assertThat(InternalCalculations.calculateTileOriginX(TileIndex.Origin, 200.0)).isEqualTo(0.0)
    assertThat(InternalCalculations.calculateTileOriginX(TileIndex.Min, 200.0)).isEqualTo(-4.294967296E14)
    assertThat(InternalCalculations.calculateTileOriginX(TileIndex.Max, 200.0)).isEqualTo(4.294967295998E14)
  }

  @Test
  fun testcalculateTileOriginX() {
    assertThat(InternalCalculations.calculateTileOriginX(TileIndex(0, 0, 0, 0), 100.0)).isEqualTo(0.0)
    assertThat(InternalCalculations.calculateTileOriginX(TileIndex(0, 1, 0, 0), 100.0)).isEqualTo(100.0)
    assertThat(InternalCalculations.calculateTileOriginX(TileIndex(0, 7, 0, 0), 100.0)).isEqualTo(700.0)
    assertThat(InternalCalculations.calculateTileOriginX(TileIndex(44, 7, 0, 0), 100.0)).isEqualTo(4_400_700.0)
  }

  @Test
  fun testSubIndexBug() {
    assertThat(InternalCalculations.calculateMainTileIndexX(-100.0, 200.0)).isEqualTo(-1)
    assertThat(InternalCalculations.calculateSubTileIndexX(-100.0, 200.0)).isEqualTo(SubIndex.Max.value)
    assertThat(InternalCalculations.calculateSubTileIndexX(-199.0, 200.0)).isEqualTo(SubIndex.Max.value)
    assertThat(InternalCalculations.calculateSubTileIndexX(-200.0, 200.0)).isEqualTo(SubIndex.Max.value)
    assertThat(InternalCalculations.calculateSubTileIndexX(-201.0, 200.0)).isEqualTo(SubIndex.Max.value - 1)
  }

  @Test
  fun testMainTileIndexX() {
    assertThat(InternalCalculations.calculateMainTileIndexX(0.0, 100.0)).isEqualTo(0)
    assertThat(InternalCalculations.calculateMainTileIndexX(500.0, 100.0)).isEqualTo(0)

    assertThat(InternalCalculations.calculateMainTileIndexX(100.0 * TileIndex.SubIndexFactor - 1, 100.0)).isEqualTo(0)
    assertThat(InternalCalculations.calculateMainTileIndexX(100.0 * TileIndex.SubIndexFactor, 100.0)).isEqualTo(1)
    assertThat(InternalCalculations.calculateMainTileIndexX(100.0 * TileIndex.SubIndexFactor + 1, 100.0)).isEqualTo(1)
  }

  @Test
  fun testMainTileIndexY() {
    assertThat(InternalCalculations.calculateMainTileIndexY(0.0, 100.0)).isEqualTo(0)
    assertThat(InternalCalculations.calculateMainTileIndexY(500.0, 100.0)).isEqualTo(0)
    assertThat(InternalCalculations.calculateMainTileIndexY(100.0 * TileIndex.SubIndexFactor - 1, 100.0)).isEqualTo(0)
    assertThat(InternalCalculations.calculateMainTileIndexY(100.0 * TileIndex.SubIndexFactor, 100.0)).isEqualTo(1)
    assertThat(InternalCalculations.calculateMainTileIndexY(100.0 * TileIndex.SubIndexFactor + 1, 100.0)).isEqualTo(1)
  }

  @Test
  fun testSubTileIndexX() {
    assertThat(InternalCalculations.calculateSubTileIndexX(0.0, 100.0)).isEqualTo(0)
    assertThat(InternalCalculations.calculateSubTileIndexX(9.99, 100.0)).isEqualTo(0)
    assertThat(InternalCalculations.calculateSubTileIndexX(99.99, 100.0)).isEqualTo(0)
    assertThat(InternalCalculations.calculateSubTileIndexX(100.0, 100.0)).isEqualTo(1)

    assertThat(InternalCalculations.calculateSubTileIndexX(101.0, 100.0)).isEqualTo(1)
    assertThat(InternalCalculations.calculateSubTileIndexX(-1.0, 100.0)).isEqualTo(999)
    assertThat(InternalCalculations.calculateSubTileIndexX(-100.0, 100.0)).isEqualTo(999)
    assertThat(InternalCalculations.calculateSubTileIndexX(-100.1, 100.0)).isEqualTo(998)

    assertThat(InternalCalculations.calculateSubTileIndexX(500.0, 100.0)).isEqualTo(5)
  }

  @Test
  fun testSubTileIndexY() {
    assertThat(InternalCalculations.calculateSubTileIndexY(0.0, 100.0)).isEqualTo(0)
    assertThat(InternalCalculations.calculateSubTileIndexY(9.99, 100.0)).isEqualTo(0)
    assertThat(InternalCalculations.calculateSubTileIndexY(99.99, 100.0)).isEqualTo(0)
    assertThat(InternalCalculations.calculateSubTileIndexY(100.0, 100.0)).isEqualTo(1)

    assertThat(InternalCalculations.calculateSubTileIndexY(101.0, 100.0)).isEqualTo(1)
    assertThat(InternalCalculations.calculateSubTileIndexY(-1.0, 100.0)).isEqualTo(999)
    assertThat(InternalCalculations.calculateSubTileIndexY(-100.0, 100.0)).isEqualTo(999)
    assertThat(InternalCalculations.calculateSubTileIndexY(-100.1, 100.0)).isEqualTo(998)

    assertThat(InternalCalculations.calculateSubTileIndexY(500.0, 100.0)).isEqualTo(5)
  }
}
