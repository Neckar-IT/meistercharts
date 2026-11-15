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
package com.meistercharts.fx.font

import it.neckar.open.resources.getResourceSafe
import javafx.scene.text.Font

/**
 * Helper class that loads the font awesome font
 *
 */
object FontAwesomeLoader {
  /**
   * The URL to the ttf fonts,
   * HOW TO UPDATE:
   * Go to https://fontawesome.com/start and scroll down to 'Other Ways to Use' and click on download.
   * Download the newest 'For The Web' version and add the ttf files (which you can find in the webfonts folder) in a new version folder under resources/tff/fontawesome/.
   * Then update the urls in the getResource safe calls as well.
   *
   * If you update to a new major version (e.g Font Awesome 7):
   * The require statements in the loadFont calls have to be changed.
   * The FontFamilies in internal/open/meistercharts/meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/font/FontFamily.kt have to be updated/changed too.
   * Look out for an 'Upgrade to Version...' guide on the font awesome website. Breaking changes will be explained there!
   * */
  val fontSolidResource: String = this::class.java.getResourceSafe("/ttf/fontawesome/6.4.2/fa-solid-900.ttf").toExternalForm()
  val fontRegularResource: String = this::class.java.getResourceSafe("/ttf/fontawesome/6.4.2/fa-regular-400.ttf").toExternalForm()
  val fontBrandsResource: String = this::class.java.getResourceSafe("/ttf/fontawesome/6.4.2/fa-brands-400.ttf").toExternalForm()

  /**
   * Only contains 39 glyphs
   */
  val fontCompatibilityResource: String = this::class.java.getResourceSafe("/ttf/fontawesome/6.4.2/fa-v4compatibility.ttf").toExternalForm()

  /**
   * The font awesome font (16 px)
   */
  val fontSolid: Font = Font.loadFont(fontSolidResource, 16.0).also {
    // JavaFX appends the style to the font family name
    require(it.family == "Font Awesome 6 Free Solid") { "Loaded fontSolid family [${it.family}] does not match expected [Font Awesome 6 Free Solid]" }
  }
  val fontRegular: Font = Font.loadFont(fontRegularResource, 16.0).also {
    // JavaFX appends the style to the font family name
    require(it.family == "Font Awesome 6 Free Regular") { "Loaded fontRegular family [${it.family}] does not match expected [Font Awesome 6 Free Regular]" }
  }
  val fontBrands: Font = Font.loadFont(fontBrandsResource, 16.0).also {
    // JavaFX appends the style to the font family name
    require(it.family == "Font Awesome 6 Brands Regular") { "Loaded fontBrands family [${it.family}] does not match expected [Font Awesome 6 Brands Regular]" }
  }

  /**
   * Initializes the font awesome font
   */
  fun loadSafe() {
    //initialization is done in the fields
  }
}
