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
import com.meistercharts.api.bar.BarChartGrouped
import com.meistercharts.api.bar.BarChartStacked
import com.meistercharts.api.bullet.BulletChart
import com.meistercharts.api.circular.CircularChart
import com.meistercharts.api.compass.Compass
import com.meistercharts.api.discrete.DiscreteTimelineChart
import com.meistercharts.api.discrete.applyDiscreteTimelineChartEasyApiDefaults
import com.meistercharts.api.histogram.Histogram
import com.meistercharts.api.line.LineChartSimple
import com.meistercharts.api.line.TimeLineChart
import com.meistercharts.api.map.MapWithStackedBars
import com.meistercharts.charts.BarChartGroupedGestalt
import com.meistercharts.charts.BarChartStackedGestalt
import com.meistercharts.charts.CategoryLineChartGestalt
import com.meistercharts.charts.CircularChartGestalt
import com.meistercharts.charts.HistogramGestalt
import com.meistercharts.charts.MapWithPaintablesGestalt
import com.meistercharts.charts.PuristicCompassGestalt
import com.meistercharts.charts.ToolTipType
import com.meistercharts.charts.bullet.BulletChartGestalt
import com.meistercharts.charts.refs.DiscreteTimelineChartGestalt
import com.meistercharts.charts.timeline.TimeLineChartWithToolbarGestalt
import com.meistercharts.history.HistoryStorageQueryMonitor
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.withQueryMonitor
import com.meistercharts.js.MeisterchartBuilderJS
import kotlinx.browser.document
import org.w3c.dom.Element

/**
 * This file contains the JS entry points that allow the creation of charts.
 *
 * ATTENTION: Do *NOT* move into a package. This file has to be placed in the default package to avoid unnecessary fqn in JS
 */

/**
 * Creates the [BarChartStacked] as a child of the element with id [id].
 * @see [createBarChartStackedFromElement]
 */
@JsExport
@JsName("createBarChartStackedFromId")
@Suppress("unused")
fun createBarChartStackedFromId(
  id: String
): BarChartStacked {
  val element = document.getElementById(id)
  requireNotNull(element) { "failed to find the element with id $id" }
  return createBarChartStackedFromElement(element)
}

/**
 * Creates the [BarChartStacked] as a child of [element].
 * @see [createBarChartStacked]
 */
@JsExport
@JsName("createBarChartStackedFromElement")
@Suppress("unused")
fun createBarChartStackedFromElement(
  element: Element
): BarChartStacked {
  val barChartStacked = createBarChartStacked()
  element.appendChild(barChartStacked.holder)
  return barChartStacked
}

/**
 * Creates the [BarChartStacked].
 */
@JsExport
@JsName("createBarChartStacked")
@Suppress("unused")
fun createBarChartStacked(): BarChartStacked {
  // The MeisterChartBuilder must be created before the gestalt to ensure that MeisterChartsPlatform is correctly initialized
  val meisterChartBuilder = MeisterchartBuilderJS.create("BarChartStacked")
  val gestalt = BarChartStackedGestalt().apply {
    configure(meisterChartBuilder)
  }
  val meisterChart = meisterChartBuilder.build()
  return BarChartStacked(gestalt, meisterChart)
}

/**
 * Creates the [BarChartGrouped] as a child of the element with id [id].
 * @see [createBarChartGroupedFromElement]
 */
@JsExport
@JsName("createBarChartGroupedFromId")
@Suppress("unused")
fun createBarChartGroupedFromId(
  id: String
): BarChartGrouped {
  val element = document.getElementById(id)
  requireNotNull(element) { "failed to find the element with id $id" }
  return createBarChartGroupedFromElement(element)
}

/**
 * Creates the [BarChartGrouped] as a child of [element].
 * @see [createBarChartGrouped]
 */
@JsExport
@JsName("createBarChartGroupedFromElement")
@Suppress("unused")
fun createBarChartGroupedFromElement(
  element: Element
): BarChartGrouped {
  val barChartGrouped = createBarChartGrouped()
  element.appendChild(barChartGrouped.holder)
  return barChartGrouped
}

/**
 * Creates the [BarChartGrouped].
 */
