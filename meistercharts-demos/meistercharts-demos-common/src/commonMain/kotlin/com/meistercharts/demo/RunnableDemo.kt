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
package com.meistercharts.demo

import kotlin.uuid.Uuid

/**
 * Represents a single demo instance that can be run.
 * Consists of the demo descriptor and the (optional) configuration.
 */
data class RunnableDemo(val demoDescriptor: MeisterchartsDemoDescriptor<*>, val configuration: PredefinedConfiguration<Any?>?) {
  /**
   * Returns the relevant UUID.
   * If the configuration is set, the UUID of the configuration is returned.
   */
  val uuid: Uuid
    get() {
      return configuration?.uuid ?: demoDescriptor.uuid
    }

  val variabilityType: VariabilityType
    get() = demoDescriptor.variabilityType

  /**
   * Returns the demo quality. Returns [DemoQuality.Untested] if the demo has no quality.
   */
  val quality: DemoQuality
    get() {
      return demoDescriptor.quality
    }

  /**
   * Returns the name for this runnable demo (*without* the UUID).
   *
   * ATTENTION: This name is *not* unique!
   */
  fun name(): String {
    return if (configuration != null) {
      "${demoDescriptor.name} - ${configuration.name}"
    } else {
      demoDescriptor.name
    }
  }

  /**
   * Returns a unique but readable description
   */
  fun uniqueDemoName(): UniqueDemoName {
    return UniqueDemoName("${name()} - $uuid")
  }
}
