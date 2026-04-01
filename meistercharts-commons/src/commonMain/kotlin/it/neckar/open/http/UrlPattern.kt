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

import it.neckar.open.collections.fastForEach
import kotlin.uuid.Uuid

/**
 * A pattern for a URL. This pattern may contain unresolved parameters.
 *
 * The pattern can be resolved to a [Url.Relative] by providing the parameter values.
 *
 */
interface UrlPattern {
  /**
   * The string value of the URL.
   * Must contain the parameter names in "{}" notation (e.g., "foobar/{uuid}").
   */
  val value: String

  /**
   * Returns the parameter names that are part of the [value].
   * Might be empty.
   */
  val parameterNames: List<UrlParameterName>

  /**
   * Appends the string to the URL as a new path segment.
   */
  @Deprecated("Do not use strings for URL patterns. Use UrlParameterName instead.", level = DeprecationLevel.WARNING)
  operator fun plus(toAppend: String): UrlPattern

  /**
   * Appends the parameter name to the URL as a new path segment.
   */
  operator fun plus(toAppend: UrlParameterName): UrlPattern

  /**
   * Appends the URL pattern
   */
  operator fun plus(toAppend: UrlPattern): UrlPattern

  @Deprecated("It does not make sense to add an URL to a URL pattern. Use UrlPattern.resolve() instead.", level = DeprecationLevel.ERROR)
  operator fun plus(toAppend: Url): UrlPattern {
    throw UnsupportedOperationException("It does not make sense to add an URL to a URL pattern. Use UrlPattern.resolve() instead.")
  }

  companion object {
    fun relative(value: String): Relative0 {
      return Relative0(value)
    }

    fun relative(value: String, parameterName: UrlParameterName): Relative1 {
      return Relative1(value, parameterName)
    }

    fun relative(value: String, parameterName1: UrlParameterName, parameterName2: UrlParameterName): Relative2 {
      return Relative2(value, parameterName1, parameterName2)
    }

    fun relative(value: String, parameterName1: UrlParameterName, parameterName2: UrlParameterName, parameterName3: UrlParameterName): Relative3 {
      return Relative3(value, parameterName1, parameterName2, parameterName3)
    }

    fun relative(value: String, parameterNames: List<UrlParameterName>): Relative {
      return RelativeMulti(value, parameterNames)
    }

    /**
     * Creates a new instance of the builder
     */
    fun builder(): Builder {
      return Builder()
    }
  }


  /**
   * Relative URL pattern
   */
  interface Relative : UrlPattern {
    /**
     * Appends the string to the URL as a new path segment.
     */
    override operator fun plus(toAppend: String): Relative

    /**
     * Appends the parameter name to the URL as a new path segment.
     */
    override operator fun plus(toAppend: UrlParameterName): Relative

    /**
     * Appends the URL to the URL
     */
    override operator fun plus(toAppend: UrlPattern): Relative


    @Deprecated("Do not use this method. Use resolve() with the correct amount of parameters instead.", level = DeprecationLevel.HIDDEN)
    fun resolve(vararg parameterValues: Any): Url.Relative {
      throw UnsupportedOperationException("Keep as hint, that this signature is a bad idea")
    }

    /**
     * Appends the given values to the URL in order of the parameter names.
     * When calling this method, all parameters must be provided.
     */
    fun resolve(parameterValues: List<String>): Url.Relative

    /**
     * Replaces all variables with the given values.
     * When calling this method, all parameters must be provided.
     */
    fun resolve(parameterValues: Map<UrlParameterName, String>): Url.Relative

    @Deprecated("Do not use this method. Use resolve() with the correct amount of parameters instead.", level = DeprecationLevel.HIDDEN)
    fun resolve(vararg parameterValues: Uuid): Url.Relative {
      throw UnsupportedOperationException("Keep as hint, that this signature is a bad idea")
    }

    fun toBuilder(): Builder {
      return Builder.of(this)
    }
  }

