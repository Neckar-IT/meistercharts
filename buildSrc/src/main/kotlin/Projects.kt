/**
 *
 */
object Projects : AbstractProjects() {
  val meistercharts_commons: ConfiguredProject = multiPlatform8(":meistercharts-commons")
  val meistercharts_test_commons: ConfiguredProject = multiPlatform8(":meistercharts-test-commons")
  val meistercharts_core: ConfiguredProject = multiPlatform8(":meistercharts-core")

  val meistercharts_history_core: ConfiguredProject = multiPlatform8(":meistercharts-history::meistercharts-history-core")
  val meistercharts_history_api: ConfiguredProject = multiPlatform8(":meistercharts-history::meistercharts-history-api")

  val meistercharts_canvas: ConfiguredProject = multiPlatform8(":meistercharts-canvas")
  val meistercharts_api_easy: ConfiguredProject = multiPlatform8(":meistercharts-api:meistercharts-easy-api")
}

object ExternalProjects : AbstractProjects() {
  //Required for compilation
}

object OtherProjects : AbstractProjects() {
  //Required for compilation
}
