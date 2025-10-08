package it.neckar.runtime.context

/**
 * Immutable snapshot of the application's current runtime state.
 *
 * This aggregates the three main classification axes ([it.neckar.runtime.context.ExecutionEnvironment], [it.neckar.runtime.context.DeploymentStage], [ServiceHost])
 * along with orthogonal flags that can influence runtime behavior.
 *
 * Created once during application bootstrap and treated as read-only
 * throughout the application's lifetime.
 *
 * ## Initialization
 * * JVM: `RuntimeContext.initializeFromEnvironment(...)`. See `RuntimeContextEnv` for the environment keys that are used.
 * * JS: To Be Done
 */
data class RuntimeContext<HostType: ServiceHost>(
  /**
   * Technical execution environment — where/how the application process is running.
   * Examples: LocalDev, Docker, Kubernetes, CI, Serverless.
   */
  val executionEnvironment: ExecutionEnvironment,

  /**
   * Business / lifecycle environment — which functional stage of delivery
   * the application is in.
   * Examples: Development, Staging, Demo, Production, QA, Sandbox.
   */
  val stage: DeploymentStage,

  /**
   * Concrete target instance / domain configuration.
   * Defines hostnames, base URLs, and other connection details
   * for this specific runtime environment.
   */
  val host: HostType,

  /**
   * Indicates whether the application is currently running in a unit test context.
   * Useful to skip heavy initialization or replace integrations with mocks.
   */
  val inUnitTest: Boolean,

  /**
   * Indicates whether the application is running in debug mode
   * (e.g., started from an IDE with a debugger attached).
   * Can be used to relax timeouts or enable verbose logging.
   */
  val debugMode: Boolean,

  /**
   * If this is the initial value, this is set to true
   */
  val initialValue: Boolean,
) {

  /**
   * Returns true if the runtime context has not been initialized
   */
  fun isInitialValue(): Boolean {
    return initialValue
  }

  companion object {
    /**
     * Technical execution environment — where/how the application process is running.
     * Examples: LocalDev, Docker, Kubernetes, CI, Serverless.
     */
    val executionEnvironment: ExecutionEnvironment
      get() {
        return current.executionEnvironment
      }

    /**
     * Business / lifecycle environment — which functional stage of delivery
     * the application is in.
     * Examples: Development, Staging, Demo, Production, QA, Sandbox.
     */
    val deploymentStage: DeploymentStage
      get() {
        return current.stage
      }

    /**
     * Concrete target instance / domain configuration.
     * Defines hostnames, base URLs, and other connection details
     * for this specific runtime environment.
     */
    val host: ServiceHost
      get() {
        return current.host
      }

    /**
     * Indicates whether the application/test is running in a Continuous Integration environment.
     * This can affect how resources are managed, tests are executed, and external services are mocked.
     */
    val inCI: Boolean
      get() = executionEnvironment == ExecutionEnvironment.CI

    /**
     * Returns true if the current runtime context is in a development stage.
     */
    val inDev: Boolean
      get() = deploymentStage == DeploymentStage.Development

    val debugMode: Boolean
      get() = current.debugMode

    val inUnitTest: Boolean
      get() = current.inUnitTest


    /**
     * The initial value that is set during application startup.
     */
    private val initialValue = getInitialValue().also {
      require(it.isInitialValue()) {
        "The initial value of the runtime context must have initialValue=true"
      }
    }

    /**
     * The current runtime context.
     *
     * This is set during application startup and must not be modified afterward.
     */
    var current: RuntimeContext<*> = initialValue
      internal set
      get() {
        if (field.isInitialValue() && initialValue.inUnitTest.not()) {
          println("Warning: The current runtime context is still the fallback context. Call `RuntimeContext.initializeFromEnvironment(serviceHostRegistry)`")
        }
        return field
      }

    /**
     * Returns true if the runtime context has been initialized to a value different from the initial fallback value.
     */
    fun isInitialized(): Boolean {
      return current.isInitialValue().not()
    }

    /**
     * Initializes the current runtime context.
     */
    fun initialize(context: RuntimeContext<*>) {
      this.current = context
    }
  }
}


/**
 * Returns the initial value of the runtime context.
 */
expect fun getInitialValue(): RuntimeContext<ServiceHost.Localhost>
