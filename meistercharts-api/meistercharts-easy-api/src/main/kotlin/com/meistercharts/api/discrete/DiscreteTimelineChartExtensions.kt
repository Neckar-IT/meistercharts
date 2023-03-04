package com.meistercharts.api.discrete

import com.meistercharts.charts.refs.DiscreteTimelineChartGestalt
import it.neckar.commons.kotlin.js.debug
import it.neckar.logging.LoggerFactory
import it.neckar.logging.ifDebug


private val logger = LoggerFactory.getLogger("com.meistercharts.api.discrete.DiscreteTimelineChartExtensions")


fun DiscreteTimelineChartGestalt.applySickDefaults() {
  //configuration.applyAxisTitleOnTop(40.0)
}

fun DiscreteTimelineChartGestalt.applyConfiguration(jsConfiguration: DiscreteTimelinechartConfiguration) {
  logger.ifDebug {
    console.debug("BulletChartGestalt.applyConfiguration", jsConfiguration)
  }
}

