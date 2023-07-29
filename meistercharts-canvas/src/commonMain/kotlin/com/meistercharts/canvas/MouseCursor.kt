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
package com.meistercharts.canvas

/**
 * The supported mouse cursors
 */
enum class MouseCursor {
  Default,

  /**
   * A pointing hand. indicates that something can be clicked - for example a hyperlink
   */
  Hand,

  /**
   * An open hand
   */
  OpenHand,

  /**
   * A closed hand. Can be used when something has been "grapped" (e.g. when panning)
   */
  ClosedHand,
  CrossHair,

  /**
   * Used for text input
   */
  Text,

  /**
   * Indicates that the UI is busy at the moment
   */
  Busy,

  /**
   * Indicates a movement
   */
  Move,

  /**
   * No cursor is shown (if supported).
   * If a platform does not support this, [Default] cursor will be shown.
   */
  None,

  ResizeNorth,
  ResizeNorthEast,
  ResizeEast,
  ResizeSouthEast,
  ResizeSouth,
  ResizeSouthWest,
  ResizeWest,
  ResizeNorthWest,
  ResizeEastWest,
  ResizeNorthSouth,
}



