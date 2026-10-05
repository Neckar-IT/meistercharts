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

/**
 * The file the project at [path] is registered in; the copied MultiplatformTargets.kt names it in
 * its error messages.
 */
fun registrationFile(@Suppress("UNUSED_PARAMETER") path: GradleProjectPath): String {
  return "buildSrc/src/main/kotlin/Projects.kt"
}

/**
 * The expression a build script writes for this project: `Projects.findOrNull(":meistercharts-core")`.
 */
val ConfiguredProject.accessor: String
  get() = "Projects.findOrNull(\"$path\")"
