package it.neckar.projects

import it.neckar.projects.KotlinTarget.Js
import it.neckar.projects.KotlinTarget.Jvm

/**
 * Project registry of the standalone meistercharts.com-sync build: `Projects.meistercharts_core` is
 * `:meistercharts-core`. populateBuildSrc copies the registry classes from
 * internal/open/build-logic/projects next to this file.
 */
object Projects : ProjectRoot() {
  val meistercharts_commons: ConfiguredProject = multiplatform("meistercharts-commons", ProjectRole.Library, Jvm, Js)
  val meistercharts_test_commons: ConfiguredProject = multiplatform("meistercharts-test-commons", ProjectRole.Library, Jvm, Js)
  val meistercharts_core: ConfiguredProject = multiplatform("meistercharts-core", ProjectRole.Library, Jvm, Js)

  val meistercharts_history_core: ConfiguredProject = multiplatform("meistercharts-history:meistercharts-history-core", ProjectRole.Library, Jvm, Js)
  val meistercharts_history_api: ConfiguredProject = multiplatform("meistercharts-history:meistercharts-history-api", ProjectRole.Library, Jvm, Js)

  val meistercharts_canvas: ConfiguredProject = multiplatform("meistercharts-canvas", ProjectRole.Library, Jvm, Js)
  val meistercharts_api_easy: ConfiguredProject = multiplatform("meistercharts-api:meistercharts-easy-api", ProjectRole.Library, Jvm, Js)
}
