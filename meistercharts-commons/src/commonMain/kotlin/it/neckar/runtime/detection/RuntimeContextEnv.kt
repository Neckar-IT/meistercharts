package it.neckar.runtime.detection

import it.neckar.runtime.context.DeploymentStage
import it.neckar.runtime.context.ExecutionEnvironment
import it.neckar.runtime.context.Hostname
import it.neckar.runtime.context.ServiceHost

/**
 * Constants for runtime context environment keys.
 */
object RuntimeContextEnv {
  /**
   * Values of [it.neckar.runtime.context.ExecutionEnvironment]
   */
  const val KEY_RUNTIME_EXECUTION_ENVIRONMENT_ENV: String = "EXECUTION_ENVIRONMENT"

  fun executionEnvironment(executionEnvironment: ExecutionEnvironment): Pair<String, String> {
    return RuntimeContextEnv.KEY_RUNTIME_EXECUTION_ENVIRONMENT_ENV to executionEnvironment.name
  }

  /**
   * Values of [it.neckar.runtime.context.DeploymentStage]
   */
  const val KEY_DEPLOYMENT_STAGE_ENV: String = "DEPLOYMENT_STAGE"

  fun deploymentStage(deploymentStage: DeploymentStage): Pair<String, String> {
    return RuntimeContextEnv.KEY_DEPLOYMENT_STAGE_ENV to deploymentStage.name
  }

  /**
   * Values of [it.neckar.runtime.context.ServiceHost]
   * This is the host name of the service that is used to connect to the backend.
   */
  const val KEY_SERVICE_HOST_ENV: String = "SERVICE_HOST"

  fun serviceHost(serviceHost: ServiceHost): Pair<String, Hostname> {
    return RuntimeContextEnv.KEY_SERVICE_HOST_ENV to serviceHost.hostname
  }
}
