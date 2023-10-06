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
package com.meistercharts.api

import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.DomainRelativeGridLayer
import com.meistercharts.algorithms.layers.GridLayer
import com.meistercharts.algorithms.layers.axis.ConstantTicksProvider
import com.meistercharts.algorithms.layers.axis.HudElementIndex
import com.meistercharts.algorithms.layers.axis.HudLabelsProvider
import com.meistercharts.algorithms.layers.axis.TickProvider
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.axis.time.TimeAxisLayer
import com.meistercharts.algorithms.layers.axis.withMaxNumberOfTicks
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.GreedyCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.LabelVisibleCondition
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.painter.stripe.enums.EnumAggregationMode
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.parseCssFontFamily
import com.meistercharts.canvas.parseCssFontStyle
import com.meistercharts.canvas.text.CanvasStringShortener
import com.meistercharts.charts.OverflowIndicatorPainter
import com.meistercharts.charts.support.threshold.ThresholdsSupport
import com.meistercharts.color.Color
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontWeight
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.provider.ValueRangeProvider
import com.meistercharts.range.LinearValueRange
import com.meistercharts.style.Palette
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.charting.api.sanitizing.sanitize
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.cached
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiDoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.unit.other.px

/**
 * Contains conversion related methods
 */

/**
 * Converts this JavaScript [Size] object into a model Size object
 */
fun Size.toModelSize(): it.neckar.geometry.Size {
  require(width != null) {
    "no width provided"
  }
  require(height != null) {
    "no height provided"
  }

  width?.let { width ->
    height?.let { height ->
      return it.neckar.geometry.Size(width, height)
    } ?: throw IllegalArgumentException("No height provided")

  } ?: throw IllegalArgumentException("No width provided")
}

/**
 * Returns an empty list, if the array is empty
 */
fun Array<Size>.toModelSizes(): List<it.neckar.geometry.Size> {
  if (this.isEmpty()) {
    return emptyList()
  }

  return this.map {
    it.toModelSize()
  }
}

fun it.neckar.geometry.Size.toJs(): Size = object : Size {
  override val width: Double = this@toJs.width.sanitize()
  override val height: Double = this@toJs.height.sanitize()
}

/**
 * Converts this JavaScript [Insets] object into a model Insets object
 */
fun Insets.toModel(): com.meistercharts.model.Insets {
  return com.meistercharts.model.Insets(
    top?.sanitize() ?: 0.0,
    right?.sanitize() ?: 0.0,
    bottom?.sanitize() ?: 0.0,
    left?.sanitize() ?: 0.0
  )
}

/**
 * Applies all non-null values to the given insets.
 * Null values are ignored.
 */
fun com.meistercharts.model.Insets.withValues(jsInsets: @px Insets): com.meistercharts.model.Insets {
  return this.copy(
    top = jsInsets.top?.sanitize() ?: top,
    left = jsInsets.left?.sanitize() ?: left,
    right = jsInsets.right?.sanitize() ?: right,
    bottom = jsInsets.bottom?.sanitize() ?: bottom,
  )
}

fun Shadow?.toModel(): com.meistercharts.style.Shadow? {
  if (this == null) {
    return null
  }

  return com.meistercharts.style.Shadow(
    color = color.toColor() ?: com.meistercharts.style.Shadow.Default.color,
    blurRadius = blurRadius?.sanitize() ?: com.meistercharts.style.Shadow.Default.blurRadius,
    offsetX = offsetX?.sanitize() ?: com.meistercharts.style.Shadow.Default.offsetX,
    offsetY = offsetY?.sanitize() ?: com.meistercharts.style.Shadow.Default.offsetY,
  )
}

fun BorderRadius?.toModel(): com.meistercharts.model.BorderRadius? {
  if (this == null) {
    return null
  }

  return com.meistercharts.model.BorderRadius(
    topLeft?.sanitize() ?: 0.0,
    topRight?.sanitize() ?: 0.0,
    bottomRight?.sanitize() ?: 0.0,
    bottomLeft?.sanitize() ?: 0.0,
  )
}

private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.api.ConverterExtensions")


/**
 * Converts this JavaScript [ValueRange] object into a model ValueRange object
 */
