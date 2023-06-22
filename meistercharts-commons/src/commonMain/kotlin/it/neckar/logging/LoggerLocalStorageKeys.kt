package it.neckar.logging

/**
 * Holds constants for local storage related stuff
 */
object LoggerLocalStorageKeys {

  /**
   * The prefix for the logger levels
   */
  const val LoggerPrefix: String = "logging."

  /**
   * The key for the debug root level
   */
  const val RootLevel: String = "${LoggerPrefix}rootLevel"
}
