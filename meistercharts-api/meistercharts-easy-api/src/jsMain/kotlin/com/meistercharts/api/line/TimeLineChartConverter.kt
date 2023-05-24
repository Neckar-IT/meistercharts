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
package com.meistercharts.api.line

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.linechart.PointStyle
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.SimpleAreaBetweenLinesPainter
import com.meistercharts.api.DataSeriesNumberFormat
import com.meistercharts.api.PointConnectionStyle
import com.meistercharts.api.PointType
import com.meistercharts.api.forEnumValueFromJsDouble
import com.meistercharts.api.toColor
import com.meistercharts.api.toHistoryEnum
import com.meistercharts.api.toModel
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.chunk
import com.meistercharts.painter.AreaBetweenLinesPainter
import com.meistercharts.painter.CirclePointPainter
import com.meistercharts.painter.PointPainter
import com.meistercharts.painter.PointStylePainter
import it.neckar.open.charting.api.sanitizing.sanitize
import it.neckar.open.collections.IntMap
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.cached
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider.Companion.invoke

object TimeLineChartConverter {
  fun toValueRangeProvider(jsDecimalDataSeriesStyles: Array<DecimalDataSeriesStyle>): MultiProvider<DecimalDataSeriesIndex, ValueRange> {
    val valueRanges = jsDecimalDataSeriesStyles.map { jsDataSeriesConfiguration ->
      jsDataSeriesConfiguration.valueRange?.toModel() ?: ValueRange.default
    }
    return MultiProvider { index -> valueRanges.getOrNull(index) ?: ValueRange.default }
  }

  fun toLineStyles(jsLineStyles: Array<TimeLineChartLineStyle>): MultiProvider<DecimalDataSeriesIndex, LineStyle> {
    val lineStyles = jsLineStyles.map { jsLineStyle ->
      toLineStyle(jsLineStyle.lineStyle)
    }

    return MultiProvider.forListModulo(lineStyles, LineStyle.Continuous)
  }

  private fun toLineStyle(jsLineStyle: com.meistercharts.api.LineStyle?): LineStyle {
    if (jsLineStyle == null) {
      return LineStyle.Continuous
    }
    var lineStyle = LineStyle.Continuous
    jsLineStyle.color?.let {
      lineStyle = lineStyle.copy(color = it.toColor())
    }
    jsLineStyle.width?.let {
      lineStyle = lineStyle.copy(lineWidth = it)
    }
    jsLineStyle.type?.let {
      lineStyle = when (it) {
        PointConnectionStyle.None -> lineStyle // don't do anything; this line type must be handled separately elsewhere
        PointConnectionStyle.Continuous -> lineStyle.copy(dashes = null)
        PointConnectionStyle.Dotted -> lineStyle.copy(dashes = Dashes.Dotted)
        PointConnectionStyle.SmallDashes -> lineStyle.copy(dashes = Dashes.SmallDashes)
        PointConnectionStyle.LargeDashes -> lineStyle.copy(dashes = Dashes.LargeDashes)
      }
    }
    return lineStyle
  }

  /**
   * Convert to point painters
   */
  fun toPointPainters(jsLineStyles: Array<TimeLineChartLineStyle>): MultiProvider<DecimalDataSeriesIndex, PointPainter?> {
    val pointPainters: List<PointPainter?> = jsLineStyles.map { jsTimelineChartLineStyle ->
      val pointSize = jsTimelineChartLineStyle.pointSize?.sanitize() ?: 2.0

      when (jsTimelineChartLineStyle.pointType?.sanitize()) {
        PointType.None -> null
        PointType.Dot -> {
          PointStylePainter(PointStyle.Dot, pointSize, false, false)
        }

        PointType.Cross -> {
          PointStylePainter(PointStyle.Cross, pointSize, false, false)
        }

        PointType.Cross45 -> PointStylePainter(PointStyle.Cross45Degrees, pointSize, false, false)

        PointType.Circle -> CirclePointPainter(false, false) {
          this.pointSize = pointSize
          this.fill //TODO update
          this.stroke //TODO update
        }

        null -> null
      }
    }

    return MultiProvider.forListOr(pointPainters, null)
  }

