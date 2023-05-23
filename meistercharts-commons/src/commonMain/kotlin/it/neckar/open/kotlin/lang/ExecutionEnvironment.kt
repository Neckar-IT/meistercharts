package it.neckar.open.kotlin.lang

/**
 * Contains information about the environment
 */
object ExecutionEnvironment {
  /**
   * Returns true if the current execution environment is (probably) a unit test
   */
  val inUnitTest: Boolean by lazy { guessInUnitTestEnvironment() }

  /**
   * Returns true if the current execution environment is (probably) a CI environment
   */
  val inCI: Boolean by lazy { guessInCIEnvironment() }

  /**
   * The current environment mode.
   */
  val environmentMode: EnvironmentMode by lazy { guessEnvironmentMode() }

  fun isDev(): Boolean {
    return environmentMode.isDev()
  }

  fun isProduction(): Boolean {
    return environmentMode.isProduction()
  }
}

/**
 * Returns true if this test is running (probably) in a unit test
 */
expect fun guessInUnitTestEnvironment(): Boolean

/**
 * Returns true if this test is running (probably) in a Continuous Integration environment (e.g. Gitlab CI)
 */
expect fun guessInCIEnvironment(): Boolean

/**
 * Guesses the environment mode
 */
expect fun guessEnvironmentMode(): EnvironmentMode


/**
 * The current environment mode.
 * This could depend on the deployment (e.g. localhost vs production server)
 */
enum class EnvironmentMode {
  /**
   * Development mode
   */
  Dev,

  /**
   * Production mode
   */
  Production,

  ;

  fun isDev(): Boolean {
    return this == Dev
  }

  fun isProduction(): Boolean {
    return this == Production
  }

}
