/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.cedarsoft.org/gpl3ce
 * (GPL 3 with Classpath Exception)
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
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
    //Verify the lower border
    val lower: Boolean
    if (includeLower) {
      lower = min.sameOrSmallerThan(other.min)
    } else {
      lower = min.smallerThan(other.min)
    }

    if (!lower) {
      return false
    }

    //Verify the upper border
    return if (includeUpper) {
      max.sameOrGreaterThan(other.max)
    } else {
      max.greaterThan(other.max)
    }
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
      var smalles: Version? = null
      var largest: Version? = null

      for (version in versions) {
        if (smalles == null || smalles.greaterThan(version)) {
          smalles = version
        }

        if (largest == null || largest.smallerThan(version)) {
          largest = version
        }
      }


      if (smalles == null || largest == null) {
        throw IllegalArgumentException("Need at least one version")
      }

      return VersionRange(smalles, largest)
    }
  }
}
