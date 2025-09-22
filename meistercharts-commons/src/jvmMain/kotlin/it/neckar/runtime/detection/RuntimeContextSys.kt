package it.neckar.runtime.detection

/**
 * Constants for runtime context environment keys.
 */
object RuntimeContextSys {
  /**
   * Values of [it.neckar.runtime.context.ExecutionEnvironment]
   */
  const val KEY_RUNTIME_EXECUTION_ENVIRONMENT_SYS: String = "execution.environment"

  /**
   * Values of [it.neckar.runtime.context.DeploymentStage]
   */
  const val KEY_DEPLOYMENT_STAGE_SYS: String = "deployment.stage"

  /**
   * Values of [it.neckar.runtime.context.ServiceHost]
   * This is the host name of the service that is used to connect to the backend.
   */
  const val KEY_SERVICE_HOST_SYS: String = "service.host"
}
