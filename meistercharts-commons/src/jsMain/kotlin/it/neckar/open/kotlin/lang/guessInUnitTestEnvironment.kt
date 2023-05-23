package it.neckar.open.kotlin.lang

/**
 * Returns true if this test is running (probably) in a unit test
 */
actual fun guessInUnitTestEnvironment(): Boolean {
  //No way to know for sure!
  return false
}

/**
 * Returns true if this test is running (probably) in a Continuous Integration environment (e.g. Gitlab CI)
 */
actual fun guessInCIEnvironment(): Boolean {
  //no way to know for sure!
  return false
}

actual fun guessEnvironmentMode(): EnvironmentMode {
  //Maybe also check external var __DEV__: Boolean = definedExternally
  //if (__DEV__ == true) {
  //  return EnvironmentMode.Dev
  //}

  if (nodeEnvIsProduction) {
    return EnvironmentMode.Production
  }

  return EnvironmentMode.Dev
}

/**
 * Copied from
 * https://github.com/Kotlin/js-externals/blob/master/externals/react-native/v0/src/index.global.kt
 *
 * Might be set/used if React is used
 */
//@Deprecated("Does not work outside of React")
//external var __DEV__: dynamic = definedExternally


internal val NODE_ENV: dynamic = js("process.env.NODE_ENV")


val nodeEnvIsProduction: Boolean
  get() = NODE_ENV === "production"
