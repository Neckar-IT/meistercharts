import org.apache.commons.lang3.SystemUtils
import org.gradle.api.Project
import java.io.File

/**
 * Contains references to preinstalled/provided tools
 */

/**
 * Returns the tools folder
 */
val Project.tools: File
  get() {
    return rootProject.file("tools").also {
      require(it.isDirectory) { "tools directory <${it.absolutePath}> does not exist or is not a directory" }
    }
  }

/**
 * Path to jib-cli
 */
@Deprecated("use JibCliPlugin instead")
val Project.jibCli: File
  get() {
    throw UnsupportedOperationException("Use JibCliPlugin instead")
  }

@Deprecated("Use the installed docker compose", level = DeprecationLevel.ERROR)
val Project.dockerCompose: File
  get() {
    throw UnsupportedOperationException("Use the installed docker compose")
  }

