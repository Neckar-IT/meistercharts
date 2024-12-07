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
 * Constant for the DEBUG_WAIT environment variable
 *
 * See [DebugWaitHelpDisableMessage] for more information on how to enable/disable this feature
 */
const val DebugWaitHelpDisableParameter: String = "debugWait"

const val DebugWaitHelpDisableMessage: String = "Add Environment variable '$DebugWaitHelpDisableParameter=false' to disable waiting in debug mode (in IntelliJ Run Configuration)"

actual fun shouldWaitInDebugMode(): Boolean {
  val debugWaitValue = System.getenv(DebugWaitHelpDisableParameter)
  return debugWaitValue?.toBoolean() != false
}

/**
 * Returns true if this test is running (probably) in a Continuous Integration environment (e.g., Gitlab CI)
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
