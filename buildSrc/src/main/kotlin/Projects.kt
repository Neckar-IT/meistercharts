import it.neckar.projects.AbstractProjects
import it.neckar.projects.ConfiguredProject
import it.neckar.projects.KotlinTarget.Js
import it.neckar.projects.KotlinTarget.Jvm

/**
 * Project definitions for the standalone meistercharts.com-sync build.
 * This is a local file — not overwritten by populateBuildSrc.
 */
object Projects : AbstractProjects() {
  val meistercharts_commons: ConfiguredProject = multiplatform(":meistercharts-commons", ProjectRole.Library, Jvm, Js)
  val meistercharts_test_commons: ConfiguredProject = multiplatform(":meistercharts-test-commons", ProjectRole.Library, Jvm, Js)
  val meistercharts_core: ConfiguredProject = multiplatform(":meistercharts-core", ProjectRole.Library, Jvm, Js)

  val meistercharts_history_core: ConfiguredProject = multiplatform(":meistercharts-history::meistercharts-history-core", ProjectRole.Library, Jvm, Js)
  val meistercharts_history_api: ConfiguredProject = multiplatform(":meistercharts-history::meistercharts-history-api", ProjectRole.Library, Jvm, Js)

  val meistercharts_canvas: ConfiguredProject = multiplatform(":meistercharts-canvas", ProjectRole.Library, Jvm, Js)
  val meistercharts_api_easy: ConfiguredProject = multiplatform(":meistercharts-api:meistercharts-easy-api", ProjectRole.Library, Jvm, Js)
}

object ExternalProjects : AbstractProjects() {
  //Required for compilation
}

object OtherProjects : AbstractProjects() {
  //Required for compilation
}
