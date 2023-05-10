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

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.model.Insets

/**
 * Binds the content area to the window size
 */
open class FitContentInViewportGestalt(contentViewportMargin: @Zoomed Insets = Insets.empty) : ContentViewportGestalt(contentViewportMargin) {
  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    super.configure(meisterChartBuilder)

    meisterChartBuilder.apply {
      contentAreaSizingStrategy = BindContentAreaSize2ContentViewport()
    }
  }
}