package it.neckar.gradle.pnpm.dependencies


import it.neckar.gradle.pnpm.dependencies.BasePnpmDependencyTask.Companion.ArgumentNameDependency
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.task


/**
 * Adds npm dependencies: version to `npm.versions.toml`, placeholder to `package.template.json`,
 * then regenerates `package.json` and updates lock files.
 *
 * Usage: `gradle installPnpmDependency -Pdependency=clsx`
 */
class InstallPnpmDependencyPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val extension = target.extensions.create<InstallPnpmDependencyPluginExtension>("installPnpmDependency").apply {
      packageJsonTemplateFile.convention(target.layout.projectDirectory.file("package.template.json"))
    }

    target.task<InstallPnpmDependencyTask>(InstallPnpmDependencyTaskName) {
      npmDependencyType = NpmDependencyType.Production
      dependsOn(AddPnpmDependencyToTemplateTaskName)
    }

    target.task<InstallPnpmDependencyTask>(InstallPnpmDevDependencyTaskName) {
      description = "Adds an npm dev-dependency; synopsis: `gradle installPnpmDevDependency -P$ArgumentNameDependency={NODE_DEP}`"
      npmDependencyType = NpmDependencyType.Dev
      dependsOn(AddPnpmDevDependencyToTemplateTaskName)
    }

    target.task<InstallPnpmDependencyTask>(InstallPnpmPeerDependencyTaskName) {
      description = "Adds an npm peer-dependency; synopsis: `gradle installPnpmPeerDependency -P$ArgumentNameDependency={NODE_DEP}`"
      npmDependencyType = NpmDependencyType.Peer
      dependsOn(AddPnpmPeerDependencyToTemplateTaskName)
    }

    //Add to template

    target.task<AddPnpmDependencyToTemplateTask>(AddPnpmDependencyToTemplateTaskName) {
      packageJsonTemplateFile = extension.packageJsonTemplateFile
    }
    target.task<AddPnpmDependencyToTemplateTask>(AddPnpmDevDependencyToTemplateTaskName) {
      description = "Adds an npm dev-dependency to the package.template.json file; synopsis: `gradle addPnpmDevDependencyToTemplate -P$ArgumentNameDependency={NODE_DEP}`"
      npmDependencyType = NpmDependencyType.Dev
      packageJsonTemplateFile = extension.packageJsonTemplateFile
    }
    target.task<AddPnpmDependencyToTemplateTask>(AddPnpmPeerDependencyToTemplateTaskName) {
      description = "Adds an npm peer-dependency to the package.template.json file; synopsis: `gradle addPnpmPeerDependencyToTemplate -P$ArgumentNameDependency={NODE_DEP}`"
      npmDependencyType = NpmDependencyType.Peer
      packageJsonTemplateFile = extension.packageJsonTemplateFile
    }
  }

  companion object {
    const val InstallPnpmDependencyTaskName: String = "installPnpmDependency"
    const val InstallPnpmDevDependencyTaskName: String = "installPnpmDevDependency"
    const val InstallPnpmPeerDependencyTaskName: String = "installPnpmPeerDependency"

    const val AddPnpmDependencyToTemplateTaskName: String = "addPnpmDependencyToTemplate"
    const val AddPnpmDevDependencyToTemplateTaskName: String = "addPnpmDevDependencyToTemplate"
    const val AddPnpmPeerDependencyToTemplateTaskName: String = "addPnpmPeerDependencyToTemplate"
  }
}

