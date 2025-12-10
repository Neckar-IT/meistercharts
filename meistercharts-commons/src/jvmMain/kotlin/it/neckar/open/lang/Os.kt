package it.neckar.open.lang

import it.neckar.open.kotlin.lang.checkNotNull

/**
 * Allows checks for the operating system
 */
object Os {
  /**
   * The name of the operating system (System property "os.name")
   */
  val osName: String = System.getProperty("os.name").checkNotNull { "Property os.name not found" }

  val isLinux: Boolean = osName.contains("Linux") || osName.contains("LINUX")
  val isWindows: Boolean = osName.contains("Windows")
  val isMac: Boolean = osName.contains("Mac") || osName.contains("MAC")
}
