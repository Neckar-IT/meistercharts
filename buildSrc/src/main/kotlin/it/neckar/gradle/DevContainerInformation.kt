package it.neckar.gradle

import java.io.File

class DevContainerInformation(
  /**
   * Contains true if run in a container
   */
  val inDockerContainer: Boolean,
) {
  companion object {
    fun create(): DevContainerInformation {
      return DevContainerInformation(
        inDockerContainer = File("/.dockerenv").exists()
      )
    }
  }
}
