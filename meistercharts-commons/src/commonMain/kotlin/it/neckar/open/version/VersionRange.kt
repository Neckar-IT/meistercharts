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
package it.neckar.open.version


import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 *
 * VersionRange class.
 *
 */
data class VersionRange
/**
 *
 * Constructor for VersionRange.
 *
 * @param min          a Version object.
 * @param max          a Version object.
 * @param includeLower a boolean.
 * @param includeUpper a boolean.
 */
@JvmOverloads constructor(
  /**
   *
   * Getter for the field `min`.
   *
   * @return a Version object.
   */
  val min: Version,
  /**
   *
   * Getter for the field `max`.
   *
   * @return a Version object.
   */
  val max: Version,
  /**
   *
   * isIncludeLower
   *
   * @return a boolean.
   */
  val includeLower: Boolean = true,
  /**
   *
   * isIncludeUpper
   *
   * @return a boolean.
   */
  val includeUpper: Boolean = true
) {

  init {
    if (max.smallerThan(min)) {
      throw IllegalArgumentException("Max <$max> is smaller than min <$min>")
    }
  }

  /**
   * Returns true if this range contains the other range completely
   *
   * @param other the other range
   * @return true if this contains the other range completely, false otherwise
   */
  fun containsCompletely(other: VersionRange): Boolean {
    //Verify the lower border. When the borders are equal, this must be at least as inclusive as
    //other there (this inclusive, or other also exclusive) — otherwise an open range would not even
    //contain itself.
    val lower = min.smallerThan(other.min) || (min == other.min && (includeLower || !other.includeLower))

    if (!lower) {
      return false
    }

    //Verify the upper border (symmetric to the lower one).
    return max.greaterThan(other.max) || (max == other.max && (includeUpper || !other.includeUpper))
  }

  /**
   *
   * contains
   *
   * @param version a Version object.
   * @return a boolean.
   */
  operator fun contains(version: Version): Boolean {
    if (includeLower) {
      if (!version.sameOrGreaterThan(min)) {
        return false
      }
    } else {
      if (!version.greaterThan(min)) {
        return false
      }
    }

    return if (includeUpper) {
      version.sameOrSmallerThan(max)
    } else {
      version.smallerThan(max)
    }
  }

  /**
   *
   * overlaps
   *
   * @param other a VersionRange object.
   * @return a boolean.
   */
  fun overlaps(other: VersionRange): Boolean {
    val lower: Boolean
    if (includeLower && other.includeUpper) {
      lower = min.sameOrSmallerThan(other.max)
    } else {
      lower = min.smallerThan(other.max)
    }

    val upper: Boolean
    if (includeUpper && other.includeLower) {
      upper = max.sameOrGreaterThan(other.min)
    } else {
      upper = max.greaterThan(other.min)
    }

    return lower && upper
  }

  /**
   * {@inheritDoc}
   */
  override fun toString(): String {
    val builder = StringBuilder()

    if (includeLower) {
      builder.append("[")
      builder.append(min)
    } else {
      builder.append("]")
      builder.append(min)
    }

    builder.append("-")

    if (includeUpper) {
      builder.append(max)
      builder.append("]")
    } else {
      builder.append(max)
      builder.append("[")
    }

    return builder.toString()
  }

  /**
   * Formats the version range.
   * Returns a single version, if this range only contains one version
   *
   * @return the formatted version
   */
  fun format(): String {
    return if (max == min) {
      "[$max]"
    } else toString()

  }

  class Factory(private val min: Version) {
    fun to(max: Version): VersionRange {
      return VersionRange(min, max)
    }

    fun to(major: Int, minor: Int, build: Int): VersionRange {
      return to(Version(major, minor, build))
    }

    fun to(): VersionRange {
      return single()
    }

    fun single(): VersionRange {
      return VersionRange(min, min)
    }
  }

  companion object {

    @JvmStatic
    fun from(min: Version): Factory {
      return Factory(min)
    }

    @JvmStatic
    fun from(major: Int, minor: Int, build: Int): Factory {
      return Factory(Version(major, minor, build))
    }

    @JvmStatic
    fun single(major: Int, minor: Int, build: Int): VersionRange {
      return single(Version.valueOf(major, minor, build))
    }

    @JvmStatic
    fun single(version: Version): VersionRange {
      return VersionRange(version, version)
    }

    /**
     * Creates a new version range from the given versions that spans all given versions.
     * Looks for the smallest and largest version and uses these to create a new version range.
     */
    @JvmStatic
    fun fromVersions(versions: Iterable<Version>): VersionRange {
      var smallest: Version? = null
      var largest: Version? = null

      for (version in versions) {
        if (smallest == null || smallest.greaterThan(version)) {
          smallest = version
        }

        if (largest == null || largest.smallerThan(version)) {
          largest = version
        }
      }


      if (smallest == null || largest == null) {
        throw IllegalArgumentException("Need at least one version")
      }

      return VersionRange(smallest, largest)
    }
  }
}
