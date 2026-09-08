package it.neckar.gradle.verify.devsetup

import org.gradle.api.InvalidUserDataException
import java.io.File

/**
 * Verifies that a `gradle.properties` file exists in the Gradle user home — it carries the
 * mandatory memory and worker settings (template: docs/gradle/sample-gradle.properties).
 */
internal fun DevSetupCheckContext.verifyGradlePropertiesExists() {
  val gradlePropertiesFile = File(System.getProperty("user.home") + "/.gradle/gradle.properties")
  if (gradlePropertiesFile.isFile.not()) {
    val templateFile = rootDirectory.resolve("docs/gradle/sample-gradle.properties")

    val solution = if (isLinux) {
      ansibleSolution
    } else {
      val command = if (isWindows) "copy ${templateFile.absolutePath} ${gradlePropertiesFile.absolutePath}"
      else "cp ${templateFile.absolutePath} ${gradlePropertiesFile.absolutePath}"
      "Copy the template File to your home directory\n${console.green(command)}"
    }

    throw InvalidUserDataException("No gradle.properties file found in your home directory\n$solution")
  }
}
