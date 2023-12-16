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
import it.neckar.geometry.Size
import it.neckar.open.test.utils.isNaN
import org.junit.jupiter.api.Test

internal class SizeTest {
  @Test
  fun testResizeAspectRatio() {
    assertThat(Size(100.0, 100.0).scaleToMax(100.0, 100.0)).isEqualTo(Size(100.0, 100.0))
    assertThat(Size(100.0, 100.0).scaleToMax(50.0, 100.0)).isEqualTo(Size(50.0, 50.0))
    assertThat(Size(100.0, 100.0).scaleToMax(499.0, 78.0)).isEqualTo(Size(78.0, 78.0))

    assertThat(Size(120.0, 100.0).scaleToMax(120.0, 100.0)).isEqualTo(Size(120.0, 100.0))
    assertThat(Size(120.0, 100.0).scaleToMax(60.0, 100.0)).isEqualTo(Size(60.0, 50.0))
    assertThat(Size(120.0, 100.0).scaleToMax(499.0, 75.0)).isEqualTo(Size(90.0, 75.0))
  }

  @Test
  fun testContainWithAspect() {
    assertThat(Size(100.0, 100.0).containWithAspectRatio(1.0)).isEqualTo(Size(100.0, 100.0))
    assertThat(Size(100.0, 100.0).containWithAspectRatio(0.5)).isEqualTo(Size(100.0, 200.0))
    assertThat(Size(100.0, 100.0).containWithAspectRatio(2.0)).isEqualTo(Size(200.0, 100.0))

    assertThat(Size(100.0, 200.0).containWithAspectRatio(1.0)).isEqualTo(Size(200.0, 200.0))
    assertThat(Size(100.0, 200.0).containWithAspectRatio(0.5)).isEqualTo(Size(100.0, 200.0))
    assertThat(Size(100.0, 200.0).containWithAspectRatio(0.25)).isEqualTo(Size(100.0, 400.0))

    assertThat(Size(100.0, 200.0).containWithAspectRatio(2.0)).isEqualTo(Size(400.0, 200.0))
  }

  @Test
  internal fun testFitWithAspec() {
    assertThat(Size(100.0, 100.0).fitWithAspectRatio(1.0)).isEqualTo(Size(100.0, 100.0))
    assertThat(Size(100.0, 100.0).fitWithAspectRatio(0.5)).isEqualTo(Size(50.0, 100.0))
    assertThat(Size(100.0, 100.0).fitWithAspectRatio(2.0)).isEqualTo(Size(100.0, 50.0))

    assertThat(Size(100.0, 200.0).fitWithAspectRatio(1.0)).isEqualTo(Size(100.0, 100.0))
    assertThat(Size(100.0, 200.0).fitWithAspectRatio(0.5)).isEqualTo(Size(100.0, 200.0))
    assertThat(Size(100.0, 200.0).fitWithAspectRatio(0.25)).isEqualTo(Size(50.0, 200.0))

    assertThat(Size(100.0, 200.0).fitWithAspectRatio(2.0)).isEqualTo(Size(100.0, 50.0))
  }

  @Test
  internal fun aspectRatio() {
    assertThat(Size(2.0, 1.0).aspectRatio).isEqualTo(2.0)
    assertThat(Size(1.0, 1.0).aspectRatio).isEqualTo(1.0)
    assertThat(Size(100.0, 150.0).aspectRatio).isEqualTo(1 / 1.5)
    assertThat(Size(20.0, 0.0).aspectRatio).isEqualTo(Double.POSITIVE_INFINITY)
    assertThat(Size(-20.0, 0.0).aspectRatio).isEqualTo(Double.NEGATIVE_INFINITY)
  }

