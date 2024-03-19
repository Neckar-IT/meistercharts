package it.neckar.open.crypt

import java.io.File
import java.security.MessageDigest
import java.util.Base64

/**
 * Contains extension functions for Crypto related classes
 *
 * Copied from https://github.com/LukasForst/katlib/blob/master/ (MIT License)
 */


/**
 * Creates SHA256 hash of the given text.
 */
fun hashSha256(textToHash: String): String = hashSha256(textToHash.toByteArray(Charsets.UTF_8))

/**
 * Creates SHA256 hash of the given file.
 */
fun hashSha256(fileToHash: File): String = hashSha256(fileToHash.readBytes())

/**
 * Creates SHA256 hash of the given byte array.
 */
fun hashSha256(bytes: ByteArray): String {
  val hashedArray = MessageDigest
    .getInstance("SHA-256")
    .digest(bytes)
  return Base64.getEncoder().encodeToString(hashedArray)
}
