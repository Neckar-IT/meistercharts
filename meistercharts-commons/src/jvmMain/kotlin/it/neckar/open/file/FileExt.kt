package it.neckar.open.file

import it.neckar.open.http.Url
import it.neckar.open.http.toUrl
import it.neckar.open.lang.Os
import java.io.File
import java.net.URI
import java.net.URLEncoder

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

/**
 * Creates a tree representation of the file system
 */
fun File.tree(showOwnName: Boolean = false): String = buildString {
  return treeRecursively(showDotInsteadOfCurrentName = showOwnName.not())
}

private fun File.treeRecursively(prefix: String = "", continuation: String = "", showDotInsteadOfCurrentName: Boolean = true): String = buildString {
  if (showDotInsteadOfCurrentName) {
    append(".\n")
  } else {
    append("$prefix${this@treeRecursively.name}\n")
  }

  if (this@treeRecursively.isDirectory) {
    val files = this@treeRecursively.listFiles()
    files?.let {
      it.sort()
      it.indices.forEach { i ->
        val isLast = i == it.size - 1
        val newPrefix = continuation + if (isLast) "└── " else "├── "
        val newContinuation = continuation + if (isLast) "    " else "│   "
        append(it[i].treeRecursively(newPrefix, newContinuation, false))
      }
    }
  }
}

/**
 * Throws an exception if this file is not a file (e.g. does not exist or is a directory)
 *
 * Returns this
 */
fun File.requireIsFile(): File {
  require(this.isFile){"File <${this.absolutePath}> is not a File"}
  return this
}

fun File.requireIsDirectory(): File {
  require(this.isDirectory){"File <${this.absolutePath}> is not a Directory"}
  return this
}
