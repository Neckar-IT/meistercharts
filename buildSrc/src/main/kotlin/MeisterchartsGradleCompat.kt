package it.neckar.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.process.ExecOutput

/*
 * The subset of build-logic/projects' GradleProjectUtils.kt the standalone build uses. That file
 * references monorepo projects and stays out of populateBuildSrc.
 */

fun Project.hasKotlinMultiplatformPlugin(): Boolean {
  return pluginManager.findPlugin(Plugins.kotlinMultiPlatform) != null
}

// The build variables the root build.gradle.kts sets on rootProject.extra, typed for the module build scripts.

val Project.gitCommit: String
  get() = rootProject.extra.get("gitCommit") as String

val Project.gitDescribe: String
  get() = rootProject.extra.get("gitDescribe") as String

val Project.buildDateDay: String
  get() = rootProject.extra.get("buildDateDay") as String

fun ExecOutput.standardOutputAsStringOnSuccess(): String {
  val result = result.get()
  result.rethrowFailure()
  return standardOutput.asText.get()
}
