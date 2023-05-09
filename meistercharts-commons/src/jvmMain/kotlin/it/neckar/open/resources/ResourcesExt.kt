package it.neckar.open.resources

import java.io.InputStream
import java.net.URL

/**
 * Extension method to load a resource that throws a nice error message if the resources could not be found
 */
fun Class<*>.getResourceSafe(url: String): URL {
  return this.getResource(url) ?: throw IllegalStateException("Could not find resource for <$url>")
}

/**
 * Extension method to load a resource that throws a nice error message if the resources could not be found
 */
fun Class<*>.getResourceAsStreamSafe(url: String): InputStream {
  return this.getResourceAsStream(url) ?: throw IllegalStateException("Could not find resource for <$url>")
}
