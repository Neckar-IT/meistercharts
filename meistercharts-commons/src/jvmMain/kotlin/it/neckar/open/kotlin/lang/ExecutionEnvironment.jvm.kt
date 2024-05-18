package it.neckar.open.kotlin.lang

import java.lang.management.ManagementFactory


/**
 * Returns true if the process is (probably) currently debugging
 */
actual fun guessDebugging(): Boolean {
  val runtime = ManagementFactory.getRuntimeMXBean()

  // When debugging, one of the arguments should look like this: -agentlib:jdwp=transport=dt_socket,server=n,suspend=y,address=127.0.0.1:43009
  return runtime.inputArguments.any {
    it.contains("jdwp")
  }
}

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