  @Test
  internal fun testComparisons() {
    assertThat(Size(2.0, 1.0).bothSmallerThan(Size(0.0, 0.0))).isFalse()
    assertThat(Size(2.0, 1.0).bothSmallerThan(Size(2.0, 1.0))).isFalse()
    assertThat(Size(2.0, 1.0).bothSmallerThan(Size(2.1, 1.0))).isFalse()
    assertThat(Size(2.0, 1.0).bothSmallerThan(Size(2.0, 1.1))).isFalse()
    assertThat(Size(2.0, 1.0).bothSmallerThan(Size(2.1, 1.1))).isTrue()
  }

  @Test
  internal fun testComparisons3() {
    assertThat(Size(2.0, 1.0).bothLargerThan(Size(0.0, 0.0))).isTrue()
    assertThat(Size(2.0, 1.0).bothLargerThan(Size(2.0, 1.0))).isFalse()
    assertThat(Size(2.0, 1.0).bothLargerThan(Size(2.1, 1.0))).isFalse()
    assertThat(Size(2.0, 1.0).bothLargerThan(Size(2.0, 1.1))).isFalse()
    assertThat(Size(2.0, 1.0).bothLargerThan(Size(2.1, 1.1))).isFalse()
  }

  @Test
  internal fun testComparisons2() {
    assertThat(Size(2.0, 1.0).atLeastOneSmallerThan(Size(0.0, 0.0))).isFalse()
    assertThat(Size(2.0, 1.0).atLeastOneSmallerThan(Size(2.0, 1.0))).isFalse()
    assertThat(Size(2.0, 1.0).atLeastOneSmallerThan(Size(2.1, 1.0))).isTrue()
    assertThat(Size(2.0, 1.0).atLeastOneSmallerThan(Size(2.0, 1.1))).isTrue()
    assertThat(Size(2.0, 1.0).atLeastOneSmallerThan(Size(2.1, 1.1))).isTrue()
  }

  @Test
  internal fun plusDelta() {
    assertThat(Size(2.0, 1.0).plus(0.0, 0.0)).isEqualTo(Size(2.0, 1.0))
    assertThat(Size(2.0, 1.0).plus(1.0, 0.0)).isEqualTo(Size(3.0, 1.0))
    assertThat(Size(2.0, 1.0).plus(0.0, 1.0)).isEqualTo(Size(2.0, 2.0))
    assertThat(Size(2.0, 1.0).plus(1.0, 1.0)).isEqualTo(Size(3.0, 2.0))
    assertThat(Size(2.0, 1.0).plus(-1.3, -5.6)).isEqualTo(Size(0.7, -4.6))
  }

  @Test
  internal fun times() {
    assertThat(Size(234.234, 98.567).times(0.0, 0.0)).isEqualTo(Size(0.0, 0.0))
    assertThat(Size(234.234, 98.567).times(1.0, 1.0)).isEqualTo(Size(234.234, 98.567))
    assertThat(Size(2.0, 3.0).times(4.0, 5.0)).isEqualTo(Size(8.0, 15.0))
  }

  @Test
  internal fun withMax() {
    assertThat(Size(3.3, 4.5).withMax(4.0, 5.0)).isEqualTo(Size(3.3, 4.5))
    assertThat(Size(3.3, 4.5).withMax(4.0, 4.0)).isEqualTo(Size(3.3, 4.0))
    assertThat(Size(3.3, 4.5).withMax(3.0, 5.0)).isEqualTo(Size(3.0, 4.5))
    assertThat(Size(3.3, 4.5).withMax(3.0, 4.0)).isEqualTo(Size(3.0, 4.0))
  }

  @Test
  internal fun withMin() {
    assertThat(Size(3.3, 4.5).withMin(4.0, 5.0)).isEqualTo(Size(4.0, 5.0))
    assertThat(Size(3.3, 4.5).withMin(4.0, 4.0)).isEqualTo(Size(4.0, 4.5))
    assertThat(Size(3.3, 4.5).withMin(3.0, 5.0)).isEqualTo(Size(3.3, 5.0))
    assertThat(Size(3.3, 4.5).withMin(3.0, 4.0)).isEqualTo(Size(3.3, 4.5))
  }

