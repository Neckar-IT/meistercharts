package com.meistercharts.resources.svg

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DefaultLayerSupport
import com.meistercharts.canvas.MockCanvas
import com.meistercharts.canvas.mock.MockCanvasRenderingContext
import com.meistercharts.canvas.PaintingLoopIndex
import com.meistercharts.canvas.mock.MockLayerPaintingContext
import com.meistercharts.model.Direction
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import com.meistercharts.resources.Icons
import org.junit.jupiter.api.Test

class PathPaintableProviderTest {
  @Test
  fun testIt() {
    val paintingContext = MockLayerPaintingContext()

    assertThat(Icons.error(Size.PX_24, alignment = Direction.TopLeft).boundingBox(paintingContext)).isEqualTo(Rectangle(0.0, 0.0, 24.0, 24.0))
    assertThat(Icons.error(Size.PX_120, alignment = Direction.TopLeft).boundingBox(paintingContext)).isEqualTo(Rectangle(0.0, 0.0, 120.0, 120.0))

    assertThat(Icons.error(Size.PX_24, alignment = Direction.Center).boundingBox(paintingContext)).isEqualTo(Rectangle(-12.0, -12.0, 24.0, 24.0))
    assertThat(Icons.error(Size.PX_120, alignment = Direction.Center).boundingBox(paintingContext)).isEqualTo(Rectangle(-60.0, -60.0, 120.0, 120.0))
  }
}
