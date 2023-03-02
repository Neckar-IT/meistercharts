package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.painter.ArrowHead
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import it.neckar.open.unit.number.MayBeNaN
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.LineSpacing
import com.meistercharts.canvas.layout.cache.CoordinatesCache
import com.meistercharts.canvas.layout.cache.ObjectCache
import com.meistercharts.canvas.paintLocation
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.design.Theme
import com.meistercharts.model.Direction
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Orientation
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.provider.CoordinatesProvider1
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiDoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.provider.MultiProviderIndexContextAnnotation
import it.neckar.open.provider.fastForEachIndexed
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.other.px

/**
 * Provides the labels for each HUD element
 */
typealias HudLabelsProvider = MultiProvider1<HudElementIndex, List<String>, LayerPaintingContext>

/**
 * Paints the HUD (usually on the value axis).
 *
 */
class ValueAxisHudLayer constructor(
  /**
   * Provides the anchor location of the hud element.
   * If NaN is returned, the element is not painted
   */
  locations: @MayBeNaN @Window CoordinatesProvider1<LayerPaintingContext>,

  /**
   * Provides the text that is painted within the HUD element
   */
  labels: HudLabelsProvider,

  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {
  val configuration: Configuration = Configuration(locations, labels).also(additionalConfiguration)

  override val type: LayerType = LayerType.Content

  override fun paintingVariables(): ValueAxisHudLayerPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : ValueAxisHudLayerPaintingVariables {
    /**
     * Contains the coordinates for the HUD element
     */
    override val coordinatesCache = @Window @MayBeNaN CoordinatesCache()

    override val anchorDirectionsCache = ObjectCache(Direction.TopLeft)

    /**
     * Contains the labels
     */
    override val labelsCache = ObjectCache<List<String>>(emptyList())

    override fun calculate(paintingContext: LayerPaintingContext) {
      val chartSupport = paintingContext.chartSupport

      @HudElementIndex val size = configuration.locations.size(paintingContext)

      //Prepare the caches
      coordinatesCache.prepare(size)
      anchorDirectionsCache.prepare(size)
      labelsCache.prepare(size)

      configuration.locations.fastForEachIndexed(paintingContext) { index: @HudElementIndex Int, x: @MayBeNaN @Window Double, y: @MayBeNaN @Window Double ->
        coordinatesCache.x(index, x)
        coordinatesCache.y(index, y)

        anchorDirectionsCache[index] = configuration.anchorDirections.valueAt(index)
        labelsCache[index] = configuration.labels.valueAt(index, paintingContext)
      }
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    paintingVariables.coordinatesCache.fastForEachIndexed { index: @HudElementIndex Int, x: @Window @MayBeNaN Double, y: @Window @MayBeNaN Double ->
      if (x.isFinite().not() || y.isFinite().not()) {
        //Skip if x or y are not finite
        return@fastForEachIndexed
      }

      gc.saved {
        gc.translate(x, y)

        paintingContext.ifDebug(DebugFeature.ShowAnchors) {
          gc.paintLocation()
        }

        val anchorDirection = paintingVariables.anchorDirectionsCache[index]

        val arrowHeadLength = configuration.arrowHeadLength.valueAt(index)
        val arrowHeadWidth = configuration.arrowHeadWidth.valueAt(index)

        gc.font(configuration.textFonts.valueAt(index))
        gc.paintTextBox(
          lines = paintingVariables.labelsCache[index],
          lineSpacing = LineSpacing.Single,
          horizontalAlignment = configuration.textAlignments.valueAt(index),
          anchorDirection = anchorDirection,
          anchorGapHorizontal = arrowHeadLength, //always use length
          anchorGapVertical = arrowHeadLength, //always use length
          boxStyle = configuration.boxStyles.valueAt(index),
          textColor = configuration.textColors.valueAt(index),
          maxStringWidth = configuration.maxWidth.valueAt(index)
        )

        gc.beginPath()
        ArrowHead.forOrientation(gc, anchorDirection, arrowHeadLength, arrowHeadWidth)
        gc.fill(configuration.arrowFills.valueAt(index))
        gc.fill()
      }
    }
  }

  class Configuration(
    /**
     * Provides the anchor location of the hud element.
     * If NaN is returned, the element is not painted
     */
    var locations: @MayBeNaN @Window CoordinatesProvider1<LayerPaintingContext>,

    /**
     * Provides the text that is painted within the HUD element
     */
    var labels: HudLabelsProvider,
  ) {

    /**
     * The direction of the anchor.
     */
    var anchorDirections: MultiProvider<HudElementIndex, Direction> = MultiProvider.always(Direction.CenterLeft)

    /**
     * The length of the arrow-head
     */
    var arrowHeadLength: @px MultiDoublesProvider<HudElementIndex> = MultiDoublesProvider.always(10.0)

    /**
     * The width of the arrow-head
     */
    var arrowHeadWidth: @px MultiDoublesProvider<HudElementIndex> = MultiDoublesProvider.always(10.0)

    /**
     * The box style for the HUD element
     */
    var boxStyles: MultiProvider<HudElementIndex, BoxStyle> = MultiProvider.always(BoxStyle.modernBlue)

    /**
     * Returns the fill color for the arrow.
     * The default implementation returns the border color of the [boxStyles]
     */
    var arrowFills: MultiProvider<HudElementIndex, Color> = MultiProvider { index: @HudElementIndex Int ->
      val boxStyle = boxStyles.valueAt(index)
      boxStyle.borderColor ?: boxStyle.fill ?: textColors.valueAt(index)
    }

    /**
     * The text color for the label
     */
    var textColors: MultiProvider<HudElementIndex, Color> = MultiProvider.always(Color.black)

    /**
     * The text font fragments
     */
    var textFonts: MultiProvider<HudElementIndex, FontDescriptorFragment> = MultiProvider.always(Theme.thresholdLabelFont())

    /**
     * The text alignment
     */
    var textAlignments: MultiProvider<HudElementIndex, HorizontalAlignment> = MultiProvider.always(HorizontalAlignment.Left)

    /**
     * The max width for the text box
     */
    var maxWidth: @px MultiDoublesProvider<HudElementIndex> = MultiDoublesProvider.always(Double.MAX_VALUE)
  }
}

/**
 * Creates a value axis hud layer - based upon the value axis layer
 */
fun ValueAxisLayer.hudLayer(
  /**
   * Provides the domain values that are painted as HUD elements
   */
  domainValues: @Domain DoublesProvider,
): ValueAxisHudLayer {
  return ValueAxisHudLayer(

    locations = object : @Window CoordinatesProvider1<LayerPaintingContext> {
      override fun size(param1: LayerPaintingContext): Int {
        return domainValues.size()
      }

      override fun xAt(index: Int, param1: LayerPaintingContext): @Window Double {
        val chartCalculator = param1.chartCalculator

        return when (style.orientation) {
          Orientation.Vertical -> paintingVariables().axisLineLocation
          Orientation.Horizontal -> {
            @DomainRelative val domainRelative = data.valueRangeProvider().toDomainRelative(domainValues.valueAt(index))

            if (style.paintRange == AxisStyle.PaintRange.ContentArea) {
              if (ChartCalculator.inContentArea(domainRelative).not()) {
                //Not within content area
                return Double.NaN
              }
            }

            @Window val xWindow = param1.chartCalculator.domainRelative2windowX(domainRelative)
            chartCalculator.inViewportOrX(xWindow, Double.NaN)
          }
        }
      }

      override fun yAt(index: Int, param1: LayerPaintingContext): @Window Double {
        val chartCalculator = param1.chartCalculator

        return when (style.orientation) {
          Orientation.Vertical -> {
            @DomainRelative val domainRelative = data.valueRangeProvider().toDomainRelative(domainValues.valueAt(index))

            if (style.paintRange == AxisStyle.PaintRange.ContentArea) {
              if (ChartCalculator.inContentArea(domainRelative).not()) {
                //Not within content area
                return Double.NaN
              }
            }

            val windowY = param1.chartCalculator.domainRelative2windowY(domainRelative)
            chartCalculator.inViewportOrY(windowY, Double.NaN)
          }

          Orientation.Horizontal -> paintingVariables().axisLineLocation
        }
      }
    }, labels = { index, _ ->
      val value = domainValues.valueAt(index)
      listOf(style.ticksFormat.format(value))
    }
  ) {
    anchorDirections = MultiProvider {
      val tickOrientation = style.tickOrientation

      when (style.side) {
        Side.Left -> when (tickOrientation) {
          Vicinity.Inside -> Direction.CenterLeft
          Vicinity.Outside -> Direction.CenterRight
        }

        Side.Right -> when (tickOrientation) {
          Vicinity.Inside -> Direction.CenterRight
          Vicinity.Outside -> Direction.CenterLeft
        }

        Side.Top -> when (tickOrientation) {
          Vicinity.Inside -> Direction.TopCenter
          Vicinity.Outside -> Direction.BottomCenter
        }

        Side.Bottom -> when (tickOrientation) {
          Vicinity.Inside -> Direction.BottomCenter
          Vicinity.Outside -> Direction.TopCenter
        }
      }
    }

    maxWidth = MultiDoublesProvider {
      when (style.side) {
        Side.Left,
        Side.Right,
        -> {
          //size (width) of axis
          // - width of axis lines
          // - arrow size
          // - box insets
          style.size - style.axisLineWidth - arrowHeadLength.valueAt(it) - boxStyles.valueAt(it).padding.offsetWidth
        }

        Side.Top,
        Side.Bottom,
        -> {
          //At the moment do not limit the width
          Double.NaN
        }
      }
    }
  }
}

/**
 * Index annotation for the HUD element index
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.LOCAL_VARIABLE)
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@MultiProviderIndexContextAnnotation
annotation class HudElementIndex


interface ValueAxisHudLayerPaintingVariables : PaintingVariables {
  /**
   * Contains the coordinates for the HUD element
   */
  val coordinatesCache: CoordinatesCache

  /**
   * Cache for anchor directions
   */
  val anchorDirectionsCache: ObjectCache<Direction>

  /**
   * Contains the labels
   */
  val labelsCache: ObjectCache<List<String>>

}
