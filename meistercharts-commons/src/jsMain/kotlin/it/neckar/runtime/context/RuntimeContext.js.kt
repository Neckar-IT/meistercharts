package it.neckar.runtime.context

actual fun getInitialValue(): RuntimeContext<ServiceHost.Localhost> {
  return RuntimeContext(
    executionEnvironment = ExecutionEnvironment.LocalDev,
    stage = DeploymentStage.Development,
    host = ServiceHost.Localhost,
    inUnitTest = false, //currently unknown in JS
    debugMode = false, //currently unknown in JS
  )
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
