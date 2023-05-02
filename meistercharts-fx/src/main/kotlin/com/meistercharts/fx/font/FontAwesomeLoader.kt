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
package com.meistercharts.fx.font

import it.neckar.open.resources.getResourceSafe
import javafx.scene.text.Font

/**
 * Helper class that loads the font awesome font
 *
 */
object FontAwesomeLoader {
  /**
   * The URL to the ttf font
   */
  val fontResource: String = this::class.java.getResourceSafe("/ttf/fa-regular-400.ttf").toExternalForm()

  /**
   * The font awesome font (16 px)
   */
  val font: Font = Font.loadFont(fontResource, 16.0)

  /**
   * Initializes the font awesome font
   */
  fun init() {
    //initialization is done in the fields
  }
}
