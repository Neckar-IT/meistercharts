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
package com.meistercharts.algorithms.tile

import assertk.*
import assertk.assertions.*
import com.meistercharts.tile.TileIndex
import com.meistercharts.tile.TileIndex.Companion.SubIndexFactor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TileIndexTest {
  @Test
  fun testAsInt() {
    assertThat(TileIndex(1, 2, 3, 4).xAsInt()).isEqualTo(1002)
    assertThat(TileIndex(1, 2, 3, 4).yAsInt()).isEqualTo(3004)
    assertThat(TileIndex.Origin.xAsInt()).isEqualTo(0)
  }

  @Test
  fun testOf() {
    assertThat(TileIndex.of(1002, 3004)).isEqualTo(TileIndex(1, 2, 3, 4))
  }

  @Test
  fun testFromSub() {
    assertThat(TileIndex.of(0, 0)).isEqualTo(TileIndex.of(0, 0, 0, 0))
    assertThat(TileIndex.of(7, 8)).isEqualTo(TileIndex.of(0, 7, 0, 8))

    assertThat(TileIndex.of(199, 207)).isEqualTo(TileIndex.of(0, 199, 0, 207))
    assertThat(TileIndex.of(2 * SubIndexFactor, SubIndexFactor + 4)).isEqualTo(TileIndex.of(2, 0, 1, 4))
  }

  @Test
  fun testComparatorByRow() {
    val indices = listOf(
      TileIndex.of(1, 0, 3, 0),
      TileIndex.of(1, 0, 3, 2),
      TileIndex.of(1, 0, 3, 3),
      TileIndex.of(1, 1, 3, 3),
    )

    assertThat(indices.sortedWith(TileIndex.compareByRow)).containsExactly(indices[0], indices[1], indices[2], indices[3])
  }

  @Test
  fun testComparatorByRow2() {
    val indices = listOf(
      TileIndex.of(1, 0, 3, 4),
      TileIndex.of(1, 0, 3, 2),
      TileIndex.of(1, 0, 3, 3),
      TileIndex.of(1, 1, 3, 3),
    )

    assertThat(indices.sortedWith(TileIndex.compareByRow)).containsExactly(indices[1], indices[2], indices[3], indices[0])
  }

  @Test
  fun `test it`() {
    val index = TileIndex.of(1, 500, 2, 500)
    assertThat(index.mainX.value).isEqualTo(1)
    assertThat(index.subX.value).isEqualTo(500)
    assertThat(index.mainY.value).isEqualTo(2)
    assertThat(index.subY.value).isEqualTo(500)
  }

  @Test
  fun testBelow() {
    assertThat(TileIndex.of(1, 500, 2, 500).below()).isEqualTo(TileIndex.of(1, 500, 2, 501))
    assertThat(TileIndex.of(1, 500, 2, SubIndexFactor - 1).below()).isEqualTo(TileIndex.of(1, 500, 3, 0))
    assertThat(TileIndex.of(1, 500, 2, 300).below()).isEqualTo(TileIndex.of(1, 500, 2, 301))
    assertThat(TileIndex.of(1, 500, 2, 999).below()).isEqualTo(TileIndex.of(1, 500, 3, 0))
  }

  @Test
  fun `test isLeftOf method`() {
    val index1 = TileIndex.of(1, 500, 2, 500)
    val index2 = TileIndex.of(2, 500, 2, 500)
    val index3 = TileIndex.of(1, 400, 2, 500)
    val index4 = TileIndex.of(1, 500, 2, 500)

    assertThat(index1.isLeftOf(index2)).isTrue()
    assertThat(index2.isLeftOf(index1)).isFalse()
    assertThat(index1.isLeftOf(index3)).isFalse()
    assertThat(index1.isLeftOf(index4)).isFalse()
  }

  @Test
  fun `test isAbove method`() {
    val index1 = TileIndex.of(2, 500, 1, 500)
    val index2 = TileIndex.of(2, 500, 2, 500)
    val index3 = TileIndex.of(2, 500, 1, 400)
    val index4 = TileIndex.of(2, 500, 1, 500)

    assertThat(index1.isAbove(index2)).isTrue()
    assertThat(index2.isAbove(index1)).isFalse()
    assertThat(index1.isAbove(index3)).isFalse()
    assertThat(index1.isAbove(index4)).isFalse()
  }

  @Test
  fun testMoreAbove() {
    assertThat(TileIndex.of(1, 500, 2, 300).above()).isEqualTo(TileIndex.of(1, 500, 2, 299))
  }

  @Test
  fun testNextX() {
    val index = TileIndex.of(5, 900, 3, 200)
    val nextIndex = index.nextX()
    assertThat(nextIndex).isEqualTo(TileIndex.of(5, 901, 3, 200))

    val indexAtMaxSubX = TileIndex.of(5, TileIndex.SubIndexFactor - 1, 3, 200)
    val nextIndexFromMaxSubX = indexAtMaxSubX.nextX()
    assertThat(nextIndexFromMaxSubX).isEqualTo(TileIndex.of(6, 0, 3, 200))
  }

  @Test
  fun testPreviousX() {
    val index = TileIndex.of(5, 900, 3, 200)
    val previousIndex = index.previousX()
    assertThat(previousIndex).isEqualTo(TileIndex.of(5, 899, 3, 200))

    val indexAtMinSubX = TileIndex.of(5, 0, 3, 200)
    val previousIndexFromMinSubX = indexAtMinSubX.previousX()
    assertThat(previousIndexFromMinSubX).isEqualTo(TileIndex.of(4, TileIndex.SubIndexFactor - 1, 3, 200))
  }

  @Test
  fun testNextY() {
    val index = TileIndex.of(5, 900, 3, 200)
    val nextIndex = index.nextY()
    assertThat(nextIndex).isEqualTo(TileIndex.of(5, 900, 3, 201))

    val indexAtMaxSubY = TileIndex.of(5, 900, 3, TileIndex.SubIndexFactor - 1)
    val nextIndexFromMaxSubY = indexAtMaxSubY.nextY()
    assertThat(nextIndexFromMaxSubY).isEqualTo(TileIndex.of(5, 900, 4, 0))
  }

  @Test
  fun testPreviousY() {
    val index = TileIndex.of(5, 900, 3, 200)
    val previousIndex = index.previousY()
    assertThat(previousIndex).isEqualTo(TileIndex.of(5, 900, 3, 199))

    val indexAtMinSubY = TileIndex.of(5, 900, 3, 0)
    val previousIndexFromMinSubY = indexAtMinSubY.previousY()
    assertThat(previousIndexFromMinSubY).isEqualTo(TileIndex.of(5, 900, 2, TileIndex.SubIndexFactor - 1))
  }

  @Test
  fun testIsWithin() {
    val topLeft = TileIndex.of(2, 500, 2, 500)
    val bottomRight = TileIndex.of(4, 600, 4, 600)

    // Test a TileIndex that is within the defined rectangle
    val withinIndex = TileIndex.of(3, 600, 3, 600)
    assertThat(withinIndex.isWithin(topLeft, bottomRight)).isTrue()

    // Test a TileIndex that is outside the defined rectangle
    val outsideIndex = TileIndex.of(5, 600, 5, 600)
    assertThat(outsideIndex.isWithin(topLeft, bottomRight)).isFalse()

    // Test a TileIndex that is on the boundary of the rectangle
    val boundaryIndex = TileIndex.of(4, 600, 4, 600)
    assertThat(boundaryIndex.isWithin(topLeft, bottomRight)).isTrue()

    // Test a TileIndex that is exactly the same as topLeft
    val topLeftIndex = TileIndex.of(2, 500, 2, 500)
    assertThat(topLeftIndex.isWithin(topLeft, bottomRight)).isTrue()

    // Test a TileIndex that is exactly the same as bottomRight but with sub-index one more than bottomRight
    val beyondBottomRightIndex = TileIndex.of(4, 601, 4, 601)
    assertThat(beyondBottomRightIndex.isWithin(topLeft, bottomRight)).isFalse()
  }

  @Test
  fun testIsWithinException() {
    val index = TileIndex.of(5, 500, 3, 500)

    val start = TileIndex.of(5, 400, 3, 400)
    val end = TileIndex.of(5, 600, 3, 600)
    assertThat(index.isWithin(start, end)).isTrue()

    val outsideStart = TileIndex.of(5, 600, 3, 600)
    val outsideEnd = TileIndex.of(6, 0, 4, 0)
    assertThat(index.isWithin(outsideStart, outsideEnd)).isFalse()
  }

  @Test
  fun testIsWithinThrowsOnInvalidArguments() {
    assertThrows<IllegalArgumentException> {
      TileIndex.of(5, 500, 3, 500).isWithin(TileIndex.of(5, 600, 3, 600), TileIndex.of(5, 400, 3, 400))
    }
  }


  @Test
  fun `test isWithin method`() {
    val index1 = TileIndex.of(1, 500, 1, 500)
    val index2 = TileIndex.of(2, 500, 2, 500)
    val index3 = TileIndex.of(3, 500, 3, 500)

    // Inside the rectangle
    assertThat(index2.isWithin(index1, index3)).isTrue()

    // On the edge of the rectangle
    assertThat(index1.isWithin(index1, index3)).isTrue()

    // Outside the rectangle
    assertThat(index3.isWithin(index1, index2)).isFalse()
  }

  @Test
  fun `test isWithin method with sub-indices`() {
    val index1 = TileIndex.of(1, 200, 1, 200)
    val index2 = TileIndex.of(1, 500, 1, 500)
    val index3 = TileIndex.of(1, 800, 1, 800)

    // Inside the rectangle
    assertThat(index2.isWithin(index1, index3)).isTrue()

    // On the edge of the rectangle
    assertThat(index1.isWithin(index1, index3)).isTrue()

    // Outside the rectangle
    assertThat(index3.isWithin(index1, index2)).isFalse()
  }

  @Test
  fun `test isWithin method with invalid input`() {
    assertThrows<IllegalArgumentException> {
      TileIndex.of(2, 500, 2, 500).isWithin(TileIndex.of(2, 500, 2, 500), TileIndex.of(1, 500, 1, 500))
    }
  }

  @Test
  fun `isRightOf returns true when TileIndex is to the right`() {
    assertThat(TileIndex.of(5, 500, 3, 300).isRightOf(TileIndex.of(5, 400, 3, 300))).isTrue()
    assertThat(TileIndex.of(2, 500, 1, 300).isRightOf(TileIndex.of(1, 800, 1, 300))).isTrue()
    assertThat(TileIndex.of(5, 400, 3, 300).isRightOf(TileIndex.of(5, 500, 3, 300))).isFalse()
    assertThat(TileIndex.of(1, 800, 1, 300).isRightOf(TileIndex.of(2, 500, 1, 300))).isFalse()
    assertThat(TileIndex.of(1, 500, 1, 300).isRightOf(TileIndex.of(1, 500, 2, 300))).isFalse()
  }

  @Test
  fun `isBelow`() {
    assertThat(TileIndex.of(5, 500, 4, 400).isBelow(TileIndex.of(5, 500, 3, 300))).isTrue()
    assertThat(TileIndex.of(5, 500, 3, 300).isBelow(TileIndex.of(5, 500, 4, 400))).isFalse()
    assertThat(TileIndex.of(1, 500, 2, 300).isBelow(TileIndex.of(1, 500, 1, 800))).isTrue()
    assertThat(TileIndex.of(1, 500, 1, 300).isBelow(TileIndex.of(1, 500, 2, 300))).isFalse()
    assertThat(TileIndex.of(1, 500, 1, 300).isBelow(TileIndex.of(2, 500, 1, 300))).isFalse()
  }

  @Test
  fun `iterateOverTileIndices correctly iterates over the indices`() {
    val start = TileIndex.of(1, SubIndexFactor - 2, 3, 4)
    val end = TileIndex.of(2, 2, 3, 8)

    var iterations = 0

    TileIndex.iterateOverTileIndices(start, end) { mainX, subX, mainY, subY ->
      println("$mainX.$subX  / $mainY.$subY")
      iterations++
    }

    // 2 mainX values * 3 subX values * 2 mainY values * 3 subY values
    val expectedIterations = 25

    assertThat(iterations).isEqualTo(expectedIterations)
  }

  @Test
  fun `xComparator correctly sorts TileIndices`() {
    val indices = listOf(
      TileIndex.of(2, 500, 1, 300),
      TileIndex.of(1, 800, 1, 300),
      TileIndex.of(2, 300, 1, 300),
      TileIndex.of(1, 500, 2, 300)
    )

    val sortedIndices = indices.sortedWith(TileIndex.compareByX)

    assertThat(sortedIndices).containsExactly(
      TileIndex.of(1, 500, 2, 300),
      TileIndex.of(1, 800, 1, 300),
      TileIndex.of(2, 300, 1, 300),
      TileIndex.of(2, 500, 1, 300)
    )
  }

  @Test
  fun `yComparator correctly sorts TileIndices`() {
    val indices = listOf(
      TileIndex.of(1, 500, 2, 300),
      TileIndex.of(1, 800, 1, 300),
      TileIndex.of(2, 300, 1, 800),
      TileIndex.of(1, 500, 1, 300)
    )

    val sortedIndices = indices.sortedWith(TileIndex.compareByY)

    assertThat(sortedIndices).containsExactly(
      TileIndex.of(1, 800, 1, 300),
      TileIndex.of(1, 500, 1, 300),
      TileIndex.of(2, 300, 1, 800),
      TileIndex.of(1, 500, 2, 300)
    )
  }

  @Test
  fun `leftOf returns correct TileIndex`() {
    assertThat(TileIndex.of(1, 500, 2, 300).leftOf()).isEqualTo(TileIndex.of(1, 499, 2, 300))
    assertThat(TileIndex.of(2, 0, 2, 300).leftOf()).isEqualTo(TileIndex.of(1, 999, 2, 300))
  }

  @Test
  fun `rightOf returns correct TileIndex`() {
    assertThat(TileIndex.of(1, 500, 2, 300).rightOf()).isEqualTo(TileIndex.of(1, 501, 2, 300))
    assertThat(TileIndex.of(1, 999, 2, 300).rightOf()).isEqualTo(TileIndex.of(2, 0, 2, 300))
  }
}
