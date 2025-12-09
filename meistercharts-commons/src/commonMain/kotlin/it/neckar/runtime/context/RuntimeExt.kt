package it.neckar.runtime.context

import it.neckar.open.http.Url
import it.neckar.projects.common.Port

/**
 * Returns the host URL of this service host as [Url.Absolute]
 */
fun ServiceHost.asUrl(port: Port): Url.Absolute {
  return hostname.asUrl(port)
}

/**
 * Returns the host URL as [Url.Absolute]
 */
fun Hostname.asUrl(port: Port): Url.Absolute {
  return Url.https(this, port)
}
