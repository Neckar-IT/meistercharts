package it.neckar.gradle.pnpm.dependencies

import it.neckar.gradle.AnsiConsole
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.Internal

/**
 * Base class for all pnpm dependency tasks
 */
abstract class BasePnpmDependencyTask : DefaultTask() {
  init {
    group = "PNPM Dependencies "
    outputs.upToDateWhen { false } //always execute this task
  }

  /**
   * Can be used to parse/write JSON
   */
  @OptIn(ExperimentalSerializationApi::class)
  @Internal
  protected val json: Json = Json {
    prettyPrintIndent = "  "
    prettyPrint = true
  }

  /**
   * Returns the dependency parameter as provided by the user using "-Pdependency={NODE_DEP}"
   */
  @Suppress("SpellCheckingInspection")
  @Internal
  protected fun getDependencyParameter(): String {
    val dependency = project.findProperty(ArgumentNameDependency) as? String

    if (dependency == null) {
      val console = AnsiConsole(project)
      logger.lifecycle(console.red("Provide the dependency as property:"))
      logger.lifecycle(console.green("\t\tgradle installPnpmDependency -P$ArgumentNameDependency={NODE_DEP}"))

      throw InvalidUserDataException("Add the dependency as property: `gradle installPnpmDependency -P$ArgumentNameDependency={NODE_DEP}`")
    }
    return dependency
  }

  companion object {
    const val ArgumentNameDependency: String = "dependency"
  }
}
