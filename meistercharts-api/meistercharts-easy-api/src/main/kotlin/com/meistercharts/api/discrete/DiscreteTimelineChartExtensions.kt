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
package com.meistercharts.api.discrete

import com.meistercharts.charts.refs.DiscreteTimelineChartGestalt
import it.neckar.commons.kotlin.js.debug
import it.neckar.logging.LoggerFactory
import it.neckar.logging.ifDebug


private val logger = LoggerFactory.getLogger("com.meistercharts.api.discrete.DiscreteTimelineChartExtensions")


fun DiscreteTimelineChartGestalt.applySickDefaults() {
  //configuration.applyAxisTitleOnTop(40.0)
}

fun DiscreteTimelineChartGestalt.applyConfiguration(jsConfiguration: DiscreteTimelineChartConfiguration) {
  logger.ifDebug {
    console.debug("BulletChartGestalt.applyConfiguration", jsConfiguration)
  }
}

