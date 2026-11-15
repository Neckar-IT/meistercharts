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
@file:Suppress("RETURN_VALUE_NOT_USED")

import com.meistercharts.charts.ChartId
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.charts.timeline.setUpDemo
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.js.MeisterChartsPlatform
import com.meistercharts.react.meistercharts
import it.neckar.commons.kotlin.js.exception.JsErrorHandler
import it.neckar.logging.Level
import it.neckar.logging.LogConfigurer
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.console.ConsoleLogFunctionsSupport
import it.neckar.logging.console.ConsoleLogPrefix
import it.neckar.open.version.VersionInformation
import react.*
import react.dom.*
import react.dom.client.*
import web.html.HTMLElement

fun main() {
  println("Starting Meistercharts Demos - React")

  JsErrorHandler.registerWindowErrorHandler()


  LogConfigurer.initializeFromLocalStorage(Level.INFO)
  ConsoleLogFunctionsSupport.init(ConsoleLogPrefix("meistercharts")) { VersionInformation.artifactIdentity }


  MeisterChartsPlatform.init()


  val rootDiv: HTMLElement = web.dom.document.getElementById("root") ?: error("Element root not found")

  createRoot(rootDiv).render(createElement<Props> {
    MeisterchartsDemosReactRoot {}
  })
}


val MeisterchartsDemosReactRoot: FC<Props> = fc("meisterchartsDemosReactRoot") { props ->
  div {
    h1 {
      +"Meistercharts Demos - React"
    }

    val gestalt: TimeLineChartGestalt = useMemo {
      logger.debug("Creating TimeLineChartGestalt in react component")

      val historyStorage = InMemoryHistoryStorage()

      val timeLineChartGestalt = TimeLineChartGestalt(initialHistoryStorage = historyStorage)
      timeLineChartGestalt.setUpDemo(historyStorage)
      timeLineChartGestalt
    }

    meistercharts(
      description = "Hello Meistercharts",
      gestalt = gestalt,
      callMarkAsDirty = true
    )
  }
}

private val logger: Logger = LoggerFactory.getLogger("MeisterchartsDemosReactMain")


