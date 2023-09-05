package it.neckar.open.resources

import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL

/**
 * Extension method to load a resource that throws a nice error message if the resources could not be found
 */
fun Class<*>.getResourceSafe(url: String): URL {
  return this.getResource(url) ?: throw FileNotFoundException("Could not find resource for <$url>")
}

/**
 * Extension method to load a resource that throws a nice error message if the resources could not be found
 */
fun Class<*>.getResourceAsStreamSafe(url: String): InputStream {
  return this.getResourceAsStream(url) ?: throw FileNotFoundException("Could not find resource for <$url>")
}
