package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.layers.text.TextLayer
import com.meistercharts.algorithms.layers.toggleShortcut
import com.meistercharts.algorithms.layers.visible
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import com.meistercharts.model.Insets
import com.meistercharts.events.KeyCode
import com.meistercharts.events.KeyStroke
import com.meistercharts.events.ModifierCombination
import com.meistercharts.version.MeisterChartsVersion

/**
 * Adds the version number layer - permanently visible
 */
fun Layers.addVersionNumber(anchorDirection: Direction = Direction.TopRight): TextLayer {
  return versionNumberLayer(anchorDirection).also {
    addLayer(it)
  }
}

/**
 * Creates a version number layer
 */
private fun versionNumberLayer(anchorDirection: Direction = Direction.TopRight): TextLayer {
  return TextLayer({ _, _ -> listOf(MeisterChartsVersion.versionAsStringVerbose) }) {
    textColor = Color.gray
    font = FontDescriptorFragment(12.0)
    this.anchorDirection = anchorDirection
    anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
    margin = Insets.of(4.0)
  }.apply {
    type = LayerType.Notification
  }
}

/**
 * Adds a version number layer that is hidden by default.
 * The Shortcut Ctrl-Alt-V toggles the visibility
 */
fun Layers.addVersionNumberHidden(anchorDirection: Direction = Direction.TopRight) {
  val layer = versionNumberLayer(anchorDirection)
    .visible(false)
    .toggleShortcut(KeyStroke(KeyCode('V'), ModifierCombination.CtrlAlt))

  this.addLayer(layer)
}
