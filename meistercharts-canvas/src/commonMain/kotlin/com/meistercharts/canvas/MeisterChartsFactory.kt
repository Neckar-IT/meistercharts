package com.meistercharts.canvas

/**
 * Contains platform dependent methods / factories.
 *
 * It is suggested to get the current factory using [MeisterChartsFactoryAccess.factory].
 *
 * It is necessary to
 * call `MeisterChartsPlatform.init()` first
 *
 */
interface MeisterChartsFactory {
  /**
   * Creates a new chart instance
   * @param description used for debugging purposes
   */
  fun createChart(chartSupport: ChartSupport, description: String): MeisterChart

  /**
   * The platform dependent canvas factory
   */
  val canvasFactory: CanvasFactory
}

/**
 * Offers a way to receive the chart factory
 */
object MeisterChartsFactoryAccess {
  /**
   * The current factory. Should be set once at startup - specific for each platform
   */
  var factory: MeisterChartsFactory? = null
}

/**
 * Returns an instance for the platform dependent chart factory.
 *
 * It is necessary to
 * call `MeisterChartsPlatform.init()` first
 */
fun meisterChartsFactory(): MeisterChartsFactory {
  return MeisterChartsFactoryAccess.factory ?: throw IllegalStateException("No meisterChartsFactory set - please call MeisterChartsPlatform.init()")
}
