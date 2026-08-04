import it.neckar.projects.AbstractProjects
import it.neckar.projects.ConfiguredProject
import it.neckar.projects.KotlinTarget.Js
import it.neckar.projects.KotlinTarget.Jvm

/**
 * Project definitions for the standalone meistercharts.com-sync build.
 * This is a local file — not overwritten by populateBuildSrc.
 */
object Projects : AbstractProjects() {
  val meistercharts_commons: ConfiguredProject = multiplatform(":meistercharts-commons", Jvm, Js)
  val meistercharts_test_commons: ConfiguredProject = multiplatform(":meistercharts-test-commons", Jvm, Js)
  val meistercharts_core: ConfiguredProject = multiplatform(":meistercharts-core", Jvm, Js)

  val meistercharts_history_core: ConfiguredProject = multiplatform(":meistercharts-history::meistercharts-history-core", Jvm, Js)
  val meistercharts_history_api: ConfiguredProject = multiplatform(":meistercharts-history::meistercharts-history-api", Jvm, Js)

  val meistercharts_canvas: ConfiguredProject = multiplatform(":meistercharts-canvas", Jvm, Js)
  val meistercharts_api_easy: ConfiguredProject = multiplatform(":meistercharts-api:meistercharts-easy-api", Jvm, Js)
}

object ExternalProjects : AbstractProjects() {
  //Required for compilation
}

object OtherProjects : AbstractProjects() {
  //Required for compilation
}
