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
package com.meistercharts.font

/**
 * Represents a font configuration consisting of a font family and a generic family.
 */
data class FontFamilyConfiguration(
  override val family: FontFamily? = null,
  override val genericFamily: GenericFamily = GenericFamily.SansSerif,
) : FontDescriptorFragment()

@Suppress("NOTHING_TO_INLINE")
inline fun FontFamily.withSansSerif(): FontFamilyConfiguration {
  return withGenericFamily(GenericFamily.SansSerif)
}

fun FontFamily.withGenericFamily(genericFamily: GenericFamily): FontFamilyConfiguration {
  return FontFamilyConfiguration(this, genericFamily)
}
