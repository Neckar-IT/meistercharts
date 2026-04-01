/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
