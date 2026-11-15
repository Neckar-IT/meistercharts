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

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.data.client.DataClient
import com.meistercharts.data.client.RestHistoryStorageAccess
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.range.ValueRange
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import it.neckar.ktor.client.createHttpClient
import it.neckar.ktor.client.installClientIdHeader
import it.neckar.ktor.client.installClientRoleHeader
import it.neckar.open.http.Url
import it.neckar.open.provider.MultiProvider
import it.neckar.rest.ClientRole
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

/**
 * A demo for the RestHistoryAccess
 */
class TimelineChartRestDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("3605b633-ccfb-4af4-8b0f-7ef8393e1a9f")
  override val name: String = "Timeline Chart Rest"
  override val description: String = "Timeline chart that reads its data from a server."

  // REST server needed / move in own category?
  override val quality: DemoQuality = DemoQuality.Low
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  //language=HTML
  override val category: DemoCategory = DemoCategory.Gestalt

  private fun HttpClientConfig<out HttpClientEngineConfig>.setupClient() {
    installClientIdHeader()
    installClientRoleHeader(ClientRole("meistercharts-demos"))

    install(DefaultRequest) {
    }
    install(ContentNegotiation) {
      this.json(
        Json {}
      )
    }
    install(HttpTimeout) {
      requestTimeoutMillis = 7000
      connectTimeoutMillis = 5000
      socketTimeoutMillis = 5000
    }
    install(WebSockets) {
      pingInterval = 500.milliseconds
    }
  }


  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val historyStorage = RestHistoryStorageAccess(DataClient(createHttpClient("TimelineChartRestDemoDescriptor") {
          setupClient()
        }, Url.localhostHttp(8088)))

        onDispose(historyStorage)
        val gestalt = TimeLineChartGestalt(historyStorage, HistoryConfiguration.empty) {
          this.lineValueRanges = MultiProvider.always(ValueRange.linear(-200.0, 200.0))
        }

        GlobalScope.launch {
          historyStorage.registerAtServer()
          gestalt.configuration.historyConfiguration = historyStorage.getConfig()
        }

        val job = historyStorage.getUpdateDescriptors()

        gestalt.configure(this)
        configure {
          layers.addClearBackground()
        }

        job.invokeOnCompletion {
          if (it != null) {
            println("Error in Job")
            it.printStackTrace()
          }
        }
      }
    }
  }
}
