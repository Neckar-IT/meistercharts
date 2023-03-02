package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import com.meistercharts.model.Size
import it.neckar.open.kotlin.lang.deleteFromStartUntilMaxSize

/**
 *
 */
class InitialResizeBehaviorDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Initial Resize Behavior"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          val windowSizes = mutableListOf<Size>()
          val contentAreaSizes = mutableListOf<Size>()

          chartSupport.rootChartState.windowSizeProperty.consumeImmediately {
            windowSizes.add(it)
            windowSizes.deleteFromStartUntilMaxSize(50)
          }
          chartSupport.rootChartState.contentAreaSizeProperty.consumeImmediately {
            contentAreaSizes.add(it)
            contentAreaSizes.deleteFromStartUntilMaxSize(50)
          }

          layers.addClearBackground()
          layers.addText({ _, _ ->
            val sizesAsString = windowSizes.map { it.format() }

            buildList {
              add("Window Sizes:")
              addAll(sizesAsString)
            }
          }) {
            font = FontDescriptorFragment.XS
            anchorPointProvider = DirectionBasedBasePointProvider(Direction.TopLeft)
            anchorDirection = Direction.TopLeft
          }

          layers.addText({ _, _ ->
            val sizesAsString = contentAreaSizes.map { it.format() }

            buildList {
              add("Content area Sizes:")
              addAll(sizesAsString)
            }
          }) {
            font = FontDescriptorFragment.XS
            anchorPointProvider = DirectionBasedBasePointProvider(Direction.TopRight)
            anchorDirection = Direction.TopRight
          }
        }
      }
    }
  }
}
