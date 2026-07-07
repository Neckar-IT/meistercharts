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
package com.meistercharts.api.map

import com.meistercharts.algorithms.layers.barchart.StackedBarWithLabelPaintable
import com.meistercharts.algorithms.layers.slippymap.PaintableOnSlippyMap
import com.meistercharts.api.MeisterChartsApiLegacy
import com.meistercharts.api.toColorProvider
import com.meistercharts.api.toModelLinear
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.withDefaultZoom
import com.meistercharts.canvas.paintable.withOriginAtBottom
import com.meistercharts.charts.MapWithPaintablesGestalt
import com.meistercharts.js.MeisterchartJS
import com.meistercharts.maps.Latitude
import com.meistercharts.maps.Longitude
import com.meistercharts.model.Zoom
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.ValueRange
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.SizedProvider1

/**
 * A map with stacked bars.
 *
 * Each [StackedBarOnMap] is placed at its latitude/longitude and painted as a labelled stacked bar.
 */
@JsExport
class MapWithStackedBars internal constructor(
  internal val gestalt: MapWithPaintablesGestalt,
  meisterChart: MeisterchartJS,
) : MeisterChartsApiLegacy<MapWithBarsData, MapWithBarsStyle>(meisterChart) {

  init {
    gestalt.applyEasyApiDefaults()
  }

  /**
   * The bars of the most recent [setData] call. The value range from [setStyle] is baked into each
   * bar paintable, so both are kept and the paintables are rebuilt whenever either changes.
   */
  private var bars: List<StackedBarOnMap> = emptyList()
  private var valueRange: LinearValueRange = ValueRange.default

  override fun setData(jsData: MapWithBarsData) {
    bars = jsData.stackedBars.orEmpty().filterNotNull()
    rebuildPaintables()
    markAsDirty()
  }

  override fun setStyle(jsStyle: MapWithBarsStyle) {
    jsStyle.valueRange?.let {
      valueRange = it.toModelLinear()
    }
    rebuildPaintables()
    markAsDirty()
  }

  private fun rebuildPaintables() {
    val paintables: List<PaintableOnSlippyMap<Paintable>> = bars.map { bar ->
      val barPaintable = StackedBarWithLabelPaintable(
        name = bar.locationName,
        valuesProvider = DoublesProvider.forValues(bar.barValues.toList()),
        valueRange = valueRange,
        colors = bar.barColors.map { it.toColorProvider() },
      ).withOriginAtBottom().withDefaultZoom()

      PaintableOnSlippyMap(Latitude(bar.latitude), Longitude(bar.longitude), barPaintable)
    }

    gestalt.configuration.paintables = object : SizedProvider1<PaintableOnSlippyMap<*>, Zoom> {
      override fun size(param1: Zoom): Int = paintables.size
      override fun valueAt(index: Int, param1: Zoom): PaintableOnSlippyMap<*> = paintables[index]
    }
  }
}
