import org.apache.commons.lang3.SystemUtils
import org.gradle.api.Project
import java.io.File

/**
 * Contains references to preinstalled/provided tools
 */

/**
 * Path to jib-cli
 */
val Project.jibCli: File
  get() {
    when {
      SystemUtils.IS_OS_LINUX -> {
        return rootProject.file("tools/jib-cli/jib-0.12.0/bin/jib")
      }

      SystemUtils.IS_OS_WINDOWS -> {
        return rootProject.file("tools/jib-cli/jib-0.12.0/bin/jib.bat")
      }

      else -> {
        throw UnsupportedOperationException("unsupported OS detected")
      }
    }
  }



