/*
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

import com.meistercharts.algorithms.layers.slippymap.SlippyMapProvider
import com.meistercharts.tile.TileIndex
import it.neckar.open.http.Url

/**
 * A [SlippyMapProvider] whose [delegate] can be swapped at runtime - used by the slippy map demos to
 * switch the selected provider through a configurable control.
 */
internal class MutableSlippyMapProvider(var delegate: SlippyMapProvider) : SlippyMapProvider {
  override fun url(tileIndex: TileIndex, zoom: Int): Url {
    return delegate.url(tileIndex, zoom)
  }

  override val legalNotice: String?
    get() = delegate.legalNotice
}
