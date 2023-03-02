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
