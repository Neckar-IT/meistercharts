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
package com.meistercharts.custom.rainsensor

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.animation.Animated
import com.meistercharts.canvas.animation.AnimationRepeatType
import com.meistercharts.canvas.animation.AnimationState
import com.meistercharts.canvas.animation.ChartAnimation
import com.meistercharts.canvas.animation.Tween
import com.meistercharts.canvas.layout.buffer.DoubleMultiBuffer
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.kotlin.lang.fastMap
import it.neckar.open.kotlin.lang.toIntFloor
import it.neckar.open.provider.MultiDoublesProvider
import it.neckar.open.provider.MultiProviderIndexContextAnnotation
import com.meistercharts.resources.LocalResourcePaintable
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/**
 * A layer that shows rain drops (or snow)
 */
class RainLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {}
) : AbstractLayer() {

  constructor(
    parallaxLayersCount: Int = 3,
    dropDuration: @ms Double = 2_000.0,
    additionalConfiguration: Configuration.() -> Unit = {}
  ): this(Configuration(parallaxLayersCount, dropDuration), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType = LayerType.Background


  /**
   * Contains the positions for each of the parallax layers (one behind each other)
   */
  var layerPositions: @pct DoubleMultiBuffer = DoubleMultiBuffer()

  override fun initialize(paintingContext: LayerPaintingContext) {
    super.initialize(paintingContext)

    ChartAnimation(object : Animated {
      val tweens: List<Tween> = configuration.parallaxLayersCount.fastMap {
        Tween(paintingContext.frameMonotonicMillis - configuration.random.nextDouble() * 1000.0, (configuration.dropDuration + it * 200.0).milliseconds, repeatType = AnimationRepeatType.Repeat)
      }

      override fun animationFrame(frameMonotonicMillis: @ms Double): AnimationState {
        layerPositions.resize(configuration.parallaxLayersCount)

        tweens.forEachIndexed { index, tween ->
          layerPositions[index] = tween.elapsedRatioForTime(frameMonotonicMillis)
        }

        return AnimationState.Active
      }
    }).also {
      paintingContext.chartSupport.onRender(it)
    }
  }

  /**
   * Contains the random x offsets for each drop
   */
  private val randomOffsetCacheHorizontal: @Zoomed MultiDoublesProvider<ParallaxLayerIndex> = MultiDoublesProvider.forArrayModulo(
    DoubleArray(100) {
      (configuration.random.nextDouble() - 0.5) * configuration.offsetAbsoluteHorizontalPerDrop
    }
  )

  /**
   * Contains the y offsets
   */
  private val randomOffsetCacheVertical: @Zoomed MultiDoublesProvider<ParallaxLayerIndex> = MultiDoublesProvider.forArrayModulo(
    DoubleArray(100) {
      (configuration.random.nextDouble() - 0.5) * configuration.offsetAbsoluteVerticalPerDrop
    }
  )


  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    //The number of drop columns
    val dropColumnsCount = (gc.width / configuration.spaceHorizontalPerDrop).toIntFloor()
    //the number of rows
    val dropRowsCount = (gc.height / configuration.spaceVerticalPerDrop).toIntFloor()

    //The delta x
    @Zoomed val deltaXAbs = gc.width / dropColumnsCount
    val deltaYPercentage: @pct Double = 1.0 / dropRowsCount

    @Zoomed val offsetHorizontalPerLayer = deltaXAbs / (configuration.parallaxLayersCount + 1)
    @pct val offsetVerticalPerLayer = deltaYPercentage / (configuration.parallaxLayersCount + 1)

    layerPositions.fastForEachIndexed { layerIndex, value: @pct Double ->
      dropRowsCount.fastFor { row ->
        @pct val percentageForRow = (1.0 - deltaYPercentage)
        @Window val y = gc.height * ((value + percentageForRow * row + offsetVerticalPerLayer * layerIndex) % 1.0)

        dropColumnsCount.fastFor { column ->
          @Window val x = deltaXAbs * (column + 0.5) + offsetHorizontalPerLayer * layerIndex

          @Zoomed val randomY = randomOffsetCacheVertical.valueAt(row * 10 + column)
          @Zoomed val randomX = randomOffsetCacheHorizontal.valueAt(row * 10 + column)

          configuration.rainDrop.paint(paintingContext, x + randomX, (y + randomY) % gc.height)
        }
      }
    }
  }


  @MustBeDocumented
  @Retention(AnnotationRetention.SOURCE)
  @MultiProviderIndexContextAnnotation
  annotation class ParallaxLayerIndex


  @ConfigurationDsl
  class Configuration(
    /**
     * The number of layers - for the parallax effect.
     * Must be greater than 0: [paint] divides by `parallaxLayersCount + 1`, therefore -1 results in
     * an offset of infinity, and 0 leaves [layerPositions] empty - no drop is painted at all.
     */
    val parallaxLayersCount: Int = 3,

    /**
     * The duration one drop takes from top to bottom.
     * Must be greater than 0: the [Tween] divides the elapsed time by the duration, therefore 0.0
     * results in a position of NaN and every drop is painted outside of the canvas.
     */
    val dropDuration: @ms Double = 2_000.0,
  ) {
    init {
      require(parallaxLayersCount > 0) { "parallaxLayersCount must be greater than 0 but was <$parallaxLayersCount>" }
      require(dropDuration > 0.0) { "dropDuration must be greater than 0 ms but was <$dropDuration>" }
    }

    /**
     * The space per drop on the x axis.
     * Must be greater than 0: [paint] derives the number of drop columns by dividing the canvas
     * width by this value, therefore 0.0 yields `Int.MAX_VALUE` columns and the render loop stalls.
     */
    var spaceHorizontalPerDrop: @Zoomed Double = 140.0
      set(value) {
        require(value > 0.0) { "spaceHorizontalPerDrop must be greater than 0 but was <$value>" }
        field = value
      }

    /**
     * The space per drop on the y axis.
     * Must be greater than 0 - for the number of drop rows, see [spaceHorizontalPerDrop].
     */
    var spaceVerticalPerDrop: @Zoomed Double = 170.0
      set(value) {
        require(value > 0.0) { "spaceVerticalPerDrop must be greater than 0 but was <$value>" }
        field = value
      }

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