@JsExport
@JsName("createBarChartGrouped")
@Suppress("unused")
fun createBarChartGrouped(): BarChartGrouped {
  // The builder must be created before the gestalt to ensure that MeisterChartsPlatform is correctly initialized
  val meisterChartBuilder = MeisterchartBuilderJS.create("BarChartGrouped")
  val gestalt = BarChartGroupedGestalt(toolTipType = ToolTipType.Balloon).apply {
    configure(meisterChartBuilder)
  }

  val meisterChart = meisterChartBuilder.build()
  return BarChartGrouped(gestalt, meisterChart)
}

/**
 * Creates the [Histogram] as a child of the element with id [id].
 * @see [createHistogramFromElement]
 */
@JsExport
@JsName("createHistogramFromId")
@Suppress("unused")
fun createHistogramFromId(
  id: String
): Histogram {
  val element = document.getElementById(id)
  requireNotNull(element) { "failed to find the element with id $id" }
  return createHistogramFromElement(element)
}

/**
 * Creates the [Histogram] as a child of [element].
 * @see [createHistogram]
 */
@JsExport
@JsName("createHistogramFromElement")
@Suppress("unused")
fun createHistogramFromElement(
  element: Element
): Histogram {
  val histogram = createHistogram()
  element.appendChild(histogram.holder)
  return histogram
}

/**
 * Creates the [Histogram].
 */
@JsExport
@JsName("createHistogram")
@Suppress("unused")
fun createHistogram(): Histogram {
  // The builder must be created before the gestalt to ensure that MeisterChartsPlatform is correctly initialized
  val meisterChartBuilder = MeisterchartBuilderJS.create("Histogram")
  val gestalt = HistogramGestalt().apply {
    configure(meisterChartBuilder)
  }

  val meisterChart = meisterChartBuilder.build()
  return Histogram(gestalt, meisterChart)
}

/**
 * Creates the [CircularChart] as a child of the element with id [id].
 * @see [createCircularChartFromElement]
 */
@Suppress("unused")
@JsExport
fun createCircularChartFromId(
  id: String
): CircularChart {
  val element = document.getElementById(id)
  requireNotNull(element) { "failed to find the element with id $id" }
  return createCircularChartFromElement(element)
}

/**
 * Creates the [CircularChart] as a child of [element].
 * @see [createCircularChart]
 */
@Suppress("unused")
@JsExport
fun createCircularChartFromElement(
  element: Element
): CircularChart {
  val circularChart = createCircularChart()
  element.appendChild(circularChart.holder)
  return circularChart
}

/**
 * Creates the [CircularChart].
 */
@Suppress("unused")
fun createCircularChart(): CircularChart {
  // The Builder must be created before the gestalt to ensure that MeisterChartsPlatform is correctly initialized
  val meisterChartBuilder = MeisterchartBuilderJS.create("CircularChart")
  val gestalt = CircularChartGestalt().apply {
    configure(meisterChartBuilder)
  }

  val meisterChart = meisterChartBuilder.build()
  return CircularChart(gestalt, meisterChart)
}

/**
 * Creates the [Compass] as a child of the element with id [id].
 * @see [createCompassFromElement]
 */
@Suppress("unused")
@JsExport
fun createCompassFromId(
  id: String
): Compass {
  val element = document.getElementById(id)
  requireNotNull(element) { "failed to find the element with id $id" }
  return createCompassFromElement(element)
}

/**
 * Creates the [Compass] as a child of [element].
 * @see [createCompass]
 */
@Suppress("unused")
@JsExport
fun createCompassFromElement(
  element: Element
): Compass {
  val compass = createCompass()
  element.appendChild(compass.holder)
  return compass
}

/**
 * Creates the [Compass].
 */
@Suppress("unused")
@JsExport
fun createCompass(): Compass {
  // The Builder must be created before the gestalt to ensure that MeisterChartsPlatform is correctly initialized
  val meisterChartBuilder = MeisterchartBuilderJS.create("Compass")
  val gestalt = PuristicCompassGestalt().apply {
    configure(meisterChartBuilder)
  }

  val meisterChart = meisterChartBuilder.build()
  return Compass(gestalt, meisterChart)
}


/**
 * Creates the [TimeLineChart] as a child of the element with id [id].
 * @see [createTimeLineChartFromElement]
 */
