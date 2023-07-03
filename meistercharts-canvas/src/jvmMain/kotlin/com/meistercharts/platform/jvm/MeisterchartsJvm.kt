/**
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
package com.meistercharts.platform.jvm

import com.meistercharts.canvas.JvmImageLoader
import com.meistercharts.resources.JvmLocalResourcePaintableFactory
import it.neckar.open.time.JvmTimerSupport
import it.neckar.open.time.jvmTimerSupport

/**
 * Contains the services and constants for Meistercharts on the JVM
 */
object MeisterchartsJvm {
  var jvmImageLoader: JvmImageLoader = JvmImageLoader { _, _ ->
    throw UnsupportedOperationException("please set the jvmImageLoader for the current platform by calling MeisterChartsPlatform.init()")
  }

  /**
   * Reference to the current [JvmTimerSupport]
   */
  var timerSupport: JvmTimerSupport by ::jvmTimerSupport

  /**
   * Holds the current instance of the local resource paintable provider
   */
  var localResourcePaintableFactory: JvmLocalResourcePaintableFactory = JvmLocalResourcePaintableFactory { _, _, _ ->
    throw UnsupportedOperationException("please set the jvmLocalResourcePaintableFactory for the current platform by calling MeisterChartPlatform.init()")
  }

}
