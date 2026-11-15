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
package com.meistercharts.demo.descriptors

import com.meistercharts.demo.configurable
import com.meistercharts.demo.configurableSeparate
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.color.Color
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.fill
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.resize.ResizeByHandlesLayer
import com.meistercharts.canvas.resize.ResizeHandler
import com.meistercharts.canvas.resizeHandlesSupport
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import com.meistercharts.geometry.DirectionBasedBasePointProvider
import it.neckar.geometry.Distance
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.geometry.VerticalAlignment
import com.meistercharts.style.BoxStyle
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.unit.other.px

/**
 *
 */
class ResizeByHandlesLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("8e4bbe3d-47f2-425d-b5fe-12559cc3cda6")
  override val name: String = "Resize by Handles Layer"
  override val category: DemoCategory = DemoCategory.Interaction
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val resizeByHandlesLayer = ResizeByHandlesLayer()

          val contentLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var contentLocation = Coordinates(450.0, 350.0)
            var contentSize = Size(150.0, 200.0)

            /**
             * The geometry the running resize gesture started from - its events carry the total distance since then.
             */
            var resizeAnchor = Rectangle(contentLocation, contentSize)

            var color = Color.blue

            override fun initialize(paintingContext: LayerPaintingContext) {
              super.initialize(paintingContext)

              paintingContext.chartSupport.resizeHandlesSupport.onResize(this, object : ResizeHandler {
                override fun armed(handleDirection: Direction) {
                  color = Color.red
                }

                override fun disarmed() {
                  color = Color.blue
                }

                override fun beginResizing(handleDirection: Direction) {
                  color = Color.orange
                  resizeAnchor = Rectangle(contentLocation, contentSize)
                }

                override fun resizing(totalDistance: Distance, handleDirection: Direction, totalX: Double, totalY: Double) {
                  //A left or top handle moves the origin as it resizes; a right or bottom one only grows the size
                  val deltaLocationX: @px @Zoomed Double
                  val deltaLocationY: @px @Zoomed Double
                  val resizeX: @px @Zoomed Double
                  val resizeY: @px @Zoomed Double

                  if (handleDirection.horizontalAlignment == HorizontalAlignment.Left) {
                    deltaLocationX = totalX
                    resizeX = -totalX
                  } else {
                    deltaLocationX = 0.0
                    resizeX = totalX
                  }

                  if (handleDirection.verticalAlignment == VerticalAlignment.Top) {
                    deltaLocationY = totalY
                    resizeY = -totalY
                  } else {
                    deltaLocationY = 0.0
                    resizeY = totalY
                  }

                  contentLocation = resizeAnchor.location.plus(deltaLocationX, deltaLocationY)
                  contentSize = resizeAnchor.size.plus(resizeX, resizeY)
                  paintingContext.chartSupport.markAsDirty(DirtyReason.DataUpdated)
                }

                override fun resizingFinished() {
                  color = Color.darkblue
                }
              })
            }

            override fun layout(paintingContext: LayerPaintingContext) {
              super.layout(paintingContext)
              paintingContext.chartSupport.resizeHandlesSupport.setResizable(this, Rectangle(contentLocation, contentSize), paintingContext.loopIndex)
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.fill(color)

              gc.fillRect(Rectangle(contentLocation, contentSize))
            }
          }
          layers.addLayer(contentLayer)
          layers.addLayer(resizeByHandlesLayer)

          layers.addText({ _, _ ->
            listOf(resizeByHandlesLayer.uiState.toString())
          }) {
            boxStyle = BoxStyle.gray
            anchorDirection = Direction.TopLeft
            anchorPointProvider = DirectionBasedBasePointProvider(Direction.TopLeft)
            font = FontDescriptorFragment.XS.asProvider()
          }

          configurableSeparate("Rect Location", contentLayer::contentLocation)

          configurableSeparate("Rect Size", contentLayer::contentSize) {
            max = 500.0
          }
          configurable("Handle Diameter", resizeByHandlesLayer.configuration::handleDiameter) {
            max = 50.0
          }
        }
      }
    }
  }
}
