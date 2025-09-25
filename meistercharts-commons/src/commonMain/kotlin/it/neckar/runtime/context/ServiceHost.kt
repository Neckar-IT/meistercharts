package it.neckar.runtime.context

/**
 * Axis 3: Concrete instance/domain.
 *
 * Keep this minimal; extend with more URLs as your app needs.
 */
interface ServiceHost {
  /**
   * The hostname of the service.
   */
  val hostname: Hostname

  /**
   * The deployment stage of the service.
   */
  val deploymentStage: DeploymentStage

  /**
   * The execution environment of the service.
   */
  val executionEnvironment: ExecutionEnvironment

  /**
   * Used as a default/fallback for standalone tools
   */
  object Localhost : ServiceHost, ServiceHostRegistry<Localhost> {
    override val hostname: Hostname
      get() = Hostname.localhost

    override val executionEnvironment: ExecutionEnvironment = ExecutionEnvironment.LocalDev

    override val deploymentStage: DeploymentStage = DeploymentStage.Development

    override fun findByHostname(hostname: Hostname?): Localhost {
      return this
    }
  }

  /**
   * Simple implementation of a service host
   */
  data class Simple(
    override val hostname: Hostname,
    override val deploymentStage: DeploymentStage = DeploymentStage.Development,
    override val executionEnvironment: ExecutionEnvironment = ExecutionEnvironment.LocalDev,
  ) : ServiceHost {
    override fun toString(): String {
      return "ServiceHost(hostname='$hostname', deploymentStage=$deploymentStage, executionEnvironment=$executionEnvironment)"
    }
  }
}
