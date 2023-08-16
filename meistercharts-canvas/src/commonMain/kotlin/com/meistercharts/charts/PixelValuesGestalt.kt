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

import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.zoom.UpdateReason
import com.meistercharts.range.ValueRange
import com.meistercharts.zoom.autoScaleByZoomSupport
import com.meistercharts.axis.AxisEndConfiguration
import it.neckar.geometry.AxisSelection
import com.meistercharts.zoom.BoundsProvider
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.ClippingLayer
import com.meistercharts.algorithms.layers.DomainRelativeGridLayer
import com.meistercharts.algorithms.layers.Limit
import com.meistercharts.algorithms.layers.LimitsLayer
import com.meistercharts.algorithms.layers.LowerLimit
import com.meistercharts.algorithms.layers.UpperLimit
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.ZeroLinesLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.clipped
import com.meistercharts.algorithms.layers.createGrid
import com.meistercharts.algorithms.layers.debug.addVersionNumberHidden
import com.meistercharts.algorithms.layers.linechart.LineChartLayer
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.linechart.LinesChartModel
import com.meistercharts.algorithms.layers.linechart.LinesChartModelIndex
import com.meistercharts.algorithms.layers.linechart.PointStyle
import com.meistercharts.algorithms.layers.linechart.toDomainRelativeY
import com.meistercharts.algorithms.layers.toolbar.ToolbarButtonFactory
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.color.Color
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.resetZoomAndTranslationToDefaults
import com.meistercharts.model.Insets
import it.neckar.geometry.Orientation
import it.neckar.geometry.Side
import com.meistercharts.model.Vicinity
import com.meistercharts.painter.LinePainter
import com.meistercharts.painter.NoLinePainter
import com.meistercharts.painter.NoPointPainter
import com.meistercharts.painter.PointPainter
import com.meistercharts.painter.PointStylePainter
import it.neckar.open.collections.emptyDoubleArray
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.asProvider1
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.kotlin.lang.randomNormal
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.formatting.intFormat
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableDouble
import it.neckar.open.observable.ObservableObject
import com.meistercharts.resources.Icons
import com.meistercharts.style.Palette
import it.neckar.open.provider.asSizedProvider
import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Visualizes the raw pixel values
 */