@JsExport
@JsName("createTimeLineChartFromId")
@Suppress("unused")
fun createTimeLineChartFromId(
  id: String
): TimeLineChart {
  val element = document.getElementById(id)
  requireNotNull(element) { "failed to find the element with id $id" }
  return createTimeLineChartFromElement(element)
}

/**
 * Creates the [TimeLineChart] as a child of [element].
 * @see [createTimeLineChart]
 */
@JsExport
@JsName("createTimeLineChartFromElement")
@Suppress("unused")
fun createTimeLineChartFromElement(
  element: Element
): TimeLineChart {
  val timeLineChart = createTimeLineChart()
  element.appendChild(timeLineChart.holder)
  return timeLineChart
}

/**
 * Creates the [TimeLineChart].
 */
@JsExport
@JsName("createTimeLineChart")
@Suppress("unused")
fun createTimeLineChart(): TimeLineChart {
  val historyStorage = InMemoryHistoryStorage()
  val historyStorageQueryMonitor: HistoryStorageQueryMonitor<InMemoryHistoryStorage> = historyStorage.withQueryMonitor()

  /**
   * Creates a timeline chart for the given history storage
   */
  // The Builder must be created before the gestalt to ensure that MeisterChartsPlatform is correctly initialized
  val meisterChartBuilder = MeisterchartBuilderJS.create("TimeLineChart")
  val gestalt = TimeLineChartWithToolbarGestalt(meisterChartBuilder.chartId, historyStorageQueryMonitor).apply {
    configure(meisterChartBuilder)
  }

  val meisterChart = meisterChartBuilder.build()
  val timeLineChart = TimeLineChart(gestalt, meisterChart, historyStorageQueryMonitor)

  historyStorage.scheduleDownSampling()
  historyStorage.scheduleCleanupService()

  //Dispose the history storage when the canvas is disposed
  meisterChart.onDispose(historyStorage)

  return timeLineChart
}

/**
 * Creates the [LineChartSimple] as a child of the element with id [id].
 * @see [createLineChartSimpleFromElement]
 */
@JsName("createLineChartSimpleFromId")
@JsExport
@Suppress("unused")
fun createLineChartSimpleFromId(
  id: String
): LineChartSimple {
  val element = document.getElementById(id)
  requireNotNull(element) { "failed to find the element with id $id" }
  return createLineChartSimpleFromElement(element)
}

/**
 * Creates the [LineChartSimple] as a child of [element].
 * @see [createLineChartSimple]
 */
@JsName("createLineChartSimpleFromElement")
@JsExport
@Suppress("unused")
fun createLineChartSimpleFromElement(
  element: Element
): LineChartSimple {
  val lineChartSimple = createLineChartSimple()
  element.appendChild(lineChartSimple.holder)
  return lineChartSimple
}

/**
 * Creates the [LineChartSimple].
 */
@JsName("createLineChartSimple")
@JsExport
@Suppress("unused")
fun createLineChartSimple(): LineChartSimple {
  // The Builder must be created before the gestalt to ensure that MeisterChartsPlatform is correctly initialized
  val meisterChartBuilder = MeisterchartBuilderJS.create("LineChartSimple")
  val gestalt = CategoryLineChartGestalt(toolTipType = ToolTipType.Balloon).apply {
    configure(meisterChartBuilder)
  }

  val meisterChart = meisterChartBuilder.build()
  return LineChartSimple(gestalt, meisterChart)
}

/**
 * Creates the [BulletChart] as a child of the element with id [id].
 * @see [createBulletChartFromElement]
 */
@JsName("createBulletChartFromId")
@JsExport
@Suppress("unused")
fun createBulletChartFromId(
  id: String,
): BulletChart {
  val element = document.getElementById(id)
  requireNotNull(element) { "failed to find the element with id $id" }
  return createBulletChartFromElement(element)
}

/**
 * Creates the [BulletChart] as a child of [element].
 * @see [createBulletChart]
 */
@JsName("createBulletChartFromElement")
@JsExport
@Suppress("unused")
fun createBulletChartFromElement(
  element: Element,
): BulletChart {
  val bulletChart = createBulletChart()
  element.appendChild(bulletChart.holder)
  return bulletChart
}

