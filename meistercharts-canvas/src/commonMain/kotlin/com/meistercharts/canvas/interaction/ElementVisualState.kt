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
package com.meistercharts.canvas.interaction

/**
 * How one element appears while the user interacts with the chart.
 *
 * This is the single vocabulary between interaction handling and painting: interaction handling projects its state onto
 * this enum, painters map this enum onto colors and icons. A painter therefore never needs to know the interaction state
 * itself, and a new interaction state never forces a change in a painter.
 */
enum class ElementVisualState {
  /**
   * The user is not interacting with the element. Each painter decides what that looks like - from invisible to a
   * plain fill.
   */
  Normal,

  /**
   * The mouse is over the element, which is not selected.
   */
  Hover,

  /**
   * The element is selected and the mouse is elsewhere.
   */
  Selected,

  /**
   * The element is selected and the mouse is over it.
   */
  SelectedHovering,

  /**
   * The element is being dragged.
   */
  Dragging,

  /**
   * The mouse is over the delete button of the element: releasing deletes it.
   */
  DeleteArmed,

  /**
   * The mouse is over the rotate button of the element: releasing rotates it.
   */
  RotateArmed,
}
