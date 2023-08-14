package it.neckar.commons.kotlin.js

import it.neckar.open.http.Url


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
