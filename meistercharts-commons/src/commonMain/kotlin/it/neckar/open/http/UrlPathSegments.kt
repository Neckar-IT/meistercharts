/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
