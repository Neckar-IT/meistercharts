package it.neckar.runtime.context

/**
 * Technical execution environment
 */
enum class ExecutionEnvironment {
  /**
   * Local development run — typically started from an IDE or directly on the developer's machine.
   * May include hot reload, local mocks, and relaxed security.
   */
  LocalDev,

  /**
   * Running inside a Docker container without orchestration (e.g., `docker run` locally or on a server).
   */
  Docker,

  /**
   * Running in a Kubernetes cluster (Pod managed by an orchestrator).
   * Usually relies on ingress controllers for TLS termination and routing.
   */
  @Deprecated("Currently not supported")
  Kubernetes,

  /**
   * Running in a Continuous Integration environment (build/test pipeline).
   * Typically short-lived, with ephemeral resources and mocked external services.
   */
  CI,
  ;

  fun inCi(): Boolean {
    return this == CI
  }

  /**
   * Returns true if this is [LocalDev]
   */
  fun isLocalDev(): Boolean {
    return this == LocalDev
  }
}
