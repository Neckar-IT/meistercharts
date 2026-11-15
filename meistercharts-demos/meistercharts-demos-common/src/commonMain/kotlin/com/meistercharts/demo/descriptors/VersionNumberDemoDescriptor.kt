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
package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.addVersionNumber
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.layer.registerDirtyListener
import com.meistercharts.canvas.timerSupport
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.open.observable.ObservableBoolean
import kotlin.time.Duration.Companion.milliseconds


class VersionNumberDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("30e73d36-5fa4-4776-b3ff-b48ab7520679")
  override val name: String = "Version Number"
  override val description: String = "Shows a version number layer."
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val versionNumberVisible = ObservableBoolean(true)

        configure {
          versionNumberVisible.registerDirtyListener(this, DirtyReason.UiStateChanged)

          layers.addClearBackground()
          layers.addVersionNumber()

          chartSupport.timerSupport.repeat(500.0.milliseconds) {
            versionNumberVisible.toggle()
          }
        }

      }
    }
  }
}
