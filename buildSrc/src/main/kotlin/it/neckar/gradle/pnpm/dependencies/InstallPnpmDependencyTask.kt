package it.neckar.gradle.pnpm.dependencies

import it.neckar.gradle.AnsiConsole
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Install the pnpm dependency by calling the corresponding tasks for this project and the `npm-dependencies-project`
 */
open class InstallPnpmDependencyTask : BasePnpmDependencyTask() {
  /**
   * The dependency type to install
   */
  @Input
  var npmDependencyType: NpmDependencyType = NpmDependencyType.Production

  init {
    description = "Adds an npm dependency to the project and updates the npm-dependencies-project; synopsis: `gradle installPnpmDependency -P$ArgumentNameDependency={NODE_DEP}`"
    outputs.upToDateWhen { false }
  }


  @TaskAction
  fun installDependency() {
    val dependency = getDependencyParameter()
    logger.lifecycle("Installing npm dependency: $dependency")
    logger.lifecycle("Execute " + AnsiConsole(project).green("gradle :kotlinUpgradeYarnLock pnpmInstall"))
  }
}