  @Test
  internal fun withWidth() {
    assertThat(Size(3.5, -2.7).withWidth(1.0)).isEqualTo(Size(1.0, -2.7))
    assertThat(Size(3.5, -2.7).withWidth(-1.0)).isEqualTo(Size(-1.0, -2.7))
    assertThat(Size(3.5, -2.7).withWidth(Double.POSITIVE_INFINITY)).isEqualTo(Size(Double.POSITIVE_INFINITY, -2.7))
    assertThat(Size(3.5, -2.7).withWidth(Double.NEGATIVE_INFINITY)).isEqualTo(Size(Double.NEGATIVE_INFINITY, -2.7))
    assertThat(Size(3.5, -2.7).withWidth(Double.MAX_VALUE)).isEqualTo(Size(Double.MAX_VALUE, -2.7))
    assertThat(Size(3.5, -2.7).withWidth(Double.MIN_VALUE)).isEqualTo(Size(Double.MIN_VALUE, -2.7))
  }

  @Test
  internal fun withHeight() {
    assertThat(Size(3.5, -2.7).withHeight(1.0)).isEqualTo(Size(3.5, 1.0))
    assertThat(Size(3.5, -2.7).withHeight(-1.0)).isEqualTo(Size(3.5, -1.0))
    assertThat(Size(3.5, -2.7).withHeight(Double.POSITIVE_INFINITY)).isEqualTo(Size(3.5, Double.POSITIVE_INFINITY))
    assertThat(Size(3.5, -2.7).withHeight(Double.NEGATIVE_INFINITY)).isEqualTo(Size(3.5, Double.NEGATIVE_INFINITY))
    assertThat(Size(3.5, -2.7).withHeight(Double.MAX_VALUE)).isEqualTo(Size(3.5, Double.MAX_VALUE))
    assertThat(Size(3.5, -2.7).withHeight(Double.MIN_VALUE)).isEqualTo(Size(3.5, Double.MIN_VALUE))
  }

  @Test
  internal fun testNaN() {
    val base = Size.zero

    val added = base.plus(Double.NaN, Double.NaN)
    assertThat(added.width).isNaN()
    assertThat(added.height).isNaN()
  }

  @Test
  fun testScaleToWidth() {
    Size(100.0, 200.0).scaleToWidth(50.0).apply {
      assertThat(this.width).isEqualTo(50.0)
      assertThat(this.height).isEqualTo(100.0)
    }
    Size(0.0, 200.0).scaleToWidth(50.0).apply {
      assertThat(this.width).isNaN()
      assertThat(this.height).isNaN()
    }
    Size(-100.0, 200.0).scaleToWidth(50.0).apply {
      assertThat(this.width).isEqualTo(50.0)
      assertThat(this.height).isEqualTo(-100.0)
    }
    Size(-100.0, -200.0).scaleToWidth(50.0).apply {
      assertThat(this.width).isEqualTo(50.0)
      assertThat(this.height).isEqualTo(100.0)
    }
  }

  @Test
  fun testScaleToHeight() {
    Size(100.0, 200.0).scaleToHeight(50.0).apply {
      assertThat(this.width).isEqualTo(25.0)
      assertThat(this.height).isEqualTo(50.0)
    }
    Size(50.0, 0.0).scaleToHeight(50.0).apply {
      assertThat(this.width).isNaN()
      assertThat(this.height).isNaN()
    }
    Size(100.0, -200.0).scaleToHeight(50.0).apply {
      assertThat(this.width).isEqualTo(-25.0)
      assertThat(this.height).isEqualTo(50.0)
    }
    Size(-100.0, -200.0).scaleToHeight(50.0).apply {
      assertThat(this.width).isEqualTo(25.0)
      assertThat(this.height).isEqualTo(50.0)
    }
  }
}

