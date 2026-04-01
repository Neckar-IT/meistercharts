/**
 * Project definitions for the standalone meistercharts.com-sync build.
 * This is a local file — not overwritten by populateBuildSrc.
 */
object Projects : AbstractProjects() {
  val meistercharts_commons: ConfiguredProject = multiPlatformLts(":meistercharts-commons")
  val meistercharts_test_commons: ConfiguredProject = multiPlatformLts(":meistercharts-test-commons")
  val meistercharts_core: ConfiguredProject = multiPlatformLts(":meistercharts-core")

  val meistercharts_history_core: ConfiguredProject = multiPlatformLts(":meistercharts-history::meistercharts-history-core")
  val meistercharts_history_api: ConfiguredProject = multiPlatformLts(":meistercharts-history::meistercharts-history-api")

  val meistercharts_canvas: ConfiguredProject = multiPlatformLts(":meistercharts-canvas")
  val meistercharts_api_easy: ConfiguredProject = multiPlatformLts(":meistercharts-api:meistercharts-easy-api")
}

object ExternalProjects : AbstractProjects() {
  //Required for compilation
}

object OtherProjects : AbstractProjects() {
  //Required for compilation
}
