package it.neckar.gradle.pnpm.workspace

import it.neckar.gradle.Plugins
import it.neckar.gradle.pnpm.dependency.NpmPackageName
import it.neckar.gradle.pnpm.dependency.PackageJsonParser
import it.neckar.gradle.pnpm.dependency.PackageNameRegistry
import it.neckar.projects.ExternalProjects
import it.neckar.projects.GradleProjectPath
import it.neckar.projects.OtherProjects
import it.neckar.projects.Projects
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * Verifies that every `workspace:` dependency declared in a pnpm module's `package.json` names a
 * package that another registered pnpm module provides.
 *
 * The counterpart to [VerifyPnpmWorkspaceYamlPlugin], which verifies the nodes: that one checks the
 * `packages:` list against the registered pnpm projects, this one checks the edges between them.
 * Both matter because the build derives its pnpm task wiring from those edges — an unresolvable one
 * costs the build-order edge and the dependency's `dist/` input.
 */
class VerifyPnpmWorkspaceDependenciesPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.plugins.apply(Plugins.base)

    target.tasks.register<VerifyPnpmWorkspaceDependenciesTask>(VerifyTaskName) {
      group = "verification"
      description = "Verifies that every workspace: dependency of a pnpm module resolves to another registered pnpm module"

      // Provider defers pnpmProjects() until task realization — GradleContext.initialize
      // runs in the root build.gradle.kts body, which is after the plugins {} block.
      val relativePathByModule = target.provider {
        (Projects.pnpmProjects() + ExternalProjects.pnpmProjects() + OtherProjects.pnpmProjects())
          .associate { it.path.path to "${it.path.filePath}/package.json" }
      }

      // Repo-relative, so the input does not carry this checkout's location.
      packageJsonPathByModule.set(relativePathByModule)
      rootDirectory = target.layout.projectDirectory
      packageJsonFiles.setFrom(
        relativePathByModule.map { paths -> paths.values.map { target.layout.projectDirectory.file(it) } },
      )
      markerFile = target.layout.buildDirectory.file("$VerifyTaskName/marker.txt")
    }
  }

  companion object {
    const val VerifyTaskName: String = "verifyPnpmWorkspaceDependencies"
  }
}

abstract class VerifyPnpmWorkspaceDependenciesTask : DefaultTask() {
  /** Gradle module path to the repo-relative path of that module's `package.json`. */
  @get:Input
  abstract val packageJsonPathByModule: MapProperty<String, String>

  /** The same files again, so a changed `package.json` makes this task out of date. */
  @get:InputFiles
  abstract val packageJsonFiles: ConfigurableFileCollection

  /** Resolves the repo-relative paths above; not an input, since only the paths decide the outcome. */
  @get:Internal
  abstract val rootDirectory: DirectoryProperty

  @get:OutputFile
  abstract val markerFile: RegularFileProperty

  @TaskAction
  fun verify() {
    val parser = PackageJsonParser()
    val rootDir = rootDirectory.get()
    val packageJsonByModule: Map<GradleProjectPath, File> = packageJsonPathByModule.get()
      .toSortedMap()
      .map { (modulePath, relativePath) -> GradleProjectPath(modulePath) to rootDir.file(relativePath).asFile }
      .toMap()

    val problems = mutableListOf<String>()

    val missingManifests = packageJsonByModule.filterValues { it.isFile.not() }
    missingManifests.forEach { (modulePath, packageJsonFile) ->
      problems += "$modulePath has no package.json at $packageJsonFile"
    }

    val parsedByModule = (packageJsonByModule - missingManifests.keys)
      .mapValues { (_, packageJsonFile) -> parser.parse(packageJsonFile) }

    val moduleByPackageName = mutableMapOf<NpmPackageName, GradleProjectPath>()
    parsedByModule.forEach { (modulePath, parsed) ->
      val packageName = parsed.name
      if (packageName == null) {
        problems += "$modulePath declares no 'name' in ${packageJsonByModule.getValue(modulePath)}"
        return@forEach
      }

      // put returns the previous holder — two modules claiming one name would otherwise leave the
      // registry pointing at whichever came last.
      moduleByPackageName.put(packageName, modulePath)?.let { alreadyClaimedBy ->
        problems += "$modulePath and $alreadyClaimedBy both declare the npm package name '$packageName'"
      }
    }

    val registry = PackageNameRegistry(moduleByPackageName)

    parsedByModule.forEach { (modulePath, parsed) ->
      parsed.workspaceDependencies.all.forEach { packageName ->
        if (registry.findGradlePathOrNull(packageName) == null) {
          problems += "$modulePath depends on '$packageName', which no registered pnpm module provides"
        }
      }
    }

    if (problems.isNotEmpty()) {
      throw GradleException(
        buildString {
          appendLine("pnpm workspace dependency verification failed across ${packageJsonByModule.size} pnpm modules:")
          problems.forEach { appendLine("  - $it") }
          appendLine()
          append("A module is registered in settings.gradle.kts and Projects.kt; the npm package it provides is the 'name' of its package.json.")
        },
      )
    }

    val marker = markerFile.get().asFile
    marker.parentFile.mkdirs()
    marker.writeText("${VerifyPnpmWorkspaceDependenciesPlugin.VerifyTaskName} OK for ${packageJsonByModule.size} pnpm modules\n")
  }
}