class PixelValuesGestalt @JvmOverloads constructor(
  val configuration: Configuration = Configuration(PixelValuesModel().also {
    it.fillWithSampleData()
  }),
  additionalConfiguration: Configuration.() -> Unit = {}
) : ChartGestalt {

  /**
   * Field for the getter
   */
  val model: PixelValuesModel
    get() = configuration.domainModel

  /**
   * The model that returns @DomainRelative values
   */
  val domainRelativeModel: @DomainRelative LinesChartModel = configuration.domainModel.toDomainRelativeY { style.yValueRange }

  val style: Configuration = Configuration(configuration.domainModel).also(additionalConfiguration)

  val fitContentInViewportGestalt: FitContentInViewportGestalt = FitContentInViewportGestalt()

  /**
   * The y-axis
   */
  val yValueAxisLayer: ValueAxisLayer = ValueAxisLayer(ValueAxisLayer.Configuration()) {
    tickOrientation = Vicinity.Outside
    ticksFormat = intFormat
    axisLineWidth = 2.0
    paintRange = AxisConfiguration.PaintRange.Continuous
  }.also {
    it.configuration.valueRangeProvider = {
      style.yValueAxisValueRangeOverride ?: style.yValueRange
    }
  }

  /**
   * The x axis
   */
  val xValueAxisLayer: ValueAxisLayer = ValueAxisLayer(ValueAxisLayer.Configuration()) {
    side = Side.Bottom
    tickOrientation = Vicinity.Outside
    axisLineWidth = 2.0
    paintRange = AxisConfiguration.PaintRange.ContentArea
    axisEndConfiguration = AxisEndConfiguration.Exact
    ticksFormat = intFormat
  }.also {
    it.configuration.valueRangeProvider = {
      style.xValueRange
    }
  }


  /**
   * The vertical grid (for the x ticks)
   */
  val verticalGridLayer: DomainRelativeGridLayer = xValueAxisLayer.createGrid {
  }

  /**
   * The horizontal grid (for the y ticks)
   */
  val horizontalGridLayer: DomainRelativeGridLayer = yValueAxisLayer.createGrid {
  }

  val clippedZeroLinesLayer: ClippingLayer<ZeroLinesLayer> = ZeroLinesLayer {
    lineWidth = 2.0
    axisToPaint = AxisSelection.X
  }.clipped()


  val toolbarGestalt: ToolbarGestalt = kotlin.run {
    val toolbarButtonFactory = ToolbarButtonFactory()

    ToolbarGestalt(
      ToolbarGestalt.Configuration(
        createDefaultZoomButtons(toolbarButtonFactory)
          .plus(
            toolbarButtonFactory.button(Icons::autoScale) {
              fitValuesInY(it.chartSupport, reason = UpdateReason.UserInteraction)
            }
          )
      )
    ) {
    }
  }

  val lineChartLayer: ClippingLayer<LineChartLayer> = LineChartLayer(LineChartLayer.Configuration(domainRelativeModel)).apply {
    configuration.pointPainters = object : MultiProvider<LinesChartModelIndex, PointPainter> {
      val pointStylePainter = PointStylePainter(PointStyle.Dot, snapXValues = true, snapYValues = true)

      override fun valueAt(index: Int): PointPainter {
        if (style.showDots) {
          pointStylePainter.color = style.getLineColor(model.mode, index)
          return pointStylePainter
        }

        return NoPointPainter
      }
    }

    configuration.linePainters = object : MultiProvider<LinesChartModelIndex, LinePainter> {
      val directLinePainter = DirectLinePainter(snapXValues = true, snapYValues = true)
      override fun valueAt(index: Int): LinePainter {
        if (!style.showLines) {
          return NoLinePainter
        }
        return directLinePainter
      }
    }

    configuration.lineStyles = object : MultiProvider<LinesChartModelIndex, LineStyle> {
      override fun valueAt(index: Int): LineStyle {
        if (!style.showLines) {
          return LineStyle.Continuous // don't care
        }
        val lineWidth = when (model.mode) {
          PixelValueVisualizationMode.Standard -> if (index == 1) {
            //The average is painted with a width of 2 px
            2.0
          } else {
            1.0
          }

          PixelValueVisualizationMode.HeldAverages -> if (index == 0) {
            //The youngest line is a little bit thicker
            1.5
          } else {
            1.0
          }
        }
        return LineStyle(lineWidth = lineWidth, color = style.getLineColor(model.mode, index))
      }
    }
  }.clipped()

  val limitsLayer: ClippingLayer<LimitsLayer> = LimitsLayer(model.limits.asSizedProvider()) {
    orientation = Orientation.Horizontal
    fill = Color.rgba(255, 255, 255, 0.85)
    stroke = Color.white
    strokeWidth = 1.0
  }.clipped()

  /**
   * Adapts the zoom level to make all y values visible
   */
  private fun fitValuesInY(chartSupport: ChartSupport, reason: UpdateReason) {
    var max: @DomainRelative Double = -Double.MAX_VALUE
    var min: @DomainRelative Double = Double.MAX_VALUE

    if (domainRelativeModel.linesCount == 0) {
      //No values, do nothing
      return
    }

    domainRelativeModel.linesCount.fastFor { lineIndex ->
      domainRelativeModel.pointsCount(lineIndex).fastFor { pointIndex ->
        @DomainRelative val valueY = domainRelativeModel.valueY(lineIndex, pointIndex)
        max = max(max, valueY)
        min = min(min, valueY)
      }
    }

    if (max == -Double.MAX_VALUE) {
      //No points found - do nothing
      return
    }
    if (min == Double.MAX_VALUE) {
      //No points found - do nothing
      return
    }

    //Reset to defaults to be sure the x-axis is correct
    chartSupport.resetZoomAndTranslationToDefaults(reason = reason)

    chartSupport.autoScaleByZoomSupport.autoScaleY(min, max, fitContentInViewportGestalt.contentViewportMargin, 0.05)
  }

  /**
   * The grid layer
   */
  val liveEdgesLayer: ClippingLayer<DomainRelativeGridLayer> = DomainRelativeGridLayer {
    lineStyles = LineStyle(color = Color.red).asProvider1()
  }.clipped()

  val detectedEdgesLayer: ClippingLayer<DomainRelativeGridLayer> = DomainRelativeGridLayer {
    lineStyles = LineStyle(color = Color.orange).asProvider1()
  }.clipped()

  val teachEdgesLayer: ClippingLayer<DomainRelativeGridLayer> = DomainRelativeGridLayer {
    lineStyles = LineStyle(color = Color("#404c4f")).asProvider1()
  }.clipped()

  init {
    style.xValueRange = ValueRange.linear(1.0, model.dataPointCount.toDouble())

    yValueAxisLayer.configuration.apply {
    }

    fitContentInViewportGestalt.contentViewportMargin = Insets.of(20.0, 20.0, 60.0, 75.0)
    fitContentInViewportGestalt.contentViewportMarginProperty.consumeImmediately {
      val withoutTop = it.withTop(0.0)
      val contentInsets = it.withTop(0.0).withRight(0.0)

      yValueAxisLayer.configuration.size = it.left
      xValueAxisLayer.configuration.size = it.bottom

      horizontalGridLayer.configuration.passpartout = withoutTop
      verticalGridLayer.configuration.passpartout = withoutTop

      clippedZeroLinesLayer.configuration.insets = { contentInsets }
      limitsLayer.configuration.insets = { contentInsets }
      lineChartLayer.configuration.insets = { contentInsets }
      liveEdgesLayer.configuration.insets = { contentInsets }
      detectedEdgesLayer.configuration.insets = { contentInsets }
      teachEdgesLayer.configuration.insets = { contentInsets }
    }
  }

  override fun configure(meisterChartBuilder: MeisterchartBuilder) {
    fitContentInViewportGestalt.configure(meisterChartBuilder)

    liveEdgesLayer.delegate.configuration.valuesProvider = object : DoublesProvider {
      override fun size(): Int = model.liveEdgesValues.size

      override fun valueAt(index: Int): @DomainRelative Double {
        val domainValue = model.liveEdgesValues[index]
        return model.pixel2domainRelativeCalculator.pixel2domainRelative(domainValue)
      }
    }

    detectedEdgesLayer.delegate.configuration.valuesProvider = object : DoublesProvider {
      override fun size(): Int = model.detectedEdgesValues.size

      override fun valueAt(index: Int): @DomainRelative Double {
        val domainValue = model.detectedEdgesValues[index]
        return model.pixel2domainRelativeCalculator.pixel2domainRelative(domainValue)
      }
    }

    teachEdgesLayer.delegate.configuration.valuesProvider = object : DoublesProvider {
      override fun size(): Int = model.teachEdgesValues.size

      override fun valueAt(index: Int): @DomainRelative Double {
        val domainValue = model.teachEdgesValues[index]
        return model.pixel2domainRelativeCalculator.pixel2domainRelative(domainValue)
      }
    }

    with(meisterChartBuilder) {
      configure {
        chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

        layers.addClearBackground()

        layers.addLayer(xValueAxisLayer)
        layers.addLayer(yValueAxisLayer)

        layers.addLayer(horizontalGridLayer)
        layers.addLayer(verticalGridLayer)
        layers.addLayer(clippedZeroLinesLayer)

        layers.addLayer(lineChartLayer)

        layers.addLayer(teachEdgesLayer.visibleIf { model.teachEdgesVisible })
        layers.addLayer(liveEdgesLayer.visibleIf { model.liveEdgesVisible })
        layers.addLayer(detectedEdgesLayer.visibleIf { model.detectedEdgesVisible })
        layers.addLayer(limitsLayer.visibleIf {
          model.limitsVisible
        })

        layers.addVersionNumberHidden()
      }

      meisterChartBuilder.enableZoomAndTranslation = true

      zoomAndTranslationConfiguration {
        zoomWithoutModifier()
      }

      zoomAndTranslationModifier {
        minZoom(1.0, 1 / (sqrt(2.0) * 1000))
        maxZoom(5.0, 5.0)
        contentAlwaysCompletelyVisible(boundsProvider = object : BoundsProvider {
          override fun top(): @ContentAreaRelative Double {
            @DomainRelative val maxTop = style.yValueRange.toDomainRelative(style.maxYValueTop)
            return -(maxTop + 1)
          }
        })
      }

      //Add toolbar at top
      toolbarGestalt.configure(this)

      //Add the version number
      VersionNumberGestalt().configure(meisterChartBuilder)
    }
  }

  @ConfigurationDsl
  class Configuration(
    /**
     * The model.
     * Y-Axis in @Domain. X-Axis in @DomainRelative
     */
    var domainModel: PixelValuesModel
  ) {
    var minColor: Color = Color.web("#3f51b5")
    var averageColor: Color = Color.web("#2196f3")
    var maxColor: Color = Color.web("#009688")
    var standardDeviationColor: Color = Color.web("#00bcd4")

    var liveEdgesColor: Color = Color.red
    var detectedEdgesColor: Color = Color.blue
    var teachEdgesColor: Color = Color.orange

    /**
     * If set to true dots are painted
     */
    val showDotsProperty: ObservableBoolean = ObservableBoolean(true)
    var showDots: Boolean by showDotsProperty

    /**
     * If set to true line are painted
     */
    val showLinesProperty: ObservableBoolean = ObservableBoolean(true)
    var showLines: Boolean by showLinesProperty

    /**
     * Returns the line color for the series
     */
    internal fun getLineColor(mode: PixelValueVisualizationMode, seriesIndex: Int): Color {
      return when (mode) {
        PixelValueVisualizationMode.Standard -> {
          when (seriesIndex) {
            0 -> minColor
            1 -> averageColor
            2 -> maxColor
            3 -> standardDeviationColor
            else -> Palette.getChartColor(seriesIndex)
          }
        }

        PixelValueVisualizationMode.HeldAverages -> {
          val opacity = (1 - seriesIndex * 0.1)
          Color.rgba(0, 0, 0, opacity)
        }
      }
    }

    /**
     * The value range that is used for the value axis (if set).
     * This value range is *NOT* used to convert the domain values but only to modify the axis
     */
    var yValueAxisValueRangeOverride: ValueRange? = null

    /**
     * The value range for the x axis
     */
    val xValueRangeProperty: ObservableObject<@Domain ValueRange> = ObservableObject(ValueRange.default)
    var xValueRange: ValueRange by xValueRangeProperty

    /**
     * The value range for the y axis
     */
    val yValueRangeProperty: ObservableObject<@Domain ValueRange> = ObservableObject(ValueRange.default)
    var yValueRange: ValueRange by yValueRangeProperty

    /**
     * The maximum value that shall be visible at the top of the chart
     */
    val maxYValueTopProperty: @Domain ObservableDouble = ObservableDouble(65_500.0)
    var maxYValueTop: @Domain Double by maxYValueTopProperty
  }
}

