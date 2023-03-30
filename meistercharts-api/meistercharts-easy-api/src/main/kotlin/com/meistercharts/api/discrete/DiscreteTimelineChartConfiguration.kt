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

import com.meistercharts.api.DiscreteAxisStyle
import com.meistercharts.api.TimeAxisStyle
import com.meistercharts.api.TimeRange
import com.meistercharts.api.line.DiscreteDataSeriesConfiguration
import com.meistercharts.api.line.HistorySettings
import com.meistercharts.history.ReferenceEntryDataSeriesIndexInt

/**
 * Configuration for the discrete timeline chart
 */
external interface DiscreteTimelineChartConfiguration {
  /**
   * The settings for the history
   */
  val historySettings: HistorySettings?

  ///**
  // * Whether the play mode is active
  // */
  //val play: Boolean?

  /**
   * The time range that is currently visible (in UTC)
   */
  val visibleTimeRange: TimeRange?

  /**
   * The configurations of data series
   */
  val discreteDataSeriesConfigurations: Array<DiscreteDataSeriesConfiguration>?

  /**
   * The indices of the visible discrete stripes.
   * Set to `[-1]` in order to make all stripes visible.
   */
  @Suppress("ArrayPrimitive")
  val visibleDiscreteStripes: Array<@ReferenceEntryDataSeriesIndexInt Int>?

  /**
   * The style to be used for the time axis
   */
  val timeAxisStyle: TimeAxisStyle?

  /**
   * The style for the discrete layers axis
   */
  val discreteAxisStyle: DiscreteAxisStyle?

  /**
   * The height of a discrete stripe
   */
  val discreteStripeHeight: Double?

  /**
   * The distance between discrete stripes
   */
  val discretesStripesDistance: Double?

  /**
   * The background color of the area that contains the discretes
   */
  val discretesBackgroundColor: String?

}
