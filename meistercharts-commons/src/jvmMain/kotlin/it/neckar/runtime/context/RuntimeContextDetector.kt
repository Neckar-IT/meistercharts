/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.runtime.context

import it.neckar.open.net.HostnameSupport
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
    forcedHostName: Hostname? = null,
    forcedHost: HostType? = null,
  ): RuntimeContext<HostType> {

    //Resolve the host
    val hostname = forcedHostName ?: resolveHostname(source) ?: guessHostName()
    val serviceHost = forcedHost ?: serviceHostRegistry.findByHostname(hostname)

    //Get the execution environment profile
    val executionEnvironment: ExecutionEnvironment = forcedExecutionEnvironment ?: if (guessInCIEnvironment()) {
      ExecutionEnvironment.CI
    } else {
      resolveProfile(source) ?: serviceHost.executionEnvironment
    }

    //Get the deployment stage
    val deploymentStage = forcedState ?: resolveStage(source) ?: serviceHost.deploymentStage

    val debugging = guessDebugging()
    val inUnitTest = guessInUnitTestEnvironment()

    return RuntimeContext<HostType>(
      executionEnvironment = executionEnvironment,
      stage = deploymentStage,
      host = serviceHost,

      debugMode = debugging,
      inUnitTest = inUnitTest,
      initialValue = false,
    )
  }

  /**
   * Returns the hostname of the current device
   */
  private fun guessHostName(): Hostname? {
    return HostnameSupport.guess()
  }

  /**
   * Resolves the execution environment profile from system properties or environment variables.
   */
  private fun resolveProfile(source: Source): ExecutionEnvironment? {
    val raw = resolveStringFromSystemOrEnv(
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
    val raw = resolveStringFromSystemOrEnv(source, RuntimeContextSys.KEY_DEPLOYMENT_STAGE_SYS, RuntimeContextEnv.KEY_DEPLOYMENT_STAGE_ENV)
    return raw?.let { parseEnumRelaxed<DeploymentStage>(it) }
  }

  /**
   * Resolves the hostname from system properties or environments
   */
  private fun resolveHostname(source: Source): Hostname? {
    return Hostname.nullable(resolveStringFromSystemOrEnv(source, RuntimeContextSys.KEY_SERVICE_HOST_SYS, RuntimeContextEnv.KEY_SERVICE_HOST_ENV))
  }

  /**
   * Helper function to resolve a string value from system properties or environment variables.
   */
  private fun resolveStringFromSystemOrEnv(source: Source, systemPropertiesKey: String, envKey: String): String? {
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
  forcedHostName: Hostname? = null,
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
  System.getProperty(DebugWaitParameterName)?.let { debugWaitValueFromSystem ->
    return debugWaitValueFromSystem.toBoolean()
  }

  System.getenv(DebugWaitParameterName)?.let { debugWaitValue ->
    return debugWaitValue.toBoolean()
  }

  return false;
}

/**
 * Enables waiting in debug mode by setting the appropriate system property.
 *
 * This function sets the system property defined by [DebugWaitParameterName] to "true",
 * which can be used to control behavior in debug mode, such as pausing execution for debugging.
 *
 * ATTENTION: Do *not* check in this call - it is only for local debugging purposes!
 */
fun enableWaitInDebugMode() {
  System.setProperty(DebugWaitParameterName, "true")
}

/**
 * Constant for the DEBUG_WAIT environment variable
 *
 * See [DebugWaitHelpEnableMessage] for more information on how to enable/disable this feature
 */
const val DebugWaitParameterName: String = "debugWait"
const val DebugWaitHelpEnableMessage: String = "Add Environment variable or system property '${DebugWaitParameterName}=true' to enable waiting in debug mode (in IntelliJ Run Configuration)"

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
    initialValue = true,
  )
}
