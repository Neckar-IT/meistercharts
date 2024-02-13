import java.io.File
import java.nio.file.Files

/**
 * Executes the block if this is null
 */
fun <T> T?.ifNull(block: () -> Unit): T? {
  if (this == null) {
    block()
  }

  return this
}


/**
 * Checks whether a given file is a symbolic link.
 */
fun File.isSymbolicLink(): Boolean {
  if (this.isFile.not()) {
    return false
  }

  return absoluteFile != canonicalFile
}

/**
 * Returns true if this file is a symlink to the provided target
 */
fun File.isSymLinkTo(targetFile: File): Boolean {
  if (this.isSymbolicLink().not()) {
    return false
  }

  return targetFile.absolutePath == canonicalPath
}

/**
 * Creates a symlink to the target
 */
fun File.symlinkTo(target: File) {
  require(target.exists()) {
    "The target file must exist"
  }

  if (this.exists()) {
    this.delete()
  }

  val targetPath = target.toPath()
  val linkPath = this.toPath()

  Files.createSymbolicLink(linkPath, targetPath)
}
