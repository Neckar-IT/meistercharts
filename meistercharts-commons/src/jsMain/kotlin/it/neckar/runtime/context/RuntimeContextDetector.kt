package it.neckar.runtime.context

import it.neckar.runtime.detection.RuntimeContextEnv

/**
 * Detects RuntimeContext from a browser environment (window.location, global variables).
 *
 * Priority: forced parameters > global JS variables > window.location.hostname > ServiceHost defaults > built-in defaults
 *
 * Global variables can be set in the browser via:
 * ```javascript
 * window.EXECUTION_ENVIRONMENT = "Docker";
 * window.DEPLOYMENT_STAGE = "Production";
 * window.SERVICE_HOST = "my-app.example.com";
 * ```
 */
object RuntimeContextDetector {

  /**
   * Detects the [RuntimeContext] from browser environment
   */
  fun <HostType : ServiceHost> detectFromEnvironment(
    serviceHostRegistry: ServiceHostRegistry<HostType>,
    forcedExecutionEnvironment: ExecutionEnvironment? = null,
    forcedDeploymentStage: DeploymentStage? = null,
    forcedHostname: Hostname? = null,
  ): RuntimeContext<HostType> {

    // Resolve the host
    val hostname = forcedHostname
      ?: getGlobalVariable(RuntimeContextEnv.KEY_SERVICE_HOST_ENV)?.let { Hostname(it) }
      ?: getBrowserHostname()

    val serviceHost = serviceHostRegistry.findByHostname(hostname)

    // Get the execution environment
    val executionEnvironment: ExecutionEnvironment = forcedExecutionEnvironment
      ?: getGlobalVariable(RuntimeContextEnv.KEY_RUNTIME_EXECUTION_ENVIRONMENT_ENV)?.let { parseExecutionEnvironment(it) }
      ?: serviceHost.executionEnvironment

    // Get the deployment stage
    val deploymentStage = forcedDeploymentStage
      ?: getGlobalVariable(RuntimeContextEnv.KEY_DEPLOYMENT_STAGE_ENV)?.let { parseDeploymentStage(it) }
      ?: serviceHost.deploymentStage

    return RuntimeContext(
      executionEnvironment = executionEnvironment,
      stage = deploymentStage,
      host = serviceHost,
      debugMode = false, // Not detectable in browser
      inUnitTest = false, // Browser apps don't run in unit tests
      initialValue = false,
    )
  }

  /**
   * Gets a global JavaScript variable by name, or null if not defined.
   */
  fun getGlobalVariable(name: String): String? {
    val value: dynamic = js("typeof window !== 'undefined' && window[name]")
    return if (value == null || value == undefined || value == false) {
      null
    } else {
      value.toString()
    }
  }

  /**
   * Gets the hostname from window.location, or null if not available.
   */
  fun getBrowserHostname(): Hostname? {
    val hostname: dynamic = js("typeof window !== 'undefined' && window.location && window.location.hostname")
    return if (hostname == null || hostname == undefined || hostname == false || hostname == "") {
      null
    } else {
      Hostname(hostname.toString())
    }
  }

  /**
   * Parses a string to [ExecutionEnvironment], case-insensitive.
   */
  private fun parseExecutionEnvironment(raw: String): ExecutionEnvironment? {
    val normalized = raw.trim().lowercase()
    return ExecutionEnvironment.entries.firstOrNull { it.name.lowercase() == normalized }
  }

  /**
   * Parses a string to [DeploymentStage], case-insensitive.
   */
  private fun parseDeploymentStage(raw: String): DeploymentStage? {
    val normalized = raw.trim().lowercase()
    return DeploymentStage.entries.firstOrNull { it.name.lowercase() == normalized }
  }
}

/**
 * Initializes the [RuntimeContext] from the browser environment.
 *
 * Reads configuration from:
 * 1. Forced parameters (highest priority)
 * 2. Global JS variables (window.EXECUTION_ENVIRONMENT, window.DEPLOYMENT_STAGE, window.SERVICE_HOST)
 * 3. Browser hostname (window.location.hostname)
 * 4. ServiceHost defaults
 */
fun <HostType : ServiceHost> RuntimeContext.Companion.initializeFromEnvironment(
  serviceHostRegistry: ServiceHostRegistry<HostType>,
  forcedExecutionEnvironment: ExecutionEnvironment? = null,
  forcedDeploymentStage: DeploymentStage? = null,
  forcedHostname: Hostname? = null,
): RuntimeContext<HostType> {
  return RuntimeContextDetector.detectFromEnvironment(
    serviceHostRegistry = serviceHostRegistry,
    forcedExecutionEnvironment = forcedExecutionEnvironment,
    forcedDeploymentStage = forcedDeploymentStage,
    forcedHostname = forcedHostname,
  ).also {
    this.current = it
  }
}

/**
 * Initializes the [RuntimeContext] for localhost.
 */
fun RuntimeContext.Companion.initializeForLocalhost(
  forcedExecutionEnvironment: ExecutionEnvironment? = null,
  forcedDeploymentStage: DeploymentStage? = null,
): RuntimeContext<ServiceHost.Localhost> {
  return RuntimeContextDetector.detectFromEnvironment(
    serviceHostRegistry = ServiceHost.Localhost,
    forcedExecutionEnvironment = forcedExecutionEnvironment,
    forcedDeploymentStage = forcedDeploymentStage,
    forcedHostname = null,
  ).also {
    this.current = it
  }
}
