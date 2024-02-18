/**
 *
 */
object Projects : AbstractProjects() {
  const val open_ksp_generating_ts_declaration = ":internal:open:ksp:ts-declaration-generator"

  const val meistercharts_commons: String = ":meistercharts-commons"
  const val meistercharts_core: String = ":meistercharts-core"

  const val meistercharts_history_core: String = ":meistercharts-history::meistercharts-history-core"
  const val meistercharts_history_api: String = ":meistercharts-history::meistercharts-history-api"

  const val meistercharts_canvas: String = ":meistercharts-canvas"
  const val meistercharts_easy_api: String = ":meistercharts-api:meistercharts-easy-api"
}

object ExternalProjects : AbstractProjects() {
  //Required for compilation
}

object OtherProjects : AbstractProjects() {
  //Required for compilation
}