fun ValueRange.toModel(): com.meistercharts.range.ValueRange {
  logger.debug("ValueRange.toModel", this)

  //ensure that the client uses the correct types
  val startSanitized = start.sanitize()
  val endSanitized = end.sanitize()

  //'linear' is the default range scale
  return when (this.scale?.sanitize() ?: ValueRangeScale.Linear) {
    ValueRangeScale.Linear -> com.meistercharts.range.ValueRange.linear(startSanitized, endSanitized)
    ValueRangeScale.Log10 -> com.meistercharts.range.ValueRange.logarithmic(startSanitized, endSanitized)
  }
}

/**
 * Converts this JavaScript [ValueRange] object into a model ValueRange object
 *
 * Enforces a linear value range; ignores [ValueRange.scale]
 */
fun ValueRange.toModelLinear(): LinearValueRange {
  return com.meistercharts.range.ValueRange.linear(start, end)
}

/**
 * Converts this JavaScript [PointConnectionStyle] object into [Dashes]
 */
fun PointConnectionStyle.toModel(): Dashes? {
  return when (this.sanitize()) {
    PointConnectionStyle.None -> null
    PointConnectionStyle.Continuous -> null
    PointConnectionStyle.Dotted -> Dashes.Dotted
    PointConnectionStyle.SmallDashes -> Dashes.SmallDashes
    PointConnectionStyle.LargeDashes -> Dashes.LargeDashes
  }
}

fun Side.toModel(): it.neckar.geometry.Side {
  return when (this.sanitize()) {
    Side.Left -> it.neckar.geometry.Side.Left
    Side.Right -> it.neckar.geometry.Side.Right
    Side.Top -> it.neckar.geometry.Side.Top
    Side.Bottom -> it.neckar.geometry.Side.Bottom
  }
}

/**
 * Converts this JavaScript [SamplingPeriod] object into a model SamplingPeriod object
 */
fun SamplingPeriod.toModel(): com.meistercharts.history.SamplingPeriod {
  return when (this.sanitize()) {
    SamplingPeriod.EveryMillisecond -> com.meistercharts.history.SamplingPeriod.EveryMillisecond
    SamplingPeriod.EveryTenMillis -> com.meistercharts.history.SamplingPeriod.EveryTenMillis
    SamplingPeriod.EveryHundredMillis -> com.meistercharts.history.SamplingPeriod.EveryHundredMillis
    SamplingPeriod.EverySecond -> com.meistercharts.history.SamplingPeriod.EverySecond
    SamplingPeriod.EveryTenSeconds -> com.meistercharts.history.SamplingPeriod.EveryTenSeconds
    SamplingPeriod.EveryMinute -> com.meistercharts.history.SamplingPeriod.EveryMinute
    SamplingPeriod.EveryTenMinutes -> com.meistercharts.history.SamplingPeriod.EveryTenMinutes
    SamplingPeriod.EveryHour -> com.meistercharts.history.SamplingPeriod.EveryHour
    SamplingPeriod.Every6Hours -> com.meistercharts.history.SamplingPeriod.Every6Hours
    SamplingPeriod.Every24Hours -> com.meistercharts.history.SamplingPeriod.Every24Hours
  }
}

fun com.meistercharts.history.SamplingPeriod.toJs(): SamplingPeriod {
  return when (this) {
    com.meistercharts.history.SamplingPeriod.EveryMillisecond -> SamplingPeriod.EveryMillisecond
    com.meistercharts.history.SamplingPeriod.EveryTenMillis -> SamplingPeriod.EveryTenMillis
    com.meistercharts.history.SamplingPeriod.EveryHundredMillis -> SamplingPeriod.EveryHundredMillis
    com.meistercharts.history.SamplingPeriod.EverySecond -> SamplingPeriod.EverySecond
    com.meistercharts.history.SamplingPeriod.EveryTenSeconds -> SamplingPeriod.EveryTenSeconds
    com.meistercharts.history.SamplingPeriod.EveryMinute -> SamplingPeriod.EveryMinute
    com.meistercharts.history.SamplingPeriod.EveryTenMinutes -> SamplingPeriod.EveryTenMinutes
    com.meistercharts.history.SamplingPeriod.EveryHour -> SamplingPeriod.EveryHour
    com.meistercharts.history.SamplingPeriod.Every6Hours -> SamplingPeriod.Every6Hours
    com.meistercharts.history.SamplingPeriod.Every24Hours -> SamplingPeriod.Every24Hours

    com.meistercharts.history.SamplingPeriod.Every5Days,
    com.meistercharts.history.SamplingPeriod.Every30Days,
    com.meistercharts.history.SamplingPeriod.Every90Days,
    com.meistercharts.history.SamplingPeriod.Every360Days,
    -> throw UnsupportedOperationException("$this is not supported")
  }
}

