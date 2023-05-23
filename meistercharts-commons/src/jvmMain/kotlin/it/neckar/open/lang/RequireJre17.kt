package it.neckar.open.lang


fun requireJre8() {
  requireExactJre("1.8")
}

/**
 * Throws an exception, if the current JVM is not running with Java 17
 */
fun requireAtLeastJre17() {
  requireAtLeastJre(17)
}

fun requireAtLeastJre19() {
  requireAtLeastJre(19)
}

private fun requireExactJre(versionStringPrefix: String) {
  val jreVersion = System.getProperty("java.version")
  require(jreVersion.startsWith("$versionStringPrefix.")) {
    "Invalid JRE version: <$jreVersion>"
  }
}

private fun requireAtLeastJre(expectedMajorVersion: Int) {
  val jreVersion = System.getProperty("java.version")

  val currentMajorVersionAsString = jreVersion.substringBefore(".")
  val currentMajorVersionAsInt: Int = currentMajorVersionAsString.toInt()

  require(expectedMajorVersion <= currentMajorVersionAsInt) {
    "Invalid JRE version: <$jreVersion>. Expected at least major version $expectedMajorVersion"
  }
}
