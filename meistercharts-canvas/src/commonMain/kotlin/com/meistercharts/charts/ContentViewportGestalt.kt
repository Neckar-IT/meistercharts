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
package com.meistercharts.charts

import com.meistercharts.algorithms.UpdateReason
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.impl.FittingInContentViewport
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.model.Insets
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.observable.ObservableObject

/**
 * Configures the content viewport using margins
 */
open class ContentViewportGestalt(
  contentViewportMargin: @Zoomed Insets,
  val updateBehavior: UpdateBehavior = ResetToDefaults,
) : ChartGestalt {
  /**
   * The current content viewport margin
   */
  val contentViewportMarginProperty: ObservableObject<@Zoomed Insets> = ObservableObject(contentViewportMargin).also {
    it.consumeChanges { oldValue, newValue ->
      logger.debug("contentViewportMargin changed from $oldValue to $newValue")
    }
  }

  var contentViewportMargin: @Zoomed Insets by contentViewportMarginProperty

  @ChartGestaltConfiguration
  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    meisterChartBuilder.apply {
      configure {

        contentViewportMarginProperty.consumeImmediately { newValue ->
          val oldValue = chartSupport.rootChartState.contentViewportMargin
          chartSupport.rootChartState.contentViewportMargin = newValue

          when (updateBehavior) {
            KeepCurrentZoomAndTranslation -> {
              //do nothing
            }

            ResetToDefaults -> {
              chartSupport.zoomAndTranslationSupport.resetToDefaults(reason = UpdateReason.ConfigurationUpdate)
            }

            ResetAffectedAxisToDefaults -> {
              val axisSelection = AxisSelection.get(
                xSelected = oldValue.left != newValue.left || oldValue.right != newValue.right,
                ySelected = oldValue.top != newValue.top || oldValue.bottom != newValue.bottom
              )

              chartSupport.zoomAndTranslationSupport.resetToDefaults(axisSelection = axisSelection, reason = UpdateReason.ConfigurationUpdate)
            }

            ResetAffectedAxisOnMarginIncreaseToDefaults -> {
              val axisSelection = AxisSelection.get(
                xSelected = newValue.left > oldValue.left || newValue.right > oldValue.right,
                ySelected = newValue.top > oldValue.top || newValue.bottom > oldValue.bottom
              )

              chartSupport.zoomAndTranslationSupport.resetToDefaults(axisSelection = axisSelection, reason = UpdateReason.ConfigurationUpdate)
            }
          }
        }
      }

      zoomAndTranslationDefaults = FittingInContentViewport
    }
  }

  inline fun setMarginTop(newTop: @Zoomed Double) {
    contentViewportMargin = contentViewportMargin.withTop(newTop)
  }

  inline fun setMarginLeft(newLeft: @Zoomed Double) {
    contentViewportMargin = contentViewportMargin.withLeft(newLeft)
  }

  inline fun setMarginBottom(newBottom: @Zoomed Double) {
    contentViewportMargin = contentViewportMargin.withBottom(newBottom)
  }

  inline fun setMarginRight(newRight: @Zoomed Double) {
    contentViewportMargin = contentViewportMargin.withRight(newRight)
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.charts.ContentViewportGestalt")
  }


  /**
   * Defines the behavior when the content viewport margin changes
   */
  sealed interface UpdateBehavior {
  }

  /**
   * Resets the zoom and translation to the defaults
   */
  data object ResetToDefaults : UpdateBehavior

  /**
   * Keeps the current zoom and translation - does nothing
   */
  data object KeepCurrentZoomAndTranslation : UpdateBehavior

  /**
   * Resets the zoom and translation to the defaults, but only for the axis that are affected by the margin change
   */
  data object ResetAffectedAxisToDefaults : UpdateBehavior

  /**
   * Only resets zoom and translations to the defaults - but only for the axis that are affected by the margin change and if the margin has increased!
   */
  data object ResetAffectedAxisOnMarginIncreaseToDefaults : UpdateBehavior
}