/**
 * Creates the [BulletChart].
 */
@JsName("createBulletChart")
@JsExport
@Suppress("unused")
fun createBulletChart(): BulletChart {
  // The Builder must be created before the gestalt to ensure that MeisterChartsPlatform is correctly initialized
  val meisterChartBuilder = MeisterchartBuilderJS.create("BulletChart")
  val gestalt = BulletChartGestalt().apply {
    configure(meisterChartBuilder)
  }

  val meisterChart = meisterChartBuilder.build()
  return BulletChart(gestalt, meisterChart)
}

/**
 * Creates the [DiscreteTimelineChart] as a child of the element with id [id].
 * @see [createDiscreteTimelineChartFromElement]
 */
@JsName("createDiscreteTimelineChartFromId")
@JsExport
@Suppress("unused")
fun createDiscreteTimelineChartFromId(
  id: String,
): DiscreteTimelineChart {
  val element = document.getElementById(id)
  requireNotNull(element) { "failed to find the element with id $id" }
  return createDiscreteTimelineChartFromElement(element)
}

/**
 * Creates the [DiscreteTimelineChart] as a child of [element].
 * @see [createDiscreteTimelineChart]
 */
@JsName("createDiscreteTimelineChartFromElement")
@JsExport
@Suppress("unused")
fun createDiscreteTimelineChartFromElement(
  element: Element,
): DiscreteTimelineChart {
  val discreteTimelineChart = createDiscreteTimelineChart()
  element.appendChild(discreteTimelineChart.holder)
  return discreteTimelineChart
}

/**
 * Creates the [DiscreteTimelineChart].
 */
@JsName("createDiscreteTimelineChart")
@JsExport
@Suppress("unused")
fun createDiscreteTimelineChart(): DiscreteTimelineChart {
  val historyStorage = InMemoryHistoryStorage()
  val historyStorageQueryMonitor: HistoryStorageQueryMonitor<InMemoryHistoryStorage> = historyStorage.withQueryMonitor()

  // The Builder must be created before the gestalt to ensure that MeisterChartsPlatform is correctly initialized
  val meisterChartBuilder = MeisterchartBuilderJS.create("DiscreteTimelineChart")
  val gestalt = DiscreteTimelineChartGestalt(historyStorageQueryMonitor).apply {
    configure(meisterChartBuilder)

    meisterChartBuilder.applyDiscreteTimelineChartEasyApiDefaults()
  }

  val meisterChart = meisterChartBuilder.build()
  val discreteTimelineChart = DiscreteTimelineChart(gestalt, meisterChart, historyStorageQueryMonitor)

  historyStorage.scheduleDownSampling()
  historyStorage.scheduleCleanupService()

  //Dispose the history storage when the canvas is disposed
  meisterChart.onDispose(historyStorage)

  return discreteTimelineChart
}

/**
 * Creates a map with stacked bars as a child of the element with id [id].
 * @see [createMapWithStackedBarsFromElement]
 */
@Suppress("unused")
@JsExport
fun createMapWithStackedBarsFromId(
  id: String,
): MapWithStackedBars {
  val element = document.getElementById(id)
  requireNotNull(element) { "failed to find the element with id $id" }
  return createMapWithStackedBarsFromElement(element)
}

/**
 * Creates a map with stacked bars as a child of [element].
 * @see [createMapWithStackedBars]
 */
@Suppress("unused")
@JsExport
fun createMapWithStackedBarsFromElement(
  element: Element
): MapWithStackedBars {
  val mapWithStackedBars = createMapWithStackedBars()
  element.appendChild(mapWithStackedBars.holder)
  return mapWithStackedBars
}

/**
 * Creates a map with stacked bars.
 */
@Suppress("unused")
@JsExport
fun createMapWithStackedBars(): MapWithStackedBars {
  // The Builder must be created before the gestalt to ensure that MeisterChartsPlatform is correctly initialized
  val meisterChartBuilder = MeisterchartBuilderJS.create("MapWithStackedBars")
  val gestalt = MapWithPaintablesGestalt(meisterChartBuilder.chartId).apply {
    configure(meisterChartBuilder)
  }

  val meisterChart = meisterChartBuilder.build()
  return MapWithStackedBars(gestalt, meisterChart)
}
