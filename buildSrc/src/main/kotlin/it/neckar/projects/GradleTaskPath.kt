package it.neckar.projects

import kotlinx.serialization.Serializable

/**
 * Value class representing a fully qualified Gradle task path.
 *
 * Provides type safety for task paths like `:internal:patterns:ktor-backend:openapiSpec`.
 * The owning project is derivable via [projectPath]; root-project tasks (`:build`) map to the
 * root path `:`.
 */
@Serializable
@JvmInline
value class GradleTaskPath(val path: String) {
  init {
    require(path.startsWith(":")) { "Gradle task path must start with ':': $path" }
    require(path.lastIndexOf(':') < path.length - 1) { "Gradle task path must end with a task name: $path" }
  }

  override fun toString(): String = path

  /** The bare task name without the project path. `:a:b:compileKotlin` → `compileKotlin`. */
  val taskName: String
    get() = path.substringAfterLast(':')

  /**
   * The owning project's path. `:a:b:compileKotlin` → `:a:b`; `:build` (root task) → `:`.
   */
  val projectPath: GradleProjectPath
    get() {
      val prefix = path.substring(0, path.lastIndexOf(':'))
      return GradleProjectPath(prefix.ifEmpty { ":" })
    }
}
