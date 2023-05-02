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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.data.client.DataClient
import com.meistercharts.data.client.RestHistoryStorageAccess
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.history.HistoryConfiguration
import it.neckar.open.provider.MultiProvider
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

/**
 * A demo for the RestHistoryAccess
 */
class TimelineChartRestDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Timeline Chart Rest"
  override val description: String = "Timeline Chart Demo using a Server to read Data"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Gestalt

  private fun HttpClientConfig<out HttpClientEngineConfig>.setupClient() {
    expectSuccess = true
    install(DefaultRequest) {
    }
    install(ContentNegotiation) {
      this.json(
        Json {}
      )
    }
    install(HttpTimeout){
      requestTimeoutMillis = 7000
      connectTimeoutMillis = 5000
      socketTimeoutMillis = 5000
    }
    install(WebSockets){
      pingInterval = 500
    }
  }


  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val historyStorage = RestHistoryStorageAccess(DataClient(HttpClient {
          setupClient()
        }, 8088))

        onDispose(historyStorage)
        val gestalt = TimeLineChartGestalt(chartId, TimeLineChartGestalt.Data(historyStorage, HistoryConfiguration.empty)) {
          this.lineValueRanges = MultiProvider.always(ValueRange.linear(-200.0, 200.0))
        }

        GlobalScope.launch {
          historyStorage.registerAtServer()
          gestalt.data.historyConfiguration = historyStorage.getConfig()
        }

        val job = historyStorage.getUpdateDescriptors()

        gestalt.configure(this)
        configure {
          layers.addClearBackground()

        }
      }
    }
  }
}
