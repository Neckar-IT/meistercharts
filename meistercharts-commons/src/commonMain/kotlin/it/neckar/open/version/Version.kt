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
 * A version
 *
 */
data class Version
/**
 * Creates a version
 *
 * @param major  the major part
 * @param minor  the minor part
 * @param build  the build
 * @param suffix the suffix
 */
@JvmOverloads
constructor(
  /**
   *
   * Getter for the field `major`.
   *
   * @return a int.
   */
  val major: Int,
  /**
   *
   * Getter for the field `minor`.
   *
   * @return a int.
   */
  val minor: Int,
  /**
   *
   * Getter for the field `build`.
   *
   * @return a int.
   */
  val build: Int,
  /**
   * Returns the optional suffix
   *
   * @return the suffix or null if no suffix has been set
   */
  val suffix: String? = null
) : Comparable<Version> {

  val isSnapshot: Boolean
    get() = suffix != null && suffix.contains("SNAPSHOT")

  init {
    if (major < 0) {
      throw IllegalArgumentException("Invalid major <$major>")
    }
    if (minor < 0) {
      throw IllegalArgumentException("Invalid minor <$minor>")
    }
    if (build < 0) {
      throw IllegalArgumentException("Invalid build <$build>")
    }
  }

  fun withMajor(major: Int): Version {
    return Version(major, minor, build, suffix)
  }

  fun withMinor(minor: Int): Version {
    return Version(major, minor, build, suffix)
  }

  fun withBuild(build: Int): Version {
    return Version(major, minor, build, suffix)
  }

  fun withSuffix(suffix: String?): Version {
    return Version(major, minor, build, suffix)
  }

  /**
   *
   * format
   *
   * @return a String object.
   */
  fun format(): String {
    return toString()
  }

  /**
   * {@inheritDoc}
   */
  override fun toString(): String {
    return if (suffix.isNullOrBlank()) {
      "$major.$minor.$build"
    } else {
      "$major.$minor.$build-$suffix"
    }
  }

  /**
   * {@inheritDoc}
   */
  override fun compareTo(other: Version): Int {
    if (major != other.major) {
      return major.compareTo(other.major)
    }

    return if (minor != other.minor) {
      minor.compareTo(other.minor)
    } else {
      build.compareTo(other.build)
    }
  }

  /**
   * Liefert den Int-Werte des Versionsnummer. Major, Minor und Build gehen maximal bis 99.
   *
   * @return the int value for the version
   */
  fun toInt(): Int {
    return major * 10000 + minor * 100 + build
  }

  /**
   *
   * sameOrSmallerThan
   *
   * @param version a Version object.
   * @return a boolean.
   */
  fun sameOrSmallerThan(version: Version): Boolean {
    return this <= version
  }

  /**
   *
   * smallerThan
   *
   * @param version a Version object.
   * @return a boolean.
   */
  fun smallerThan(version: Version): Boolean {
    return this < version
  }

  /**
   *
   * sameOrGreaterThan
   *
   * @param version a Version object.
   * @return a boolean.
   */
  fun sameOrGreaterThan(version: Version): Boolean {
    return this >= version
  }

  /**
   *
   * greaterThan
   *
   * @param version a Version object.
   * @return a boolean.
   */
  fun greaterThan(version: Version): Boolean {
    return this > version
  }

  /**
   * @param major the major
   * @param minor the minor
   * @param build the build
   * @noinspection ParameterHidesMemberVariable
   */
  fun equals(major: Int, minor: Int, build: Int): Boolean {
    return this.major == major && this.minor == minor && this.build == build
  }

  companion object {

    /**
     * Parses a version
     *
     * @param version the version number as string
     * @return the parsed version
     *
     * @throws IllegalArgumentException if any.
     */
    @JvmStatic
    fun parse(version: String): Version {
      val indexDot0 = version.indexOf('.')
      val indexDot1 = version.indexOf('.', indexDot0 + 2)
      val indexMinus = version.indexOf('-', indexDot1 + 2)

      if (indexDot0 == -1 || indexDot1 == -1) {
        throw IllegalArgumentException("Cannot parse <$version>")
      }

      val major = version.substring(0, indexDot0).toInt()
      val minor = version.substring(indexDot0 + 1, indexDot1).toInt()

      return if (indexMinus == -1) {
        val build = version.substring(indexDot1 + 1).toInt()
        Version(major, minor, build)
      } else {
        val build = version.substring(indexDot1 + 1, indexMinus).toInt()
        val suffix = version.substring(indexMinus + 1)
        Version(major, minor, build, suffix)
      }
    }

    /**
     * Returns the fallback version in case of a parse exception
     */
    @JvmStatic
    fun parseOrFallback(version: String, fallback: Version): Version {
      return try {
        parse(version)
      } catch (ignore: IllegalArgumentException) {
        fallback
      }
    }

    /**
     * Returns null in case of a parse exception
     */
    @JvmStatic
    fun parseOrNull(version: String): Version? {
      return try {
        parse(version)
      } catch (ignore: IllegalArgumentException) {
        null
      }
    }

    @JvmStatic
    @Throws(VersionMismatchException::class)
    fun verifyMatch(expected: Version, actual: Version) {
      if (expected != actual) {
        throw VersionMismatchException(expected, actual)
      }
    }

    @JvmStatic
    fun valueOf(major: Int, minor: Int, build: Int): Version {
      return of(major, minor, build)
    }

    @JvmStatic
    fun valueOf(major: Int, minor: Int, build: Int, suffix: String?): Version {
      return of(major, minor, build, suffix)
    }

    @JvmStatic
    fun of(major: Int, minor: Int, build: Int): Version {
      return Version(major, minor, build)
    }

    @JvmStatic
    fun of(major: Int, minor: Int, build: Int, suffix: String?): Version {
      return Version(major, minor, build, suffix)
    }

    @Suppress("ObjectPropertyName")
    @JvmStatic
    val _1_0_0: Version = of(1, 0, 0)

  }
}
