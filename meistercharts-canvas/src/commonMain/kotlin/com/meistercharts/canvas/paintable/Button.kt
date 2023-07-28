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
package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.resolve
import com.meistercharts.algorithms.layers.toolbar.ButtonPaintableProvider
import com.meistercharts.color.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.fillRoundedRect
import com.meistercharts.canvas.paintable.ButtonState.Companion.disabled
import com.meistercharts.design.Theme
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Rectangle
import it.neckar.open.i18n.TextKey
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableObject
import it.neckar.open.unit.other.px

/**
 * An interactive button
 * @param buttonPainter determines the appearance of this [Button]
 * @param width the width of the bounding box of this [Button]
 * @param height the height of the bounding box of this [Button]
 * @param priority the priority of the button - required when there is not enough space for all buttons to be visible at the same time
 *
 */
class Button(
  private val buttonPainter: ButtonPainter,
  val width: @px Double,
  val height: @px Double,
  /**
   * The priority of the button
   */
  val priority: ButtonPriority = ButtonPriority.AlwaysVisible,
) : Paintable {

  val boundingBox: Rectangle = Rectangle(0.0, 0.0, width, height)

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return boundingBox
  }

  /**
   * The actions that are notified on click
   */
  private val actions: MutableList<(ButtonPressedEvent) -> Unit> = mutableListOf()

  /**
   * The state this button is currently in
   */
  val stateProperty: ObservableObject<ButtonState> = ObservableObject(ButtonState.default)

  /**
   * The state this button is currently in
   */
  var state: ButtonState by stateProperty

  /**
   * Direct access to [ButtonState.selected]
   */
  val selectedProperty: ObservableBoolean = ObservableBoolean(false)
    .also { selectedProperty ->

      selectedProperty.bindBidirectional(
        stateProperty,
        { selected, oldConvertedValue -> oldConvertedValue.copy(selected = selected) },
        { newValueToConvert, _ -> newValueToConvert.selected }
      )
    }

  /**
   * Direct access to [ButtonState.selected]
   */
  var selected: Boolean by selectedProperty

  /**
   * Adds [action] to the list of actions called when this button is clicked.
   */
  fun action(action: (ButtonPressedEvent) -> Unit) {
    actions.add(action)
  }

  /**
   * Notify this button about being pressed
   */
  fun onDown() {
    if (state.disabled) {
      //can not press when disabled
      return
    }

    if (state.pressed) {
      //Already pressed
      return
    }

    if (state.enabled) {
      state = state.copy(pressed = true)
    }
  }

  /**
   * Notify this button about being released
   */
  fun onUp(chartSupport: ChartSupport, coordinates: Coordinates) {
    if (state.disabled) {
      //Can not release when disabled
      return
    }

    if (state.pressed.not()) {
      //Only fire action, if down has been on the same button
      return
    }

    //Up after down on same button, therefore
    val event = ButtonPressedEvent(chartSupport, coordinates, this)
    actions.forEach {
      it(event)
    }

    //Disable pressed
    state = state.copy(pressed = false)
  }

  /**
   * Updates the hover state
   */
  fun hover(newHover: Boolean) {
    if (newHover) {
      hover()
    } else {
      noHover()
    }
  }

  /**
   * Notify this button about the cursor/pointer moving over this button
   */
  fun hover() {
    if (state.hover) {
      return
    }

    state = state.copy(hover = true)
  }

  /**
   * Notify this button about the cursor/pointer leaving this button
   */
  fun noHover() {
    if (state.hover.not()) {
      return
    }

    state = state.copy(hover = false, pressed = false)
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    paintingContext.gc.translate(x, y)
    buttonPainter.paint(paintingContext, state, width, height)
  }

  /**
   * Toggles the selected state.
   *
   * Only useful to be called for toggle buttons
   */
  fun toggleSelected() {
    selectedProperty.toggle()
  }
}

/**
 * Describes a state a button may be in
 */
data class ButtonState(
  // the states are adopted from https://material.io/design/interaction/states.html#usage

  /**
   * If enabled, the button can be clicked
   * @see disabled
   */
  val enabled: Boolean = true,

  /**
   * A hover state communicates when a user has placed a cursor above an interactive button.
   */
  val hover: Boolean = false,

  /**
   * A focused state communicates when a user has highlighted a button, using an input method such as a keyboard or voice.
   */
  val focused: Boolean = false,

  /**
   * A selected state communicates a user choice.
   *
   * Only applicable for toggle buttons
   */
  val selected: Boolean = false,

  /**
   * A pressed state communicates a user tap.
   */
  val pressed: Boolean = false,

  ) {
  /**
   * A disabled state communicates a noninteractive button.
   * @see enabled
   */
  val disabled: Boolean = !enabled

  /**
   * Returns the simple button state
   */
  val simple: Simple
    get() {
      return when {
        pressed -> Simple.Pressed
        hover   -> Simple.Hover
        else    -> Simple.Default
      }
    }

  val simpleToggle: SimpleToggle
    get() {
      return when {
        pressed -> SimpleToggle.Pressed
        selected -> SimpleToggle.Selected
        hover -> SimpleToggle.Hover
        else -> SimpleToggle.Default
      }
    }

  companion object {
    /**
     * A default button state
     */
    val default: ButtonState = ButtonState()
    val disabled: ButtonState = ButtonState(enabled = false)
  }

  /**
   * Represents a simplified button state - can be used for when statements
   */
  enum class Simple {
    Default,
    Pressed,
    Hover
  }

  /**
   * Represents a simplified button state - can be used for when statements
   */
  enum class SimpleToggle {
    Default,
    Pressed,
    Hover,
    Selected
  }
}

