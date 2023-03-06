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
package com.meistercharts.algorithms.layers.toolbar

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.paintable.Button
import com.meistercharts.canvas.paintable.ButtonPressedEvent
import com.meistercharts.canvas.paintable.ButtonPriority
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.toButtonPainter
import com.meistercharts.canvas.resetOnlyZoom
import com.meistercharts.canvas.resetZoomAndTranslationToDefaults
import com.meistercharts.canvas.zoomIn
import com.meistercharts.canvas.zoomOut
import com.meistercharts.model.Size
import com.meistercharts.resources.Icons

/**
 * Provides an image for a given button state
 */
typealias ButtonPaintableProvider = (ButtonState) -> Paintable

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
  val fillProvider: (state: ButtonState) -> Color = defaultFillProvider
) {

  /**
   * Creates a button with a given size and an action that is called for each button click
   * @param buttonPaintableProvider provides the image depending on the current state
   * @param size this button uses the given size and *not* the [sizeProvider] set at the factory
   * @param action the action to be executed when the button is clicked
   */
  fun button(
    /**
     * Provides the paintable based upon the button state
     */
    buttonPaintableProvider: ButtonPaintableProvider,
    /**
     * The size for the button
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
   * Creates a button using  a paintable resolver.
   *
   * This method uses the default [sizeProvider] and [fillProvider]
   */
  fun button(
    /**
     * Returns a paintable for a size and fill (e.g. `Icons::zoomIn`)
     */
    paintableResolver: (size: Size, fill: Color) -> Paintable,
    /**
     * The button priority
     */
    priority: ButtonPriority = ButtonPriority.AlwaysVisible,
    /**
     * The action that is called on button press
     */
    action: (ButtonPressedEvent) -> Unit,
  ): Button {
    val buttonPaintableProvider = DefaultButtonPaintableProvider(paintableResolver, sizeProvider, fillProvider)
    return button(buttonPaintableProvider::getPaintable, sizeProvider(ButtonState.default), priority, action)
  }

  /**
   * Creates a toggle button that automatically toggles the `selected` state on click
   *
   * Usually you would bind the selected property of the button after creating it.
   */
  fun toggleButton(
    defaultPaintableResolver: (size: Size, fill: Color) -> Paintable,
    selectedPaintableResolver: (size: Size, fill: Color) -> Paintable
  ): Button {
    return button(DefaultToggleButtonPaintableProvider(defaultPaintableResolver, selectedPaintableResolver, sizeProvider, fillProvider)::getPaintable, sizeProvider(ButtonState.default)) {
      it.target.toggleSelected()
    }
  }
}

/**
 * Creates a button that zooms in when clicked
 */
fun ToolbarButtonFactory.zoomInButton(): Button = button(Icons::zoomIn) { event -> event.chartSupport.zoomIn() }

/**
 * Creates a button that zooms out when clicked
 */
fun ToolbarButtonFactory.zoomOutButton(): Button = button(Icons::zoomOut) { event -> event.chartSupport.zoomOut() }

/**
 * Creates a button that resets the zoom when clicked
 */
fun ToolbarButtonFactory.resetZoomButton(): Button = button(Icons::resetZoom) { event -> event.chartSupport.resetOnlyZoom() }

/**
 * Creates a button that resets the zoom and translation when clicked
 */
fun ToolbarButtonFactory.resetZoomAndTranslationButton(): Button = button(Icons::home) { event -> event.chartSupport.resetZoomAndTranslationToDefaults() }

/**
 * Default implementation that
 */
class DefaultButtonPaintableProvider(
  /**
   * Returns the paintable for the given size and fill
   */
  val paintableResolver: (size: Size, fill: Color) -> Paintable,

  val sizeProvider: (state: ButtonState) -> Size,
  val fillProvider: (state: ButtonState) -> Color
) {

  /**
   * Returns the paintable for the given state using the [paintableResolver]
   */
  fun getPaintable(buttonState: ButtonState): Paintable {
    val size = sizeProvider(buttonState)
    val fill = fillProvider(buttonState)

    return paintableResolver(size, fill)
  }
}

class DefaultToggleButtonPaintableProvider(
  /**
   * Returns the paintable for the given size and fill
   */
  val defaultPaintableResolver: (size: Size, fill: Color) -> Paintable,
  val selectedPaintableResolver: (size: Size, fill: Color) -> Paintable,

  val sizeProvider: (state: ButtonState) -> Size,
  val fillProvider: (state: ButtonState) -> Color
) {

  /**
   * Returns the paintable for the given state
   */
  fun getPaintable(buttonState: ButtonState): Paintable {
    val size = sizeProvider(buttonState)
    val fill = fillProvider(buttonState)

    return if (buttonState.selected) {
      selectedPaintableResolver(size, fill)
    } else {
      defaultPaintableResolver(size, fill)
    }
  }
}

/**
 * Returns the default fill color for a button
 */
val defaultFillProvider: (state: ButtonState) -> Color = DefaultToolbarButtonFillProvider()::color

/**
 * Provides default sizes for a button paintable. Returns larger sizes for pressed/hover
 */
val defaultSizeProvider: (state: ButtonState) -> Size = DefaultToolbarButtonSizeProvider()::size

/**
 * Default implementation for button size provider
 */
class DefaultToolbarButtonSizeProvider(
  var defaultSize: Size = Size.PX_40,
  var activeSize: Size = Size.PX_50
) {
  fun size(state: ButtonState): Size {
    if (state.disabled) {
      return defaultSize
    }

    return when {
      state.pressed -> activeSize
      state.hover -> activeSize
      else -> defaultSize
    }
  }
}

/**
 * Provides a fixed color depending on the state
 */
class DefaultToolbarButtonFillProvider {
  fun color(state: ButtonState): Color {
    return when {
      state.disabled -> Color.rgba(200, 200, 200, 0.6)
      state.pressed  -> Color.rgba(150, 150, 150, 1.0)
      state.hover    -> Color.rgba(150, 150, 150, 0.75)
      state.focused  -> Color.rgba(150, 150, 150, 0.85)
      else           -> Color.rgba(150, 150, 150, 0.6)
    }
  }
}
