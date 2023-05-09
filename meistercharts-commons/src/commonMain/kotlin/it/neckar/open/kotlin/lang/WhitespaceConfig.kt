package it.neckar.open.kotlin.lang

/**
 * Configuration of the whitespaces
 */
enum class WhitespaceConfig(
  /**
   * Used for "normal" spaces
   */
  val space: Char,
  /**
   * Used for small spaces (e.g. between amount and unit)
   */
  val smallSpace: Char,
) {
  /**
   * Uses non breaking spaces
   */
  NonBreaking(SpecialChars.nbsp, SpecialChars.nnbsp),

  /**
   * Uses spaces
   */
  Spaces(' ', ' '),

  /**
   * Uses always the "large" nbsp
   */
  NonBreakingOnlyNbsp(SpecialChars.nbsp, SpecialChars.nbsp),
  ;


}
