package com.meistercharts.demo.descriptors.history

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.SlowHistoryLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addTimeAxis
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.history.CachedRemoteHistoryStorage
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.impl.MockSinusHistoryStorage
import com.meistercharts.history.query.AsyncHistoryAccess
import com.meistercharts.model.Insets

/**
 * Demo that visualizes the functionality of the [SlowHistoryLayer]
 */
class SlowHistoryLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Slow History Layer"
  override val description: String = "A history layer with tiles support"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
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
          layers.addLayer(ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { valueRange })) {
            titleProvider = { _, _ -> "Sin" }
          })
          layers.addTimeAxis(contentAreaTimeRange)
        }
      }
    }
  }
}