  /**
   * Relative pattern with multiple parameters.
   * It is preferred to use [Relative0], [Relative1], [Relative2], [Relative3] for patterns with a fixed number of parameters.
   *
   * This is the default implementation for patterns with a variable number of parameters.
   * Should not be used explicitly: Use the interface [it.neckar.open.http.UrlPattern.Relative] instead.
   */
  data class RelativeMulti(
    /**
     * Contains the URL including parameters with "{}" notation
     */
    override val value: String,
    /**
     * The parameter names that are part of the [value]
     */
    override val parameterNames: List<UrlParameterName>,
  ) : Relative {

    init {
      require(parameterNames.isNotEmpty()) {
        "The URL [$value] must contain at least one variable - but no variables provided"
      }

      require(value.contains("{")) {
        "The URL [$value] must contain at least one variable"
      }

      parameterNames.fastForEach { variable ->
        require(value.contains("{${variable.value}}")) {
          "The URL [$value] must contain the variable [${variable.value}]"
        }
      }
    }

    override operator fun plus(toAppend: UrlPattern): Relative {
      return RelativeMulti(appendUrlStrings(value, toAppend.value), parameterNames + toAppend.parameterNames)
    }

    override operator fun plus(toAppend: UrlParameterName): Relative {
      return RelativeMulti(appendUrlStrings(value, toAppend.asUrlPatternParameter()), parameterNames + toAppend)
    }

    /**
     * Appends something to the URL
     */
    override operator fun plus(toAppend: String): Relative {
      return RelativeMulti(appendUrlStrings(value, toAppend), parameterNames)
    }

    override fun resolve(parameterValues: List<String>): Url.Relative {
      require(parameterValues.size == parameterNames.size) {
        "The number of values (${parameterValues.size}) must match the number of variables (${parameterNames.size})"
      }

      val replaced = parameterNames.foldIndexed(value) { index, acc: String, variable ->
        acc.replace("{${variable.value}}", parameterValues[index])
      }

      return Url.relative(replaced)
    }

    /**
     * Replaces all variables with the given values
     */
    override fun resolve(parameterValues: Map<UrlParameterName, String>): Url.Relative {
      val replaced = parameterNames.fold(value) { acc, variable ->
        acc.replace("{${variable.value}}", parameterValues[variable] ?: throw IllegalArgumentException("Missing parameter [${variable.value}]"))
      }

      return Url.Relative(replaced)
    }

    override fun toString(): String {
      return value
    }
  }

  /**
   * Relative pattern with no parameters
   */
  data class Relative0(
    override val value: String,
  ) : Relative {

    init {
      require(!value.contains("{")) {
        "The URL [$value] must not contain any variables"
      }
    }

    override val parameterNames: List<UrlParameterName>
      get() = emptyList()

    override fun plus(toAppend: String): Relative0 {
      return Relative0(appendUrlStrings(value, toAppend))
    }

    override operator fun plus(toAppend: UrlParameterName): Relative1 {
      return Relative1(appendUrlStrings(value, toAppend.asUrlPatternParameter()), toAppend)
    }

    override operator fun plus(toAppend: UrlPattern): Relative {
      return RelativeMulti(appendUrlStrings(value, toAppend.value), toAppend.parameterNames)
    }

    operator fun plus(toAppend: Relative0): Relative0 {
      return Relative0(appendUrlStrings(value, toAppend.value))
    }

    operator fun plus(toAppend: Relative1): Relative1 {
      return Relative1(appendUrlStrings(value, toAppend.value), toAppend.parameterName)
    }

    operator fun plus(toAppend: Relative2): Relative2 {
      return Relative2(appendUrlStrings(value, toAppend.value), toAppend.parameterName1, toAppend.parameterName2)
    }

    operator fun plus(toAppend: Relative3): Relative3 {
      return Relative3(appendUrlStrings(value, toAppend.value), toAppend.parameterName1, toAppend.parameterName2, toAppend.parameterName3)
    }

    fun resolve(): Url.Relative {
      return Url.relative(value)
    }

    override fun resolve(parameterValues: List<String>): Url.Relative {
      require(parameterValues.isEmpty()) {
        "No parameter values are expected for this pattern"
      }

      return resolve()
    }

    override fun resolve(parameterValues: Map<UrlParameterName, String>): Url.Relative {
      require(parameterValues.isEmpty()) {
        "No parameter values are expected for this pattern"
      }

      return resolve()
    }

    override fun toString(): String {
      return value
    }
  }

