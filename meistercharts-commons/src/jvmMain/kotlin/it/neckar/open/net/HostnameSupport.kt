package it.neckar.open.net

import it.neckar.runtime.context.Hostname
import java.io.BufferedReader
import java.net.InetAddress

/**
 * Allows access to the current hostname of the current device
 */
object HostnameSupport {
  fun guess(): Hostname? {
    guessFromInetAddress()?.let { return it }
    guessFromEnvironment()?.let { return it }
    guessFromCommandLine()?.let { return it }
    return null
  }

  val FallbackHost: Hostname = Hostname("unknown-host")

  fun guessOrFallback(): Hostname {
    return guess() ?: FallbackHost
  }

  fun guessFromInetAddress(): Hostname? {
    try {
      val host = InetAddress.getLocalHost().hostName
      if (host.isNotBlank()) return Hostname(host)
    } catch (_: Exception) {
    }

    return null
  }

  /**
   * Try common env var names
   */
  fun guessFromEnvironment(): Hostname? {
    System.getenv("HOSTNAME")?.let { if (it.isNotBlank()) return Hostname(it) }
    System.getenv("COMPUTERNAME")?.let { if (it.isNotBlank()) return Hostname(it) }

    return null
  }

  fun guessFromCommandLine(): Hostname? {
    // fallback: execute `hostname` shell command
    try {
      val proc = ProcessBuilder("hostname").start()

      val line = proc.inputStream.bufferedReader().use(BufferedReader::readLine)
      if (line.isNullOrBlank().not()) return Hostname(line.trim())
    } catch (_: Exception) {
    }

    return null
  }
}
