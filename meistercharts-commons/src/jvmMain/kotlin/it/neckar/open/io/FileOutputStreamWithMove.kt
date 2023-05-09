package it.neckar.open.io

import java.io.File
import java.io.FilterOutputStream
import java.nio.file.Files

/**
 * Writes to a temporary file and moves to the target file name on [close]
 */
class FileOutputStreamWithMove constructor(val file: File) : FilterOutputStream(null) {
  /**
   * Whether the stream has been closed already
   */
  private var closed: Boolean = false

  /**
   * The tmp file that is written first
   */
  val tmpFile: File = File(file.parent, file.name + SUFFIX_TMP + "_" + System.nanoTime()).also {
    it.deleteOnExit()

    this.out = it.outputStream().buffered()
  }

  override fun close() {
    super.close()

    if (closed) {
      return
    }

    //Only move the file if it exists
    if (tmpFile.exists()) {
      //delete the original file first - overwrite mode
      if (file.exists()) {
        file.delete()
      }

      Files.move(tmpFile.toPath(), file.toPath())
    }
    closed = true
  }

  companion object {
    /**
     * The suffix for the tmp file
     */
    const val SUFFIX_TMP: String = ".tmp"
  }
}

/**
 * Creates a new file input stream that first writes to a tmp file and moves the file on close
 */
fun File.outputStreamWithMove(): FileOutputStreamWithMove {
  return FileOutputStreamWithMove(this)
}
