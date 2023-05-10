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
package com.meistercharts.algorithms.layers

import it.neckar.open.provider.BooleanProvider

/**
 * Holds an [Layer] delegate and paints it depending on value of visible property
 */
open class LayerVisibilityAdapter<out T : Layer>(
  delegate: T,
  /**
   * The visibility condition. If it returns true, the layer is visible
   */
  val visibleCondition: () -> Boolean,
  /**
   * If set to true the events will be delegated, even if the layer is invisible
   */
  val delegateEventsIfInvisible: Boolean = false,
) : DelegatingLayer<T>(delegate = delegate,
  delegateEventsCondition = BooleanProvider {
    delegateEventsIfInvisible || visibleCondition()
  }
) {
  /**
   * If the layer is visible
   */
  @Deprecated("do not use?")
  open val visible: Boolean
    get() {
      return visibleCondition()
    }

  override val description: String
    get() = "VisibilityAdapter{${delegate.description}}"

  /**
   * Returns true if the events should be delegated at the moment, false otherwise
   */
  @Deprecated("no longer required")
  private fun delegateEvents() = delegateEventsIfInvisible || visible

  override fun layoutDelegate(paintingContext: LayerPaintingContext) {
    if (visibleCondition()) {
      super.layoutDelegate(paintingContext)
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    if (visibleCondition()) {
      super.paint(paintingContext)
    }
  }
}

/**
 * Wraps the layer and only shows it if the given condition returns true
 */
fun <T : Layer> T.visibleIf(delegateEventsIfInvisible: Boolean = false, visibleCondition: () -> Boolean): LayerVisibilityAdapter<T> {
  return LayerVisibilityAdapter(this, visibleCondition, delegateEventsIfInvisible)
}

/**
 * Only wraps the layer in a [LayerVisibilityAdapter] if the provided [visibleCondition] is not null
 */
fun Layer.visibleIf(delegateEventsIfInvisible: Boolean = false, visibleCondition: (() -> Boolean)?): Layer {
  if (visibleCondition == null) {
    return this
  }

  return visibleIf(delegateEventsIfInvisible, visibleCondition)
}

