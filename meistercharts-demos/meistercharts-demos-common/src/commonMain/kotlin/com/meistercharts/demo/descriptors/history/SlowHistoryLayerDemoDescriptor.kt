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
package com.meistercharts.demo.descriptors.history

import kotlin.uuid.Uuid
import com.meistercharts.time.TimeRange
import com.meistercharts.range.ValueRange
import com.meistercharts.zoom.FittingWithMargin
import com.meistercharts.algorithms.layers.SlowHistoryLayer
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.axis.time.addTimeAxis
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.history.CachedRemoteHistoryStorage
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.impl.MockSinusHistoryStorage
import com.meistercharts.history.AsyncHistoryAccess
import com.meistercharts.model.Insets

/**
 * Demo that visualizes the functionality of the [SlowHistoryLayer]
 */
class SlowHistoryLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("16579319-aed4-410b-961c-71e13af26263")
  override val name: String = "Slow History Layer"
  override val description: String = "A history layer with tiles support"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configureAsTimeChart()

        val contentAreaTimeRange = TimeRange.oneMinuteUntilNow()

        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.of(50.0))
        }

        val valueRange = ValueRange.linear(-100.0, 100.0)
        val historyStorage = MockSinusHistoryStorage()

        configure {
          val remoteHistoryStorage = CachedRemoteHistoryStorage(object : AsyncHistoryAccess {
            override fun query(descriptor: HistoryBucketDescriptor, consumer: (HistoryBucket) -> Unit) {
              consumer(historyStorage.get(descriptor))
              markAsDirty()
            }
          })

          layers.addClearBackground()
          layers.addLayer(SlowHistoryLayer(remoteHistoryStorage, valueRange, contentAreaTimeRange))
          layers.addLayer(ContentAreaDebugLayer())
          layers.addLayer(ValueAxisLayer(ValueAxisLayer.Configuration(valueRangeProvider = { valueRange })) {
            titleProvider = { _, _ -> "Sin" }
          })
          layers.addTimeAxis(contentAreaTimeRange)
        }
      }
    }
  }
}

