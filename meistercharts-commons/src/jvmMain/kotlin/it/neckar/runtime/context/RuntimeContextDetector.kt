package it.neckar.runtime.context

import it.neckar.runtime.detection.RuntimeContextEnv
import it.neckar.runtime.detection.RuntimeContextSys
import java.lang.management.ManagementFactory
import kotlin.enums.enumEntries

/**
 * Detects RuntimeContext from System properties, environment variables, optional config, and defaults.
 *
 * Priority: System properties > Environment variables > Config map > Built-in defaults.
 *
 * Keys (system property or env var):
 * Defined in [RuntimeContextSys] and [RuntimeContextEnv]:
 */
object RuntimeContextDetector {
  /**
   * Provides the system / environment properties
   */
  data class Source(
    val systemProperties: Map<String, String>,
    val environment: Map<String, String>,
  ) {
    companion object {
      /**
       * Creates a default [Source] instance using the current system properties and environment variables.
       */
      fun default(): Source {
        return Source(
          systemProperties = System.getProperties().map { it.key.toString() to it.value.toString() }.toMap(),
          environment = System.getenv(),
        )
      }
    }
  }

  /**
   * Detects the [it.neckar.runtime.context.RuntimeContext]
   */
  fun <HostType: ServiceHost> detectFromEnvironment(
    serviceHostRegistry: ServiceHostRegistry<HostType>,
    source: Source = Source.default(),

    forcedExecutionEnvironment: ExecutionEnvironment? = null,
    forcedState: DeploymentStage? = null,
    forcedHostName: String? = null,
    forcedHost: HostType? = null,
  ): RuntimeContext<HostType> {

    //Get the execution environment profile
    val executionEnvironment: ExecutionEnvironment = forcedExecutionEnvironment ?: if (guessInCIEnvironment()) {
      ExecutionEnvironment.CI
    } else {
      resolveProfile(source) ?: ExecutionEnvironment.LocalDev
    }

    //Get the deployment stage
    val stage = forcedState ?: resolveStage(source) ?: DeploymentStage.Development

    //Resolve the host name
    val hostNameRaw = forcedHostName ?: resolveString(source, RuntimeContextSys.KEY_SERVICE_HOST_SYS, RuntimeContextEnv.KEY_SERVICE_HOST_ENV)
    val serviceHost = forcedHost ?: serviceHostRegistry.findByHostname(hostNameRaw)

    val debugging = guessDebugging()
    val inUnitTest = guessInUnitTestEnvironment()

    return RuntimeContext<HostType>(
      executionEnvironment = executionEnvironment,
      stage = stage,
      host = serviceHost,

      debugMode = debugging,
      inUnitTest = inUnitTest,
    )
  }

  /**
   * Resolves the execution environment profile from system properties or environment variables.
   */
  private fun resolveProfile(source: Source): ExecutionEnvironment? {
    val raw = resolveString(
      source = source,
      systemPropertiesKey = RuntimeContextSys.KEY_RUNTIME_EXECUTION_ENVIRONMENT_SYS,
      envKey = RuntimeContextEnv.KEY_RUNTIME_EXECUTION_ENVIRONMENT_ENV
    )
    return raw?.let { parseEnumRelaxed<ExecutionEnvironment>(it) }
  }

  /**
   * Resolves the deployment stage from system properties or environment variables.
   */
  private fun resolveStage(source: Source): DeploymentStage? {
    val raw = resolveString(source, RuntimeContextSys.KEY_DEPLOYMENT_STAGE_SYS, RuntimeContextEnv.KEY_DEPLOYMENT_STAGE_ENV)
    return raw?.let { parseEnumRelaxed<DeploymentStage>(it) }
  }

  /**
   * Helper function to resolve a string value from system properties or environment variables.
   */
  private fun resolveString(source: Source, systemPropertiesKey: String, envKey: String): String? {
    return source.systemProperties[systemPropertiesKey] ?: source.environment[envKey]
  }

  /**
   * Parses a string value into an enum of type [E].
   */
  private inline fun <reified E : Enum<E>> parseEnumRelaxed(raw: String): E {
    val normalized = raw.trim().lowercase()

    enumEntries<E>().forEach { entry ->
      if (entry.name.lowercase() == normalized) {
        return entry
      }
    }

    throw IllegalStateException("Invalid value [$raw] for enum ${E::class.simpleName}. Allowed: ${enumValues<E>().joinToString()}.")
  }
}

