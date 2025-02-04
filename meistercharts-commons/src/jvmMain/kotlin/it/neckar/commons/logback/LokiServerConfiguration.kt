package it.neckar.commons.logback

import it.neckar.open.http.Url

/**
 * Configuration for the loki server
 */
data class LokiServerConfiguration(
  val lokiServerUrl: Url.Absolute,
  val username: String,
  val password: String,
) {
  companion object {
    /**
     * Default configuration for Neckar IT
     */
    val NeckarIT: LokiServerConfiguration = LokiServerConfiguration(
      lokiServerUrl = Url.absolute("https://monitoring.neckar.it/loki/api/v1/push"),
      username = "loki",
      password = "loki-neckarit",
    )
  }
}
