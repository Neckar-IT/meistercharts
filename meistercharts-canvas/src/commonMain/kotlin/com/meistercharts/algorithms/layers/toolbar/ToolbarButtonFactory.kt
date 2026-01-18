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
package com.meistercharts.algorithms.layers.toolbar

import com.meistercharts.canvas.paintable.Button
import com.meistercharts.canvas.paintable.ButtonPressedEvent
import com.meistercharts.canvas.paintable.ButtonPriority
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.paintable.toButtonPainter
import com.meistercharts.canvas.resetOnlyZoom
import com.meistercharts.canvas.resetZoomAndTranslationToDefaults
import com.meistercharts.canvas.zoomIn
import com.meistercharts.canvas.zoomOut
import com.meistercharts.color.ColorProviderNullable
import com.meistercharts.resources.Icons
import com.meistercharts.zoom.UpdateReason
import it.neckar.geometry.Size

/**
 * Creates buttons commonly used in [ToolbarLayer]s
 */
class ToolbarButtonFactory(
  /**
   * Size provider based upon the button state.
   * The size provider is used for methods that do not provide an own size
   */
  val sizeProvider: (state: ButtonState) -> Size = defaultSizeProvider,
  /**
   * Fill provider based upon the button state.
   * The fill provider is used for methods that do not provide a button painter
   */
  val fillProvider: (state: ButtonState) -> ColorProviderNullable = defaultFillProvider,
) {

  /**
   * Creates a button with a given size and an action that is called for each button click
   */
  fun button(
    /**
     * Provides the paintable based upon the button state
     */
    buttonPaintableProvider: ButtonPaintableProvider,
    /**
     * The size for the button. Overwrites the size provided by [sizeProvider] set at the factory itself
     */
    size: Size,

    priority: ButtonPriority = ButtonPriority.AlwaysVisible,
    /**
     * The action that is executed on press on the button
     */
    action: (ButtonPressedEvent) -> Unit,
  ): Button {
    val buttonPainter = buttonPaintableProvider.toButtonPainter()

    return Button(buttonPainter, size.width, size.height, priority).apply {
      action(action)
    }
  }

  /**
   * Creates a button using a paintable resolver.
   *
   * This method uses the default [sizeProvider] and [fillProvider]
   */
  fun button(
    /**
     * Returns the paintable for a size and fill (e.g. `Icons::zoomIn`)
     */
    buttonPaintableResolver: ButtonPaintableResolver,
    /**
     * The button priority
     */
    priority: ButtonPriority = ButtonPriority.AlwaysVisible,
    /**
     * The action that is called on button press
     */
    action: (ButtonPressedEvent) -> Unit,
  ): Button {
    val buttonPaintableProvider = DefaultButtonPaintableProvider(buttonPaintableResolver, sizeProvider, fillProvider)
    return button(buttonPaintableProvider::getPaintable, sizeProvider(ButtonState.default), priority, action)
  }

  /**
   * Creates a toggle button.
   * Sets the [Button.action] that automatically toggles the `[Button.selectedProperty]` state on click
   *
   * Usually you would bind the selected property of the button after creating it.
   */
  fun toggleButton(
    defaultPaintableResolver: ButtonPaintableResolver,
    selectedPaintableResolver: ButtonPaintableResolver = defaultPaintableResolver,
  ): Button {
    return button(
      buttonPaintableProvider = DefaultToggleButtonPaintableProvider(
        unselectedPaintableResolver = defaultPaintableResolver,
        selectedPaintableResolver = selectedPaintableResolver,
        sizeProvider = sizeProvider,
        fillProvider = fillProvider
      )::getPaintable,
      size = sizeProvider(ButtonState.default),
    ) {
      it.target.toggleSelected()
    }
  }
}

/**
 * Creates a button that zooms in when clicked
 */
fun ToolbarButtonFactory.zoomInButton(): Button = button(Icons::zoomIn) { event -> event.chartSupport.zoomIn(reason = UpdateReason.UserInteraction) }

/**
 * Creates a button that zooms out when clicked
 */
fun ToolbarButtonFactory.zoomOutButton(): Button = button(Icons::zoomOut) { event -> event.chartSupport.zoomOut(reason = UpdateReason.UserInteraction) }

/**
 * Creates a button that resets the zoom when clicked
 */
fun ToolbarButtonFactory.resetZoomButton(): Button = button(Icons::resetZoom) { event -> event.chartSupport.resetOnlyZoom(reason = UpdateReason.UserInteraction) }

/**
 * Creates a button that resets the zoom and translation when clicked
 */
fun ToolbarButtonFactory.resetZoomAndTranslationButton(): Button = button(Icons::home) { event -> event.chartSupport.resetZoomAndTranslationToDefaults(reason = UpdateReason.UserInteraction) }

/**
 * Returns the default fills color for a button
 */
val defaultFillProvider: (state: ButtonState) -> ColorProviderNullable = DefaultToolbarButtonFillProvider()::color

/**
 * Provides default sizes for button paintables. Returns larger sizes for pressed/hover
 */
val defaultSizeProvider: (state: ButtonState) -> Size = DefaultToolbarButtonSizeProvider()::size
