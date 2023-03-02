package com.meistercharts.canvas

/**
 * Contains extension tools for texts
 */

/**
 * Shortens a string if necessary
 */
interface StringShortener {
  /***
   * Shortens the given text.
   * If the truncation symbol is longer than [maxCharacters] a "!" will be returned to avoid confusion
   *
   * If shortening is not possible (e.g. the max characters are 0) null is returned.
   */
  fun shorten(text: String, maxCharacters: Int, truncationSymbol: String = "…"): String?

  /**
   * Shortens the string to max length in characters. Uses the truncation symbol if provided.
   * Truncates the end
   */
  object TruncateToLength : StringShortener {
    override fun shorten(text: String, maxCharacters: Int, truncationSymbol: String): String? {
      return truncateToLength(text, maxCharacters, truncationSymbol)
    }

    override fun toString(): String {
      return "Truncate to length"
    }
  }

  /**
   * Shortens the string to max length in characters. Uses the truncation symbol.
   * Truncates the center
   */
  object TruncateCenterToLength : StringShortener {
    override fun shorten(text: String, maxCharacters: Int, truncationSymbol: String): String? {
      return truncateCenterToLength(text, maxCharacters, truncationSymbol)
    }

    override fun toString(): String {
      return "Truncate center length"
    }
  }

  object NoOp : StringShortener {
    override fun shorten(text: String, maxCharacters: Int, truncationSymbol: String): String {
      return text
    }

    override fun toString(): String {
      return "NoOp"
    }
  }
}

/**
 * Truncates the text to the given max characters
 */
fun truncateToLength(text: String, maxCharacters: Int, truncationSymbol: String = "…"): String? {
  if (maxCharacters == 0) {
    return null
  }

  if (text.length <= maxCharacters) {
    //text is short enough - return the text itself
    return text
  }

  if (truncationSymbol.length > maxCharacters) {
    //we do not have any space for the truncation symbol - therefore return "!"
    return "!"
  }

  if (maxCharacters < truncationSymbol.length) {
    //shorter than truncation symbol. Therefore, we just return the text truncated
    return text.take(maxCharacters)
  }

  return text.take(maxCharacters - truncationSymbol.length) + truncationSymbol
}

/**
 * Truncates the text to the given characters. Truncates the center.
 */
fun truncateCenterToLength(text: String, maxCharacters: Int, truncationSymbol: String = "…"): String? {
  if (maxCharacters == 0) {
    return null
  }

  if (text.length <= maxCharacters) {
    return text
  }

  if (truncationSymbol.length > maxCharacters) {
    //we do not have any space for the truncation symbol - therefore return "!"
    return "!"
  }

  val maxPartsLength = maxCharacters - truncationSymbol.length

  //on an odd length, the first part is one longer
  val secondPartLength = maxPartsLength / 2
  val firstPartLength = maxCharacters - truncationSymbol.length - secondPartLength

  val firstPart = text.take(firstPartLength)
  val lastPart = text.takeLast(secondPartLength)

  return firstPart + truncationSymbol + lastPart
}
