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

import it.neckar.open.unit.other.Exclusive
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.si.ms


/**
 * Contains the data for the discrete timeline chart
 */
expect interface DiscreteTimelineChartData {
  /**
   * Index corresponds to the data series index.
   * Contains one entry for each data series.
   *
   *
   * The entries for the data series are *not* aligned.
   */
  val series: Array<DiscreteDataEntriesForDataSeries>
}

/**
 * Contains the entries for a single discrete data series
 */
expect interface DiscreteDataEntriesForDataSeries {
  /**
   * Contains all entries for this data series.
   * Must not overlap!
   */
  val entries: Array<@Sorted(by = "start") DiscreteDataEntry>
}

/**
 *
 * Interface representing a discrete data entry.
 */
expect interface DiscreteDataEntry {
  /**
   * The start time of the data entry in milliseconds, inclusive
   */
  val start: @ms @Inclusive Double

  /**
   * The end time of the data entry in milliseconds, exclusive
   */
  val end: @ms @Exclusive Double

  /**
   * A string representing the label of the data entry
   */
  val label: String?

  /**
   * Representing the status of the data entry.
   */
  val status: Double? //must be double since JS does not support Int
}
