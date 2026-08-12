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
package com.meistercharts.algorithms.layers.legend

import assertk.*
import assertk.assertions.*
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.mock.MockLayerPaintingContext
import it.neckar.geometry.Rectangle
import it.neckar.open.provider.SizedProvider1
import org.junit.jupiter.api.Test

class VerticallyStackedLabelsPaintableTest {
  @Test
  fun `an empty labels provider results in an empty bounding box`() {
    val paintable = VerticallyStackedLabelsPaintable(labels = labels(0))

    assertThat(paintable.boundingBox(MockLayerPaintingContext())).isEqualTo(Rectangle.zero)
  }

  @Test
  fun `every label adds a row plus the gap between the rows`() {
    val paintingContext = MockLayerPaintingContext()

    //The mock font metrics result in a line height of 19.0, the default entries gap is 5.0
    assertThat(VerticallyStackedLabelsPaintable(labels = labels(1)).boundingBox(paintingContext).getHeight()).isEqualTo(19.0)
    assertThat(VerticallyStackedLabelsPaintable(labels = labels(2)).boundingBox(paintingContext).getHeight()).isEqualTo(19.0 * 2 + 5.0)
  }

  private fun labels(count: Int): SizedProvider1<String, ChartSupport> {
    return SizedProvider1.of(count) { index, _: ChartSupport -> "Text @ $index" }
  }
}