/**
 * The priority of a toolbar button
 *
 * The priority determines when a button is visible.
 */
enum class ButtonPriority {
  /**
   * A button with this priority must always be visible
   */
  AlwaysVisible,

  /**
   * A button with this priority may be hidden if the available space does not suffice
   */
  MayBeHidden
}

/**
 * An event that is used when a button has been pressed
 */
data class ButtonPressedEvent(
  val chartSupport: ChartSupport,
  val coordinates: Coordinates,
  /**
   * The button for which the event occurred
   */
  val target: Button,
)

/**
 * Paints a button
 */
fun interface ButtonPainter {
  /**
   * Paints a button with the given [state]
   *
   * @param paintingContext the context
   * @param state the current state of the button
   * @param width the width of the button regarding its bounding box
   * @param height the height of the button regarding its bounding box
   */
  fun paint(paintingContext: LayerPaintingContext, state: ButtonState, width: @px Double, height: @px Double)
}

/**
 * Provides a button color depending on a button state
 */
fun interface ButtonColorProvider {
  operator fun invoke(buttonState: ButtonState): Color
}

/**
 * A default implementation of [ButtonColorProvider]
 */
class DefaultButtonColorProvider(
  private val disabledColor: Color,
  private val pressedColor: Color,
  private val hoverColor: Color,
  private val focusedColor: Color,
  private val defaultColor: Color
) : ButtonColorProvider {
  override fun invoke(buttonState: ButtonState): Color {
    return when {
      buttonState.disabled -> disabledColor
      buttonState.pressed  -> pressedColor
      buttonState.hover    -> hoverColor
      buttonState.focused  -> focusedColor
      else                 -> defaultColor
    }
  }
}

/**
 * Provides the same color for all button states
 */
class SingleButtonColorProvider(private val buttonColor: Color) : ButtonColorProvider {
  override fun invoke(buttonState: ButtonState): Color = buttonColor
}

/**
 * Turns this [ButtonPaintableProvider] into a [ButtonPainter]
 */
fun ButtonPaintableProvider.toButtonPainter(): ButtonPainter {
  return ButtonPainter { paintingContext, state, width, height ->
    this(state).paintInBoundingBox(paintingContext, width * 0.5, height * 0.5, Direction.Center)
  }
}

/**
 * A simple [ButtonPainter] for buttons with text
 */
open class DefaultButtonPainter(
  val text: TextKey?,
  private val buttonBackgroundColorProvider: ButtonColorProvider,
  private val buttonForegroundColorProvider: ButtonColorProvider
) : ButtonPainter {

  override fun paint(paintingContext: LayerPaintingContext, state: ButtonState, width: Double, height: Double) {
    val gc = paintingContext.gc

    val buttonRadius = 3.0 //TODO make radius configurable

    gc.fill(buttonBackgroundColorProvider(state))
    gc.fillRoundedRect(0.0, 0.0, width, height, buttonRadius)

    if (text == null) {
      return
    }
    val buttonLabel = text.resolve(paintingContext)
    if (buttonLabel.isBlank()) {
      return
    }

    val foregroundColor = buttonForegroundColorProvider(state)
    gc.fill(foregroundColor)
    gc.font(Theme.buttonFont())
    gc.fillText(buttonLabel, width * 0.5, height * 0.5, Direction.Center, 0.0, 0.0, width)
  }
}

/**
 * A [ButtonPainter] for primary buttons using the corporate design
 */
class DefaultPrimaryButtonPainter(text: TextKey?) : DefaultButtonPainter(
  text,
  Theme.primaryButtonBackgroundColors(),
  Theme.primaryButtonForegroundColors()
)

/**
 * A [ButtonPainter] for secondary buttons using the corporate design
 */
class DefaultSecondaryButtonPainter(text: TextKey?) : DefaultButtonPainter(
  text,
  Theme.secondaryButtonBackgroundColors(),
  Theme.secondaryButtonForegroundColors()
)

