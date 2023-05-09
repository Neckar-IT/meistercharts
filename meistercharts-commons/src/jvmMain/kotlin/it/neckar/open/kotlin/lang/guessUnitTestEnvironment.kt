package it.neckar.open.kotlin.lang

actual fun guessInUnitTestEnvironment(): Boolean {
  for (element in Thread.currentThread().stackTrace) {
    if (element.className.startsWith("org.junit.")) {
      return true
    }
  }
  return false
}

/**
 * Returns true if this test is running (probably) in a Continuous Integration environment (e.g. Gitlab CI)
 */
actual fun guessInCIEnvironment(): Boolean {
  return System.getenv("GITLAB_CI") != null
}

/**
 * Guesses the environment mode
 */
actual fun guessEnvironmentMode(): EnvironmentMode {
  return EnvironmentMode.Dev
}