/**
 * Converts this JavaScript [TimeRange] object into a model TimeRange object
 */
@Suppress("RedundantRequireNotNullCall")
fun TimeRange.toModel(): com.meistercharts.time.TimeRange {
  requireNotNull(start) { "no start provided " }
  requireNotNull(end) { "no end provided " }
  return com.meistercharts.time.TimeRange(start, end)
}

/**
 * Converts a chart time range to a JS time range
 */
fun com.meistercharts.time.TimeRange.toJs(): TimeRange {
  val timeRange = js("{}") as TimeRange
  timeRange.asDynamic().start = start
  timeRange.asDynamic().end = end
  return timeRange
}

fun HistoryBucketDescriptor.toHistoryQueryDescriptorJs(): HistoryQueryDescriptor {
  return HistoryQueryDescriptor(
    start,
    end,
    bucketRange.samplingPeriod.distance
  )
}

/**
 * Converts this JavaScript [Zoom] object into a model Zoom object
 */
fun Zoom.toModel(): com.meistercharts.model.Zoom {
  requireNotNull(scaleX) { "no scale-x provided " }
  requireNotNull(scaleY) { "no scale-y provided " }
  return com.meistercharts.model.Zoom(scaleX ?: 1.0, scaleY ?: 1.0)
}

fun LineStyle.toModel(): com.meistercharts.algorithms.layers.linechart.LineStyle {
  var dashes: Dashes? = null
  var lineWidth = 1.0
  var lineColor = Palette.defaultGray

  this.color.toColor()?.let {
    lineColor = it
  }

  this.type?.let {
    dashes = it.toModel()
  }
  this.width?.let {
    lineWidth = it
  }
  return com.meistercharts.algorithms.layers.linechart.LineStyle(lineWidth = lineWidth, dashes = dashes, color = lineColor)
}

/**
 * Converts this JavaScript [FontStyle] object into a [FontDescriptorFragment]
 */
fun FontStyle.toFontDescriptorFragment(): FontDescriptorFragment {
  var descriptorFragment = FontDescriptorFragment.empty

  family?.let {
    parseCssFontFamily(it)?.let { fontFamily ->
      descriptorFragment = descriptorFragment.withFamily(fontFamily)
    }
  }

  style?.let {
    parseCssFontStyle(it)?.let { fontStyle ->
      descriptorFragment = descriptorFragment.withStyle(fontStyle)
    }
  }

  weight?.let {
    descriptorFragment = descriptorFragment.withWeight(FontWeight(it))
  }

  size?.let {
    descriptorFragment = descriptorFragment.withSize(FontSize(it))
  }

  return descriptorFragment
}

private fun com.meistercharts.algorithms.layers.AxisConfiguration.applyAxisStyle(jsStyle: AxisStyle?) {
  if (jsStyle == null) {
    return
  }

  //debug code: uncomment the following lines to check whether the custom element sets all relevant properties
  //this.side = it.neckar.geometry.Side.Bottom
  //this.tickLabelGap = 30.0
  //this.tickLength = 30.0
  //this.lineColor = Color.red
  //this.axisLineWidth = 5.0
  //this.setTitle("Hello World")
  //this.titleColor = Color.goldenrod
  //this.titleFont = FontDescriptorFragment.XL

  jsStyle.axisSide?.toModel()?.let {
    this.side = it
  }

  jsStyle.axisSize?.let {
    this.size = it
  }

  jsStyle.tickLabelGap?.let {
    this.tickLabelGap = it
  }

  jsStyle.tickLength?.let {
    this.tickLength = it
  }

  jsStyle.axisLineColor.toColor()?.let {
    this.lineColor = it.asProvider()
  }

  jsStyle.axisLineWidth?.let {
    this.axisLineWidth = it
  }

  jsStyle.title?.let {
    this.setTitle(it)
  }

  jsStyle.titleColor.toColor()?.let {
    this.titleColor = it.asProvider()
  }

  jsStyle.titleFont?.toFontDescriptorFragment()?.let {
    this.titleFont = it
  }

  jsStyle.tickFont?.toFontDescriptorFragment()?.let {
    this.tickFont = it
  }

  jsStyle.tickLabelColor.toColor()?.let {
    this.tickLabelColor = it.asProvider()
  }
}

