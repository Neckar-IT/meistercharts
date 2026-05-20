package it.neckar.gradle.pnpm.workspace

import it.neckar.gradle.Plugins
import it.neckar.projects.Projects
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * Verifies that the committed `pnpm-workspace.yaml` `packages:` list matches the set of pnpm projects
 * declared in [Projects.pnpmProjects] plus [VerifyPnpmWorkspaceYamlPluginExtension.manualEntries].
 *
 * Fails fast if entries are missing, unknown, or duplicated. Order is not enforced.
 */
class VerifyPnpmWorkspaceYamlPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.plugins.apply(Plugins.base)

    val extension = target.extensions.create<VerifyPnpmWorkspaceYamlPluginExtension>(ExtensionName).apply {
      workspaceYamlFile.convention(target.layout.projectDirectory.file("pnpm-workspace.yaml"))
    }

    target.tasks.register<VerifyPnpmWorkspaceYamlTask>(VerifyTaskName) {
      group = "verification"
      description = "Verifies that pnpm-workspace.yaml lists the same packages as Projects.pnpmProjects() + manualEntries"

      workspaceYamlFile = extension.workspaceYamlFile
      manualEntries = extension.manualEntries
      // Provider defers Projects.pnpmProjects() until task realization — GradleContext.initialize
      // runs in the root build.gradle.kts body, which is after the plugins {} block.
      expectedProjectPaths.set(target.provider {
        Projects.pnpmProjects().map { it.path.filePath }
      })
      markerFile = target.layout.buildDirectory.file("verifyPnpmWorkspaceYaml/marker.txt")
    }
  }

  companion object {
    const val ExtensionName: String = "verifyPnpmWorkspaceYaml"
    const val VerifyTaskName: String = "verifyPnpmWorkspaceYaml"
  }
}

abstract class VerifyPnpmWorkspaceYamlPluginExtension {
  /**
   * Path to the `pnpm-workspace.yaml` to verify. Defaults to `<projectDir>/pnpm-workspace.yaml`.
   */
  abstract val workspaceYamlFile: RegularFileProperty

  /**
   * Additional entries that must appear in the `packages:` list beyond [Projects.pnpmProjects].
   * Use for build-output paths or other locations not covered by the Gradle project graph.
   */
  abstract val manualEntries: ListProperty<String>
}

abstract class VerifyPnpmWorkspaceYamlTask : DefaultTask() {
  @get:InputFile
  abstract val workspaceYamlFile: RegularFileProperty

  @get:Input
  abstract val manualEntries: ListProperty<String>

  @get:Input
  abstract val expectedProjectPaths: ListProperty<String>

  @get:OutputFile
  abstract val markerFile: RegularFileProperty

  @TaskAction
  fun verify() {
    val file = workspaceYamlFile.get().asFile
    val actualEntries = parsePackagesList(file)

    val expectedEntries: Set<String> = buildSet {
      addAll(expectedProjectPaths.get())
      addAll(manualEntries.get())
    }

    val problems = mutableListOf<String>()

    val duplicates = actualEntries.groupBy { it }.filterValues { it.size > 1 }.keys.sorted()
    if (duplicates.isNotEmpty()) {
      problems += "Duplicate in ${file.name}: ${duplicates.joinToString(", ")}"
    }

    val actualSet = actualEntries.toSet()
    val missing = (expectedEntries - actualSet).sorted()
    if (missing.isNotEmpty()) {
      problems += "Missing in ${file.name}: ${missing.joinToString(", ")}"
    }

    val unknown = (actualSet - expectedEntries).sorted()
    if (unknown.isNotEmpty()) {
      problems += "Unknown in ${file.name}: ${unknown.joinToString(", ")}"
    }

    if (problems.isNotEmpty()) {
      throw GradleException(
        buildString {
          appendLine("pnpm-workspace.yaml verification failed for ${file.path}:")
          problems.forEach { appendLine("  - $it") }
          appendLine()
          append("Expected packages are defined by Projects.pnpmProjects() in buildSrc plus manualEntries in build.gradle.kts.")
        },
      )
    }

    val marker = markerFile.get().asFile
    marker.parentFile.mkdirs()
    marker.writeText("verifyPnpmWorkspaceYaml OK\n")
  }
}

/**
 * Parses the `packages:` list of a pnpm-workspace.yaml file.
 *
 * Other top-level keys in the file are ignored (`strictMode = false`).
 * Fails if the `packages:` key is missing entirely.
 */
private fun parsePackagesList(file: File): List<String> {
  val yaml = Yaml(
    configuration = Yaml.default.configuration.copy(
      strictMode = false,
    ),
  )
  val parsed = yaml.decodeFromString(PnpmWorkspaceYaml.serializer(), file.readText())
  return parsed.packages
    ?: throw GradleException("pnpm-workspace.yaml at ${file.path}: 'packages:' section not found")
}

@Serializable
private data class PnpmWorkspaceYaml(
  val packages: List<String>? = null,
)
