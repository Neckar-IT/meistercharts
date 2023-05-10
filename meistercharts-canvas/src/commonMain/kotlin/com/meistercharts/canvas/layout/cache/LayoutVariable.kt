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
 * Implementing classes must only be used within a layout cache.
 * They will be reused for each layout (they have only vars).
 *
 * They should be resettet before each layout.
 */
interface LayoutVariable {
  /**
   * Resets all values to the defaults.
   *
   * This method should be called to ensure that no old values are used accidentally.
   * The painting code should work just fine without calling this method.
   */
  fun reset()
}
