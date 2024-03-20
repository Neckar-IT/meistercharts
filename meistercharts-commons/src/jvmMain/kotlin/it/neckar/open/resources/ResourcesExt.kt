package it.neckar.open.resources

import it.neckar.open.http.Url
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import kotlin.reflect.KClass

/**
 * Extension method to load a resource that throws a nice error message if the resources could not be found
 */
inline fun Class<*>.getResourceSafe(url: String): URL {
  return getResourceSafe(Url(url))
}

fun Class<*>.getResourceSafe(url: Url): URL {
  return this.getResource(url.toString()) ?: throw FileNotFoundException("Could not find resource for <$url>")
}

inline fun KClass<*>.getResourceSafe(url: String): URL {
  return getResourceSafe(Url(url))
}

inline fun KClass<*>.getResourceSafe(url: Url): URL {
  return this.java.getResourceSafe(url)
}

/**
 * Extension method to load a resource that throws a nice error message if the resources could not be found
 */
fun Class<*>.getResourceAsStreamSafe(url: String): InputStream {
  return this.getResourceAsStream(url) ?: throw FileNotFoundException("Could not find resource for <$url>")
}

fun Class<*>.getResourceAsStreamSafe(url: Url): InputStream {
  return this.getResourceAsStreamSafe(url.value)
}

fun KClass<*>.getResourceAsStreamSafe(path: String): InputStream {
  return this.java.getResourceAsStreamSafe(path)
}

fun KClass<*>.getResourceAsStreamSafe(path: Url): InputStream {
  return this.getResourceAsStreamSafe(path.value)
}

