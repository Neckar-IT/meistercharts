package com.meistercharts.algorithms.layers

import it.neckar.open.observable.ObservableBoolean

/**
 * Holds an [Layer] delegate and paints it depending on value of visible property
 */
@Deprecated("Use the LayerVisibilityAdapter without state")
class LayerVisibilityAdapterWithState(
  delegate: Layer,
  /**
   * The visibility property. If set to true, the wrapper layer is painted and events are processed
   */
  val visibleProperty: ObservableBoolean = ObservableBoolean(true),
  /**
   * If set to true the events will be delegated, even if the layer is invisible
   */
  delegateEventsIfInvisible: Boolean = false
) : LayerVisibilityAdapter(delegate, {
  visibleProperty.get()
}, delegateEventsIfInvisible) {
  /**
   * If the layer is visible
   */
  override var visible: Boolean by visibleProperty

  override val description: String
    get() = "VisibilityAdapterWithState{${delegate.description}}"

  /**
   * Toggles the visibility
   */
  fun toggleVisibility() {
    visibleProperty.toggle()
  }

  override fun toString(): String {
    return "LayerVisibilityAdapterWithState($delegate)"
  }
}

/**
 * Wraps the layer into an [LayerVisibilityAdapter]
 */
fun Layer.visibleIf(visibleProperty: ObservableBoolean = ObservableBoolean(true), delegateEventsIfInvisible: Boolean = false): LayerVisibilityAdapterWithState {
  return LayerVisibilityAdapterWithState(this, visibleProperty, delegateEventsIfInvisible)
}


/**
 * Wraps the layer into an [LayerVisibilityAdapter]
 */
fun Layer.visible(initialVisibility: Boolean): LayerVisibilityAdapterWithState {
  return LayerVisibilityAdapterWithState(this).also {
    it.visible = initialVisibility
  }
}
