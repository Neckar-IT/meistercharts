package it.neckar.runtime.context


/**
 * Business/lifecycle environment.
 *
 * Describes the functional environment in the software delivery lifecycle.
 */
enum class DeploymentStage {
  /**
   * Active development environment — unstable, feature work in progress,
   * often using test data and relaxed validation.
   */
  Development,

  /**
   * Pre-production environment mirroring Production setup and configuration.
   * Used for final integration, performance, and acceptance testing.
   */
  Staging,

  /**
   * Demonstration environment — contains demo data and possibly hard-coded credentials.
   * No registration possible; may auto-login a demo user.
   */
  Demo,

  /**
   * Live, customer-facing production environment — stable, secured,
   * with real customer data and strict policies.
   */
  Production,

  /**
   * Quality assurance / dedicated testing environment.
   * Used for automated/manual testing with controlled data sets.
   */
  QA,

  /**
   * Isolated environment for experiments, proof-of-concepts, or user sandboxes.
   * No guarantee of data persistence or stability.
   */
  Sandbox;

  fun isDev(): Boolean {
    return this == Development
  }

  fun isDevelopmentOrDemo(): Boolean {
    return this == Development || this == Demo
  }

  fun isDemo(): Boolean {
    return this == Demo
  }
}
