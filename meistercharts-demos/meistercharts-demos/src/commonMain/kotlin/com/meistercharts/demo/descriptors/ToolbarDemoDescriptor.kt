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
package com.meistercharts.demo.descriptors

import com.meistercharts.model.Orientation
import com.meistercharts.algorithms.layers.TimeAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.toolbar.ToolbarButtonFactory
import com.meistercharts.algorithms.layers.toolbar.ToolbarLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.paintable.Button
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.registerDirtyListener
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Direction
import it.neckar.open.collections.fastForEach
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableObject
import com.meistercharts.resources.Icons
import it.neckar.logging.LoggerFactory

class ToolbarDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Toolbar"
  override val description: String = "## How to create a toolbar"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      val isAnimated = ObservableBoolean(true)
      val isAnimationEnabled = ObservableBoolean(true)
      val autoScale = ObservableBoolean(false)
      val timestampsMode = ObservableObject(TimeAxisLayer.TimestampsMode.Absolute)


      val fillProvider = object {
        var buttonFill: Color = Color("#6200EE")
        var buttonFillDisabled: Color = Color("#8A8A8A")
        var buttonFillHover: Color = Color("#9452F3")
        var buttonFillPressed: Color = Color.orange

        fun color(state: ButtonState): Color {
          return when {
            state.disabled -> buttonFillDisabled
            state.pressed  -> buttonFillPressed
            state.hover    -> buttonFillHover
            else           -> buttonFill
          }
        }
      }

      meistercharts {
        configure {
          layers.addClearBackground()

          val buttonFactory = ToolbarButtonFactory(fillProvider = fillProvider::color)

          // set up bidirectional binding // TODO wrap this into a general purpose function
          val isTimestampsAbsolute = ObservableBoolean(false)
          timestampsMode.consumeImmediately {
            isTimestampsAbsolute.value = it == TimeAxisLayer.TimestampsMode.Absolute
          }
          isTimestampsAbsolute.consumeImmediately {
            timestampsMode.value = if (it) TimeAxisLayer.TimestampsMode.Absolute else TimeAxisLayer.TimestampsMode.Relative
          }

          isAnimated.consume {
            if (it) {
              logger.debug("go to last data point")
            }
          }

          fun createButtons(): List<Button> {
            return listOf(
              buttonFactory.button(Icons::home) {
                logger.debug("home")
              },
              buttonFactory.toggleButton(Icons::pause, Icons::play).apply {
                selectedProperty.bindBidirectional(isAnimated)
              },
              buttonFactory.button(Icons::zoomIn) { logger.debug("zoom in") },
              buttonFactory.button(Icons::zoomOut) { logger.debug("zoom out") },
              buttonFactory.button(Icons::hourglass) { throw UnsupportedOperationException("Must not be called for disabled!") }.also { it.state = ButtonState.disabled },
              buttonFactory.button(Icons::zoomIn) { logger.debug("zoom in") },
              buttonFactory.button(Icons::zoomOut) { logger.debug("zoom out") },
              buttonFactory.button(Icons::first) { logger.debug("go to first data point") },
              buttonFactory.button(Icons::last) { logger.debug("go to last data point") },
              buttonFactory.toggleButton(Icons::noAutoScale, Icons::autoScale).apply { selectedProperty.bindBidirectional(autoScale) },
              buttonFactory.toggleButton(Icons::timestampsRelative, Icons::timestampsAbsolute).apply { selectedProperty.bindBidirectional(isTimestampsAbsolute) },
              buttonFactory.toggleButton(Icons::pause, Icons::play).apply {
                selectedProperty.bindBidirectional(isAnimated)
              },
            )
          }

          //Top
          val toolbarTop = ToolbarLayer(
            createButtons()
          ) {
            anchorDirection = Direction.TopCenter
            layoutOrientation = Orientation.Horizontal
          }

          //Bottom
          val toolbarBottom = ToolbarLayer(
            createButtons()
          ) {
            anchorDirection = Direction.BottomCenter
            layoutOrientation = Orientation.Horizontal
          }

          //Left
          val toolbarLeft = ToolbarLayer(createButtons()) {
            anchorDirection = Direction.TopLeft
            layoutOrientation = Orientation.Vertical
          }

          //Right
          val toolBarRight = ToolbarLayer(createButtons()) {
            anchorDirection = Direction.BottomRight
            layoutOrientation = Orientation.Vertical
          }


          val toolbars = listOf(
            toolbarTop,
            toolbarBottom,
            toolbarLeft,
            toolBarRight
          )

          toolbars.fastForEach {
            layers.addLayer(it)
          }

          //Connect the dirty state
          isAnimated.registerDirtyListener(this, DirtyReason.UiStateChanged)
          isAnimationEnabled.registerDirtyListener(this, DirtyReason.UiStateChanged)
          autoScale.registerDirtyListener(this, DirtyReason.ConfigurationChanged)

          configurableDouble("Gap", toolbarTop.configuration.gap) {
            max = 100.0
            onChange {
              toolbars.fastForEach { toolbar ->
                toolbar.configuration.gap = it
              }
              markAsDirty()
            }
          }

          configurableDouble("Button gap", toolbarTop.configuration.buttonGap) {
            max = 100.0
            onChange {
              toolbars.fastForEach { toolbar ->
                toolbar.configuration.buttonGap = it
              }
              markAsDirty()
            }
          }

          configurableColorPicker("Fill", fillProvider::buttonFill)
          configurableColorPicker("Fill hover", fillProvider::buttonFillHover)
          configurableColorPicker("Fill pressed", fillProvider::buttonFillPressed)
          configurableColorPicker("Fill disabled", fillProvider::buttonFillDisabled)
        }
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.demo.descriptors.ToolbarDemoDescriptor")
  }
}
