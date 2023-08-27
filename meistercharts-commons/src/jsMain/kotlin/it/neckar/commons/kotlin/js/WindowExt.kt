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

  return Url("$protocol//$hostWithPort$directory")
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
  return URLSearchParams(location.search)
}

fun URLSearchParams.getBoolean(enumOption: Enum<*>): Boolean? {
  return getBoolean(enumOption.name)
}

fun URLSearchParams.getBoolean(parameterName: String): Boolean? {
  get(parameterName)?.let { parameterValue ->
    console.debug("Boolean parameter: [$parameterName] from url: $parameterValue")
    try {
      return parameterValue.toBoolean()
    } catch (e: Exception) {
      console.warn("Could not parse $parameterValue as Boolean due to ${e.message}")
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
    console.debug("$parameterName from url: $parameterValue")
    try {
      return parameterValue.toDouble()
    } catch (e: Exception) {
      console.warn("Could not parse $parameterValue as Boolean due to ${e.message}")
    }
  }
  return null
}
