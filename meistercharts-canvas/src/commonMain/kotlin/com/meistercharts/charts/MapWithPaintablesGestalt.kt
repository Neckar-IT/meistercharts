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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.barchart.StackedBarWithLabelPaintable
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.algorithms.layers.legend.LegendLayer
import com.meistercharts.algorithms.layers.slippymap.PaintableOnSlippyMap
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.RectanglePaintable
import com.meistercharts.canvas.paintable.SymbolAndTextKeyPaintable
import com.meistercharts.canvas.paintable.withDefaultZoom
import com.meistercharts.canvas.paintable.withOriginAtBottom
import com.meistercharts.canvas.saved
import com.meistercharts.maps.Latitude
import com.meistercharts.maps.Longitude
import com.meistercharts.maps.SlippyMapCenter
import com.meistercharts.model.Direction
import com.meistercharts.model.Orientation
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom
import com.meistercharts.range.ValueRange
import com.meistercharts.style.Palette
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.getModulo
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.provider.DefaultDoublesProvider
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.SizedProvider1
import it.neckar.open.provider.asSizedProvider
import it.neckar.open.provider.fastForEach

/**
 * Gestalt for map with paintables on it
 */
class MapWithPaintablesGestalt(
  val chartId: ChartId,
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {},
) : ChartGestalt {

  val style: Style = Style().also(styleConfiguration)

  val mapGestalt: MapGestalt = MapGestalt(chartId = chartId).apply {
    data.slippyMapCenter = createDefaultMapCenter()
  }

  private val paintablesLayer = PaintablesLayer()

  val legendLayer: LegendLayer = LegendLayer(
    createDefaultLegendModel(),
    Orientation.Vertical,
  ) {
    anchorDirection = Direction.TopRight
    entriesGap = 10.0
  }

  override fun configure(meisterChartBuilder: MeisterchartBuilder) {
    mapGestalt.configure(meisterChartBuilder)

    meisterChartBuilder.configure {
      layers.addLayer(paintablesLayer)
      layers.addLayer(legendLayer.visibleIf(style.showLegendProperty))

      layers.addVersionNumberHidden()
    }
  }

  class Data {
    var paintables: SizedProvider1<PaintableOnSlippyMap<*>, Zoom> = createDefaultPaintables()
  }

  @ConfigurationDsl
  class Style {
    /**
     * Whether to show the legend or not
     */
    val showLegendProperty: ObservableBoolean = ObservableBoolean(true)

    var showLegend: Boolean by showLegendProperty
  }

  companion object {
    fun createDefaultMapCenter(): SlippyMapCenter {
      return SlippyMapCenter(Latitude(48.430004), Longitude(9.0575), 13)
    }

    //population distributed among 6 age groups for different locations (see https://www.statistik-bw.de/SRDB/?R=GS416025)
    fun createDefaultPaintables(): SizedProvider1<PaintableOnSlippyMap<*>, Zoom> {
      return object : SizedProvider1<PaintableOnSlippyMap<*>, Zoom> {
        override fun size(param1: Zoom): Int {
          return when {
            param1.scaleX >= 16.0 -> {
              all.size()
            }

            param1.scaleX > 2.0 -> {
              aggregated1.size()
            }

            param1.scaleX > 1.0 -> {
              aggregated2.size()
            }

            else -> aggregated3.size()
          }
        }

        override fun valueAt(index: Int, param1: Zoom): PaintableOnSlippyMap<*> {
          return when {
            param1.scaleX >= 16.0 -> {
              all.valueAt(index)
            }

            param1.scaleX > 2.0 -> {
              aggregated1.valueAt(index)
            }

            param1.scaleX > 1.0 -> {
              aggregated2.valueAt(index)
            }

            else -> aggregated3.valueAt(index)
          }
        }
      }
    }

    val all =
      listOf(
        PaintableOnSlippyMap(
          Latitude(48.406389),
          Longitude(9.0575),
          StackedBarWithLabelPaintable("Mössingen", DefaultDoublesProvider(listOf(15.69787338, 3.13859692, 7.743827915, 17.47250061, 34.94011244, 21.00708873)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(Latitude(48.452), Longitude(9.1), StackedBarWithLabelPaintable("Gomaringen", DefaultDoublesProvider(listOf(15.35630646, 3.479424557, 7.906769265, 18.32273893, 36.44474183, 18.49001896)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()),
        PaintableOnSlippyMap(
          Latitude(48.4203),
          Longitude(9.032),
          StackedBarWithLabelPaintable("Ofterdingen", DefaultDoublesProvider(listOf(16.67309919, 3.145503667, 7.255885758, 18.1976071, 36.24083365, 18.48707063)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(
          Latitude(48.4507),
          Longitude(9.0607),
          StackedBarWithLabelPaintable("Dußlingen", DefaultDoublesProvider(listOf(16.61592693, 3.444960743, 7.691075148, 19.59621855, 35.84361481, 16.80820381)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(
          Latitude(48.51578),
          Longitude(9.055846),
          StackedBarWithLabelPaintable("Tübingen", DefaultDoublesProvider(listOf(12.36093808, 2.243568728, 15.70279545, 27.05833497, 27.34246935, 15.29189343)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(
          Latitude(48.483333),
          Longitude(9.216667),
          StackedBarWithLabelPaintable("Reutlingen", DefaultDoublesProvider(listOf(13.92482631, 2.832606913, 8.379579683, 19.59004013, 34.60579122, 20.66715574)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(Latitude(48.4312), Longitude(9.069516), StackedBarWithLabelPaintable("Nehren", DefaultDoublesProvider(listOf(15.03656307, 3.542047532, 7.129798903, 17.09323583, 37.22577697, 19.9725777)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()),
        PaintableOnSlippyMap(
          Latitude(48.3943),
          Longitude(8.9726),
          StackedBarWithLabelPaintable("Bodelshausen", DefaultDoublesProvider(listOf(14.82254697, 3.375086987, 7.707028532, 17.48434238, 36.4822547, 20.12874043)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(
          Latitude(48.477222),
          Longitude(8.934444),
          StackedBarWithLabelPaintable("Rottenburg a. N.", DefaultDoublesProvider(listOf(14.63175421, 3.218301667, 9.068722487, 18.6825719, 35.56554068, 18.83310905)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
      ).asSizedProvider()


    val aggregated1 =
      listOf(
        PaintableOnSlippyMap(
          Latitude(48.406389),
          Longitude(9.0575),
          StackedBarWithLabelPaintable("Mössingen", DefaultDoublesProvider(listOf(15.69787338, 3.13859692, 7.743827915, 17.47250061, 34.94011244, 21.00708873)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(
          Latitude(48.4459170232),
          Longitude(9.0809050549),
          StackedBarWithLabelPaintable("Du/Go/Ne", DefaultDoublesProvider(listOf(15.35630646, 3.479424557, 7.906769265, 18.32273893, 36.44474183, 18.49001896)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(
          Latitude(48.51578),
          Longitude(9.055846),
          StackedBarWithLabelPaintable("Tübingen", DefaultDoublesProvider(listOf(12.36093808, 2.243568728, 15.70279545, 27.05833497, 27.34246935, 15.29189343)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(
          Latitude(48.483333),
          Longitude(9.216667),
          StackedBarWithLabelPaintable("Reutlingen", DefaultDoublesProvider(listOf(13.92482631, 2.832606913, 8.379579683, 19.59004013, 34.60579122, 20.66715574)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(
          Latitude(48.3943),
          Longitude(8.9726),
          StackedBarWithLabelPaintable("Bodelshausen", DefaultDoublesProvider(listOf(14.82254697, 3.375086987, 7.707028532, 17.48434238, 36.4822547, 20.12874043)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(
          Latitude(48.477222),
          Longitude(8.934444),
          StackedBarWithLabelPaintable("Rottenburg a. N.", DefaultDoublesProvider(listOf(14.63175421, 3.218301667, 9.068722487, 18.6825719, 35.56554068, 18.83310905)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
      ).asSizedProvider()

    val aggregated2 =
      listOf(
        PaintableOnSlippyMap(
          Latitude(48.51578),
          Longitude(9.055846),
          StackedBarWithLabelPaintable("Tübingen", DefaultDoublesProvider(listOf(12.36093808, 2.243568728, 15.70279545, 27.05833497, 27.34246935, 15.29189343)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(
          Latitude(48.483333),
          Longitude(9.216667),
          StackedBarWithLabelPaintable("Reutlingen", DefaultDoublesProvider(listOf(13.92482631, 2.832606913, 8.379579683, 19.59004013, 34.60579122, 20.66715574)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
        PaintableOnSlippyMap(
          Latitude(48.477222),
          Longitude(8.934444),
          StackedBarWithLabelPaintable("Rottenburg a. N.", DefaultDoublesProvider(listOf(14.63175421, 3.218301667, 9.068722487, 18.6825719, 35.56554068, 18.83310905)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
      ).asSizedProvider()

    val aggregated3 =
      listOf(
        PaintableOnSlippyMap(
          Latitude(48.51578),
          Longitude(9.055846),
          StackedBarWithLabelPaintable("Neckar-Alb", DefaultDoublesProvider(listOf(12.36093808, 2.243568728, 15.70279545, 27.05833497, 27.34246935, 15.29189343)), ValueRange.default, Palette.chartColors).withOriginAtBottom().withDefaultZoom()
        ),
      ).asSizedProvider()


    //6 age groups (see https://www.statistik-bw.de/SRDB/?R=GS416025)
    fun createDefaultLegendModel(): SizedProvider<Paintable> {
      return listOf(
        SymbolAndTextKeyPaintable(RectanglePaintable(Size.PX_16, Palette.chartColors.getModulo(0)), TextKey.simple("< 15")),
        SymbolAndTextKeyPaintable(RectanglePaintable(Size.PX_16, Palette.chartColors.getModulo(1)), TextKey.simple("15 - 18")),
        SymbolAndTextKeyPaintable(RectanglePaintable(Size.PX_16, Palette.chartColors.getModulo(2)), TextKey.simple("18 - 25")),
        SymbolAndTextKeyPaintable(RectanglePaintable(Size.PX_16, Palette.chartColors.getModulo(3)), TextKey.simple("25 - 40")),
        SymbolAndTextKeyPaintable(RectanglePaintable(Size.PX_16, Palette.chartColors.getModulo(4)), TextKey.simple("40 - 65")),
        SymbolAndTextKeyPaintable(RectanglePaintable(Size.PX_16, Palette.chartColors.getModulo(5)), TextKey.simple("> 65"))
      ).asSizedProvider()
    }
  }

  inner class PaintablesLayer : AbstractLayer() {
    override val type: LayerType
      get() = LayerType.Content

    override fun paint(paintingContext: LayerPaintingContext) {
      data.paintables.fastForEach(paintingContext.chartState.zoom) { bar ->
        paintingContext.gc.saved {
          bar.paint(paintingContext)
        }
      }
    }
  }

}
