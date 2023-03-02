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
