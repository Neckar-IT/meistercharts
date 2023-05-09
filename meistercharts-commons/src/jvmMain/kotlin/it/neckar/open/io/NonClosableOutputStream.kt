package it.neckar.open.io

import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Represents an output stream that can *not* be closed.
 *
 * Calls to [close] are ignored
 */
class NonClosableOutputStream(out: OutputStream) : FilterOutputStream(out) {
  @Throws(IOException::class)
  override fun close() {
    //Do nothing!
  }
}