  /**
   * Relative pattern with exactly one parameter
   */
  data class Relative1(
    override val value: String,
    val parameterName: UrlParameterName,
  ) : Relative {

    init {
      require(value.contains("{${parameterName.value}}")) {
        "The URL [$value] must contain the variable [${parameterName.value}]"
      }
    }

    override val parameterNames: List<UrlParameterName>
      get() = listOf(parameterName)

    override operator fun plus(toAppend: UrlParameterName): Relative2 {
      return Relative2(appendUrlStrings(value, toAppend.asUrlPatternParameter()), parameterName, toAppend)
    }

    override operator fun plus(toAppend: String): Relative1 {
      return Relative1(appendUrlStrings(value, toAppend), parameterName)
    }

    override fun plus(toAppend: UrlPattern): Relative {
      return RelativeMulti(appendUrlStrings(value, toAppend.value), parameterNames + toAppend.parameterNames)
    }

    operator fun plus(toAppend: Relative0): Relative1 {
      return Relative1(appendUrlStrings(value, toAppend.value), this.parameterName)
    }

    operator fun plus(toAppend: Relative1): Relative2 {
      return Relative2(appendUrlStrings(value, toAppend.value), this.parameterName, toAppend.parameterName)
    }

    operator fun plus(toAppend: Relative2): Relative3 {
      return Relative3(appendUrlStrings(value, toAppend.value), this.parameterName, toAppend.parameterName1, toAppend.parameterName2)
    }

    fun resolve(parameterValue: Uuid): Url.Relative {
      return resolve(parameterValue.toString())
    }

    fun resolve(parameterValue: String): Url.Relative {
      val replaced = value.replace("{${parameterName.value}}", parameterValue)
      return Url.relative(replaced)
    }

    override fun resolve(parameterValues: List<String>): Url.Relative {
      require(parameterValues.size == 1) {
        "The number of values (${parameterValues.size}) must match the number of variables (1)"
      }

      return resolve(parameterValues[0])
    }

    override fun resolve(parameterValues: Map<UrlParameterName, String>): Url.Relative {
      return resolve(parameterValues[parameterName] ?: throw IllegalArgumentException("Missing parameter [${parameterName.value}]"))
    }

    override fun toString(): String {
      return value
    }
  }


  /**
   * A relative pattern URL with exactly two parameters
   */
  data class Relative2(
    override val value: String,
    val parameterName1: UrlParameterName,
    val parameterName2: UrlParameterName,
  ) : Relative {

    init {
      require(value.contains("{${parameterName1.value}}")) {
        "The URL [$value] must contain the variable [${parameterName1.value}]"
      }
      require(value.contains("{${parameterName2.value}}")) {
        "The URL [$value] must contain the variable [${parameterName2.value}]"
      }
    }

    override val parameterNames: List<UrlParameterName>
      get() = listOf(parameterName1, parameterName2)

    override operator fun plus(toAppend: String): Relative2 {
      return Relative2(appendUrlStrings(value, toAppend), parameterName1, parameterName2)
    }

    override operator fun plus(toAppend: UrlParameterName): Relative3 {
      return Relative3(appendUrlStrings(value, toAppend.asUrlPatternParameter()), parameterName1, parameterName2, toAppend)
    }

    override operator fun plus(toAppend: UrlPattern): RelativeMulti {
      return RelativeMulti(appendUrlStrings(value, toAppend.value), parameterNames + toAppend.parameterNames)
    }

    operator fun plus(toAppend: Relative0): Relative2 {
      return Relative2(appendUrlStrings(value, toAppend.value), parameterName1, parameterName2)
    }

    operator fun plus(toAppend: Relative1): Relative3 {
      return Relative3(appendUrlStrings(value, toAppend.value), parameterName1, parameterName2, toAppend.parameterName)
    }

    fun resolve(parameterValue1: Uuid, parameterValue2: Uuid): Url.Relative {
      return resolve(parameterValue1.toString(), parameterValue2.toString())
    }

    fun resolve(parameterValue1: String, parameterValue2: String): Url.Relative {
      val replaced = value
        .replace("{${parameterName1.value}}", parameterValue1)
        .replace("{${parameterName2.value}}", parameterValue2)
      return Url.relative(replaced)
    }

    override fun resolve(parameterValues: List<String>): Url.Relative {
      require(parameterValues.size == 2) {
        "The number of values (${parameterValues.size}) must match the number of variables (2)"
      }

      return resolve(parameterValues[0], parameterValues[1])
    }

    override fun resolve(parameterValues: Map<UrlParameterName, String>): Url.Relative {
      return resolve(
        parameterValues[parameterName1] ?: throw IllegalArgumentException("Missing parameter [${parameterName1.value}]"),
        parameterValues[parameterName2] ?: throw IllegalArgumentException("Missing parameter [${parameterName2.value}]")
      )
    }

    override fun toString(): String {
      return value
    }
  }

