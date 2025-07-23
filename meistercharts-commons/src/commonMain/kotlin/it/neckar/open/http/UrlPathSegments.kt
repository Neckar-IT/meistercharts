package it.neckar.open.http

/**
 * Contains the segments of a (relative) URL.
 * Consists of the path segments and path parameters.
 */
data class UrlPathSegments(
  /**
   * The segments of the URL.
   * The first segment is the first segment after the leading slash.
   * E.g., for "/api/projects/123/configurations/456" the segments are:
   * - "api"
   * - "projects"
   * - UrlParameterName("123")
   * - "configurations"
   * - UrlParameterName("456")
   */
  val elements: List<UrlPathSegment>
) {
  init {
    require(elements.isNotEmpty()) { "UrlSegments must not be empty" }
  }

  /**
   * Formats the URL path segments as a string.
   * Contains a leading slash and joins the segments with slashes.
   */
  fun format(): String {
    return elements.joinToString(separator = "/", prefix = "/") { it.toString() }
  }

  /**
   * Returns all parameter names in the URL path segments.
   */
  fun parameterNames(): List<UrlParameterName> {
    return elements.filterIsInstance<UrlParameterName>()
  }

  override fun toString(): String {
    return format()
  }

  companion object {
    fun parse(path: String): UrlPathSegments {
      require(path.isNotEmpty()) { "Path must not be empty" }
      require(path.startsWith("/")) { "Path must start with a leading slash: $path" }

      val segments = path.split("/").filter { it.isNotEmpty() }.map { segment ->
        if (segment.startsWith("{") && segment.endsWith("}")) {
          UrlParameterName(segment.substring(1, segment.length - 1))
        } else {
          UrlPathSegment.Segment(segment)
        }
      }

      return UrlPathSegments(segments)
    }
  }
}
