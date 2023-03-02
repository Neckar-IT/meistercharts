package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import it.neckar.open.formatting.decimalFormat
import com.meistercharts.style.BoxStyle
import kotlin.time.DurationUnit
import kotlin.time.measureTime

/**
 */
class TranslatePerformanceDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Translate Performance"
  override val category: DemoCategory
    get() = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()
          layers.addLayer(MyTranslateLayer())
        }
      }
    }
  }
}

private class MyTranslateLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc


    measureTime {
      for (i in 0..10_000) {
        callSavedRecursive(gc, 7)
      }
    }.let {
      gc.resetTransform()
      gc.paintTextBox("Recursive saved (10_000 * 7) took: ${decimalFormat.format(it.toDouble(DurationUnit.MILLISECONDS))} ms", Direction.TopLeft, 10.0, 10.0, BoxStyle.none, Color.black)
    }

    measureTime {
      for (i in 0..20_000) {
        callSavedRecursive(gc, 5)
      }
    }.let {
      gc.resetTransform()
      gc.paintTextBox("Recursive saved (20_000 * 5) took: ${decimalFormat.format(it.toDouble(DurationUnit.MILLISECONDS))} ms", Direction.TopLeft, 30.0, 30.0, BoxStyle.none, Color.black)
    }

    measureTime {
      for (i in 0..100_000) {
        callSavedRecursive(gc, 1)
      }
    }.let {
      gc.resetTransform()
      gc.paintTextBox("Recursive saved (100_000 * 1) took: ${decimalFormat.format(it.toDouble(DurationUnit.MILLISECONDS))} ms", Direction.TopLeft, 50.0, 50.0, BoxStyle.none, Color.black)
    }
  }

  private fun callSavedRecursive(gc: CanvasRenderingContext, count: Int) {
    gc.saved {
      val newCount = count - 1
      if (newCount == 0) {
        return@saved
      }

      callSavedRecursive(gc, newCount)
    }
  }
}
