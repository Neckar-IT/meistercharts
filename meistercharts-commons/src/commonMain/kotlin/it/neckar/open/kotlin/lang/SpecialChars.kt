package it.neckar.open.kotlin.lang

object SpecialChars {
  /**
   * Non-breaking space
   */
  const val nbsp: Char = Typography.nbsp

  /**
   * Narrow non-breaking space
   */
  const val nnbsp: Char = '\u202F'

  const val thinSpace: Char = '\u2009'
  const val hairSpace: Char = '\u200A'
  const val zeroWidthSpace: Char = '\u200B'
  const val enSpace: Char = '\u2002'
  const val emSpace: Char = '\u2003'
  const val figureSpace: Char = '\u2007'
  const val punctuationSpace: Char = '\u2008'
  const val mathematicalSpace: Char = '\u205F'
}

/**
 * Replaces all uncommon spaces with default spaces.
 * This method can be used to sanitize strings before they are printed to the console
 */
fun String.replaceUnusualSpaces(): String {
  val stringBuilder = StringBuilder(this.length)

  for (char in this) {
    when (char) {
      SpecialChars.nbsp,
      SpecialChars.nnbsp, // Narrow No-Break Space (NNBSP)
      SpecialChars.thinSpace, // Thin Space
      SpecialChars.hairSpace, // Hair Space
      SpecialChars.zeroWidthSpace, // Zero Width Space
      SpecialChars.enSpace, // En Space
      SpecialChars.emSpace, // Em Space
      SpecialChars.figureSpace, // Figure Space
      SpecialChars.punctuationSpace, // Punctuation Space
      SpecialChars.mathematicalSpace,  // Medium Mathematical Space (MMSP)
      -> stringBuilder.append(' ')

      else -> stringBuilder.append(char)
    }
  }

  return stringBuilder.toString()
}
