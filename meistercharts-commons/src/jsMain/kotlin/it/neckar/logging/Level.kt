package it.neckar.logging

/**
 * Represents a log level.
 * Inspired by slf4j
 */
actual enum class Level {
  ERROR,
  WARN,
  INFO,
  DEBUG,
  ;

  companion object {
    /**
     * Guesses the level from a string
     */
    fun guess(levelAsString: String?): Level? {
      if (levelAsString.isNullOrBlank()) return null

      val uppercase = levelAsString.trim().uppercase()

      //Check for direct hits
      when (uppercase) {
        "ERROR" -> return ERROR
        "WARN" -> return WARN
        "INFO" -> return INFO
        "DEBUG" -> return DEBUG
        else -> {}
      }

      when {
        uppercase.startsWith("E") -> return ERROR
        uppercase.startsWith("W") -> return WARN
        uppercase.startsWith("I") -> return INFO
        uppercase.startsWith("D") -> return DEBUG
        else -> {}
      }

      return null
    }
  }
}
