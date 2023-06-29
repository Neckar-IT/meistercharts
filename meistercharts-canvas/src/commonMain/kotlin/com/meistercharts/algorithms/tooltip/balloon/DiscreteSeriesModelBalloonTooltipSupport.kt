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
package com.meistercharts.algorithms.tooltip.balloon

import com.meistercharts.algorithms.layers.LayerVisibilityAdapter
import com.meistercharts.algorithms.layers.legend.HeadlineAbovePaintable
import com.meistercharts.algorithms.layers.legend.LegendEntryIndex
import com.meistercharts.algorithms.layers.legend.SymbolAndLabelLegendPaintable
import com.meistercharts.algorithms.layers.legend.SymbolAndLabelLegendPaintable.Companion.defaultSymbols
import com.meistercharts.algorithms.layers.legend.withHeadline
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.color.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.textService
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.model.Size
import it.neckar.open.i18n.DefaultTextService
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextService
import it.neckar.open.i18n.isEmpty
import it.neckar.open.i18n.resolve
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.provider.SizedProvider1
import it.neckar.open.unit.number.MayBeNegative

/**
 * Balloon tooltip support for discrete series.
 * Specific implementation for discrete series
 */
class DiscreteSeriesModelBalloonTooltipSupport(
  val tooltipPlacement: DiscreteSeriesBalloonTooltipPlacementSupport,

  /**
   * Provides the headline to be used in the tooltip
   */
  headline: (textService: TextService, i18nConfiguration: I18nConfiguration) -> String,

  /**
   * Returns the reference entry ID
   */
  val referenceEntryIdProvider: () -> @MayBeNoValueOrPending ReferenceEntryId,
  /**
   * Returns the reference entry data - only useful if [referenceEntryIdProvider] has provided an entry id
   */
  val referenceEntryDataProvider: () -> ReferenceEntryData?,

  statusProvider: () -> @MayBeNoValueOrPending HistoryEnumSet,
  statusEnumProvider: () -> HistoryEnum?,

  /**
   * Provides the status colors
   */
  val statusColor: () -> Color,
) {

  /**
   * Paints the symbols and the labels
   */
  val symbolAndLegendPaintable: SymbolAndLabelLegendPaintable = SymbolAndLabelLegendPaintable(
    labels = object : SizedProvider1<String, ChartSupport> {
      private var statusEnum: HistoryEnum? = null
      private var textKey: TextKey? = null

      override fun size(param1: ChartSupport): Int {
        statusEnum = statusEnumProvider()
        val hasStatusEnum = statusEnum != null

        textKey = referenceEntryDataProvider()?.label
        val hasTextKey = textKey.isEmpty().not()

        return when {
          hasStatusEnum && hasTextKey -> 2
          hasStatusEnum || hasTextKey -> 1
          else -> 0
        }
      }

      override fun valueAt(index: Int, param1: ChartSupport): String {
        val textService = param1.textService
        val i18nConfiguration = param1.i18nConfiguration

        //val referenceEntryId = referenceEntryIdProvider()

        return when (index) {
          0 -> {
            //Text key or status?
            if (textKey.isEmpty()) {
              formatStatusEnum(statusEnum, textService, i18nConfiguration) //No text key, return the status enum
            } else {
              textKey!!.resolve(textService, i18nConfiguration)
            }
          }

          1 -> {
            formatStatusEnum(statusEnum, textService, i18nConfiguration)
          }

          else -> throw IllegalArgumentException("Invalid index $index")
        }
      }

      private fun formatStatusEnum(statusEnum: HistoryEnum?, textService: DefaultTextService, i18nConfiguration: I18nConfiguration): String {
        @MayBeNoValueOrPending val status = statusProvider()

        return when {
          statusEnum == null -> throw IllegalStateException("no status enum found")
          status.isPending() -> status.toString()
          status.isNoValue() -> status.toString()
          else -> statusEnum.value(status.firstSetOrdinal()).key.resolve(textService, i18nConfiguration)
        }
      }
    },
    symbols = createSymbolsProvider(),
  )

  /**
   * Creates the symbols provider that is used by the [symbolAndLegendPaintable]
   * This method can be called later to recreate the provider with a different symbol size
   */
  private fun createSymbolsProvider(symbolSize: Size = Size.PX_16): MultiProvider<LegendEntryIndex, Paintable> {
    return defaultSymbols(symbolSize = symbolSize, symbolColors = { index: Int ->
      if (index == 0 && referenceEntryDataProvider()?.label.isEmpty().not()) {
        Color.transparent
      } else {
        statusColor()
      }
    })
  }

  /**
   * The tooltip content paintable
   */
  val tooltipContentPaintable: HeadlineAbovePaintable<SymbolAndLabelLegendPaintable> = symbolAndLegendPaintable.withHeadline { textService, i18nConfiguration ->
    headline(textService, i18nConfiguration)
  }

  /**
   * Applies the symbol size for the legend
   */
  fun applyLegendSymbolSize(symbolSize: Size) {
    symbolAndLegendPaintable.configuration.symbols = createSymbolsProvider(symbolSize)
  }

  /**
   * Creates the tooltip layer
   */
  fun createTooltipLayer(): LayerVisibilityAdapter<BalloonTooltipLayer> {
    return BalloonTooltipLayer(
      BalloonTooltipLayer.Configuration(
        coordinates = tooltipPlacement.coordinates,
        tooltipContent = MultiProvider1.always(tooltipContentPaintable)
      )
    ) {
    }.also {
      it.tooltipPainter.configuration.noseSide = {
        tooltipPlacement.tooltipSide.flipped()
      }
    }.visibleIf {
      referenceEntryIdProvider() != ReferenceEntryId.NoValue
    }
  }

  /**
   * Annotation that identifies providers that are filtered and only contain the finite elements
   */
  @Target(AnnotationTarget.TYPE)
  annotation class OnlyFiniteIndex
}
