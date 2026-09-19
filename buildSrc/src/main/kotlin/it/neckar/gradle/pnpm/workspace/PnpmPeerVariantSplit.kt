package it.neckar.gradle.pnpm.workspace

import it.neckar.gradle.pnpm.dependency.NpmPackageName

/**
 * One version of [packageName] installed as several copies. TypeScript sees one declaration file per
 * copy: an augmentation of one copy is missing on the other, and a shared type becomes incompatible.
 */
data class PnpmPeerVariantSplit(
  val packageName: NpmPackageName,
  val version: PnpmSnapshotKey.Version,
  val dependentsByVariant: Map<PnpmSnapshotKey, List<PnpmDependent>>,
) {
  /** The copies and their users, one per line, indented by [indentation]. */
  fun describe(indentation: String): String {
    return buildString {
      appendLine("$indentation$packageName@$version:")
      dependentsByVariant.forEach { (variant, dependents) ->
        appendLine("$indentation  $variant")
        dependents.forEach { appendLine("$indentation    used by $it") }
      }
    }
  }

  companion object {
    fun compute(graph: PnpmLockfileGraph, packageNames: Set<NpmPackageName>): List<PnpmPeerVariantSplit> {
      return graph.snapshotKeys
        .filter { it.packageName in packageNames }
        .distinct()
        .groupBy { it.packageName to it.version }
        .filterValues { it.size > 1 }
        .map { (packageVersion, variants) ->
          val (packageName, version) = packageVersion
          PnpmPeerVariantSplit(
            packageName = packageName,
            version = version,
            dependentsByVariant = variants.associateWith { variant ->
              // vitest and vite come in through installing blocks only; a copy without a user is an entry the graph skipped.
              graph.dependentsOf(variant).also {
                check(it.isNotEmpty()) { "pnpm-lock.yaml: no dependencies, devDependencies or optionalDependencies entry resolves to $variant" }
              }
            },
          )
        }
    }
  }
}
