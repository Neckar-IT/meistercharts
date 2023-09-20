package it.neckar.open.kotlin.lang


/**
 * Converts a byte array to base64
 *
 * Use [toBase64Url] when encoding for URLs
 */
fun ByteArray.toBase64(useUrlEncoding: Boolean = false): String {
  return if (useUrlEncoding) {
    kotlin.io.encoding.Base64.UrlSafe.encode(this)
  } else {
    kotlin.io.encoding.Base64.encode(this)
  }
}

fun String.toBase64(useUrlEncoding: Boolean = false): String {
  return this.encodeToByteArray().toBase64(useUrlEncoding)
}

fun String.fromBase64(useUrlEncoding: Boolean = false): ByteArray {
  return if (useUrlEncoding) {
    kotlin.io.encoding.Base64.UrlSafe.decode(this)
  } else {
    kotlin.io.encoding.Base64.decode(this)
  }
}

fun String.fromBase64String(): String {
  return fromBase64().decodeToString()
}

/**
 * Converts a base64 encoded string to byte array - uses only characters that are allowed in an URL
 */
fun ByteArray.toBase64Url(): String {
  return kotlin.io.encoding.Base64.UrlSafe.encode(this)
}

/**
 * Converts a base64 encoded string to byte array - uses only characters that are allowed in an URL
 */
fun ByteArray.toBase64UrlString(): String {
  return this.toBase64Url()
}

fun String.fromBase64Url(): ByteArray {
  return kotlin.io.encoding.Base64.UrlSafe.decode(this)
}

fun String.fromBase64UrlString(): String {
  return fromBase64Url().decodeToString()
}
