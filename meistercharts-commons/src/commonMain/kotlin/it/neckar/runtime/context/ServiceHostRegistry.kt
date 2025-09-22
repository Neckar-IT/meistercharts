package it.neckar.runtime.context

/**
 * Registry to resolve hosts by name or provide a stage-default.
 * Provide your own implementation in your app.
 *
 * Usually implemented by the companion object of the [ServiceHost] enum.
 */
interface ServiceHostRegistry<out T: ServiceHost> {
  /**
   * Returns a host by the hostname.
   * Should return a default host if the hostname is null or empty.
   */
  fun findByHostname(hostname: String?): T
}
