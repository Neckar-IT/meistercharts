package it.neckar.gradle.pnpm.workspace

import it.neckar.gradle.pnpm.dependency.NpmPackageName
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class VerifyPnpmLockfilePeerVariantsTask : DefaultTask() {
  @get:InputFile
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val lockfile: RegularFileProperty

  @get:Input
  abstract val packageNames: SetProperty<String>

  @get:OutputFile
  abstract val markerFile: RegularFileProperty

  @TaskAction
  fun verify() {
    val graph = PnpmLockfileGraph.parse(lockfile.get().asFile.readText())
    val singleCopyPackageNames: Set<NpmPackageName> = packageNames.get().map { NpmPackageName(it) }.toSet()

    val missingPackageNames = graph.packageNamesWithoutSnapshot(singleCopyPackageNames)
    if (missingPackageNames.isNotEmpty()) {
      throw GradleException("pnpm-lock.yaml holds no snapshot of $missingPackageNames. See ai/guides/pnpm-dependencies.md.")
    }

    val splits = PnpmPeerVariantSplit.compute(graph, singleCopyPackageNames)
    if (splits.isNotEmpty()) {
      throw GradleException(
        buildString {
          appendLine("pnpm-lock.yaml installs a version as several copies, each resolved against other peers:")
          splits.forEach { append(it.describe(indentation = "  ")) }
          appendLine()
          append(
            "Declare the peer that differs between the copies as a `catalog:` devDependency in the root package.json, " +
              "then run `pnpm install --lockfile-only`. See ai/guides/pnpm-dependencies.md.",
          )
        },
      )
    }

    val marker = markerFile.get().asFile
    marker.parentFile.mkdirs()
    marker.writeText("${VerifyPnpmLockfilePeerVariantsPlugin.VerifyTaskName} OK for ${packageNames.get().sorted()}\n")
  }
}
