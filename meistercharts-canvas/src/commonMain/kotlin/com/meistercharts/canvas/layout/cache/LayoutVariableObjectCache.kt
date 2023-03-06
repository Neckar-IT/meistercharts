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
package com.meistercharts.canvas.layout.cache

/**
 * A cache for [LayoutVariable]s.
 * These will be reused for every layout pass.
 *
 * ATTENTION: This cache might contain more objects than requested!
 */
class LayoutVariableObjectCache<T : LayoutVariable>(
  /**
   * The factory that is used to create new elements.
   *
   * ATTENTION: The factory is only called once for each index.
   * The created objects are reused for each layout pass afterwards.
   */
  factory: () -> T
) : AbstractLayoutVariableObjectCache<T>(factory) {

  override fun reset() {
    //only reset the values - that list has the correct size
    //objectsStock might have additional objects - these are not used, therefore can be ignored safely
    values.forEachIndexed { index, _ ->
      values[index].reset()
    }
  }

}
