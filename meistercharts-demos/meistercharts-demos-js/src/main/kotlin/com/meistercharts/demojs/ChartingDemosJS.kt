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

