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

    val serviceHost = serviceHostRegistry.resolveHost(hostname)

    // Get the execution environment. The host is NOT a fallback here: stage/environment come from
    // EXECUTION_ENVIRONMENT / DEPLOYMENT_STAGE (the values the host constants declare and the
    // DeploymentPlugin templates into the deploy config), so reading them off the host again would
    // be redundant. When nothing is configured (local/test) fall back to the conservative defaults.
    val executionEnvironment: ExecutionEnvironment = forcedExecutionEnvironment
      ?: getGlobalVariable(RuntimeContextEnv.KEY_RUNTIME_EXECUTION_ENVIRONMENT_ENV)?.let { parseExecutionEnvironment(it) }
      ?: ExecutionEnvironment.LocalDev

    // Get the deployment stage
    val deploymentStage = forcedDeploymentStage
      ?: getGlobalVariable(RuntimeContextEnv.KEY_DEPLOYMENT_STAGE_ENV)?.let { parseDeploymentStage(it) }
      ?: DeploymentStage.Development

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
): RuntimeContext<ServiceHost.Default> {
  return RuntimeContextDetector.detectFromEnvironment(
    serviceHostRegistry = ServiceHost.Default,
    forcedExecutionEnvironment = forcedExecutionEnvironment,
    forcedDeploymentStage = forcedDeploymentStage,
    forcedHostname = null,
  ).also {
    this.current = it
  }
}
