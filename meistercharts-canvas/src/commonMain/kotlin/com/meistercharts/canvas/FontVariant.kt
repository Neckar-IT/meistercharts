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
package com.meistercharts.canvas

/**
 * Selects a normal, or small-caps face from a font family
 *
 * [CSS font-variant](https://developer.mozilla.org/de/docs/Web/CSS/font-variant)
 *
 * Attention! small-caps may lead to unforeseen problems! Use with care.
 */
enum class FontVariant {
  /**
   * A normal font variant
   */
  Normal,

  /**
   * Attention! This may lead to unforeseen problems. Use with care
   */
  SmallCaps
}