/**
 * Initializes the [it.neckar.runtime.context.RuntimeContext] from the environment.
 */
fun <HostType: ServiceHost> RuntimeContext.Companion.initializeFromEnvironment(
  serviceHostRegistry: ServiceHostRegistry<HostType>,
  source: RuntimeContextDetector.Source = RuntimeContextDetector.Source.default(),

  forcedExecutionEnvironment: ExecutionEnvironment? = null,
  forcedDeploymentStage: DeploymentStage? = null,
  forcedHostName: String? = null,
): RuntimeContext<HostType> {
  return RuntimeContextDetector.detectFromEnvironment(
    serviceHostRegistry = serviceHostRegistry,
    source = source,

    forcedExecutionEnvironment = forcedExecutionEnvironment,
    forcedState = forcedDeploymentStage,
    forcedHostName = forcedHostName,
  ).also {
    this.current = it
  }
}

/**
 * Initializes the [it.neckar.runtime.context.RuntimeContext] for localhost.
 *
 * This is useful for local development environments where you want to ensure that the context is set up correctly.
 */
fun  RuntimeContext.Companion.initializeForLocalhost(
  serviceHostRegistry: ServiceHostRegistry<ServiceHost.Localhost> = ServiceHost.Localhost,
  source: RuntimeContextDetector.Source = RuntimeContextDetector.Source.default(),

  forcedExecutionEnvironment: ExecutionEnvironment? = null,
  forcedState: DeploymentStage? = null,
  forcedHost: ServiceHost.Localhost? = null,
): RuntimeContext<ServiceHost.Localhost> {
  return RuntimeContextDetector.detectFromEnvironment(
    serviceHostRegistry = serviceHostRegistry,
    source = source,

    forcedExecutionEnvironment = forcedExecutionEnvironment,
    forcedState = forcedState,
    forcedHost = forcedHost ?: ServiceHost.Localhost,
  ).also {
    this.current = it
  }
}

/**
 * Returns true if the process is (probably) currently debugging
 */
fun guessInUnitTestEnvironment(): Boolean {
  for (element in Thread.currentThread().stackTrace) {
    if (element.className.startsWith("org.junit.")) {
      return true
    }
  }
  return false
}

/**
 * Returns true if the process is (probably) currently debugging
 */
fun guessDebugging(): Boolean {
  val runtime = ManagementFactory.getRuntimeMXBean()

  // When debugging, one of the arguments should look like this: -agentlib:jdwp=transport=dt_socket,server=n,suspend=y,address=127.0.0.1:43009
  return runtime.inputArguments.any {
    it.contains("jdwp")
  }
}

/**
 * Returns true if this test is running (probably) in a Continuous Integration environment (e.g., Gitlab CI)
 */
fun guessInCIEnvironment(): Boolean {
  return System.getenv("GITLAB_CI") != null
}

/**
 * Returns true if the application is running in debug mode and should wait for user debugging.
 * This can be used to identify strategic points in the code where you want to pause execution
 * for debugging purposes (e.g., before clearing caches, starting critical operations, etc.).
 */
fun shouldWaitInDebugMode(): Boolean {
  val debugWaitValue = System.getenv(DebugWaitHelpDisableParameter)
  return debugWaitValue?.toBoolean() != false
}


/**
 * Constant for the DEBUG_WAIT environment variable
 *
 * See [DebugWaitHelpDisableMessage] for more information on how to enable/disable this feature
 */
const val DebugWaitHelpDisableParameter: String = "debugWait"
const val DebugWaitHelpDisableMessage: String = "Add Environment variable '${DebugWaitHelpDisableParameter}=false' to disable waiting in debug mode (in IntelliJ Run Configuration)"


/**
 * Fallback runtime context for local development.
 */
actual fun getInitialValue(): RuntimeContext<ServiceHost.Localhost> {
  val guessInCIEnvironment = guessInCIEnvironment()
  val debugging = guessDebugging()
  val inUnitTest = guessInUnitTestEnvironment()

  val executionEnvironment: ExecutionEnvironment = if (guessInCIEnvironment) {
    ExecutionEnvironment.CI
  } else {
    ExecutionEnvironment.LocalDev
  }

  return RuntimeContext(
    executionEnvironment = executionEnvironment,
    stage = DeploymentStage.Development,
    host = ServiceHost.Localhost,
    inUnitTest = inUnitTest,
    debugMode = debugging,
  )
}
