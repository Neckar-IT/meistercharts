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
package com.meistercharts.data.rest

import it.neckar.open.version.Version
import kotlin.jvm.JvmField

/**
 * Contains information about the rest protocol
 */
object MeisterchartsRestProtocolInfo {
  /**
   * The protocol version.
   * The major version has to be the same for client and server. Otherwise, a connection is not possible.
   * The minor version may be different: The client can be *older* (smaller number) than the server.
   *
   * Therefore, the server may support older clients
   * Incompatible changes require an update of the major version
   */
  @JvmField
  val ProtocolVersion: Version = Version.valueOf(0, 0, 1)
}
