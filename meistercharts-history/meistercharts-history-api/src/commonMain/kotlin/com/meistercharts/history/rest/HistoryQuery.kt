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
