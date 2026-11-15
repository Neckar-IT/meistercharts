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
package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.geometry.DirectionBasedBasePointProvider
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.deleteFromStartUntilMaxSize
import it.neckar.open.time.monotonicTimeSource
import it.neckar.open.unit.si.ms
import kotlin.time.ComparableTimeMark
import kotlin.time.DurationUnit

/**
 *
 */
class InitialResizeBehaviorDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("78516de0-837e-46ae-8d81-d85f4cec9e18")
  override val name: String = "Initial Resize Behavior"
  override val category: DemoCategory = DemoCategory.LowLevelTests
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          val start: ComparableTimeMark = monotonicTimeSource.markNow()

          val windowSizes = mutableListOf<SizeEvent>()
          val contentAreaSizes = mutableListOf<SizeEvent>()

          chartSupport.rootChartState.windowSizeProperty.consumeImmediately {
            windowSizes.add(SizeEvent(it, start.elapsedNow().toDouble(DurationUnit.MILLISECONDS)))
            windowSizes.deleteFromStartUntilMaxSize(50)
          }
          chartSupport.rootChartState.contentAreaSizeProperty.consumeImmediately {
            contentAreaSizes.add(SizeEvent(it, start.elapsedNow().toDouble(DurationUnit.MILLISECONDS)))
            contentAreaSizes.deleteFromStartUntilMaxSize(50)
          }

          layers.addClearBackground()
          layers.addText({ _, _ ->
            val sizesAsString = windowSizes.map { it.format() }

            buildList {
              add("Window Sizes:")
              addAll(sizesAsString)
            }
          }) {
            font = FontDescriptorFragment.XS.asProvider()
            anchorPointProvider = DirectionBasedBasePointProvider(Direction.TopLeft)
            anchorDirection = Direction.TopLeft
          }

          layers.addText({ _, _ ->
            val sizesAsString = contentAreaSizes.map { it.format() }

            buildList {
              add("Content area Sizes:")
              addAll(sizesAsString)
            }
          }) {
            font = FontDescriptorFragment.XS.asProvider()
            anchorPointProvider = DirectionBasedBasePointProvider(Direction.TopRight)
            anchorDirection = Direction.TopRight
          }
        }
      }
    }
  }

  data class SizeEvent(val size: Size, val time: @ms Double) {
    fun format(): String {
      return "${size.format()} @: ${decimalFormat2digits.format(time)} ms"
    }
  }
}
