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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableSizeSeparate
import com.meistercharts.demo.section
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import com.meistercharts.model.Size
import com.meistercharts.charts.lizergy.planning.ModuleSize
import com.meistercharts.charts.lizergy.planning.PvModuleSampleModels
import com.meistercharts.charts.lizergy.planning.PvRoofPlanningGestalt
import com.meistercharts.charts.lizergy.planning.PvRoofPlanningModel
import com.meistercharts.charts.lizergy.planning.UnusableArea
import com.meistercharts.style.BoxStyle
import it.neckar.open.kotlin.lang.enumEntries
import kotlin.math.roundToInt

/**
 *
 */
class PvRoofPlanningDemoDescriptor : ChartingDemoDescriptor<() -> PvRoofPlanningModel> {
  override val name: String = "Photovoltaics Roof Planning"
  override val category: DemoCategory = DemoCategory.Lizergy

  override val predefinedConfigurations: List<PredefinedConfiguration<() -> PvRoofPlanningModel>> = listOf(
    PredefinedConfiguration({ PvModuleSampleModels.getSimpleGrid() }, "Simple Grid"),
    PredefinedConfiguration({ PvModuleSampleModels.getGridWithHoles() }, "Grid with holes"),
    PredefinedConfiguration({ PvModuleSampleModels.onlyUnusableArea() }, "Unusable Area"),
    PredefinedConfiguration({ PvModuleSampleModels.realistic() }, "Realistic"),
    PredefinedConfiguration({ PvModuleSampleModels.allTypes() }, "All"),
  )

  override fun createDemo(configuration: PredefinedConfiguration<() -> PvRoofPlanningModel>?): ChartingDemo {
    requireNotNull(configuration) { "Configuration required" }

    val model: PvRoofPlanningModel = configuration.payload()

    return ChartingDemo {
      meistercharts {

        val gestalt = PvRoofPlanningGestalt(PvRoofPlanningGestalt.Data(model))
        gestalt.configure(this)

        this.configure {
          layers.addText({ _, _ ->
            listOf(gestalt.pvRoofPlanningLayer.uiState.toString())
          }) {
            boxStyle = BoxStyle.gray
            anchorDirection = Direction.TopLeft
            anchorPointProvider = DirectionBasedBasePointProvider(Direction.TopLeft)
            font = FontDescriptorFragment.XS
          }
        }

        configurableEnum("Mode", gestalt.pvRoofPlanningLayer.style::mode, enumEntries())

        section("Roof")

        configurableSizeSeparate("roof (mm)", model::roofSize) {
          max = 15_000.0
        }

        configurableInsetsSeparate("Insets (mm)", model::suggestedRoofInsets) {
          max = 1_000.0
        }

        section("Modules")

        configurableDouble("Module longer (mm)", model.modulesSize.longer.toDouble()) {
          max = 2_000.0

          onChange {
            model.modulesSize = ModuleSize(it.roundToInt(), model.modulesSize.shorter)
            markAsDirty()
          }
        }
        configurableDouble("Module shorter (mm)", model.modulesSize.shorter.toDouble()) {
          max = 2_000.0

          onChange {
            model.modulesSize = ModuleSize(model.modulesSize.longer, it.roundToInt())
            markAsDirty()
          }
        }

        section("New Areas")

        declare {

          button("Add module area") {
            model.addModuleArea()
            markAsDirty()
          }

          button("Add unusable area") {
            model.addUnusableArea(UnusableArea(Coordinates.origin, Size(1500, 1200)))
            markAsDirty()
          }
        }
      }
    }
  }
}
