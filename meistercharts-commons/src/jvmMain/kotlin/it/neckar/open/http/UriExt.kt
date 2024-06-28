package it.neckar.open.http

import java.io.File
import java.net.URI

/**
 * Converts a file uri to a file.
 *
 * Attention: The URI *must* be a file uri (start with file:/)
 */
fun URI.toFile(): File {
  return File(this)
}
