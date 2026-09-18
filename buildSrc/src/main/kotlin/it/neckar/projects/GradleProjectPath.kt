package it.neckar.projects

import kotlinx.serialization.Serializable

/**
 * Value class representing a Gradle project path.
 *
 * Provides type safety for project paths like `:internal:open:commons:typescript:typescript-utils`.
 */
@Serializable
@JvmInline
value class GradleProjectPath(val path: String) {
  init {
    require(path.startsWith(":")) { "Gradle project path must start with ':': $path" }
    require(path.isNotBlank()) { "Gradle project path must not be blank" }
  }

  override fun toString(): String = path

  /**
   * The project's location as a filesystem-relative path (relative to the Gradle root).
   *
   * Example: `:internal:patterns:ktor-backend` → `internal/patterns/ktor-backend`.
   */
  val filePath: String
    get() = path.removePrefix(":").replace(':', '/')

  /**
   * The path of [taskName] in this project: `:internal:patterns:ktor-backend` + `build` →
   * `:internal:patterns:ktor-backend:build`. The root project (`:`) yields `:build`.
   */
  fun task(taskName: String): GradleTaskPath {
    require(taskName.isNotBlank()) { "Task name must not be blank" }
    return GradleTaskPath(if (path == ":") ":$taskName" else "$path:$taskName")
  }

  /**
   * The path of [relativePath] below this project: `:internal:open` + `:commons:kotlin-lang` →
   * `:internal:open:commons:kotlin-lang`.
   */
  fun resolve(relativePath: GradleProjectPath): GradleProjectPath {
    return when {
      this == Root -> relativePath
      relativePath == Root -> this
      else -> GradleProjectPath(path + relativePath.path)
    }
  }

  /**
   * The inverse of [resolve]: `:internal:open` and `:internal:open:commons:kotlin-lang` →
   * `:commons:kotlin-lang`. Null when [descendantPath] is neither this project nor below it.
   */
  fun relativizeOrNull(descendantPath: GradleProjectPath): GradleProjectPath? {
    return when {
      this == Root -> descendantPath
      descendantPath == this -> Root
      descendantPath.path.startsWith("$path:") -> GradleProjectPath(descendantPath.path.removePrefix(path))
      else -> null
    }
  }

  companion object {
    /** The root project of a build. */
    val Root: GradleProjectPath = GradleProjectPath(":")
  }
}
