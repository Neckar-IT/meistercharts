import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

/**
 * Holdes references to Gradle
 */
object GradleContext {
  fun initialize(gradle: Gradle) {
    this.gradle = gradle
  }

  /**
   * The Gradle process itself
   */
  lateinit var gradle: Gradle
    private set

  /**
   * The root project
   */
  val rootProject: Project
    get() {
      return gradle.rootProject
    }

  /**
   * Returns the project instance.
   * Replace with context receivers as soon as https://github.com/gradle/gradle/issues/24221 has been fixed
   */
  fun project(path: String): Project {
    return gradle.rootProject.project(path)
  }
}
