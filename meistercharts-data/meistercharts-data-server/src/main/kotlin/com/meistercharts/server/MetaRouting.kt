/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.server

import com.meistercharts.data.rest.MeisterchartsRestProtocolInfo
import com.meistercharts.version.MeisterChartsVersion
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Contains the routing for metadata
 */
@Deprecated("Remove with meta routing from ktor-server")
object MetaRouting {
  val metaRouting: Routing.() -> Unit = {
    get("") {
      call.respond<String>("Charting Server is online")
    }

    get("version") {
      call.respond<String>(MeisterChartsVersion.versionAsStringVerbose)
    }

    get("version/protocol") {
      call.respond<String>(MeisterchartsRestProtocolInfo.ProtocolVersion.format())
    }

    get("boom") {
      throw IllegalStateException("Something is broken!")
    }

    get("heartbeat") {
      call.respond<String>(ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
    }
  }
}
