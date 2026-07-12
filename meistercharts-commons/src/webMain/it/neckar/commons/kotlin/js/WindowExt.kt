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
package it.neckar.commons.kotlin.js

import it.neckar.open.http.Url
import kotlinx.browser.window
import org.w3c.dom.Window
import org.w3c.dom.url.URLSearchParams


/**
 * Calculates the base url for the current location.
 * Returns the "directory" of the current location
 */
fun org.w3c.dom.Window.baseUrl(): Url {
  val protocol = location.protocol
  val hostWithPort = location.host //also contains the port
  val pathname = location.pathname
  val directory = pathname.substringBeforeLast("/")

  return Url.absolute("$protocol//$hostWithPort$directory")
}

/**
 * Returns the URL parameter value
 */
fun getUrlParameter(paramName: String): String? {
  return window.urlSearchParams().get(paramName)
}

/**
 * Returns the [URLSearchParams] object
 */
fun Window.urlSearchParams(): URLSearchParams {
  return URLSearchParams(location.search.toJsString())
}

fun URLSearchParams.getBoolean(enumOption: Enum<*>): Boolean? {
  return getBoolean(enumOption.name)
}

fun URLSearchParams.getBoolean(parameterName: String): Boolean? {
  get(parameterName)?.let { parameterValue ->
    consoleDebug("Boolean parameter: [$parameterName] from url: $parameterValue")
    try {
      return parameterValue.toBoolean()
    } catch (e: Exception) {
      consoleWarn("Could not parse $parameterValue as Boolean due to ${e.message}")
    }
  }

  return null
}

/**
 * Extracts the number parameter from the URLSearchParams
 */
fun URLSearchParams.getNumber(enumOption: Enum<*>): Double? {
  return getNumber(enumOption.name)
}

fun URLSearchParams.getNumber(parameterName: String): Double? {
  get(parameterName)?.let { parameterValue ->
    consoleDebug("$parameterName from url: $parameterValue")
    try {
      return parameterValue.toDouble()
    } catch (e: Exception) {
      consoleWarn("Could not parse $parameterValue as Double due to ${e.message}")
    }
  }
  return null
}

private fun consoleDebug(message: String): Unit = js("console.debug(message)")

private fun consoleWarn(message: String): Unit = js("console.warn(message)")
