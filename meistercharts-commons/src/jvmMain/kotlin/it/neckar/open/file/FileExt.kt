package it.neckar.open.file

import java.io.File

/**
 * File extensions
 */


/**
 * Returns a new file that replaces a leading "~/" with the user home.
 *
 * Returns this, if the path does *not* start with "~/"
 */
fun File.replaceLeadingTilde(): File {
  if (path.startsWith("~/")) {
    return File(path.replaceFirst("~/", "${System.getProperty("user.home")}/"))
  }

  return this
}
