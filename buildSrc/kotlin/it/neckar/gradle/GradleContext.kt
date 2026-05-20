package it.neckar.gradle

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

/**
 * Holdes references to Gradle
 */
object GradleContext {
  fun initialize(gradle: Gradle) {
    this.gradle = gradle
    callbacks.forEach { it(gradle) }
  }

  /**
   * The Gradle process itself
   */
  lateinit var gradle: Gradle
    private set

  /**
   * Returns null if the [gradle] instance has not been initialized yet
   */
  fun rootProjectOrNull(): Project? {
    return if (::gradle.isInitialized) {
      gradle.rootProject
    } else {
      null
    }
  }

  /**
   * The root project
   */
  val rootProject: Project
    get() {
      return gradle.rootProject
    }

  /**
   * Returns the project instance for the given [path].
   */
  fun project(path: String): Project {
    return gradle.rootProject.project(path)
  }

  private val callbacks = mutableListOf<(gradle: Gradle) -> Unit>()

  /**
   * Registers a callback called when the Gradle instance has been initialized
   */
  fun onGradleInitialized(callback: (gradle: Gradle) -> Unit) {
    callbacks.add(callback)
  }
}
