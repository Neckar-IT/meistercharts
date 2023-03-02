package com.meistercharts.history.rest

/**
 */

/**
 * Converts the history query to url parameters
 */
fun HistoryQuery.toUrlQueryString(): String {
  return "${HistoryQuery.QueryParams.from}=${queryRange.from}&${HistoryQuery.QueryParams.to}=${queryRange.to}&${HistoryQuery.QueryParams.resolution}=${queryRange.resolution.distance}"
  //&${HistoryQuery.QueryParams.dsIds}=${ids.joinToString(",") { "${it.id}" }
}
