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

import com.meistercharts.canvas.components.NativeComponentsSupport
import com.meistercharts.events.DefaultKeyEventBroker
import com.meistercharts.events.DefaultMouseEventBroker
import com.meistercharts.events.DefaultPointerEventBroker
import com.meistercharts.events.DefaultTouchEventBroker
import it.neckar.open.observable.ObservableObject

/**
 * Abstract base class for canvas
 */
abstract class AbstractCanvas(
  override val type: CanvasType
) : Canvas {

  override val mouseCursor: ObservableObject<MouseCursor> = ObservableObject(MouseCursor.Default)

  override val tooltip: ObservableObject<String?> = ObservableObject(null)

  override val mouseEvents: DefaultMouseEventBroker = DefaultMouseEventBroker()

  override val keyEvents: DefaultKeyEventBroker = DefaultKeyEventBroker()

  override val pointerEvents: DefaultPointerEventBroker = DefaultPointerEventBroker()

  override val touchEvents: DefaultTouchEventBroker = DefaultTouchEventBroker()

  override val nativeComponentsSupport: NativeComponentsSupport = NativeComponentsSupport()

  /**
   * Checks that this canvas is of type [CanvasType.OffScreen]
   */
  fun requireOffScreenCanvas() {
    require(type == CanvasType.OffScreen)
  }

  /**
   * Checks that this canvas is of type [CanvasType.Main]
   */
  fun requireMainCanvas() {
    require(type == CanvasType.Main)
  }
}
