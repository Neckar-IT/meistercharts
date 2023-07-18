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
package com.meistercharts.resize

import com.meistercharts.zoom.ZoomAndTranslationSupport

/**
 * The behavior when the window is resized
 *
 */
fun interface WindowResizeBehavior {
  /**
   * Is called when a resize (of the window or the content area) has happened.
   * Depending on the implementation of [com.meistercharts.canvas.ContentAreaSizingStrategy] either
   * the content area or the window area or both might have changed.
   */
  fun handleResize(zoomAndTranslationSupport: ZoomAndTranslationSupport, windowResizeEvent: WindowResizeEvent)
}

