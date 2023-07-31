import org.apache.commons.lang3.SystemUtils
import org.gradle.api.Project
import java.io.File

/**
 * Contains references to preinstalled/provided tools
 */

/**
 * Path to jib-cli
 */
@Deprecated("use JibCliPlugin instead")
val Project.jibCli: File
  get() {
    val jibCliDir = File(tools, "jib-cli/jib-0.12.0")
    val jibCliBinDir = File(jibCliDir, "bin").also { require(it.isDirectory) { "jib-cli bin directory <${it.absolutePath}> does not exist or is not a directory" } }

    return when {
      SystemUtils.IS_OS_LINUX -> {
        File(jibCliBinDir, "jib").also { require(it.isFile) { "jib-cli binary <${it.absolutePath}> does not exist or is not a file" } }
      }

      SystemUtils.IS_OS_WINDOWS -> {
        File(jibCliBinDir, "jib.bat").also { require(it.isFile) { "jib-cli binary <${it.absolutePath}> does not exist or is not a file" } }
      }

      else -> {
        throw UnsupportedOperationException("unsupported OS detected")
      }
    }
  }

@Deprecated("Use the installed docker compose", level = DeprecationLevel.ERROR)
val Project.dockerCompose: File
  get() {
    throw UnsupportedOperationException("Use the installed docker compose")
  }

/**
 * Returns the tools folder
 */
val Project.tools: File
  get() {
    return rootProject.file("tools").also {
      require(it.isDirectory) { "tools directory <${it.absolutePath}> does not exist or is not a directory" }
    }
  }

