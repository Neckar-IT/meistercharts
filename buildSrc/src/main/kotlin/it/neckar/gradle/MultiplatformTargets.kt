package it.neckar.gradle

import it.neckar.projects.ConfiguredProject
import it.neckar.projects.KotlinTarget
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

/**
 * Declares the Kotlin targets a multiplatform module is registered for, and nothing else.
 *
 * The registered set (`multiplatform(path, Jvm, LinuxX64)` in the project registry) is the single
 * source of truth. A module build script may configure a registered target — `js { … }` to add a
 * webpack setup, for instance — but declaring an unregistered one is caught by
 * [verifyDeclaredTargets].
 */
fun KotlinMultiplatformExtension.declareTargets(project: Project, targets: Set<KotlinTarget>) {
  targets.forEach { target ->
    when (target) {
      KotlinTarget.Jvm -> declareJvmTarget()
      KotlinTarget.Js -> declareJsTarget(project)
      KotlinTarget.WasmJs -> declareWasmJsTarget(project)
      KotlinTarget.LinuxX64 -> declareLinuxX64Target()
    }
  }

  // Creates the intermediate source sets (webMain for js + wasmJs, nativeMain, …) for the targets
  // declared above. Called after the declarations so it only creates source sets a target uses.
  applyDefaultHierarchyTemplate()
}

/**
 * The Kotlin source directories detekt analyses for a multiplatform module: the directories of
 * every main source set, intermediate ones (`webMain`, `nativeMain`) included.
 *
 * Asked of the Kotlin extension rather than composed from the target set or read off `src/`, so a
 * module holding its sources elsewhere is analysed too — `internal/open/commons/descriptors/` keeps
 * them under `build-logic/descriptors/src/` and pulls them in with `kotlin.srcDir`.
 *
 * Two exclusions: test source sets (their name ends in `Test`) and anything under the build
 * directory, which is generated code — `internal/open/version-info/` adds its generated constants
 * to `commonMain` that way.
 *
 * Resolved lazily: the source sets are complete only once the module build script has run.
 */
fun Project.multiplatformDetektSourceDirectories(): Provider<List<File>> {
  val buildDirectory = layout.buildDirectory

  return provider {
    val kotlinMultiplatformExtension = extensions.getByType(KotlinMultiplatformExtension::class.java)
    val generatedRoot = buildDirectory.get().asFile

    kotlinMultiplatformExtension.sourceSets
      .filter { it.name.endsWith("Main") }
      .flatMap { it.kotlin.srcDirs }
      .filter { it.exists() && it.startsWith(generatedRoot).not() }
      .distinct()
      .sorted()
  }
}

/**
 * Registers `printSourceSets`, which lists every Kotlin source set of the module with its source and
 * resource directories.
 */
fun Project.registerPrintSourceSetsTask() {
  val project = this

  tasks.register("printSourceSets") {
    doLast {
      val ansiConsole = project.console

      logger.lifecycle("------------------------------------------------------------")
      logger.lifecycle(ansiConsole.green("Source Sets:"))
      logger.lifecycle("------------------------------------------------------------")

      val kotlinMultiplatformExtension: KotlinMultiplatformExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

      kotlinMultiplatformExtension.sourceSets.all {
        logger.lifecycle(ansiConsole.orange(name))

        logger.lifecycle("  ${ansiConsole.gray("Source Dirs:")}")
        this.kotlin.srcDirs.forEach {
          logger.lifecycle("    ${ansiConsole.white(it.relativeTo(project.projectDir))}")
        }

        logger.lifecycle("  ${ansiConsole.gray("Resource Dirs:")}")
        this.resources.srcDirs.forEach {
          logger.lifecycle("    ${ansiConsole.white(it.relativeTo(project.projectDir))}")
        }
      }
    }
  }
}

/**
 * Fails the build when a module's actual Kotlin targets deviate from its registered target set, in
 * either direction:
 *
 * - a target declared by the module build script but not registered — the registry then lies about
 *   what the module builds, and everything derived from it (detekt sources above all) silently skips
 *   that code
 * - a registered target the module never got — a typo in the registry that would otherwise surface
 *   as a missing artifact much later
 *
 * Runs in `afterEvaluate` so the module build script has had its say, and compares the real target
 * names instead of scanning build-script text — no syntax escapes it.
 *
 * `wasmJs` is exempt: it is an opt-in target that only exists with `-PwasmJs=true`
 * (see [isWasmJsEnabled]), so its absence is expected rather than a defect.
 */
fun Project.verifyDeclaredTargets(configuredProject: ConfiguredProject) {
  afterEvaluate {
    val kotlinExtension = extensions.getByType(KotlinMultiplatformExtension::class.java)

    // "metadata" is the KMP common-code target that KGP always adds — it corresponds to no entry.
    val actualTargetNames = kotlinExtension.targets.names.filter { it != "metadata" }.toSortedSet()
    val expectedTargetNames = configuredProject.targets
      .filter { it != KotlinTarget.WasmJs || isWasmJsEnabled() }
      .map { it.sourceSetPrefix }
      .toSortedSet()

    require(actualTargetNames == expectedTargetNames) {
      buildString {
        appendLine("Kotlin targets of $path deviate from its registration in the project registry.")
        appendLine("  registered: ${expectedTargetNames.joinToString(", ").ifEmpty { "<none>" }}")
        appendLine("  actual:     ${actualTargetNames.joinToString(", ").ifEmpty { "<none>" }}")
        appendLine("A module build script must not declare a target. Register it instead:")
        appendLine("  multiplatform(\"$path\", ${configuredProject.targets.joinToString(", ") { it.name }})")
      }
    }
  }
}

/**
 * Fails the build when a `src/<prefix>Main` source directory has no matching entry in the module's
 * registered target set — the hole that let a whole source set go unanalysed and uncompiled while
 * looking perfectly normal in the file tree.
 *
 * Intermediate source sets created by the default hierarchy template (`webMain`, `nativeMain`,
 * `appleMain`, …) are not targets and are therefore accepted.
 */
fun Project.verifyTargetSourceDirectories(configuredProject: ConfiguredProject) {
  val intermediateSourceSetNames = setOf("commonMain", "webMain", "nativeMain", "appleMain", "linuxMain", "mingwMain")

  val targetPrefixes = configuredProject.targets.map { it.mainSourceSetName }.toSet()
  val allTargetPrefixes = KotlinTarget.entries.map { it.mainSourceSetName }.toSet()

  val sourceRoot = projectDir.resolve("src")
  val unregistered = (sourceRoot.listFiles() ?: emptyArray())
    .filter { it.isDirectory && it.name.endsWith("Main") }
    .map { it.name }
    .filter { it !in intermediateSourceSetNames }
    .filter { it in allTargetPrefixes && it !in targetPrefixes }
    .sorted()

  require(unregistered.isEmpty()) {
    buildString {
      appendLine("$path has source directories for Kotlin targets it is not registered for: ${unregistered.joinToString(", ")}")
      appendLine("Nothing compiles or analyses that code. Register the target in the project registry:")
      appendLine("  multiplatform(\"$path\", …)")
    }
  }
}
