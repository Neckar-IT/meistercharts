package it.neckar.open.http

/**
 * Tagging interface for URL path segments.
 */
sealed interface UrlPathSegment {
  /**
   * Represents a segment in a URL path - which is *not* a parameter.
   */
  data class Segment(val value: String) : UrlPathSegment {
    init {
      require(value.contains("/").not()) {
        "UrlPathSegment.Segment must not contain a slash: $value"
      }
    }

    override fun toString(): String {
      return value
    }
  }
}
