package it.neckar.gradle.pnpm.workspace

import it.neckar.gradle.Plugins
import it.neckar.gradle.pnpm.dependency.NpmPackageName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register

/**
 * Verifies that `pnpm-lock.yaml` installs every version of [singleCopyPackageNames] as one copy, so a
 * [PnpmPeerVariantSplit] fails the merge request gate with the packages using each copy.
 */
class VerifyPnpmLockfilePeerVariantsPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.plugins.apply(Plugins.base)

    target.tasks.register<VerifyPnpmLockfilePeerVariantsTask>(VerifyTaskName) {
      group = "verification"
      description = "Verifies that pnpm-lock.yaml installs ${singleCopyPackageNames.joinToString(" and ")} as one copy per version"

      lockfile = target.layout.projectDirectory.file("pnpm-lock.yaml")
      packageNames.set(singleCopyPackageNames.map { it.name })
      markerFile = target.layout.buildDirectory.file("$VerifyTaskName/marker.txt")
    }
  }

  companion object {
    const val VerifyTaskName: String = "verifyPnpmLockfilePeerVariants"

    /**
     * Packages whose types other packages extend or share: `@testing-library/jest-dom/vitest` augments
     * the `Assertion` of vitest, and the `Reporter` of vitest and the `Plugin` of vite cross package boundaries.
     */
    val singleCopyPackageNames: Set<NpmPackageName> = setOf(NpmPackageName("vitest"), NpmPackageName("vite"))
  }
}
