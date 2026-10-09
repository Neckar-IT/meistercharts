package it.neckar.gradle

/**
 * The plugin IDs `build-logic/registry/build.gradle.kts` registers and the task names the merge request gate of `build-logic/plugins`
 * reads from them. [Plugins] holds the IDs of `build-logic/plugins`.
 */
object RegistryPlugins {
  /** The root build only: `generateIgnoreFxProjectSet` writes the project sets of the registry, [VerifyIgnoreFxProjectSetTaskName] compares them. */
  const val generateIgnoreProjectSets: String = "it.neckar.generation.ignore-project-sets"

  const val VerifyIgnoreFxProjectSetTaskName: String = "verifyIgnoreFxProjectSet"
}
