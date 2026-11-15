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
package com.meistercharts.fx

import it.neckar.open.annotations.Allocates
import it.neckar.open.annotations.AllocationCost
import it.neckar.open.collections.Cache
import it.neckar.open.collections.cache
import com.meistercharts.fx.toJavaFx
import javafx.scene.effect.DropShadow

/**
 * Cache for JavaFX shadow object
 */
object ShadowFxCache {
  /**
   * The tiles cache
   */
  internal val cache: Cache<Int, DropShadow> = cache("ShadowFxCache", 200)


  /**
   * Returns a (cached) shadow object
   */
  @Allocates(AllocationCost.Constant) //cache miss allocates - hit path is allocation-free (explicit get-then-store)
  fun shadow(blurRadius: Double, offsetX: Double, offsetY: Double, color: com.meistercharts.color.Color): DropShadow {
    val hashCode = (blurRadius.hashCode() + offsetX * 1000 + offsetY * 100000 + color.hashCode()).toInt()

    //Do *NOT* use #getOrStore: its capturing lambda would allocate on every call - even on cache hits
    val found = cache[hashCode]
    if (found != null) {
      return found
    }

    val created = DropShadow(blurRadius, offsetX, offsetY, color.toJavaFx())
    cache.store(hashCode, created)
    return created
  }
}
