package it.neckar.charting.custom.rainsensor

import com.meistercharts.algorithms.BinaryValueRange
import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.model.Orientation
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.axis.AxisEndConfiguration
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.FittingWithMarginAspectRatio
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.BinaryTicksProvider
import com.meistercharts.algorithms.layers.CategoryLinesLayer
import com.meistercharts.algorithms.layers.TransformingChartStateLayer
import com.meistercharts.algorithms.layers.DefaultCategoryLayouter
import com.meistercharts.algorithms.layers.LayerList
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.clippedToContentArea
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.toolbar.ToolbarButtonFactory
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.algorithms.model.SingleSeriesCategorySeriesModel
import com.meistercharts.algorithms.paintable.ObjectFit
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.withAdditionalTranslation
import com.meistercharts.algorithms.withAxisOrientation
import com.meistercharts.algorithms.withContentAreaSize
import com.meistercharts.algorithms.withZoom
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.FixedContentAreaSize
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.fillRoundedRectCoordinates
import com.meistercharts.canvas.paintable.Button
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.strokeRoundedRectCoordinates
import com.meistercharts.canvas.timerSupport
import com.meistercharts.charts.ChartGestalt
import com.meistercharts.charts.FixedChartGestalt
import com.meistercharts.charts.ToolbarGestalt
import com.meistercharts.custom.rainsensor.RainLayer
import com.meistercharts.custom.rainsensor.RainSensorAnimationManager
import com.meistercharts.custom.rainsensor.RainSensorLayer
import com.meistercharts.custom.rainsensor.RainSensorModel
import com.meistercharts.custom.rainsensor.RainSensorResources
import com.meistercharts.custom.rainsensor.Weather
import com.meistercharts.custom.rainsensor.WindowAction
import com.meistercharts.design.Theme
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import com.meistercharts.model.Insets
import com.meistercharts.model.Rectangle
import com.meistercharts.model.SidesSelection
import com.meistercharts.model.Size
import com.meistercharts.model.Vicinity
import com.meistercharts.model.Zoom
import it.neckar.open.kotlin.lang.deleteFromStartUntilMaxSize
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MutableDoublesProvider
import it.neckar.open.formatting.intFormat
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ReadOnlyObservableBoolean
import it.neckar.open.unit.other.pct
import kotlin.time.Duration.Companion.milliseconds

/**
 *
 */