/**
 * Applies the JS style to the time axis
 */
fun TimeAxisLayer.Configuration.applyTimeAxisStyle(jsStyle: TimeAxisStyle?) {
  applyAxisStyle(jsStyle)

  //The offset should use the same font as the tick labels
  jsStyle?.tickFont?.toFontDescriptorFragment()?.let {
    this.offsetTickFont = it
  }

  jsStyle?.offsetTickLabelColor?.toColor()?.let {
    this.offsetTickLabelColor = it
  }

  jsStyle?.offsetAreaSize?.let {
    this.offsetAreaSize = it
  }

  jsStyle?.offsetAreaTickLabelGap?.let {
    this.offsetAreaTickLabelGap = it
  }

  jsStyle?.offsetAreaFills?.let { colorCodes ->
    this.offsetAreaFills = MultiProvider.forListModulo(colorCodes.map { it.toColor() }, Color.silver)
  }
}

/**
 * Applies the JS style to the category axis
 *
 * The title-location will always be set to the top of the axis.
 */
fun CategoryAxisLayer.Configuration.applyCategoryAxisStyle(jsStyle: CategoryAxisStyle?) {
  if (jsStyle == null) {
    return
  }

  applyAxisStyle(jsStyle)

  jsStyle.titleGap?.let {
    this.titleGap = it
  }

  jsStyle.justifyTickContent?.sanitize()?.let {
    //Problem: the images are set by the properties of the BarChartData interface prior to
    //setting the axisLabelPainter at this point. Hence, we must store the old images provider
    //and restore it after creating the new axisLabelPainter
    when (it) {
      JustifyTickContent.SpaceGreedily -> {
        if ((this.axisLabelPainter is DefaultCategoryAxisLabelPainter)) {
          //Copy images from old painter
          val oldImagesProvider = (this.axisLabelPainter as DefaultCategoryAxisLabelPainter).style.imagesProvider
          this.axisLabelPainter = GreedyCategoryAxisLabelPainter { imagesProvider = oldImagesProvider }
        }

        require(this.axisLabelPainter is GreedyCategoryAxisLabelPainter) {
          "Invalid instance of axisLabelPainter: ${axisLabelPainter::class.simpleName}"
        }

        this.labelVisibleCondition = LabelVisibleCondition.all
      }

      JustifyTickContent.SpaceEvenly -> {
        if (this.axisLabelPainter is GreedyCategoryAxisLabelPainter) {
          //Copy images from old painter
          val oldImagesProvider = (this.axisLabelPainter as GreedyCategoryAxisLabelPainter).style.imagesProvider
          this.axisLabelPainter = DefaultCategoryAxisLabelPainter { imagesProvider = oldImagesProvider }
        }

        require(this.axisLabelPainter is DefaultCategoryAxisLabelPainter) {
          "Invalid instance of axisLabelPainter: ${axisLabelPainter::class.simpleName}"
        }

        this.labelVisibleCondition = LabelVisibleCondition.all
      }

      JustifyTickContent.SpaceGreedilyPreferRoundIndices -> {
        if ((this.axisLabelPainter is DefaultCategoryAxisLabelPainter)) {
          //Copy images from old painter
          val oldImagesProvider = (this.axisLabelPainter as DefaultCategoryAxisLabelPainter).style.imagesProvider
          this.axisLabelPainter = GreedyCategoryAxisLabelPainter { imagesProvider = oldImagesProvider }
        }

        require(this.axisLabelPainter is GreedyCategoryAxisLabelPainter) {
          "Invalid instance of axisLabelPainter: ${axisLabelPainter::class.simpleName}"
        }

        this.labelVisibleCondition = LabelVisibleCondition { labelIndex, labelCount, _ ->
          val factor = when {
            labelCount < 10 -> 1
            labelCount < 50 -> 5
            labelCount < 200 -> 10
            labelCount < 500 -> 50
            else -> 100
          }

          labelIndex.value % factor == factor - 1
        }
      }
    }
  }

  jsStyle.minTickLabelDistance?.let {
    this.axisLabelPainter.setMinTickLabelDistance(it)
  }

  jsStyle.iconSize?.let {
    this.axisLabelPainter.setImageSize(it)
  }
}

