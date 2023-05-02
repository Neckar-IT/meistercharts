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

import com.meistercharts.demo.descriptors.history.TimeLineChartGestaltWithToolbarDemoDescriptor
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.containsAll

/**
 * Starts and displays the demo denoted by the given id
 */
class ChartingDemosJS(
  /**
   * a comma-separated list of terms to identify the demo to start (must match all)
   */
  initialSearchTerms: String,
  showConfigurationPane: Boolean
) : AbstractChartingDemosJS() {

  private val searchTermsList = initialSearchTerms
    .toLowerCase()
    .split(",", " ", "+")
    .map { it.trim() }

  init {
    startDemo(showConfigurationPane)
  }

  private fun startDemo(showConfigurationPane: Boolean) {
    getDemoDescriptors().fastForEach { demoDescriptor ->
      val lowerCaseDemoClassName = demoDescriptor::class.simpleName?.toLowerCase().orEmpty()
      val predefinedConfigurations = demoDescriptor.predefinedConfigurations

      if (predefinedConfigurations.isEmpty()) {
        if (matchesSearchTermList(lowerCaseDemoClassName)) {
          startDemo(demoDescriptor, null, showConfigurationPane)
          return
        }
      } else {
        predefinedConfigurations.fastForEach { predefinedConfiguration ->
          if (matchesSearchTermList("$lowerCaseDemoClassName ${predefinedConfiguration.description.lowercase()}")) {
            startDemo(demoDescriptor, predefinedConfiguration, showConfigurationPane)
            return
          }
        }
      }
    }

    //no match -> fall back to TimeLineChartGestaltWithToolbarDemoDescriptor
    val demoDescriptor = TimeLineChartGestaltWithToolbarDemoDescriptor()
    val predefinedConfiguration = demoDescriptor.predefinedConfigurations.firstOrNull()
    startDemo(demoDescriptor, predefinedConfiguration, showConfigurationPane)
  }

  /**
   * Returns true if [candidate] contains all elements of [searchTermsList]
   */
  private fun matchesSearchTermList(candidate: String): Boolean {
    return candidate.containsAll(searchTermsList)
  }

}