  fun toMinMaxAreaPainters(jsLineStyles: Array<TimeLineChartLineStyle>): MultiProvider<DecimalDataSeriesIndex, AreaBetweenLinesPainter?> {
    val areaPainters: List<AreaBetweenLinesPainter?> = jsLineStyles.map { jsTimelineChartLineStyle ->
      val color = jsTimelineChartLineStyle.minMaxAreaColor.toColor()

      when (jsTimelineChartLineStyle.showMinMaxArea.sanitize()) {
        true -> {
          SimpleAreaBetweenLinesPainter(false, false)
        }

        false -> null
        null -> null
      }
    }

    return MultiProvider.forListOrNull(areaPainters)
  }

  fun toMinMaxAreaColors(jsLineStyles: Array<TimeLineChartLineStyle>, fallbackColor: Color = Color.lightgray): MultiProvider<DecimalDataSeriesIndex, Color> {
    val colors = jsLineStyles.map { jsTimelineChartLineStyle ->
      jsTimelineChartLineStyle.minMaxAreaColor.toColor() ?: fallbackColor
    }

    return MultiProvider.forListOr(colors, fallbackColor)
  }

  /**
   * Converts the given [Sample] into a [HistoryChunk]
   */
  fun toHistoryChunk(jsSample: Sample, historyConfiguration: HistoryConfiguration): HistoryChunk? {
    return toHistoryChunk(arrayOf(jsSample), historyConfiguration)
  }

  /**
   * Converts the given [Sample]s into a [HistoryChunk]
   */
  fun toHistoryChunk(jsSamples: Array<Sample>, historyConfiguration: HistoryConfiguration): HistoryChunk? {
    if (jsSamples.isEmpty()) {
      return null
    }

    //Sort the samples by timestamp
    val sortedSamples = jsSamples.sortedBy {
      it.timestamp
    }

    return historyConfiguration.chunk(sortedSamples.size) { timestampIndex ->
      val jsSample = sortedSamples[timestampIndex.value]

      addValues(
        timestamp = jsSample.timestamp,
        decimalValuesProvider = { dataSeriesIndex ->
          jsSample.decimalValues?.get(dataSeriesIndex.value) ?: HistoryChunk.NoValue
        },
        enumValuesProvider = { dataSeriesIndex ->
          val jsEnumValue = jsSample.enumValues?.get(dataSeriesIndex.value)
          if (jsEnumValue == null) HistoryEnumSet.NoValue else HistoryEnumSet.forEnumValueFromJsDouble(jsEnumValue.sanitize())
        },
        referenceEntryStatusProvider = { TODO() },
        referenceEntryIdProvider = { TODO() },
        referenceEntriesDataMap = ReferenceEntriesDataMap.generated, //TODO
      )
    }
  }

  /**
   * Creates the history configuration from the JS data series objects
   */
  fun toHistoryConfiguration(
    jsDecimalDataSeries: Array<DecimalDataSeries>,
    jsEnumDataSeries: Array<EnumDataSeries>,
  ): HistoryConfiguration {
    return historyConfiguration {
      jsDecimalDataSeries.fastForEach { jsConfig ->
        decimalDataSeries(DataSeriesId(jsConfig.id), TextKey(jsConfig.name))
      }
      jsEnumDataSeries.fastForEach { jsConfig ->
        enumDataSeries(DataSeriesId(jsConfig.id), TextKey(jsConfig.name), jsConfig.enumConfiguration.toHistoryEnum())
      }
    }
  }

  fun toCrossWireFormat(dataSeriesNumberFormat: DataSeriesNumberFormat): MultiProvider<DecimalDataSeriesIndex, CachedNumberFormat> {
    val map = IntMap<CachedNumberFormat>()

    return invoke { dataSeriesIndex ->
      map.getOrPut(dataSeriesIndex) {
        object : NumberFormat {
          override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
            return dataSeriesNumberFormat.format(dataSeriesIndex, value, i18nConfiguration.formatLocale.locale)
          }
        }.cached()
      }
    }

  }
}
