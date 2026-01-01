package it.neckar.runtime.context

/**
 * Axis 3: Concrete instance/domain.
 *
 * Keep this minimal; extend with more URLs as your app needs.
 *
 * ## Creating Project-Specific ServiceHost Implementations
 * Create implementations in `buildSrc/src/main/kotlin/it/neckar/projects/<project>/`.
 * Examples: [it.neckar.projects.mea.MeaHost], [it.neckar.projects.elektromeister.ElektroMeisterHost]
 *
 * To use these in JS/browser code, apply the [it.neckar.gradle.project.ProvideSourceCodeFromBuildSrcPlugin]
 * which creates symlinks from buildSrc sources into the project's generated sources.
 *
 * @see it.neckar.gradle.project.ProvideSourceCodeFromBuildSrcPlugin for setup instructions
 * @see ServiceHostRegistry for hostname-based lookup
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
