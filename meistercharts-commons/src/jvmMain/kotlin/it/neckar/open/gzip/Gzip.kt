package it.neckar.open.gzip

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.text.Charsets.UTF_8

/**
 * Zips the provided string to a byte array
 */
fun gzip(content: String): ByteArray {
  return gzip(content.toByteArray(UTF_8))
}

/**
 * Compresses the provided content
 */
fun gzip(uncompressed: ByteArray): ByteArray {
  val outputStream = ByteArrayOutputStream()
  GZIPOutputStream(outputStream).use { it.write(uncompressed) }
  return outputStream.toByteArray()
}

/**
 * Unzips the provided compressed byte array
 */
fun ungzip(compressed: ByteArray): ByteArray {
  return GZIPInputStream(compressed.inputStream()).use { it.readBytes() }
}
