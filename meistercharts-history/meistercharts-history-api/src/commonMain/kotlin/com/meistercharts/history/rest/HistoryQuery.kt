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
package com.meistercharts.history.rest

/**
 * Request history data.
 *
 * ATTENTION: Currently the data for all data series is requested. There is not
 * way to query only some data series.
 *
 */
data class HistoryQuery(
  val queryRange: QueryRange,
  //, val ids: List<DataSeriesId>
) {

  /**
   * Contains string constants that are used when converting a history query to an url with parameters
   */
  object QueryParams {
    const val from: String = "from"
    const val to: String = "to"
    const val resolution: String = "resolution"
    //const val dsIds: String = "dsIds"
  }
}
