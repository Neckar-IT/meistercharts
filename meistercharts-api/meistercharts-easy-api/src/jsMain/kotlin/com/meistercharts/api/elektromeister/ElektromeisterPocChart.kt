package com.meistercharts.api.elektromeister

import com.meistercharts.api.MeisterChartsApi
import com.meistercharts.api.toJs
import com.meistercharts.elektromeister.ElektromeisterPocGestalt
import com.meistercharts.js.MeisterchartJS
import it.neckar.elektromeister.rest.quote.ElektromeisterQuote
import it.neckar.elektromeister.rest.quote.createQuote
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.observable.ObservableObject


/**
 *
 */
@JsExport
class ElektromeisterPocChart internal constructor(
  /**
   * The gestalt that is configured
   */
  internal val gestalt: ElektromeisterPocGestalt,

  meisterChart: MeisterchartJS,
) : MeisterChartsApi<ElektromeisterPocChartConfig>(meisterChart) {

  private var configurationAlreadySet = false

  /**
   * Contains the "current" quote.
   * This property is updated whenever the model changes.
   */
  private var currentQuoteProperty: ObservableObject<ElektromeisterQuote> = ObservableObject(calculateQuote())
  private val currentQuote: ElektromeisterQuote? by currentQuoteProperty

  init {
    //Recalculate the quote - if necessary and update the current quote property
    gestalt.configuration.model.floorPlan.consumeAllProperties {
      logger.info("Floor plan changed: $it")
      val quote = calculateQuote()
      logger.info("Quote updated: $quote")
      currentQuoteProperty.value = quote
    }
  }

  /**
   * Calculates the quote for the current state.
   */
  private fun calculateQuote(): ElektromeisterQuote {
    val floorPlan = gestalt.configuration.model.floorPlan.toFloorPlan()
    return floorPlan.createQuote()
  }

  /**
   * Ensures that only one single model listener is registered.
   */
  private val modelListenerDisposeSupport = DisposeSupport(mode = DisposeSupport.Mode.MultiDispose)

  override fun setConfiguration(jsConfiguration: ElektromeisterPocChartConfig) {
    logger.debug("ElektromeisterPocChart.setConfiguration", jsConfiguration)

    logger.debug("setConfiguration called!")
    if (configurationAlreadySet) {
      logger.info("######## WARNING! Already set configuration! ########")
    }
    configurationAlreadySet = true

    //Unregister all listeners first
    modelListenerDisposeSupport.dispose()

    val onModelChangeCallback = jsConfiguration.onModelChanged
    logger.info("onModelChangeCallback: $onModelChangeCallback")

    //Register the callback
    currentQuoteProperty.consume {
      onModelChangeCallback?.modelChanged(it?.toJs() ?: return@consume)
    }.also {
      modelListenerDisposeSupport.onDispose(it)
    }

    markAsDirty()
  }

  /**
   * Returns the current quote.
   */
  fun getCurrentQuote(): ElektromeisterQuote {
    return currentQuoteProperty.value.toJs()
  }

  companion object {
    internal val logger: Logger = LoggerFactory.getLogger("com.meistercharts.api.bar.ElektromeisterPocChart")
  }
}

/**
 * Contains the configuration - from JS
 */
@JsExport
external interface ElektromeisterPocChartConfig {
  /**
   * The callback that is notified about model changes
   */
  val onModelChanged: ModelChangeListener?
}

/**
 * Is notified about changes to the model
 */
@JsExport
external interface ModelChangeListener {
  /**
   * Will be called if the model has changed
   */
  fun modelChanged(newModel: ElektromeisterQuote): Unit
}
