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
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableSize
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.model.Insets
import com.meistercharts.resources.Icons
import com.meistercharts.style.BoxStyle
import it.neckar.geometry.Direction
import it.neckar.geometry.Size

/**
 *
 */
class AnchorDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("7e45b4bf-efd4-4881-a4b4-7b47c6eb7f1c")
  override val name: String = "Anchor Demo"

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "How to configure the anchor"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          val anchorLayer = AnchorLayer()
          layers.addLayer(anchorLayer)


          configurable("Anchor Direction", anchorLayer::anchorDirection, kotlin.enums.enumEntries<Direction>().toList()) {
          }

          configurable("Anchor Gap Horizontal", anchorLayer::anchorGapHorizontal) {
            max = 60.0
          }
          configurable("Anchor Gap Vertical", anchorLayer::anchorGapVertical) {
            max = 60.0
          }

          configurable("Image size", anchorLayer::imageSize) {
          }

          configurableDouble("Box Insets", anchorLayer.boxStyle.padding.top) {
            max = 30.0

            onChange {
              anchorLayer.boxStyle.padding = Insets.of(it)
              markAsDirty()
            }
          }

          configurable("Font", property = anchorLayer::font) {
          }
        }
      }
    }
  }
}

private class AnchorLayer : AbstractLayer() {
  var anchorDirection: Direction = Direction.Center
  var anchorGapHorizontal: Double = 0.0
  var anchorGapVertical: Double = 0.0
  var font: FontDescriptorFragment = FontDescriptorFragment.XL
  var imageSize = Size.PX_50
  val boxStyle = BoxStyle.gray

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.stroke(Color.silver)

    //Center with text box
    gc.saved {
      val y = gc.height / 2.0
      val x = gc.width / 2.0

      gc.strokeLine(0.0, y, gc.width, y)
      gc.strokeLine(x, 0.0, x, gc.height)

      //translate to the center
      gc.translate(x, y)

      gc.font(font)
      gc.paintTextBox("This is the painted text", anchorDirection, anchorGapHorizontal, anchorGapVertical, boxStyle, Color.blueviolet())
    }

    //FILL TEXT
    gc.saved {
      //Stroke the cross
      val y = gc.height / 4.0
      val x = gc.width / 4.0

      gc.strokeLine(0.0, y, gc.width, y)
      gc.strokeLine(x, 0.0, x, gc.height)

      //translate to the center
      gc.translate(x, y)

      gc.font(font)
      gc.fillText("This is the painted text", 0.0, 0.0, anchorDirection, anchorGapHorizontal, anchorGapVertical)
    }

    //STROKE TEXT
    gc.saved {
      //Stroke the cross
      val y = gc.height / 4.0
      val x = gc.width / 4.0 * 3

      //only one line necessary
      gc.strokeLine(x, 0.0, x, gc.height)

      //translate to the center
      gc.translate(x, y)

      gc.font(font)
      gc.strokeText("This is the painted text", 0.0, 0.0, anchorDirection, anchorGapHorizontal, anchorGapVertical)
    }

    //image
    gc.saved {
      //Stroke the cross
      val y = gc.height / 4.0 * 3
      val x = gc.width / 4.0

      //only one line necessary
      gc.strokeLine(0.0, y, gc.width, y)

      //translate to the center
      gc.translate(x, y)

      gc.font(font)

      Icons.home(imageSize).paintInBoundingBox(paintingContext, 0.0, 0.0, anchorDirection, anchorGapHorizontal, anchorGapVertical)
    }

    //image + text
    gc.saved {
      //Stroke the cross
      val y = gc.height / 4.0 * 3
      val x = gc.width / 4.0 * 3

      //no line necessary

      //translate to the center
      gc.translate(x, y)

      gc.font(font)

      Icons.home(imageSize)
        .paintInBoundingBox(paintingContext, 0.0, 0.0, anchorDirection, anchorGapHorizontal, anchorGapVertical)
      gc.fillText("This is the painted text", 0.0, 0.0, anchorDirection, anchorGapHorizontal, anchorGapVertical)
    }
  }
}
