package it.neckar.gradle

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

