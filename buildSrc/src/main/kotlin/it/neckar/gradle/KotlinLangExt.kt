package it.neckar.gradle

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
  return Files.isSymbolicLink(this.toPath())
}

/**
 * Returns true if this file is a symlink to the provided target
 */
fun File.isSymLinkTo(targetFile: File): Boolean {
  if (isSymbolicLink().not()) {
    return false
  }

  val symlinkTarget = Files.readSymbolicLink(toPath())

  val resolvedSymlinkTarget = toPath().parent.resolve(symlinkTarget).normalize().toAbsolutePath()
  val expectedTargetPath = targetFile.toPath().toAbsolutePath().normalize()

  return resolvedSymlinkTarget == expectedTargetPath
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

fun <T> T?.requireNotNull(lazyMessage: () -> Any): T {
  requireNotNull(this, lazyMessage)
  return this
}

fun <T> T?.requireNotNull(): T {
  requireNotNull(this)
  return this
}
