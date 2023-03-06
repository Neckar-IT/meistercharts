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
package com.meistercharts.fx.time

import com.meistercharts.algorithms.time.DataPoint
import it.neckar.open.time.nowMillis
import it.neckar.open.javafx.properties.*
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.util.Duration

/**
 * Helper class that records data points
 */
class TimeDiagramModelRecorder<T>(
  val model: TimeDiagramModel2<T>,
  valueFactory: ValueFactory<T>
) : Disposable {

  private val disposeSupport = DisposeSupport()

  /**
   * Whether the recorder is currently recording
   */
  val recordingProperty: BooleanProperty = SimpleBooleanProperty()
    .apply {
      addListener { _, _, newValue ->
        if (newValue!!) {
          timeline.play()
        } else {
          timeline.stop()
        }
      }
    }

  var recording: Boolean by recordingProperty

  /**
   * The timeline that is used for recording
   */
  private val timeline: Timeline = Timeline(
    KeyFrame(
      Duration.millis(model.expectedDistanceBetweenDataPoints),
      { model.addDataPoint(DataPoint(nowMillis(), valueFactory.create())) }
    )
  ).apply {
    cycleCount = Animation.INDEFINITE
  }.also {
    disposeSupport.onDispose {
      it.stop()
    }
  }

  override fun dispose() {
    disposeSupport.dispose()
  }

  /**
   * Used to create a value
   */
  fun interface ValueFactory<T> {
    fun create(): T
  }
}