/**
 * Fills this data model with sample data
 */
fun PixelValuesModel.fillWithSampleData() {
  //Initialize the held indices
  10.fastFor {
    heldAverageValues.add(DoubleArray(dataPointCount))
  }

  for (i in 0 until dataPointCount) {
    minValues[i] = randomNormal(20.0, 5.0)
    averageValues[i] = randomNormal(30.0, 5.0)
    maxValues[i] = randomNormal(40.0, 5.0)

    stdDeviationValues[i] = randomNormal(3.0, 2.0)

    heldAverageValues.fastForEach { heldArray ->
      heldArray[i] = randomNormal(30.0, 5.0)
    }
  }

  for (i in 0 until 3) {
    liveEdgesValues = doubleArrayOf(10.1, 15.0, 77.0)
    detectedEdgesValues = doubleArrayOf(10.0, 11.0, 15.0, 52.0)
    teachEdgesValues = doubleArrayOf(37.0, 72.0)
  }
}

/**
 * Model for the pixel values diagram
 */
@Domain
open class PixelValuesModel(
  /**
   * The number of pixels ("data points") are recorded
   */
  val dataPointCount: Int = 256,
  /**
   * The number of curves that are shown in the mode [PixelValueVisualizationMode.HeldAverages]
   */
  val heldAveragesMaxCount: Int = 10
) : LinesChartModel {

  /**
   * The limits that are visualized.
   * This map always must contain exactly two entries. The first is the lower limit, the second is the upper limit
   */
  internal val limits: MutableList<Limit> = mutableListOf(LowerLimit(0.25), UpperLimit(0.95))

  /**
   * The lower limit (on the left side).
   * The limits are provided relative to the value range of the x axis - in pixels.
   *
   * Limits are visualized as white area.
   * The limits are visible if the x axis is configured to show pixel values.
   */
  var lowerLimit: @DomainRelative LowerLimit
    get() = limits[0] as LowerLimit
    set(value) {
      limits[0] = value
    }

  /**
   * The upper limit (on the right side).
   * The limits are provided relative to the value range of the x axis - in pixels.
   *
   * Limits are visualized as white area - if enabled.
   * The limits are visible if the x axis is configured to show pixel values.
   */
  var upperLimit: @DomainRelative UpperLimit
    get() = limits[1] as UpperLimit
    set(value) {
      limits[1] = value
    }

  /**
   * The min values (Values in pixels)
   */
  val minValues: @Domain DoubleArray = DoubleArray(dataPointCount)

  /**
   * The average values (Values in pixels)
   */
  val averageValues: @Domain DoubleArray = DoubleArray(dataPointCount)

  /**
   * The max values (Values in pixels)
   */
  val maxValues: @Domain DoubleArray = DoubleArray(dataPointCount)

  /**
   * The standard deviation values
   *
   * (Values in pixels)
   */
  val stdDeviationValues: @Domain DoubleArray = DoubleArray(dataPointCount)

  /**
   * Contains the held averages (if there are some).
   * Also contains the *current* values
   *
   * Newest values are held at the *start*
   *
   * (Values in pixels)
   */
  val heldAverageValues: MutableList<@Domain DoubleArray> = mutableListOf()

  /**
   * The current mode of the model
   */
  var mode: PixelValueVisualizationMode = PixelValueVisualizationMode.Standard

  /**
   * Whether the limits are visible
   */
  var limitsVisible: Boolean = true

  /**
   * If set to true the live edges [liveEdgesValues] are shown
   */
  var liveEdgesVisible: Boolean = true

  /**
   * If set to true the detected edges [detectedEdgesValues] are shown
   */
  var detectedEdgesVisible: Boolean = true

  /**
   * If set to true the teach edges [teachEdgesValues] are shown
   */
  var teachEdgesVisible: Boolean = true

  /**
   * The values for the live edges (values in pixels)
   */
  var liveEdgesValues: @Domain DoubleArray = emptyDoubleArray()

  /**
   * The values for the detected edges (values in pixels)
   */
  var detectedEdgesValues: @Domain DoubleArray = emptyDoubleArray()

  /**
   * The values for the teach edges (values in pixels)
   */
  var teachEdgesValues: @Domain DoubleArray = emptyDoubleArray()

  /**
   * Calculates the domain relative value for a given pixel value
   */
  var pixel2domainRelativeCalculator: Pixel2domainRelativeCalculator = LinearPixel2domainRelativeCalculator(dataPointCount)

  override val linesCount: Int
    get() {
      return when (mode) {
        PixelValueVisualizationMode.Standard -> 4 //min,max,avg,stdDeviation
        PixelValueVisualizationMode.HeldAverages -> heldAverageValues.size //only the held averages are shown
      }
    }

  override fun pointsCount(lineIndex: Int): Int {
    return dataPointCount
  }

  override fun valueX(lineIndex: Int, index: Int): Double {
    return pixel2domainRelativeCalculator.pixel2domainRelative(index.toDouble())
  }

  override fun valueY(lineIndex: Int, index: Int): Double {
    return when (mode) {
      PixelValueVisualizationMode.Standard -> when (lineIndex) {
        0 -> min(index)
        1 -> average(index)
        2 -> max(index)
        3 -> stdDeviation(index)
        else -> throw IllegalArgumentException("Invalid series index: <$lineIndex>")
      }

      PixelValueVisualizationMode.HeldAverages -> {
        heldAverageValues[lineIndex][index]
      }
    }
  }

  fun min(index: Int): Double {
    return minValues[index]
  }

  fun average(index: Int): Double {
    return this.averageValues[index]
  }

  fun max(index: Int): Double {
    return maxValues[index]
  }

  fun stdDeviation(index: Int): Double {
    return this.stdDeviationValues[index]
  }

  /**
   * This method copies the current averages to [heldAverageValues]
   */
  fun holdAverages() {
    while (heldAverageValues.size > heldAveragesMaxCount) {
      heldAverageValues.removeLast()
    }

    //Create a copy and save the values
    heldAverageValues.add(0, averageValues.copyOf())
  }
}

/**
 * The visualization mode
 */
enum class PixelValueVisualizationMode {
  /**
   * Shows the standard model (min/max/average)
   */
  Standard,

  /**
   * Shows the held averages. The youngest one is shown in black, the older ones are faded out.
   */
  HeldAverages
}


/**
 * Converts a pixel value to a domain relative value (0..1)
 */
fun interface Pixel2domainRelativeCalculator {
  /**
   * Returns the domain relative value for a given pixel value
   */
  fun pixel2domainRelative(pixel: @Domain Double): @DomainRelative Double
}

/**
 * Places the pixels on a linear scale
 */
class LinearPixel2domainRelativeCalculator(val dataPointCount: Int) : Pixel2domainRelativeCalculator {
  override fun pixel2domainRelative(pixel: Double): Double {
    return 1 / (dataPointCount.toDouble() - 1) * pixel
  }
}