fun CategoryAxisLabelPainter.setMinTickLabelDistance(distance: @px Double) {
  if (this is DefaultCategoryAxisLabelPainter) {
    this.style.labelWithinCategoryGap = distance
  } else if (this is GreedyCategoryAxisLabelPainter) {
    this.style.categoryLabelGap = distance
  }
}

fun CategoryAxisLabelPainter.setImageSize(imageSize: @px Double) {
  if (this is DefaultCategoryAxisLabelPainter) {
    this.style.imageSize = it.neckar.geometry.Size(imageSize, imageSize)
  } else if (this is GreedyCategoryAxisLabelPainter) {
    this.style.imageSize = it.neckar.geometry.Size(imageSize, imageSize)
  }
}

fun CategoryAxisLabelPainter.setImagesProvider(imagesProvider: MultiProvider<CategoryIndex, Paintable?>) {
  if (this is DefaultCategoryAxisLabelPainter) {
    this.style.imagesProvider = imagesProvider
  } else if (this is GreedyCategoryAxisLabelPainter) {
    this.style.imagesProvider = imagesProvider
  }
}

/**
 * Applies the JS style to the value axis.
 *
 * The title-location will always be set to the top of the axis and
 * the tick-labels will always be truncated with an ellipsis.
 */
fun ValueAxisLayer.Configuration.applyValueAxisStyle(jsStyle: ValueAxisStyle?) {
  if (jsStyle == null) {
    return
  }

  jsStyle.presentationType?.sanitize()?.let {
    when (it) {
      ValueAxisPresentationType.Default -> {
        this.showAxisLine()
        this.showTicks()
        if (this.ticks == ConstantTicksProvider.only0) {
          //Do *not* overwrite other tick providers
          //There might be a logarithmic tick provider set
          this.ticks = TickProvider.linear
        }
      }

      ValueAxisPresentationType.Only0 -> {
        this.hideAxisLine()
        this.hideTicks()
        this.ticks = ConstantTicksProvider.only0
      }
    }
  }

  applyAxisStyle(jsStyle)

  this.valueLabelStringShortener = CanvasStringShortener.exactButSlowTruncateToLength

  //Title gap is no longer relevant, since we are using top layer
  //jsStyle?.titleGap

  //debug code: uncomment the following lines to check whether the custom element sets all relevant properties
  //this.ticks = this.ticks.withMaxNumberOfTicks(0)
  //this.tickFormat = percentageFormat2digits

  jsStyle.maxTickCount?.let {
    this.ticks = this.ticks.withMaxNumberOfTicks(it)
  }

  jsStyle.ticksFormat?.toNumberFormat()?.let {
    this.ticksFormat = it
  }
}

/**
 * Applies the title style to the top title layer
 */
fun AxisTopTopTitleLayer.Configuration.applyTitleStyle(jsStyle: AxisStyle) {
  jsStyle.titleColor.toColor()?.let {
    this.titleColor = it.asProvider()
  }

  jsStyle.titleFont?.toFontDescriptorFragment()?.let {
    this.titleFont = it
  }

  jsStyle.titleGap?.let {
    this.titleGapVertical = it
  }
}

/**
 * Converts this [NumberFormat] into a [CachedNumberFormat]
 */
fun com.meistercharts.api.NumberFormat.toNumberFormat(): CachedNumberFormat {
  val ref = this
  return object : NumberFormat {
    override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
      return ref.format(value, i18nConfiguration.formatLocale.locale)
    }
  }.cached()
}

fun CrossWireLayer.Configuration.applyCrossWireStyle(jsStyle: CrossWireStyle?) {
  if (jsStyle == null) {
    return
  }
  jsStyle.wireColor?.toColor()?.let {
    wireColor = it
  }

  jsStyle.wireWidth?.let {
    wireWidth = it
  }
}

/**
 * Applies the axis style
 */
