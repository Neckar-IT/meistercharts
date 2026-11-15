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
package com.meistercharts.demo

import it.neckar.open.time.ClockNowProvider
import it.neckar.open.unit.si.ms

/**
 * Configuration for a demo run
 */
data class DemoRunConfiguration(
  /**
   * The minimum quality of the demos that are shown
   */
  val minQuality: DemoQuality,
  /**
   * THe minimum visibility of the demos that are shown
   */
  val minVisibility: DemoCategory.Visibility,

  /**
   * If set to true, the configuration pane is shown
   */
  val showConfigurationPane: Boolean = true,

  /**
   * If set to true the navigation is shown
   */
  val showNavigationPane: Boolean = true,

  /**
   * If set to true the description is shown
   */
  val showDescriptionPane: Boolean = true,

  /**
   * The virtual time handler for a demo.
   * Should be overwritten if the demo uses [it.neckar.open.time.nowMillis] - to ensure that the demo is deterministic
   */
  val demoVirtualTimeHandler: DemoVirtualTimeHandler = DemoVirtualTimeHandler.Default,
) {

  /**
   * Returns true if the given demo descriptor matches the configuration
   */
  fun matches(descriptor: MeisterchartsDemoDescriptor<*>): Boolean {
    return matchesQuality(descriptor) && matchesVisibility(descriptor)
  }

  private fun matchesVisibility(descriptor: MeisterchartsDemoDescriptor<*>): Boolean {
    return descriptor.category.visibility >= minVisibility
  }

  private fun matchesQuality(descriptor: MeisterchartsDemoDescriptor<*>): Boolean {
    return descriptor.quality >= minQuality
  }

  fun format(): String {
    return "$minQuality | $minVisibility"
  }

  /**
   * Contains the url parameter names
   */
  @Suppress("EnumEntryName")
  enum class UrlParamNames {
    minQuality,
    minVisibility,

    showConfigurationPane,
    showNavigationPane,
    showDescriptionPane,

    initialTime,
    timeProgressionStep,
    elapsedTime,
  }

  companion object {
    fun of(
      minQuality: DemoQuality?,
      minVisibility: DemoCategory.Visibility?,
      showConfigurationPane: Boolean?,
      showNavigationPane: Boolean?,
      showDescriptionPane: Boolean?,
      initialTime: @ms Double?,
      timeProgressionStep: @ms Double?,
      elapsedTime: @ms Double?,
    ): DemoRunConfiguration {

      val virtualTimeHandler: DemoVirtualTimeHandler = if (elapsedTime == null) {
        DemoVirtualTimeHandler.Default
      } else {
        DemoVirtualTimeHandler.fixedOffsetStepByStep(
          initialTime = initialTime ?: ClockNowProvider.nowMillis(),
          targetOffset = elapsedTime,
          timeProgressionStep = timeProgressionStep
        )
      }


      return DemoRunConfiguration(
        minQuality ?: DemoQuality.High,
        minVisibility ?: DemoCategory.Visibility.Public,
        showConfigurationPane ?: true,
        showNavigationPane ?: true,
        showDescriptionPane ?: true,
        virtualTimeHandler,
      )
    }

    /**
     * The default configuration - only showing high quality demos
     */
    val Default: DemoRunConfiguration = DemoRunConfiguration(DemoQuality.High, DemoCategory.Visibility.Public)

    /**
     * Showing all demos
     */
    val All: DemoRunConfiguration = DemoRunConfiguration(DemoQuality.Low, DemoCategory.Visibility.DevOnly)
  }
}
