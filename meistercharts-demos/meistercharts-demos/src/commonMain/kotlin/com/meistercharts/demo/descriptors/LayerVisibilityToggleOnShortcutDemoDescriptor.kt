package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.TextLayer
import com.meistercharts.algorithms.layers.toggleShortcut
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.events.KeyCode
import com.meistercharts.events.KeyStroke
import com.meistercharts.events.ModifierCombination
import it.neckar.open.observable.ObservableBoolean

class LayerVisibilityToggleOnShortcutDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Toggle Visibility on shortcut"
  override val description: String = "Toggles the visibility of the text layer with Ctrl-Alt-V"
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val textLayerVisible = ObservableBoolean(true)

        configure {
          layers.addClearBackground()
          layers.addLayer(
            TextLayer({ _, _ -> listOf("Press Ctrl-Alt-V to toggle this text") })
              .visibleIf(textLayerVisible)
              .toggleShortcut(KeyStroke(KeyCode('V'), ModifierCombination.CtrlAlt))
          )
        }
      }
    }
  }
}
