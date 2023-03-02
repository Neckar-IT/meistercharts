package com.meistercharts.canvas

import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import it.neckar.open.observable.ObservableObject

/**
 * Contains the debug configuration.
 *
 * The debug configuration is created once for each [CanvasRenderingContext].
 */
class DebugConfiguration {
  /**
   * Contains the enabled debug featuresDemo
   */
  val enabledFeaturesProperty: ObservableObject<Set<DebugFeature>> = ObservableObject(emptySet())
  val enabledFeatures: Set<DebugFeature> by enabledFeaturesProperty

  /**
   * Returns the configuration for the given feature
   */
  operator fun get(feature: DebugFeature): Boolean {
    return enabledFeatures.contains(feature)
  }

  /**
   * Enables/disables all debug features
   */
  fun setAll(enabled: Boolean) {
    if (enabled) {
      this.enabledFeaturesProperty.value = DebugFeature.values().toSet()
    } else {
      this.enabledFeaturesProperty.value = emptySet()
    }
  }

  fun toggle() {
    setAll(enabledFeatures.isEmpty())
  }

  fun enable(debugFeature: DebugFeature) {
    set(debugFeature, true)
  }

  fun set(debugFeature: DebugFeature, enabled: Boolean) {
    if (enabled) {
      enabledFeaturesProperty.getAndSet {
        buildSet {
          addAll(it)
          add(debugFeature)
        }
      }
    } else {
      enabledFeaturesProperty.getAndSet {
        buildSet {
          addAll(it)
          remove(debugFeature)
        }
      }
    }
  }
}

enum class DebugFeature {
  /**
   * Shows the bounds
   */
  ShowBounds,

  /**
   * Shows anchors (usually for texts)
   */
  ShowAnchors,

  /**
   * Shows the values (e.g. for bar charts)
   */
  ShowValues,

  /**
   * Paints the text bounds for the original texts - not using the string shortener / max width
   */
  UnShortenedTexts,

  /**
   * Paints the max text width
   */
  ShowMaxTextWidth,

  /**
   * Paints overlapping texts (e.g. for labels)
   */
  OverlappingTexts,

  /**
   * Shows tiles debug information
   */
  TilesDebug,

  /**
   * Shows min and max values
   */
  ShowMinMax,

  /**
   * Shows the history gaps
   */
  HistoryGaps,

  /**
   * Show the control points for bezier curves
   */
  BezierControlPoints,

  /**
   * Show performance related information
   */
  PerformanceMeasurements,

  /**
   * Paint descriptions of incoming events on the canvas
   */
  LogEvents,

  ;

  /**
   * Returns true if the debug feature is enabled in the given painting context
   */
  fun enabled(paintingContext: LayerPaintingContext): Boolean {
    return enabled(paintingContext.debug)
  }

  /**
   * Returns true if this debug feature is enabled in the given debug configuration
   */
  fun enabled(debugConfiguration: DebugConfiguration): Boolean {
    return debugConfiguration[this]
  }
}
