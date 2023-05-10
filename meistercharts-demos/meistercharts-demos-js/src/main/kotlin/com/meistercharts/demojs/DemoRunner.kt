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
package com.meistercharts.demojs

import org.w3c.dom.HTMLElement

/**
 * Starts a tabbed view that allows to select and configure various demo applications
 * @param container the element that will hold the demo
 */
@JsExport
@Suppress("unused") // public API
@JsName("startChartingDemos")
fun startChartingDemos(container: HTMLElement) {
  println("startChartingDemos")

  ChartingDemosWithNavigationJS()
}

/**
 * Starts a single demo
 * @param container the element that will hold the demo
 * @param searchTerms a comma-separated list of terms to identify the demo to start (must match all)
 * @param showConfigurationPane if set to true, the configuration pane is added
 */
@JsExport
@Suppress("unused") // public API
@JsName("startChartingDemo")
fun startChartingDemo(container: HTMLElement, searchTerms: String, showConfigurationPane: Boolean = true) {
  println("startChartingDemo: demoId = $searchTerms")

  ChartingDemosJS(searchTerms, showConfigurationPane)
}

@JsExport
@Suppress("unused") // public API
@JsName("start100CircularChartsDemos")
fun start100CircularChartsDemos(container: HTMLElement, withLegend: Boolean) {
  println("start100CircularChartsDemos")

  MultiCircularChartsDemo(container, withLegend)
}