class RainSensorGestalt(
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {},
) : ChartGestalt {

  val style: Style = Style().also(styleConfiguration)

  init {
    //
    // ATTENTION: Register listeners in init block to ensure all objects have been created
    //
  }

  /**
   * Shows the raining background
   */
  val rainLayer: RainLayer = RainLayer(RainLayer.Data(4))

  /**
   * Shows the snow background
   */
  val snowLayer: RainLayer = RainLayer(RainLayer.Data(4, dropDuration = 10_000.0)) {
    rainDrop = RainSensorResources.snowFlake
  }

  val sunLayer: AbstractLayer = object : AbstractLayer() {
    override val type: LayerType = LayerType.Background

    override fun paint(paintingContext: LayerPaintingContext) {
      val gc = paintingContext.gc
      RainSensorResources.sun.paintInBoundingBox(paintingContext, gc.width, 0.0, Direction.TopRight)
    }
  }

  val rainSensorLayer: RainSensorLayer = RainSensorLayer(
    RainSensorLayer.Data(
      { data.model.openAngle },
      { data.isSnowing }
    ))

  val rainSensorAnimationManager: RainSensorAnimationManager = RainSensorAnimationManager(data.model)

  private val fixedChartGestalt: FixedChartGestalt = FixedChartGestalt()

  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    with(meisterChartBuilder) {
      fixedChartGestalt.configure(meisterChartBuilder)

      //Overwrite settings from fixed chart gestalt
      zoomAndTranslationDefaults {
        FittingWithMarginAspectRatio()
      }
      contentAreaSizingStrategy = FixedContentAreaSize { RainSensorResources.roofSize }

      configure {
        chartSupport.onRefresh(rainSensorAnimationManager)

        chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtTop

        layers.addClearBackground()
        layers.addLayer(rainLayer.visibleIf {
          data.isRaining
        }.clippedToContentArea(SidesSelection.onlyBottom))
        layers.addLayer(snowLayer.visibleIf {
          data.isSnowing
        }.clippedToContentArea(SidesSelection.onlyBottom))
        layers.addLayer(sunLayer.visibleIf {
          data.isSunny
        }.clippedToContentArea(SidesSelection.onlyBottom))

        layers.addLayer(rainSensorLayer)
      }

      //Create the toolbar
      val toolbarButtonFactory = ToolbarButtonFactory()
      val buttons = buildList<Button> {
        add(toolbarButtonFactory.button(getButtonPaintableProvider(RainSensorResources.iconSun) { data.isSunny }, style.toolbarButtonSize) {
          data.model.weather = Weather.Sunny
          data.model.nextAction = WindowAction.Open
          it.chartSupport.markAsDirty()
        })
        add(toolbarButtonFactory.button(getButtonPaintableProvider(RainSensorResources.iconRain) { data.isRaining }, style.toolbarButtonSize) {
          data.model.weather = Weather.Rain
          data.model.nextAction = WindowAction.Close
          it.chartSupport.markAsDirty()
        })
        add(toolbarButtonFactory.button(getButtonPaintableProvider(RainSensorResources.iconSnow) { data.isSnowing }, style.toolbarButtonSize) {
          data.model.weather = Weather.Snow
          data.model.nextAction = WindowAction.Close
          it.chartSupport.markAsDirty()
        })
      }

      //val binaryLayer = BinaryLayer(BinaryLayer.Data(object : BooleanValuesProvider {
      //  override val size: Int = 10
      //
      //  override fun valueAt(index: Int): Boolean {
      //    return index % 2 == 0
      //  }
      //}))

      @Zoomed
      val innerLayersInsets = Insets.of(20.0, 20.0, 20.0, 50.0)

      val innerLayersGap = 30.0

      /**
       * Paints the white background
       */
      val insetsBackgroundLayer = object : AbstractLayer() {
        override val type: LayerType = LayerType.Content

        override fun paint(paintingContext: LayerPaintingContext) {
          val gc = paintingContext.gc
          val chartCalculator = paintingContext.chartCalculator

          gc.fill(Color.white)
          gc.fillRoundedRectCoordinates(
            chartCalculator.contentAreaRelative2windowX(0.0) - innerLayersInsets.left,
            chartCalculator.contentAreaRelative2windowY(0.0) - innerLayersInsets.top,
            chartCalculator.contentAreaRelative2windowX(1.0) + innerLayersInsets.bottom,
            chartCalculator.contentAreaRelative2windowY(1.0) + innerLayersInsets.right,
            10.0
          )

          gc.stroke(style.hoverColor)
          gc.strokeRoundedRectCoordinates(
            chartCalculator.contentAreaRelative2windowX(0.0) - innerLayersInsets.left,
            chartCalculator.contentAreaRelative2windowY(0.0) - innerLayersInsets.top,
            chartCalculator.contentAreaRelative2windowX(1.0) + innerLayersInsets.bottom,
            chartCalculator.contentAreaRelative2windowY(1.0) + innerLayersInsets.right,
            10.0
          )

          gc.stroke(Color.lightgray)
          gc.strokeRoundedRectCoordinates(
            chartCalculator.contentAreaRelative2windowX(0.0),
            chartCalculator.contentAreaRelative2windowY(0.0),
            chartCalculator.contentAreaRelative2windowX(1.0),
            chartCalculator.contentAreaRelative2windowY(1.0),
            10.0
          )
        }
      }

      val binaryValueAxisLayer: ValueAxisLayer = ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { BinaryValueRange })) {
        paintRange = AxisStyle.PaintRange.ContentArea
        tickOrientation = Vicinity.Outside
        axisEndConfiguration = AxisEndConfiguration.Exact
        ticks = BinaryTicksProvider
        ticksFormat = intFormat
        size = innerLayersInsets.left - 5.0
        margin = Insets.onlyLeft(innerLayersGap)
      }

      /**
       * Paints the lines
       */
      val categoryLinesLayer: CategoryLinesLayer = CategoryLinesLayer(
        CategoryLinesLayer.Data(
          SingleSeriesCategorySeriesModel(
            data.windowAnglePercentages
          )
        )
      ) {
        valueRange = ValueRange.percentage
        layoutCalculator = DefaultCategoryLayouter() {
          minCategorySize = 1.0
        }
        pointPainters = MultiProvider.alwaysNull()
        lineStyles = MultiProvider.always(LineStyle(lineWidth = 2.0, color = Theme.chartColors().valueAt(0)))
      }

      val innerLayers = LayerList(insetsBackgroundLayer, binaryValueAxisLayer, categoryLinesLayer)

      configure {
        //Collect the data
        chartSupport.timerSupport.repeat(500.milliseconds) {
          data.storeCurrentWindowAnglePercentage()
          chartSupport.markAsDirty()
        }

        layers.addLayer(TransformingChartStateLayer(innerLayers) { chartState ->
          val chartCalculator = ChartCalculator(chartState)

          @Window val targetWidth = chartCalculator.domainRelativeDelta2ZoomedX(0.5) - innerLayersGap
          @Window val targetHeight = chartCalculator.domainRelativeDelta2ZoomedY(0.25) - innerLayersGap
          @Window val bottom = chartCalculator.contentAreaRelative2windowY(1.0) - innerLayersGap - innerLayersInsets.bottom

          @Window val top = bottom - targetHeight

          chartState.withContentAreaSize(Size(targetWidth, targetHeight))
            .withAdditionalTranslation(Distance.of(innerLayersGap + innerLayersInsets.left, top))
            .withZoom(Zoom.default)
            .withAxisOrientation(axisOrientationYOverride = AxisOrientationY.OriginAtBottom)
        })
      }

      val toolbarGestalt: ToolbarGestalt = ToolbarGestalt(ToolbarGestalt.Data(buttons)).also {
        it.toolbarLayer.configuration.anchorDirection = Direction.TopCenter
        it.toolbarLayer.configuration.layoutOrientation = Orientation.Horizontal
      }

      toolbarGestalt.configure(meisterChartBuilder)
    }
  }

  class Data(
    val model: RainSensorModel = RainSensorModel(),
    /**
     * The number of points in the line chart
     */
    val lineChartModelLength: Int = 20,
  ) {
    /**
     * The window angle percentages
     */
    val windowAnglePercentages: @pct MutableDoublesProvider = MutableDoublesProvider().also {
      //Fill with initial values
      lineChartModelLength.fastFor { index ->
        it.add(0.0)
      }
    }

    fun storeCurrentWindowAnglePercentage() {
      val nextValue = if (model.currentAction == WindowAction.Open) 0.0 else 1.0
      windowAnglePercentages.add(nextValue)
      windowAnglePercentages.values.deleteFromStartUntilMaxSize(lineChartModelLength)
    }

    val isRainingProperty: ReadOnlyObservableBoolean = ObservableBoolean(true).also {
      it.bind(model.weatherProperty.map {
        it == Weather.Rain
      })
    }
    val isRaining: Boolean by isRainingProperty

    val isSnowingProperty: ReadOnlyObservableBoolean = ObservableBoolean(true).also {
      it.bind(model.weatherProperty.map {
        it == Weather.Snow
      })
    }
    val isSnowing: Boolean by isSnowingProperty

    val isSunnyProperty: ReadOnlyObservableBoolean = ObservableBoolean(true).also {
      it.bind(model.weatherProperty.map {
        it == Weather.Sunny
      })
    }
    val isSunny: Boolean by isSunnyProperty
  }

  class Style {
    val toolbarButtonSize: Size = Size(60.0, 60.0)
    val defaultColor: Color = Color.web("#3C9A90")
    val hoverColor: Color = Color.web("#003248")
    val pressedColor: Color = Color.web("#1A475A")
  }


  private fun getButtonPaintableProvider(icon: Paintable, size: Size = style.toolbarButtonSize, selected: () -> Boolean): (ButtonState) -> Paintable {
    return { buttonState ->
      object : Paintable {
        override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle = Rectangle.topLeft(size)

        override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
          val gc = paintingContext.gc

          val fill: Color = when {
            buttonState.pressed -> style.pressedColor
            buttonState.hover -> style.hoverColor
            selected() -> style.pressedColor
            else -> style.defaultColor
          }

          gc.fill(fill)
          gc.fillRect(x, y, size)

          icon.paintInBoundingBox(
            paintingContext = paintingContext,
            x = x + size.width / 2.0,
            y = y + size.height / 2.0,
            direction = Direction.Center,
            gapHorizontal = 0.0,
            gapVertical = 0.0,
            boundingBoxSize = size, objectFit = ObjectFit.ContainNoGrow
          )
        }
      }
    }
    //
    //  val image: Paintable = when (buttonState.simpleToggle) {
    //    ButtonState.SimpleToggle.Default                                    -> RainSensorResources.iconRain
    //    ButtonState.SimpleToggle.Pressed, ButtonState.SimpleToggle.Selected -> RainSensorResources.iconRain
    //    ButtonState.SimpleToggle.Hover                                      -> RainSensorResources.iconRain
    //  }
    //
    //  val combinedPaintable = CombinedPaintable(image, object : Paintable {
    //    override fun boundingBox(chartSupport: ChartSupport): Rectangle {
    //      return Rectangle.zero
    //    }
    //
    //    override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    //      val gc = paintingContext.gc
    //      gc.font(corporateDesign.h3)
    //      gc.fill(
    //        if (buttonState.pressed || buttonState.selected) {
    //          Color.lime
    //        } else {
    //          Color.black
    //        }
    //      )
    //      gc.fillText(label, x, y, Direction.Center)
    //    }
    //  }, Distance.none)
    //
    //  combinedPaintable
    //}
  }

}

