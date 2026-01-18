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
package com.meistercharts.font

import it.neckar.open.unit.other.px
import kotlin.jvm.JvmOverloads

typealias FontDescriptorProvider = () -> FontDescriptor
typealias FontDescriptorFragmentProvider = () -> FontDescriptorFragment

/**
 * Describes a font that can be set to a graphics context
 */
class FontDescriptor(
  /**
   * The font family - if set.
   * If null or empty, the [genericFamily] is used.
   */
  override val families: List<FontFamily>? = null,
  override val size: FontSize = FontSize.Default,
  override val weight: FontWeight = FontWeight.Normal,
  override val style: FontStyle = FontStyle.Normal,
  override val variant: FontVariant = FontVariant.Normal,
  /**
   * The generic font family that is used if no font family is set.
   * The genric font family must always be set.
   */
  override val genericFamily: GenericFontFamily = GenericFontFamily.SansSerif,
) : FontDescriptorFragment() {

  constructor(
    fontFamilyConfiguration: FontFamilyConfiguration,
    size: FontSize = FontSize.Default,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
    variant: FontVariant = FontVariant.Normal,
  ) : this(fontFamilyConfiguration.families, size, weight, style, variant, fontFamilyConfiguration.genericFamily)

  companion object {
    val Default: FontDescriptor = FontDescriptor(size = FontSize.Default)
    val L: FontDescriptor = FontDescriptor(size = FontSize.L)
    val XL: FontDescriptor = FontDescriptor(size = FontSize.XL)

    operator fun invoke(
      family: FontFamily? = null,
      size: FontSize = FontSize.Default,
      weight: FontWeight = FontWeight.Normal,
      style: FontStyle = FontStyle.Normal,
      variant: FontVariant = FontVariant.Normal,
      genericFamily: GenericFontFamily = GenericFontFamily.SansSerif,

      ): FontDescriptor {

      val families: List<FontFamily>? = family?.let { listOf(it) }
      return FontDescriptor(families = families, size = size, weight = weight, style = style, variant = variant, genericFamily = genericFamily)
    }
  }

  override fun toString(): String {
    return "FontDescriptor(families=$families, size=$size, weight=$weight, style=$style, variant=$variant, genericFamily=$genericFamily)"
  }

  /**
   * returns a string formatted for usage in a css file.
   * @return String, eg: "oswald" or for multiple font families: "oswald","Open Sans". Includes the generic font.
   */
  fun toHtmlFontString(): String {
    if (families.isNullOrEmpty()) {
      return genericFamily.keyword
    }

    val fontPartLabel = families.map { """"${it.family}"""" }
    return (fontPartLabel + genericFamily.keyword).joinToString(",")
  }
}

/**
 * Applies the given fragment and returns a new font descriptor.
 *
 * The values of the provided [moreImportant] are used - if they exist.
 */
fun FontDescriptor.combineWith(moreImportant: FontDescriptorFragment?): FontDescriptor {
  if (moreImportant == null || moreImportant.isEmpty() || this == moreImportant) {
    return this
  }

  //Returns directly if it is already a font descriptor
  if (moreImportant is FontDescriptor) {
    return moreImportant
  }

  val combinedFamily = moreImportant.families ?: families
  val combinedSize = moreImportant.size ?: size
  val combinedWeight = moreImportant.weight ?: weight
  val combinedStyle = moreImportant.style ?: style
  val combinedVariant = moreImportant.variant ?: variant
  val combinedGenericFamily = moreImportant.genericFamily ?: genericFamily

  return FontDescriptor(combinedFamily, combinedSize, combinedWeight, combinedStyle, combinedVariant, combinedGenericFamily)
}

/**
 * Combines two font descriptor fragments.
 * Takes the values from the more important
 */
fun FontDescriptorFragment.combineWith(moreImportant: FontDescriptorFragment?): FontDescriptorFragment {
  if (moreImportant == null) {
    return this
  }

  //Returns directly if it is already a font descriptor
  if (moreImportant is FontDescriptor) {
    return moreImportant
  }

  val combinedFamily = moreImportant.families ?: families
  val combinedSize = moreImportant.size ?: size
  val combinedWeight = moreImportant.weight ?: weight
  val combinedStyle = moreImportant.style ?: style
  val combinedVariant = moreImportant.variant ?: variant
  val combinedGenericFamily = moreImportant.genericFamily ?: genericFamily

  return FontDescriptorFragment(combinedFamily, combinedSize, combinedWeight, combinedStyle, combinedVariant, combinedGenericFamily)
}

/**
 * Contains parts of a font
 */
open class FontDescriptorFragment @JvmOverloads constructor(
  open val families: List<FontFamily>? = null,
  open val size: FontSize? = null,
  open val weight: FontWeight? = null,
  open val style: FontStyle? = null,
  open val variant: FontVariant? = null,
  open val genericFamily: GenericFontFamily? = null,
) {

  constructor(@px size: Double) : this(size = FontSize(size))

  constructor(
    familyConfiguration: FontFamilyConfiguration,
    size: FontSize? = null,
    weight: FontWeight? = null,
    style: FontStyle? = null,
    variant: FontVariant? = null,
  ) : this(familyConfiguration.families, size, weight, style, variant, familyConfiguration.genericFamily)

  /**
   * Returns true if all properties are null
   */
  fun isEmpty(): Boolean {
    return families == null
      && size == null
      && weight == null
      && style == null
      && variant == null
      && genericFamily == null
  }

  /**
   * Fills the missing values with values from [FontDescriptor.Default]
   */
  fun withDefaultValues(): FontDescriptor {
    return FontDescriptor.Default.combineWith(this)
  }

  /**
   * Creates a copy of this [FontDescriptorFragment] with the given [families]. Can contain multiple [FontFamily].
   */
  fun withFamilies(families: List<FontFamily>?): FontDescriptorFragment {
    return FontDescriptorFragment(families, size, weight, style, variant, genericFamily)
  }

  /**
   * Creates a copy of this [FontDescriptorFragment] with the given [family]
   */
  fun withFamily(family: FontFamily): FontDescriptorFragment {
    return FontDescriptorFragment(listOf(family), size, weight, style, variant, genericFamily)
  }

  /**
   * Creates a copy of this [FontDescriptorFragment] with the given [size]
   */
  fun withSize(size: FontSize): FontDescriptorFragment {
    return FontDescriptorFragment(families, size, weight, style, variant, genericFamily)
  }

  /**
   * Creates a copy of this [FontDescriptorFragment] with the given [weight]
   */
  fun withWeight(weight: FontWeight): FontDescriptorFragment {
    return FontDescriptorFragment(families, size, weight, style, variant, genericFamily)
  }

  /**
   * Creates a copy of this [FontDescriptorFragment] with the given [style]
   */
  fun withStyle(style: FontStyle): FontDescriptorFragment {
    return FontDescriptorFragment(families, size, weight, style, variant, genericFamily)
  }

  /**
   * Creates a copy of this [FontDescriptorFragment] with the given [variant]
   */
  fun withVariant(variant: FontVariant): FontDescriptorFragment {
    return FontDescriptorFragment(families, size, weight, style, variant, genericFamily)
  }

  /**
   * Creates a copy of this [FontDescriptorFragment] with the given [genericFamily]
   */
  fun withGenericFamily(genericFamily: GenericFontFamily): FontDescriptorFragment {
    return FontDescriptorFragment(families, size, weight, style, variant, genericFamily)
  }

  companion object {
    /**
     * Creates a new instance using a single [FontFamily] and the given parameters.
     */
    operator fun invoke(
      family: FontFamily? = null,
      size: FontSize? = null,
      weight: FontWeight? = null,
      style: FontStyle? = null,
      variant: FontVariant? = null,
      genericFamily: GenericFontFamily? = null,
    ): FontDescriptorFragment {

      val families: List<FontFamily>? = family?.let { listOf(it) }
      return FontDescriptorFragment(families, size, weight, style, variant, genericFamily)
    }


    /**
     * An empty font descriptor fragment
     */
    val empty: FontDescriptorFragment = FontDescriptorFragment()

    val XXS: FontDescriptorFragment = FontDescriptorFragment(size = FontSize.XXS)
    val XS: FontDescriptorFragment = FontDescriptorFragment(size = FontSize.XS)
    val S: FontDescriptorFragment = FontDescriptorFragment(size = FontSize.S)
    val DefaultSize: FontDescriptorFragment = FontDescriptorFragment(size = FontSize.Default)
    val L: FontDescriptorFragment = FontDescriptorFragment(size = FontSize.L)
    val XL: FontDescriptorFragment = FontDescriptorFragment(size = FontSize.XL)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is FontDescriptorFragment) return false

    if (families != other.families) return false
    if (size != other.size) return false
    if (weight != other.weight) return false
    if (style != other.style) return false
    if (variant != other.variant) return false
    if (genericFamily != other.genericFamily) return false

    return true
  }

  override fun hashCode(): Int {
    var result = families?.hashCode() ?: 0
    result = 31 * result + (size?.hashCode() ?: 0)
    result = 31 * result + (weight?.hashCode() ?: 0)
    result = 31 * result + (style?.hashCode() ?: 0)
    result = 31 * result + (variant?.hashCode() ?: 0)
    result = 31 * result + (genericFamily?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "FontDescriptorFragment($families, ${size?.size}, ${weight?.weight}, $style, $variant, $genericFamily)"
  }
}

/**
 * If the font family is (probably)
 * * the unconfigured default font of the browser
 * * a serif font
 *
 * This will return a new instance with the family set to null.
 * Any FontDescriptorFragment with exactly one FontFamily which contains "Serif" will be set to null!
 */
fun FontDescriptorFragment.eraseSoleSerifFamily(): FontDescriptorFragment {
  if (hasSoleSerifFamily()) {
    return withFamilies(null)
  }
  return this
}

/**
 * Returns true if this font descriptor fragment has a single family and that family is a serif family
 */
fun FontDescriptorFragment.hasSoleSerifFamily(): Boolean {
  return families?.size == 1
    && families?.first()?.isProbablyDefaultSerifFamily() == true
}

