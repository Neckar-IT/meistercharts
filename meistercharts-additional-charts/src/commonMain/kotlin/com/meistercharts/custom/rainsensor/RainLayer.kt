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
package com.meistercharts.custom.rainsensor

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.animation.Animated
import com.meistercharts.canvas.animation.AnimationRepeatType
import com.meistercharts.canvas.animation.AnimationState
import com.meistercharts.canvas.animation.ChartAnimation
import com.meistercharts.canvas.animation.Tween
import com.meistercharts.canvas.currentFrameTimestamp
import com.meistercharts.canvas.layout.cache.DoubleCache
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.kotlin.lang.fastMap
import it.neckar.open.kotlin.lang.toIntFloor
import it.neckar.open.provider.MultiDoublesProvider
import it.neckar.open.provider.MultiProviderIndexContextAnnotation
import com.meistercharts.resources.LocalResourcePaintable
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms
import kotlin.random.Random

/**
 * A layer that shows rain drops (or snow)
 */
class RainLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  val style: Style = Style().also(styleConfiguration)

  /**
   * Contains the positions for each of the parallax layers (one behind each other)
   */
  var layerPositions: @pct DoubleCache = DoubleCache()

  override fun initialize(paintingContext: LayerPaintingContext) {
    super.initialize(paintingContext)

    ChartAnimation(object : Animated {
      val tweens: List<Tween> = data.parallaxLayersCount.fastMap {
        Tween(currentFrameTimestamp - style.random.nextDouble() * 1000.0, data.dropDuration + it * 200.0, repeatType = AnimationRepeatType.Repeat)
      }

      override fun animationFrame(frameTimestamp: Double): AnimationState {
        layerPositions.ensureSize(data.parallaxLayersCount)

        tweens.forEachIndexed { index, tween ->
          layerPositions[index] = tween.elapsedRatioForTime(frameTimestamp)
        }

        return AnimationState.Active
      }
    }).also {
      paintingContext.chartSupport.onRefresh(it)
    }
  }

  /**
   * Contains the random x offsets for each drop
   */
  private val randomOffsetCacheHorizontal: @Zoomed MultiDoublesProvider<ParallaxLayerIndex> = MultiDoublesProvider.forArrayModulo(
    DoubleArray(100) {
      (style.random.nextDouble() - 0.5) * style.offsetAbsoluteHorizontalPerDrop
    }
  )

  /**
   * Contains the y offsets
   */
  private val randomOffsetCacheVertical: @Zoomed MultiDoublesProvider<ParallaxLayerIndex> = MultiDoublesProvider.forArrayModulo(
    DoubleArray(100) {
      (style.random.nextDouble() - 0.5) * style.offsetAbsoluteVerticalPerDrop
    }
  )


  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    //The number of drop columns
    val dropColumnsCount = (gc.width / style.spaceHorizontalPerDrop).toIntFloor()
    //the number of rows
    val dropRowsCount = (gc.height / style.spaceVerticalPerDrop).toIntFloor()

    //The delta x
    @Zoomed val deltaXAbs = gc.width / dropColumnsCount
    val deltaYPercentage: @pct Double = 1.0 / dropRowsCount

    @Zoomed val offsetHorizontalPerLayer = deltaXAbs / (data.parallaxLayersCount + 1)
    @pct val offsetVerticalPerLayer = deltaYPercentage / (data.parallaxLayersCount + 1)

    layerPositions.fastForEachIndexed { layerIndex, value: @pct Double ->
      dropRowsCount.fastFor { row ->
        @pct val percentageForRow = (1.0 - deltaYPercentage)
        @Window val y = gc.height * ((value + percentageForRow * row + offsetVerticalPerLayer * layerIndex) % 1.0)

        dropColumnsCount.fastFor { column ->
          @Window val x = deltaXAbs * (column + 0.5) + offsetHorizontalPerLayer * layerIndex

          @Zoomed val randomY = randomOffsetCacheVertical.valueAt(row * 10 + column)
          @Zoomed val randomX = randomOffsetCacheHorizontal.valueAt(row * 10 + column)

          style.rainDrop.paint(paintingContext, x + randomX, (y + randomY) % gc.height)
        }
      }
    }
  }


  @MustBeDocumented
  @Retention(AnnotationRetention.SOURCE)
  @MultiProviderIndexContextAnnotation
  annotation class ParallaxLayerIndex


  class Data(
    /**
     * The number of layers - for the parallax effect
     */
    val parallaxLayersCount: Int = 3,

    /**
     * The duration one drop takes from top to bottom
     */
    val dropDuration: @ms Double = 2_000.0,
  ) {
  }

  class Style {
    /**
     * The space per drop on the x axis
     */
    var spaceHorizontalPerDrop: @Zoomed Double = 140.0

    /**
     * The space per drop on the y axis
     */
    var spaceVerticalPerDrop: @Zoomed Double = 170.0

    var rainDrop: LocalResourcePaintable = RainSensorResources.rainDrop

    /**
     * The random generator that is used to calculate the offset
     */
    var random: Random = it.neckar.open.kotlin.lang.random

    /**
     * The total distance (from min to max) a raindrop may be offset vertically
     */
    var offsetAbsoluteHorizontalPerDrop: @Zoomed Double = 150.0

    var offsetAbsoluteVerticalPerDrop: @Zoomed Double = 150.0
  }
}
