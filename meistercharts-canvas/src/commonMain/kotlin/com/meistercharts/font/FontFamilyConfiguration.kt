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
