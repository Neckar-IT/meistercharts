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
package com.meistercharts.demojs

import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoDescriptors
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.DemoRunConfiguration
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demojs.descriptors.DemoDescriptorsJS
import it.neckar.commons.kotlin.js.getBoolean
import it.neckar.commons.kotlin.js.getNumber
import it.neckar.commons.kotlin.js.urlSearchParams
import it.neckar.logging.LoggerFactory
import it.neckar.open.annotations.JsExportForTs
import it.neckar.open.annotations.JsNameForTs
import it.neckar.open.unit.si.ms
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URLSearchParams

/**
 * Starts a single demo. This method is called from the `index.html` file.
 * It shows a single demo and - optionally - allows to configure it.
 */
@JsExportForTs
@Suppress("unused") // public API
@JsNameForTs("startMeisterchartsDemo")
fun startMeisterchartsDemo(
  navigationContainer: HTMLElement,
  meisterchartsContainer: HTMLElement,
  descriptionContainer: HTMLElement,
  configurationPaneContainer: HTMLElement,
  allDemoDescriptors: List<MeisterchartsDemoDescriptor<*>> = buildList {
    addAll(DemoDescriptors.descriptors)
    addAll(DemoDescriptorsJS.descriptors)
  },
) {

  registerDemoErrorHandlers()

  val params = window.urlSearchParams()
  val uuidFragment: String? = params.get("uuid")
  logger.info("startMeisterchartsDemo: uuid = $uuidFragment")
  val demoRunConfiguration = extractDemoRunConfiguration()
  val meisterchartsDemosJS = MeisterchartsDemosJS(
    navigationContainer = navigationContainer,
    meisterchartsContainer = meisterchartsContainer,
    descriptionContainer = descriptionContainer,
    configurationPaneContainer = configurationPaneContainer,
    demoRunConfiguration = demoRunConfiguration,
    allDemoDescriptors = allDemoDescriptors,
  )
  meisterchartsDemosJS.startDemoByUuid(uuidFragment)

}


/**
 * Extracts the demo run configuration from the provided parameters
 */
private fun extractDemoRunConfiguration(): DemoRunConfiguration {
  val params = URLSearchParams(window.location.search.toJsString())

  val all = params.getBoolean("all")

  val minQuality: DemoQuality
  val minVisibility: DemoCategory.Visibility
  if (all == true) {
    minQuality = DemoQuality.Broken
    minVisibility = DemoCategory.Visibility.DevOnly
    logger.info("Showing all demos")
  } else {
    minQuality = params.extractMinQuality()
    minVisibility = params.extractMinVisibility()
  }


  @ms val initialTime = params.getNumber(DemoRunConfiguration.UrlParamNames.initialTime)
  @ms val timeProgressionStep = params.getNumber(DemoRunConfiguration.UrlParamNames.timeProgressionStep)
  @ms val elapsedTime = params.getNumber(DemoRunConfiguration.UrlParamNames.elapsedTime)

  return DemoRunConfiguration.of(
    minQuality = minQuality,
    minVisibility = minVisibility,
    showConfigurationPane = params.extractShowConfigurationPane(),
    showNavigationPane = params.extractShowNavigation(),
    showDescriptionPane = params.extractShowDescription(),
    initialTime = initialTime,
    timeProgressionStep = timeProgressionStep,
    elapsedTime = elapsedTime
  )
}

private fun URLSearchParams.extractShowNavigation(): Boolean {
  return getBoolean(DemoRunConfiguration.UrlParamNames.showNavigationPane) != false
}

private fun URLSearchParams.extractShowConfigurationPane(): Boolean {
  return getBoolean(DemoRunConfiguration.UrlParamNames.showConfigurationPane) != false
}

private fun URLSearchParams.extractShowDescription(): Boolean {
  return getBoolean(DemoRunConfiguration.UrlParamNames.showDescriptionPane) != false
}

fun URLSearchParams.extractMinVisibility(): DemoCategory.Visibility {
  get(DemoRunConfiguration.UrlParamNames.minVisibility.name)?.let { minVisibilityParamString ->
    logger.debug("minVisibility from url: $minVisibilityParamString")
    try {
      return DemoCategory.Visibility.valueOf(minVisibilityParamString)
    } catch (e: Exception) {
      logger.warn("Could not parse $minVisibilityParamString as DemoCategory.Visibility due to ${e.message}")
    }
  }

  return DemoCategory.Visibility.Public
}

/**
 * Extracts the minQuality parameter from the URLSearchParams
 */
fun URLSearchParams.extractMinQuality(): DemoQuality {
  get(DemoRunConfiguration.UrlParamNames.minQuality.name)?.let { minQualityParamString ->
    logger.debug("minQuality from url: $minQualityParamString")
    // workaround: dependency=libs:kotlin:2.4.20 observed-on=2026-10-07 upstream=https://youtrack.jetbrains.com/issue/KT-35116
    // Enum.valueOf throws a different exception type per platform, so the catch takes every Exception.
    try {
      return DemoQuality.valueOf(minQualityParamString)
    } catch (e: Exception) {
      logger.warn("Could not parse $minQualityParamString as DemoQuality due to ${e.message}")
    }
  }

  return DemoQuality.High
}

private val logger = LoggerFactory.getLogger("com.meistercharts.demojs.DemoRunner")

/**
 * Registers window-level error handlers that display errors in the demo UI.
 * The JsErrorHandler infrastructure (window.onerror payloads) is target-specific.
 */
internal expect fun registerDemoErrorHandlers()
