package it.neckar.gradle.pnpm.dependencies

import normalizeNpmNameToAlias
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.net.URI

/**
 * Adds an npm dependency:
 * 1. Placeholder to `package.template.json` (via [AddPnpmDependencyToTemplateTask])
 * 2. Version entry to `gradle/npm.versions.toml`
 * 3. Runs `generatePackageJson`, `kotlinUpgradeYarnLock`, `pnpmInstall` in a second Gradle invocation
 *
 * Usage: `gradle installPnpmDependency -Pdependency=clsx`
 *
 * Fetches the latest version from the npm registry automatically.
 * Override with `-PdepVersion=2.1.1` if needed.
 */
open class InstallPnpmDependencyTask : BasePnpmDependencyTask() {
  /**
   * The dependency type to install
   */
  @Input
  var npmDependencyType: NpmDependencyType = NpmDependencyType.Production

  init {
    description = "Adds an npm dependency to package.template.json and npm.versions.toml; synopsis: `gradle installPnpmDependency -P$ArgumentNameDependency={NODE_DEP}`"
    outputs.upToDateWhen { false }
  }


  @TaskAction
  fun installDependency() {
    val dependency = getDependencyParameter()
    val alias = normalizeNpmNameToAlias(dependency)
    val version = (project.findProperty("depVersion") as? String) ?: fetchLatestVersion(dependency)

    addToToml(dependency, alias, version)

    logger.lifecycle("Added npm dependency: $dependency@$version")
    logger.lifecycle("  → npm.versions.toml + package.template.json updated")
    logger.lifecycle("  → Running generatePackageJson + lock file updates...")

    runGenerateAndInstall()
  }

  private fun addToToml(dependency: String, alias: String, version: String) {
    val tomlFile = project.rootProject.file("gradle/npm.versions.toml")
    val content = tomlFile.readText()

    val entry = "# npm: $dependency\n$alias = \"$version\""

    if (content.contains("$alias = ")) {
      logger.lifecycle("TOML alias '$alias' already exists — skipping TOML update")
      return
    }

    val updatedContent = content.trimEnd() + "\n" + entry + "\n"
    tomlFile.writeText(updatedContent)
  }

  /**
   * Runs generatePackageJson + kotlinUpgradeYarnLock + pnpmInstall in a separate Gradle process.
   *
   * A second invocation is needed because the TOML file was just modified — Gradle's version catalog
   * is loaded at configuration time and does not pick up changes mid-build.
   */
  private fun runGenerateAndInstall() {
    val gradlew = project.rootProject.file("gradlew").absolutePath
    val tasks = listOf(
      "${project.path}:generatePackageJson",
      ":kotlinUpgradeYarnLock",
      ":pnpmInstall",
    )

    val process = ProcessBuilder(listOf(gradlew) + tasks)
      .directory(project.rootProject.projectDir)
      .inheritIO()
      .start()

    val exitCode = process.waitFor()
    if (exitCode != 0) {
      throw RuntimeException("generatePackageJson + lock file update failed (exit code $exitCode)")
    }
  }

  /**
   * Fetches the latest version of an npm package from the registry.
   */
  private fun fetchLatestVersion(packageName: String): String {
    val encodedName = packageName.replace("/", "%2F")
    val url = URI("https://registry.npmjs.org/$encodedName/latest").toURL()

    val response = url.openConnection().apply {
      setRequestProperty("Accept", "application/json")
      connectTimeout = 10_000
      readTimeout = 10_000
    }.getInputStream().bufferedReader().readText()

    val versionMatch = Regex(""""version"\s*:\s*"([^"]+)"""").find(response)
      ?: throw RuntimeException("Could not determine latest version for npm package '$packageName'")

    return versionMatch.groupValues[1]
  }
}
