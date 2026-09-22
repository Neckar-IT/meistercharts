package it.neckar.gradle.pnpm.workspace

import it.neckar.gradle.pnpm.dependency.NpmPackageName
import it.neckar.projects.GradleProjectPath
import it.neckar.projects.Projects

/**
 * The pnpm modules whose package only one other pnpm module may declare, each with that consumer.
 */
class ExclusiveConsumers(private val consumerByModule: Map<GradleProjectPath, GradleProjectPath>) {

  /** Matches any key `"<package name>":`, so `peerDependenciesMeta` counts; `"name"` is a value and never matches. */
  fun foreignDeclarationsIn(manifest: PackageJsonManifest, packageNameByModule: Map<GradleProjectPath, NpmPackageName>): List<ForeignConsumerDeclaration> {
    val lines = manifest.text.lines()
    return consumerByModule
      .filterValues { consumer -> consumer != manifest.module }
      .flatMap { (module, consumer) ->
        // A module without a name is reported by the task itself.
        val packageName = packageNameByModule[module] ?: return@flatMap emptyList()
        val key = Regex(""""${Regex.escape(packageName.name)}"\s*:""")
        lines.withIndex()
          .filter { (_, line) -> key.containsMatchIn(line) }
          .map { (index, _) ->
            ForeignConsumerDeclaration(
              packageJson = manifest.path,
              line = index + 1,
              module = manifest.module,
              packageName = packageName,
              consumer = consumer,
            )
          }
      }
  }

  /** An entry naming a module outside [pnpmModules] would make [foreignDeclarationsIn] match nothing. */
  fun staleEntries(pnpmModules: Set<GradleProjectPath>): List<String> {
    return consumerByModule.flatMap { (module, consumer) ->
      listOf(module, consumer)
        .filter { pnpmModules.contains(it).not() }
        .map { "ExclusiveConsumers names $it, which is no registered pnpm module" }
    }
  }

  companion object {
    /** `typescript-react-ui` wraps and re-exports every shadcn primitive; apps import from there. */
    val Monorepo: ExclusiveConsumers = ExclusiveConsumers(
      mapOf(
        Projects.open.commons.typescript.typescriptReactUiPrimitives.path to Projects.open.commons.typescript.typescriptReactUi.path,
      ),
    )
  }
}
