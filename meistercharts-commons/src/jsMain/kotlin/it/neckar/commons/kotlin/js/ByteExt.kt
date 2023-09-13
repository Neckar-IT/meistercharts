package it.neckar.commons.kotlin.js

import js.buffer.ArrayBuffer
import js.typedarrays.Int8Array

/**
 * Convert js ArrayBuffer to Bytearray
 * https://slack-chats.kotlinlang.org/t/467728/is-there-a-way-to-convert-arraybuffer-to-a-bytearray
 */
fun ArrayBuffer.toByteArray(): ByteArray {
  return Int8Array(this).unsafeCast<ByteArray>()
}
