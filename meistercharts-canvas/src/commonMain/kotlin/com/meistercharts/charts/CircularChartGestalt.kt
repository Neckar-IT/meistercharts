package com.meistercharts.charts

import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.circular.CircularChartLayer
import com.meistercharts.algorithms.layers.circular.CircularChartLegendLayer
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Insets
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MutableDoublesProvider
import it.neckar.open.provider.delegate
import it.neckar.open.provider.toRelative
import it.neckar.open.i18n.TextKey
import it.neckar.open.observable.ObservableObject
import com.meistercharts.resources.Icons
import com.meistercharts.style.Palette
import it.neckar.open.unit.other.pct

/**
 * Configuration for a simple circular chart
 */
class CircularChartGestalt(
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {}
) : ChartGestalt {

  val style: Style = Style().also(styleConfiguration)

  val layer: CircularChartLayer = CircularChartLayer(CircularChartLayer.Data(data::relativeValuesProvider.delegate())) {
  }

  val legendLayer: CircularChartLegendLayer = CircularChartLegendLayer(data::absoluteValuesProvider.delegate()) {
    font = FontDescriptorFragment(16.0)
  }

  init {
    style.colorsProviderProperty.consumeImmediately {
      layer.style.segmentsColorProvider = it
    }

    legendLayer.style.segmentsLabelProvider = createDefaultLabelProvider()
    legendLayer.style.segmentsImageProvider = createDefaultImageProvider(style::colorsProvider.delegate())
  }

  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    FixedChartGestalt(Insets.of(75.0)).configure(meisterChartBuilder)

    meisterChartBuilder.apply {
      configure {
        chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtTop

        layers.addClearBackground()
        layers.addLayer(layer)
        layers.addLayer(legendLayer)

        layers.addVersionNumberHidden()
      }
    }
  }

  class Data(
    /**
     * Provides values for circular chart segments.
     * The values are provided in percentage. The sum of the values must not be greater than 1.0
     */
    var absoluteValuesProvider: @Domain DoublesProvider = createDefaultValuesProvider(),

    /**
     * Provides the *relative* values that are used to calculate the size of the segments
     */
    var relativeValuesProvider: @Domain @pct DoublesProvider = absoluteValuesProvider.toRelative()
  )

  @StyleDsl
  class Style {
    val colorsProviderProperty: ObservableObject<MultiProvider<CircularChartLegendLayer.CircleSegmentIndex, Color>> = ObservableObject(createDefaultColorsProvider())
    var colorsProvider: MultiProvider<CircularChartLegendLayer.CircleSegmentIndex, Color> by colorsProviderProperty
  }

  companion object {
    /**
     * Provides default values for segments
     */
    fun createDefaultValuesProvider(): @Domain DoublesProvider {
      return MutableDoublesProvider().also {
        it.addAll(listOf(30.0, 15.0, 25.0, 30.0))
      }
    }

    /**
     * Provides default colors for segments
     */
    fun createDefaultColorsProvider(): MultiProvider<CircularChartLegendLayer.CircleSegmentIndex, Color> {
      return MultiProvider.forListModulo(
        listOf(
          Palette.stateSuperior,
          Palette.stateOffline,
          Palette.stateError,
          Palette.stateWarning
        )
      )
    }

    /**
     * Provides default labels for segments
     */
    fun createDefaultLabelProvider(): MultiProvider<CircularChartLegendLayer.CircleSegmentIndex, TextKey?> {
      return MultiProvider.forListOrNull(
        listOf(
          TextKey.simple("OK"),
          TextKey.simple("Unknown"),
          TextKey.simple("Errors"),
          TextKey.simple("Warnings")
        )
      )
    }

    /**
     * Provides default images for segments
     */
    fun createDefaultImageProvider(colorsProvider: MultiProvider<CircularChartLegendLayer.CircleSegmentIndex, Color>): MultiProvider<CircularChartLegendLayer.CircleSegmentIndex, Paintable?> {
      return MultiProvider { index ->
        val fill = colorsProvider.valueAt(index)

        when (index) {
          0 -> Icons.ok(fill = fill)
          1 -> Icons.questionmark(fill = fill)
          2 -> Icons.error(fill = fill)
          3 -> Icons.warning(fill = fill)
          else -> null
        }
      }
    }
  }

}
