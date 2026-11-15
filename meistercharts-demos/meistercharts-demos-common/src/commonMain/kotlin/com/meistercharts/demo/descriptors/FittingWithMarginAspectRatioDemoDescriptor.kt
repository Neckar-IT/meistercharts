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
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.canvas.BindContentAreaSize2Property
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableSizeSeparate
import com.meistercharts.model.Insets
import com.meistercharts.zoom.FittingInContentViewportAspectRatio
import com.meistercharts.zoom.UpdateReason
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import it.neckar.open.observable.ObservableObject

/**
 */
class FittingWithMarginAspectRatioDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("106054dc-7379-4ac0-962d-5ed4ab587a52")
  override val name: String = "Content Area: Fitting with margin - Aspect Ratio"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val config = object {
          var alignment: Direction = Direction.TopLeft
          var contentAreaSizeProperty: ObservableObject<Size> = ObservableObject(Size(400.0, 700.0))
          var contentAreaSize by contentAreaSizeProperty
        }

        contentAreaSizingStrategy = BindContentAreaSize2Property(config.contentAreaSizeProperty)

        enableZoomAndTranslation = true
        zoomAndTranslationConfiguration {
          zoomWithoutModifier()
        }

        zoomAndTranslationDefaults {
          FittingInContentViewportAspectRatio(
            alignment = config::alignment
          )
        }

        configure {
          chartSupport.rootChartState.contentViewportMargin = Insets.of(30.0, 10.0, 20.0, 40.0)

          config.contentAreaSizeProperty.consume {
            chartSupport.zoomAndTranslationSupport.resetToDefaults(reason = UpdateReason.UserInteraction)
          }

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          /**
           * It is necessary to resize the window to force updates
           */
          configurableSeparate("Content Viewport Margin", chartSupport.rootChartState::contentViewportMargin) {
            onChange {
              chartSupport.zoomAndTranslationSupport.resetToDefaults(reason = UpdateReason.UserInteraction)
            }
          }

          configurable("Alignment", config::alignment, Direction.entries) {
            onChange {
              chartSupport.zoomAndTranslationSupport.resetToDefaults(reason = UpdateReason.UserInteraction)
            }
          }

          configurableSeparate("Content Area Size", config::contentAreaSize) {
            max = 1000.0
            onChange {
              chartSupport.zoomAndTranslationSupport.resetToDefaults(reason = UpdateReason.UserInteraction)
            }
          }
        }
      }
    }
  }
}