fun CategoryAxisLayer.Configuration.applyEnumAxisStyle(jsStyle: EnumAxisStyle?) {
  applyAxisStyle(jsStyle)

  if (jsStyle == null) {
    return
  }

  jsStyle.labelWrapMode?.let { jsWrapMode ->
    this.axisLabelPainter = DefaultCategoryAxisLabelPainter {
      this.wrapMode = jsWrapMode.toModel()
    }
  }
}

fun CategoryAxisLayer.Configuration.applyDiscreteAxisStyle(jsStyle: DiscreteAxisStyle?) {
  applyAxisStyle(jsStyle)

  if (jsStyle == null) {
    return
  }

  jsStyle.labelWrapMode?.let { jsWrapMode ->
    this.axisLabelPainter = DefaultCategoryAxisLabelPainter {
      this.wrapMode = jsWrapMode.toModel()
    }
  }

  jsStyle.backgroundColor?.toColor()?.let { backgroundColor ->
    this.background = { backgroundColor }
  }
}

/**
 * Converts the array to a threshold values provider
 */
fun Array<Threshold>?.toThresholdValuesProvider(): @Domain DoublesProvider {
  return if (this == null) {
    DoublesProvider.empty
  } else {
    DoublesProvider.forValues(map { jsThreshold -> jsThreshold.value })
  }
}

/**
 * Wraps the array in a multi provider for thresholds.
 * Splits the provided string at "\n"
 */
fun Array<Threshold>?.toThresholdLabelsProvider(): HudLabelsProvider {
  return if (this == null) {
    MultiProvider1.empty()
  } else {
    MultiProvider.forListOrException<HudElementIndex, List<String>>(map { jsThreshold ->
      jsThreshold.label.split("\n")
    })
  }
}

/**
 * Applies the JS thresholds to the thresholds-support
 */
fun <Key> ThresholdsSupport<Key>.applyThresholdStyles(jsThresholds: Array<Threshold>?, key: Key) {
  if (jsThresholds == null) {
    return
  }

  val hudLayer = getHudLayer(key)

  hudLayer.configuration.boxStyles = MultiProvider.forListModulo(
    jsThresholds.map { jsThreshold -> jsThreshold.labelBoxStyle.toModel() }
  )
  hudLayer.configuration.boxStylesActive = MultiProvider.forListModulo(
    //TODO remove the fallback asap
    jsThresholds.map { jsThreshold -> jsThreshold.labelBoxStyleActive?.toModel() ?: jsThreshold.labelBoxStyle.toModel() }
  )

  hudLayer.configuration.arrowHeadLength = MultiDoublesProvider.forArrayModulo(
    jsThresholds.map { jsThreshold -> jsThreshold.arrowHeadLength }.toDoubleArray()
  )

  hudLayer.configuration.arrowHeadWidth = MultiDoublesProvider.forArrayModulo(
    jsThresholds.map { jsThreshold -> jsThreshold.arrowHeadWidth }.toDoubleArray()
  )

  hudLayer.configuration.textColors = MultiProvider.forListModulo(
    jsThresholds.map { jsThreshold -> jsThreshold.labelColor.toColor() }
  )
  hudLayer.configuration.textColorsActive = MultiProvider.forListModulo(
    //TODO remove the fallback asap
    jsThresholds.map { jsThreshold -> jsThreshold.labelColorActive.toColor() ?: jsThreshold.labelColor.toColor() }
  )

  hudLayer.configuration.textFonts = MultiProvider.forListModulo(
    jsThresholds.map { jsThreshold -> jsThreshold.labelFont.toFontDescriptorFragment() }
  )

  //Threshold lines layer
  val thresholdLinesLayer = getThresholdLinesLayer(key)

  thresholdLinesLayer.configuration.lineStyles = MultiProvider.forListModulo(
    jsThresholds.map { jsThreshold -> jsThreshold.lineStyle.toModel() }
  )

  thresholdLinesLayer.configuration.activeLineStyles = MultiProvider.forListModulo(
    //TODO remove the fallback asap
    jsThresholds.map { jsThreshold -> jsThreshold.lineStyleActive?.toModel() ?: jsThreshold.lineStyle.toModel() }
  )
}

/**
 * Applies the JavaScript style [jsStyle] to this [DomainRelativeGridLayer]'s style
 */
