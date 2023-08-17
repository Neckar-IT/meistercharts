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
 * Returns a "file://" URL
 */
fun File.formatAbsolutePath(): String {
  return toURI().toURL().toString().replace("file:/", "file:///")
}

fun File.requireIsFile(): File {
  require(this.isFile){"File <${this.absolutePath}> is not a File"}
  return this
}
