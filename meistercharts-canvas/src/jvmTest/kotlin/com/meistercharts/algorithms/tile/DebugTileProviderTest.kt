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
package com.meistercharts.algorithms.tile

import assertk.assertThat
import assertk.assertions.isSameAs
import com.meistercharts.charts.ChartId
import com.meistercharts.model.Zoom
import com.meistercharts.tile.TileIndex
import it.neckar.geometry.Size
import org.junit.jupiter.api.Test

class DebugTileProviderTest {
  @Test
  fun `tile identifier returns the requested identifier`() {
    val provider = DebugTileProvider(Size.PX_120)
    val identifier = TileIdentifier(ChartId(17), TileIndex.of(7, 8), Zoom.default)

    val tile = provider.getTile(identifier)

    //Reading identifier must return the requested one - a self-referencing getter would StackOverflow.
    assertThat(tile!!.identifier).isSameAs(identifier)
  }
}