fun DomainRelativeGridLayer.Configuration.applyLinesStyle(
  jsStyle: GridStyle?,
  valueRangeProvider: ValueRangeProvider,
) {

  //debug code: uncomment the following lines to check whether the custom element sets all relevant properties
  //this.lineStyles = { _: Double -> com.meistercharts.algorithms.layers.linechart.LineStyle(Color.green, 8.0, Dashes.LargeDashes) }

  jsStyle?.lineColors?.let { gridLineColorProvider ->
    lineStyles = { value: @DomainRelative Double ->
      val domainValue = valueRangeProvider().toDomain(value)
      com.meistercharts.algorithms.layers.linechart.LineStyle(color = gridLineColorProvider.lineColor(domainValue).toColor())
    }
  }
}

/**
 * Applies the JavaScript style [jsStyle] to this [GridLayer]'s style
 */
fun GridLayer.Configuration.applyLinesStyle(jsStyle: GridStyle?) {
  jsStyle?.lineColors?.let { gridLineColorProvider ->
    lineStyles = MultiProvider { index ->
      com.meistercharts.algorithms.layers.linechart.LineStyle(color = gridLineColorProvider.lineColor(index.toDouble()).toColor())
    }
  }
}

/**
 * Turns the given JavaScript box-styles into a provider of colors
 */
fun <IndexContext> toColors(jsBoxStyles: Array<BoxStyle>): MultiProvider<IndexContext, Color> {
  val colors = jsBoxStyles.map { jsBoxStyle ->
    jsBoxStyle.color.toColor() ?: Color.white
  }
  return MultiProvider.forListModulo(colors)
}

/**
 * Turns the given JavaScript box-styles into a provider of model-box-styles
 */
fun <IndexContext> toBoxStyles(jsBoxStyles: Array<BoxStyle>): MultiProvider<IndexContext, com.meistercharts.style.BoxStyle> {
  val boxStyles = jsBoxStyles.map { jsBoxStyle ->
    jsBoxStyle.toModel()
  }

  return MultiProvider.forListModulo(boxStyles)
}

/**
 * Turns the given JavaScript box-style into a model-box-style
 */
fun BoxStyle.toModel(): com.meistercharts.style.BoxStyle {
  val fill = backgroundColor.toColor()
  val borderColor = borderColor.toColor()
  val padding = padding?.toModel() ?: com.meistercharts.model.Insets(5.0, 7.0, 5.0, 7.0)
  val shadow = shadow.toModel()
  val radii = borderRadius.toModel()

  return com.meistercharts.style.BoxStyle(fill = fill, borderColor = borderColor, padding = padding, shadow = shadow, radii = radii)
}

/**
 * Turns the given JavaScript wrap-mode into a model wrap-mode
 */
fun WrapMode.toModel(): com.meistercharts.algorithms.layers.barchart.LabelWrapMode {
  return when (this.sanitize()) {
    WrapMode.NoWrap -> com.meistercharts.algorithms.layers.barchart.LabelWrapMode.NoWrap
    WrapMode.IfNecessary -> com.meistercharts.algorithms.layers.barchart.LabelWrapMode.IfNecessary
  }
}

/**
 * Converts a string to a color
 */
fun String?.toColor(): Color? {
  if (this == null) {
    return null
  }

  return this.toColor()
}

/**
 * Converts a string to a color
 */
fun String.toColor(): Color {
  return Color.unparsed(this.sanitize())
}

/**
 * Applies the style to an overflow indicator painter
 */
fun OverflowIndicatorPainter.applyStyle(jsStyle: OverflowIndicatorStyle) {
  this.configuration.applyDefaultIndicators(
    jsStyle.fill.toColor(),
    jsStyle.stroke.toColor(),
    jsStyle.strokeWidth ?: 1.0,
    jsStyle.arrowHeadLength,
    jsStyle.arrowHeadWidth
  )
}

fun com.meistercharts.api.EnumAggregationMode.toModel(): EnumAggregationMode {
  return when (this) {
    com.meistercharts.api.EnumAggregationMode.ByOrdinal -> EnumAggregationMode.ByOrdinal
    com.meistercharts.api.EnumAggregationMode.MostTime -> EnumAggregationMode.MostTime
  }
}
