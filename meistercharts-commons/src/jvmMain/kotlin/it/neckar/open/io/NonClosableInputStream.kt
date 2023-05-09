package it.neckar.open.io

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Represents an input stream that can *not* be closed.
 *
 * Calls to [close] are ignored
 */
class NonClosableInputStream(inputStream: InputStream) : FilterInputStream(inputStream) {
  @Throws(IOException::class)
  override fun close() {
    //Do nothing!
  }
}
