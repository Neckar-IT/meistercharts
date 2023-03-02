package com.meistercharts.demo.descriptors.benchmark

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontFamily
import com.meistercharts.canvas.FontSize
import com.meistercharts.canvas.FontStyle
import com.meistercharts.canvas.FontVariant
import com.meistercharts.canvas.FontWeight
import com.meistercharts.canvas.saved
import com.meistercharts.model.Direction
import com.meistercharts.style.Palette.getChartColor

/**
 * Text/font related benchmarks
 */
object TextBenchmarks {
  /**
   * The text to be used for painting text
   */
  private const val sampleText = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

  /**
   * All font families to be tested
   */
  private val fontFamilies = listOf(
    FontFamily.Monospace,
    FontFamily.SansSerif,
    FontFamily.Serif
  )

  /**
   * All font sizes to be tested
   */
  private val fontSizes = listOf(
    FontSize.XS,
    FontSize.S,
    FontSize.Default,
    FontSize.L,
    FontSize.XL,
    FontSize.Max
  )

  /**
   * All font weights to be tested
   */
  private val fontWeights = listOf(
    FontWeight.Thin,
    FontWeight.ExtraLight,
    FontWeight.Light,
    FontWeight.Regular,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
    FontWeight.ExtraBold,
    FontWeight.Black
  )

  /**
   * All font styles to be tested
   */
  private val fontStyles = listOf(
    FontStyle.Italic,
    FontStyle.Normal,
    FontStyle.Oblique
  )

  /**
   * All font variants to be tested
   */
  private val fontVariants = listOf(
    FontVariant.Normal,
    FontVariant.SmallCaps
  )

  /**
   * The number of combinations of all font attributes
   */
  private val fontPermutations = fontFamilies.size * fontSizes.size * fontWeights.size * fontStyles.size * fontVariants.size

  val benchmarkOperations: List<BenchmarkOperation> = listOf(
    BenchmarkOperation("strokeText", 1_000, this::strokeText),
    BenchmarkOperation("strokeTextMaxWidth", 1_000, this::strokeTextMaxWidth),
    BenchmarkOperation("fillText", 100_000, this::fillText),
    BenchmarkOperation("fillTextMaxWidth", 10_000, this::fillTextMaxWidth),
    BenchmarkOperation("calculateTextWidth", fontPermutations, this::calculateTextWidth),
    BenchmarkOperation("font", fontPermutations, this::font),
    BenchmarkOperation("getFontMetrics", fontPermutations, this::getFontMetrics)
  )

  private fun iterFonts(callback: (FontDescriptorFragment) -> Unit) {
    for (fontFamily in fontFamilies) {
      for (fontSize in fontSizes) {
        for (fontWeight in fontWeights) {
          for (fontStyle in fontStyles) {
            for (fontVariant in fontVariants) {
              callback(FontDescriptorFragment(fontFamily, fontSize, fontWeight, fontStyle, fontVariant))
            }
          }
        }
      }
    }
  }

  private fun strokeText(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      paintingContext.gc.strokeStyle(getChartColor(7))
      paintingContext.gc.font(FontDescriptorFragment.XL)
      for (i in 0 until executionCount) {
        paintingContext.gc.strokeText(sampleText, 0.0, 0.0, Direction.CenterLeft, i * 0.1)
      }
    }
  }

  private fun strokeTextMaxWidth(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      paintingContext.gc.strokeStyle(getChartColor(8))
      paintingContext.gc.font(FontDescriptorFragment.XL)
      for (i in 0 until executionCount) {
        paintingContext.gc.strokeText(sampleText, 0.0, 0.0, Direction.CenterLeft, i * 0.1, i * 0.1, 100.0)
      }
    }
  }

  private fun fillText(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      paintingContext.gc.fillStyle(getChartColor(1))
      paintingContext.gc.font(FontDescriptorFragment.XL)
      for (i in 0 until executionCount) {
        paintingContext.gc.fillText(sampleText, 0.0, 0.0, Direction.CenterLeft, i * 0.01, i * 0.01)
      }
    }
  }

  private fun fillTextMaxWidth(paintingContext: LayerPaintingContext, executionCount: Int) {
    paintingContext.gc.saved {
      paintingContext.gc.fillStyle(getChartColor(2))
      paintingContext.gc.font(FontDescriptorFragment.XL)
      for (i in 0 until executionCount) {
        paintingContext.gc.fillText(sampleText, 0.0, 0.0, Direction.CenterLeft, i * 0.01, i * 0.1, 100.0)
      }
    }
  }

  private fun getFontMetrics(paintingContext: LayerPaintingContext, executionCount: Int) {
    require(executionCount == fontPermutations) { "$executionCount != $fontPermutations" }
    paintingContext.gc.saved {
      iterFonts {
        paintingContext.gc.font(it)
        paintingContext.gc.getFontMetrics()
      }
    }
  }

  private fun calculateTextWidth(paintingContext: LayerPaintingContext, executionCount: Int) {
    require(executionCount == fontPermutations) { "$executionCount != $fontPermutations" }
    paintingContext.gc.saved {
      iterFonts {
        paintingContext.gc.font(it)
        paintingContext.gc.calculateTextWidth(sampleText)
      }
    }
  }


  private fun font(paintingContext: LayerPaintingContext, executionCount: Int) {
    require(executionCount == fontPermutations) { "$executionCount != $fontPermutations" }
    paintingContext.gc.saved {
      iterFonts {
        paintingContext.gc.font(it)
      }
    }
  }
}
