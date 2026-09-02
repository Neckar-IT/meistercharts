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
package it.neckar.open.test.utils.testcontainer

import com.github.dockerjava.api.command.CreateContainerCmd
import com.github.dockerjava.api.model.Ports
import it.neckar.open.kotlin.lang.requireNotNull
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.GenericContainer
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Publishes the container's ports on the address this JVM reaches the Docker daemon on, instead of on every
 * interface. Docker writes its port forwards into the `DOCKER` nat chain, ahead of any host firewall, so on a
 * host with a public address the container port answers from the internet.
 */
fun GenericContainer<*>.publishPortsOnDockerHostOnly() {
  withCreateContainerCmdModifier { cmd ->
    cmd.rewritePublishedPortsTo(resolveBindAddress(DockerClientFactory.instance().dockerHostIpAddress()))
  }
}

/**
 * Keeps each host port — absent for a randomized one, a number for `withFixedExposedPort`.
 */
internal fun CreateContainerCmd.rewritePublishedPortsTo(bindAddress: String) {
  //docker-java turns an empty host IP into null, which Docker reads as every interface.
  require(bindAddress.isNotBlank()) { "Blank bind address for <$image>" }

  val hostConfig = hostConfig.requireNotNull { "No host config on the create command for <$image>" }
  val declaredBindings = hostConfig.portBindings.requireNotNull { "No port bindings on the create command for <$image>" }

  val boundToDockerHost = Ports()
  declaredBindings.bindings.forEach { (exposedPort, bindings) ->
    bindings
      .requireNotNull { "Port $exposedPort of <$image> is exposed without a binding to rewrite" }
      .forEach { binding ->
        boundToDockerHost.bind(exposedPort, Ports.Binding(bindAddress, binding.hostPortSpec))
      }
  }

  hostConfig.withPortBindings(boundToDockerHost)
}

/**
 * Turns the Docker host Testcontainers resolved into a literal address, because Docker's `HostIp` takes no
 * host names. [GenericContainer.getHost] returns the same [dockerHost], so the tests reach the port where it
 * is bound; it has to be local on the daemon host too, which a hand-set `TESTCONTAINERS_HOST_OVERRIDE` need not be.
 */
internal fun resolveBindAddress(dockerHost: String?): String {
  //An empty host is not a null one, and InetAddress.getByName answers both with the loopback address.
  require(dockerHost.isNullOrBlank().not()) { "Testcontainers resolved no Docker host — check DOCKER_HOST and TESTCONTAINERS_HOST_OVERRIDE" }

  try {
    return InetAddress.getByName(dockerHost).hostAddress
  } catch (e: UnknownHostException) {
    throw IllegalArgumentException("Docker host <$dockerHost> does not resolve, so no port can be published on it", e)
  }
}