  /**
   * Represents a relative URL pattern with exactly three parameters
   */
  data class Relative3(
    override val value: String,
    val parameterName1: UrlParameterName,
    val parameterName2: UrlParameterName,
    val parameterName3: UrlParameterName,
  ) : Relative {

    init {
      require(value.contains("{${parameterName1.value}}")) {
        "The URL [$value] must contain the variable [${parameterName1.value}]"
      }
      require(value.contains("{${parameterName2.value}}")) {
        "The URL [$value] must contain the variable [${parameterName2.value}]"
      }
      require(value.contains("{${parameterName3.value}}")) {
        "The URL [$value] must contain the variable [${parameterName3.value}]"
      }
    }

    override val parameterNames: List<UrlParameterName>
      get() = listOf(parameterName1, parameterName2, parameterName3)

    override operator fun plus(toAppend: String): Relative3 {
      return Relative3(appendUrlStrings(value, toAppend), parameterName1, parameterName2, parameterName3)
    }

    override operator fun plus(toAppend: UrlParameterName): RelativeMulti {
      return RelativeMulti(appendUrlStrings(value, toAppend.asUrlPatternParameter()), listOf(parameterName1, parameterName2, parameterName3))
    }

    override fun plus(toAppend: UrlPattern): RelativeMulti {
      return RelativeMulti(appendUrlStrings(value, toAppend.value), parameterNames + toAppend.parameterNames)
    }

    operator fun plus(toAppend: Relative0): Relative3 {
      return Relative3(appendUrlStrings(value, toAppend.value), parameterName1, parameterName2, parameterName3)
    }

    fun resolve(parameterValue1: Uuid, parameterValue2: Uuid, parameterValue3: Uuid): Url.Relative {
      return resolve(parameterValue1.toString(), parameterValue2.toString(), parameterValue3.toString())
    }

    fun resolve(parameterValue1: String, parameterValue2: String, parameterValue3: String): Url.Relative {
      val replaced = value
        .replace("{${parameterName1.value}}", parameterValue1)
        .replace("{${parameterName2.value}}", parameterValue2)
        .replace("{${parameterName3.value}}", parameterValue3)
      return Url.relative(replaced)
    }

    override fun resolve(parameterValues: List<String>): Url.Relative {
      require(parameterValues.size == 3) {
        "The number of values (${parameterValues.size}) must match the number of variables (3)"
      }

      return resolve(parameterValues[0], parameterValues[1], parameterValues[2])
    }

    override fun resolve(parameterValues: Map<UrlParameterName, String>): Url.Relative {
      return resolve(
        parameterValues[parameterName1] ?: throw IllegalArgumentException("Missing parameter [${parameterName1.value}]"),
        parameterValues[parameterName2] ?: throw IllegalArgumentException("Missing parameter [${parameterName2.value}]"),
        parameterValues[parameterName3] ?: throw IllegalArgumentException("Missing parameter [${parameterName3.value}]")
      )
    }

    override fun toString(): String {
      return value
    }
  }

  /**
   * Builder for URL patterns
   */
  class Builder {
    /**
     * The string value of the URL.
     * Must contain the parameter names in "{}" notation (e.g., "foobar/{uuid}").
     */
    private var value: String = ""

    /**
     * Returns the parameter names that are part of the [value].
     * Might be empty.
     */
    private val parameterNames = mutableListOf<UrlParameterName>()

    fun append(segment: String): Builder {
      return (build() + segment).toBuilder()
    }

    fun append(parameterName: UrlParameterName): Builder {
      return (build().plus(parameterName)).toBuilder()
    }

    fun build(): Relative {
      return when (parameterNames.size) {
        0 -> Relative0(value)
        1 -> Relative1(value, parameterNames[0])
        2 -> Relative2(value, parameterNames[0], parameterNames[1])
        3 -> Relative3(value, parameterNames[0], parameterNames[1], parameterNames[2])
        else -> relative(value = value, parameterNames = parameterNames)
      }
    }

    companion object {
      fun of(relative: Relative): Builder {
        return Builder().apply {
          value = relative.value
          parameterNames.addAll(relative.parameterNames)
        }
      }
    }
  }
}
