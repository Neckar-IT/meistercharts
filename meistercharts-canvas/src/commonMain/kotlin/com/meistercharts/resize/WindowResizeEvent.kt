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

import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import it.neckar.open.unit.number.MayBeZero

/**
 * Is called when a resize has happened.
 * Contains both the old and new size of the content area and the window area.
 */
data class WindowResizeEvent(
  @Window val oldWindowSize: Size,
  @Window val newWindowSize: Size,

  @ContentArea @MayBeZero val oldContentAreaSize: Size,
  @ContentArea @MayBeZero val newContentAreaSize: Size,

  @Zoomed @MayBeZero val contentViewportMargin: Insets,
)
