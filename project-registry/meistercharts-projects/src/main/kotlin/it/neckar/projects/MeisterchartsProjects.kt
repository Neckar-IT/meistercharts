package it.neckar.projects

import it.neckar.projects.KotlinTarget.Js
import it.neckar.projects.KotlinTarget.Jvm
import it.neckar.projects.KotlinTarget.WasmJs

/**
 * The projects under `internal/open/meistercharts`. The monorepo registers them below `:internal:open:meistercharts`, the export of
 * `internal/open/meistercharts` below `:`.
 */
class MeisterchartsProjects(parent: ConfiguredProject, relativePath: GradleProjectPath) : ConfiguredProject(parent, relativePath, ProjectType.ProjectParent, ProjectRole.Container) {
  companion object {
    /** This file, relative to `internal/open/meistercharts`. */
    const val RegistryFile: String = "project-registry/meistercharts-projects/src/main/kotlin/it/neckar/projects/MeisterchartsProjects.kt"
  }

  val meisterchartsAdditionalCharts: ConfiguredProject = multiplatform("meistercharts-additional-charts", ProjectRole.Library, Jvm, Js, WasmJs)
  val meisterchartsApi: MeisterchartsApi = MeisterchartsApi(this)
  val meisterchartsCanvas: ConfiguredProject = multiplatform("meistercharts-canvas", ProjectRole.Library, Jvm, Js, WasmJs)
  val meisterchartsCore: ConfiguredProject = multiplatform("meistercharts-core", ProjectRole.Library, Jvm, Js, WasmJs)
  val meisterchartsData: MeisterchartsData = MeisterchartsData(this)
  val meisterchartsDemos: MeisterchartsDemos = MeisterchartsDemos(this)
  val meisterchartsDeployment: ConfiguredProject = other("meistercharts-deployment", ProjectRole.Deployment)
  val meisterchartsDocs: ConfiguredProject = other("meistercharts-docs", ProjectRole.Library)
  val meisterchartsExamples: ConfiguredProject = multiplatform("meistercharts-examples", ProjectRole.Library, Jvm, Js)
  val meisterchartsFx: ConfiguredProject = jvm("meistercharts-fx", ProjectRole.Library)
  val meisterchartsHistory: MeisterchartsHistory = MeisterchartsHistory(this)
  val meisterchartsLinkPoc: ConfiguredProject = pnpm("meistercharts-link-poc", ProjectRole.TestSuite)
  val meisterchartsReact: ConfiguredProject = pnpm("meistercharts-react", ProjectRole.Library)
  val meisterchartsReactKotlin: ConfiguredProject = multiplatform("meistercharts-react-kotlin", ProjectRole.Library, Jvm, Js)
  val meisterchartsVersionInfo: ConfiguredProject = multiplatform("meistercharts-version-info", ProjectRole.Library, Jvm, Js, WasmJs)

  class MeisterchartsApi(parent: ConfiguredProject) : ConfiguredProject(parent, GradleProjectPath(":meistercharts-api"), ProjectType.Intermediate, ProjectRole.Container) {
    val meisterchartsEasyApi: ConfiguredProject = multiplatform("meistercharts-easy-api", ProjectRole.Library, Jvm, Js)
  }

  class MeisterchartsData(parent: ConfiguredProject) : ConfiguredProject(parent, GradleProjectPath(":meistercharts-data"), ProjectType.Intermediate, ProjectRole.Container) {
    val meisterchartsDataApi: ConfiguredProject = multiplatform("meistercharts-data-api", ProjectRole.Library, Jvm, Js, WasmJs)
    val meisterchartsDataClient: ConfiguredProject = multiplatform("meistercharts-data-client", ProjectRole.Library, Jvm, Js, WasmJs)
    val meisterchartsDataServer: ConfiguredProject = jvm("meistercharts-data-server", ProjectRole.Service)
  }

  class MeisterchartsDemos(parent: ConfiguredProject) : ConfiguredProject(parent, GradleProjectPath(":meistercharts-demos"), ProjectType.Intermediate, ProjectRole.Container) {
    val meisterchartsDemosCommon: ConfiguredProject = multiplatform("meistercharts-demos-common", ProjectRole.Library, Jvm, Js, WasmJs)
    val meisterchartsDemosJs: ConfiguredProject = multiplatform("meistercharts-demos-js", ProjectRole.Frontend, Jvm, Js, WasmJs)
    val meisterchartsDemosReact: ConfiguredProject = multiplatform("meistercharts-demos-react", ProjectRole.Frontend, Jvm, Js)
  }

  class MeisterchartsHistory(parent: ConfiguredProject) : ConfiguredProject(parent, GradleProjectPath(":meistercharts-history"), ProjectType.Intermediate, ProjectRole.Container) {
    val meisterchartsHistoryApi: ConfiguredProject = multiplatform("meistercharts-history-api", ProjectRole.Library, Jvm, Js, WasmJs)
    val meisterchartsHistoryCore: ConfiguredProject = multiplatform("meistercharts-history-core", ProjectRole.Library, Jvm, Js, WasmJs)
  }
}
