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

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import it.neckar.open.kotlin.lang.requireNotNull
import org.testcontainers.containers.GenericContainer

/**
 * Checks against the running container what [publishPortsOnDockerHostOnly] promises: every exposed port
 * published, and nothing published anywhere but on the address [GenericContainer.getHost] reports.
 */
fun GenericContainer<*>.assertPublishedOnDockerHostOnly() {
  val expectedHostIp = resolveBindAddress(host)
  val inspection = containerInfo.requireNotNull { "<$dockerImageName> has not been started, so it publishes nothing yet" }

  val published = inspection.networkSettings.ports.bindings
  //Docker reports a port the image exposes but nobody published with a null array, and getExposedPorts()
  //drops the protocol — so the two are matched by port number over what is actually published.
  val publishedPorts = published.filterValues { it != null }.keys.map { it.port }

  assertThat(publishedPorts, "published ports of <$dockerImageName>").isNotEmpty()
  assertThat(publishedPorts, "published ports of <$dockerImageName>").containsAll(*exposedPorts.toTypedArray())

  published.forEach { (exposedPort, bindings) ->
    bindings?.forEach { binding ->
      assertThat(binding.hostIp, "host IP published for $exposedPort of <$dockerImageName>").isEqualTo(expectedHostIp)
    }
  }
}
