package it.neckar.gradle.pnpm.dependencies


import it.neckar.gradle.pnpm.dependencies.BasePnpmDependencyTask.Companion.ArgumentNameDependency
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.task


/**
 * Installs a npm dependency to the package.template.json file.
 * Also updates all o
 */
class InstallPnpmDependencyPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val extension = target.extensions.create<InstallPnpmDependencyPluginExtension>("installPnpmDependency").apply {
      dependencyFile.convention(target.layout.projectDirectory.file("npmDependencies.json"))
      packageJsonTemplateFile.convention(target.layout.projectDirectory.file("package.template.json"))
    }


    /**
     * This task is only relevant if this is the `npm-dependencies-project`
     */
    target.task<InstallPnpmDependencyToNpmDependenciesProject>(InstallPnpmDependencyToNpmDependenciesProjectTaskName) {
      dependencyFile = extension.dependencyFile
    }

    /**
     * This task is relevant for all projects but the `npm-dependencies-project`
     */
    target.task<InstallPnpmDependencyTask>(InstallPnpmDependencyTaskName) {
      npmDependencyType = NpmDependencyType.Production
      finalizedBy(":internal:closed:tools:npm-dependencies-project:$InstallPnpmDependencyToNpmDependenciesProjectTaskName")

      //Ensure the dependency is added to *our* `package.template.json` file
      dependsOn(AddPnpmDependencyToTemplateTaskName)
    }

    target.task<InstallPnpmDependencyTask>(InstallPnpmDevDependencyTaskName) {
      description = "Adds an npm dev-dependency to the project and updates the npm-dependencies-project; synopsis: `gradle installPnpmDevDependency -P$ArgumentNameDependency={NODE_DEP}`"
      npmDependencyType = NpmDependencyType.Dev
      finalizedBy(":internal:closed:tools:npm-dependencies-project:$InstallPnpmDependencyToNpmDependenciesProjectTaskName")

      //Ensure the dependency is added to *our* `package.template.json` file
      dependsOn(AddPnpmDevDependencyToTemplateTaskName)
    }

    target.task<InstallPnpmDependencyTask>(InstallPnpmPeerDependencyTaskName) {
      description = "Adds an npm peer-dependency to the project and updates the npm-dependencies-project; synopsis: `gradle installPnpmPeerDependency -P$ArgumentNameDependency={NODE_DEP}`"
      npmDependencyType = NpmDependencyType.Peer
      finalizedBy(":internal:closed:tools:npm-dependencies-project:$InstallPnpmDependencyToNpmDependenciesProjectTaskName")

      //Ensure the dependency is added to *our* `package.template.json` file
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

    const val InstallPnpmDependencyToNpmDependenciesProjectTaskName: String = "installPnpmDependencyToNpmDependenciesProject"
  }
}

