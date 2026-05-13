/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.open.text

import java.util.LinkedList
import java.util.Locale
import java.util.regex.Pattern

/**
 * Transforms words to singular, plural, humanized (human readable), underscore, camel case, or ordinal form. This is inspired by
 * the [Inflector](http://api.rubyonrails.org/classes/Inflector.html) class in [Ruby on Rails](http://www.rubyonrails.org), which is distributed under the [Rails license](http://wiki.rubyonrails.org/rails/pages/License).
 */
class Inflector {

  internal class Rule(
    internal val expression: String,
    replacement: String?
  ) {
    internal val expressionPattern: Pattern = Pattern.compile(this.expression, Pattern.CASE_INSENSITIVE)
    internal val replacement: String = replacement ?: ""

    /**
     * Apply the rule against the input string, returning the modified string or null if the rule didn't apply (and no
     * modifications were made)
     *
     * @param input the input string
     * @return the modified string if this rule applied, or null if the input was not modified by this rule
     */
    fun apply(input: String): String? {
      val matcher = this.expressionPattern.matcher(input)
      if (!matcher.find()) {
        return null
      }
      return matcher.replaceAll(this.replacement)
    }

    override fun hashCode(): Int {
      return expression.hashCode()
    }

    override fun equals(other: Any?): Boolean {
      if (other === this) {
        return true
      }
      if (other != null && other.javaClass == this.javaClass) {
        val that = other as Rule
        if (this.expression.equals(that.expression, ignoreCase = true)) {
          return true
        }
      }
      return false
    }

    override fun toString(): String {
      return "$expression, $replacement"
    }
  }

  private val plurals = LinkedList<Rule>()
  private val singulars = LinkedList<Rule>()
  /**
   * Get the set of words that are not processed by the Inflector. The resulting map is directly modifiable.
   *
   * @return the set of uncountable words
   */
  /**
   * The lowercase words that are to be excluded and not processed. This map can be modified by the users via
   * [.getUncountables].
   */
  val uncountables: MutableSet<String?> = HashSet<String?>()

  constructor() {
    initialize()
  }

  internal constructor(original: Inflector) {
    this.plurals.addAll(original.plurals)
    this.singulars.addAll(original.singulars)
    this.uncountables.addAll(original.uncountables)
  }

  // ------------------------------------------------------------------------------------------------
  // Usage functions
  // ------------------------------------------------------------------------------------------------
  /**
   * Returns the plural form of the word in the string.
   *
   *
   * Examples:
   *
   * <pre>
   * inflector.pluralize(&quot;post&quot;)               #=&gt; &quot;posts&quot;
   * inflector.pluralize(&quot;octopus&quot;)            #=&gt; &quot;octopi&quot;
   * inflector.pluralize(&quot;sheep&quot;)              #=&gt; &quot;sheep&quot;
   * inflector.pluralize(&quot;words&quot;)              #=&gt; &quot;words&quot;
   * inflector.pluralize(&quot;the blue mailman&quot;)   #=&gt; &quot;the blue mailmen&quot;
   * inflector.pluralize(&quot;CamelOctopus&quot;)       #=&gt; &quot;CamelOctopi&quot;
  </pre> *
   *
   *
   *
   *
   * Note that if the [Object.toString] is called on the supplied object, so this method works for non-strings, too.
   *
   *
   * @param word the word that is to be pluralized.
   * @return the pluralized form of the word, or the word itself if it could not be pluralized
   *
   * @see .singularize
   */
  fun pluralize(word: Any?): String? {
    if (word == null) {
      return null
    }
    val wordStr = word.toString().trim { it <= ' ' }
    if (wordStr.length == 0) {
      return wordStr
    }
    if (isUncountable(wordStr)) {
      return wordStr
    }
    for (rule in this.plurals) {
      val result = rule.apply(wordStr)
      if (result != null) {
        return result
      }
    }
    return wordStr
  }

  fun pluralize(
    word: Any?,
    count: Int
  ): String? {
    if (word == null) {
      return null
    }
    if (count == 1 || count == -1) {
      return word.toString()
    }
    return pluralize(word)
  }

  /**
   * Returns the singular form of the word in the string.
   *
   *
   * Examples:
   *
   * <pre>
   * inflector.singularize(&quot;posts&quot;)             #=&gt; &quot;post&quot;
   * inflector.singularize(&quot;octopi&quot;)            #=&gt; &quot;octopus&quot;
   * inflector.singularize(&quot;sheep&quot;)             #=&gt; &quot;sheep&quot;
   * inflector.singularize(&quot;words&quot;)             #=&gt; &quot;word&quot;
   * inflector.singularize(&quot;the blue mailmen&quot;)  #=&gt; &quot;the blue mailman&quot;
   * inflector.singularize(&quot;CamelOctopi&quot;)       #=&gt; &quot;CamelOctopus&quot;
  </pre> *
   *
   *
   *
   *
   * Note that if the [Object.toString] is called on the supplied object, so this method works for non-strings, too.
   *
   *
   * @param word the word that is to be pluralized.
   * @return the pluralized form of the word, or the word itself if it could not be pluralized
   *
   * @see .pluralize
   */
  fun singularize(word: String): String {
    val wordStr = word.trim { it <= ' ' }
    if (wordStr.isEmpty()) {
      return wordStr
    }
    if (isUncountable(wordStr)) {
      return wordStr
    }
    for (rule in this.singulars) {
      val result = rule.apply(wordStr)
      if (result != null) {
        return result
      }
    }
    return wordStr
  }

  /**
   * Converts strings to lowerCamelCase. This method will also use any extra delimiter characters to identify word boundaries.
   *
   *
   * Examples:
   *
   * <pre>
   * inflector.lowerCamelCase(&quot;active_record&quot;)       #=&gt; &quot;activeRecord&quot;
   * inflector.lowerCamelCase(&quot;first_name&quot;)          #=&gt; &quot;firstName&quot;
   * inflector.lowerCamelCase(&quot;name&quot;)                #=&gt; &quot;name&quot;
   * inflector.lowerCamelCase(&quot;the-first_name&quot;,'-')  #=&gt; &quot;theFirstName&quot;
  </pre> *
   *
   *
   *
   * @param lowerCaseAndUnderscoredWord the word that is to be converted to camel case
   * @param delimiterChars              optional characters that are used to delimit word boundaries
   * @return the lower camel case version of the word
   *
   * @see .underscore
   * @see .camelCase
   * @see .upperCamelCase
   */
  fun lowerCamelCase(
    lowerCaseAndUnderscoredWord: String?,
    vararg delimiterChars: Char
  ): String? {
    return camelCase(lowerCaseAndUnderscoredWord, false, *delimiterChars)
  }

  /**
   * Converts strings to UpperCamelCase. This method will also use any extra delimiter characters to identify word boundaries.
   *
   *
   * Examples:
   *
   * <pre>
   * inflector.upperCamelCase(&quot;active_record&quot;)       #=&gt; &quot;SctiveRecord&quot;
   * inflector.upperCamelCase(&quot;first_name&quot;)          #=&gt; &quot;FirstName&quot;
   * inflector.upperCamelCase(&quot;name&quot;)                #=&gt; &quot;Name&quot;
   * inflector.lowerCamelCase(&quot;the-first_name&quot;,'-')  #=&gt; &quot;TheFirstName&quot;
  </pre> *
   *
   *
   *
   * @param lowerCaseAndUnderscoredWord the word that is to be converted to camel case
   * @param delimiterChars              optional characters that are used to delimit word boundaries
   * @return the upper camel case version of the word
   *
   * @see .underscore
   * @see .camelCase
   * @see .lowerCamelCase
   */
  fun upperCamelCase(
    lowerCaseAndUnderscoredWord: String?,
    vararg delimiterChars: Char
  ): String? {
    return camelCase(lowerCaseAndUnderscoredWord, true, *delimiterChars)
  }

  /**
   * By default, this method converts strings to UpperCamelCase. If the `uppercaseFirstLetter` argument to false,
   * then this method produces lowerCamelCase. This method will also use any extra delimiter characters to identify word
   * boundaries.
   *
   *
   * Examples:
   *
   * <pre>
   * inflector.camelCase(&quot;active_record&quot;,false)    #=&gt; &quot;activeRecord&quot;
   * inflector.camelCase(&quot;active_record&quot;,true)     #=&gt; &quot;ActiveRecord&quot;
   * inflector.camelCase(&quot;first_name&quot;,false)       #=&gt; &quot;firstName&quot;
   * inflector.camelCase(&quot;first_name&quot;,true)        #=&gt; &quot;FirstName&quot;
   * inflector.camelCase(&quot;name&quot;,false)             #=&gt; &quot;name&quot;
   * inflector.camelCase(&quot;name&quot;,true)              #=&gt; &quot;Name&quot;
  </pre> *
   *
   *
   *
   * @param lowerCaseAndUnderscoredWord the word that is to be converted to camel case
   * @param uppercaseFirstLetter        true if the first character is to be uppercased, or false if the first character is to be
   * lowercased
   * @param delimiterChars              optional characters that are used to delimit word boundaries
   * @return the camel case version of the word
   *
   * @see .underscore
   * @see .upperCamelCase
   * @see .lowerCamelCase
   */
  fun camelCase(
    lowerCaseAndUnderscoredWord: String?,
    uppercaseFirstLetter: Boolean,
    vararg delimiterChars: Char
  ): String? {
    var lowerCaseAndUnderscoredWord = lowerCaseAndUnderscoredWord
    if (lowerCaseAndUnderscoredWord == null) {
      return null
    }
    lowerCaseAndUnderscoredWord = lowerCaseAndUnderscoredWord.trim { it <= ' ' }
    if (lowerCaseAndUnderscoredWord.length == 0) {
      return ""
    }
    if (uppercaseFirstLetter) {
      var result: String = lowerCaseAndUnderscoredWord
      // Replace any extra delimiters with underscores (before the underscores are converted in the next step)...
      for (delimiterChar in delimiterChars) {
        result = result.replace(delimiterChar, '_')
      }

      // Change the case at the beginning at after each underscore ...
      return Companion.replaceAllWithUppercase(result, "(^|_)(.)", 2)
    }
    if (lowerCaseAndUnderscoredWord.length < 2) {
      return lowerCaseAndUnderscoredWord
    }
    val upperCamelCased = requireNotNull(camelCase(lowerCaseAndUnderscoredWord, true, *delimiterChars)) {
      "Recursive camelCase returned null for non-null input"
    }
    return "" + lowerCaseAndUnderscoredWord.get(0).lowercaseChar() + upperCamelCased.substring(1)
  }

  /**
   * Makes an underscored form from the expression in the string (the reverse of the [ camelCase][.camelCase] method. Also changes any characters that match the supplied delimiters into underscore.
   *
   *
   * Examples:
   *
   * <pre>
   * inflector.underscore(&quot;activeRecord&quot;)     #=&gt; &quot;active_record&quot;
   * inflector.underscore(&quot;ActiveRecord&quot;)     #=&gt; &quot;active_record&quot;
   * inflector.underscore(&quot;firstName&quot;)        #=&gt; &quot;first_name&quot;
   * inflector.underscore(&quot;FirstName&quot;)        #=&gt; &quot;first_name&quot;
   * inflector.underscore(&quot;name&quot;)             #=&gt; &quot;name&quot;
   * inflector.underscore(&quot;The.firstName&quot;)    #=&gt; &quot;the_first_name&quot;
  </pre> *
   *
   *
   *
   * @param camelCaseWord  the camel-cased word that is to be converted;
   * @param delimiterChars optional characters that are used to delimit word boundaries (beyond capitalization)
   * @return a lower-cased version of the input, with separate words delimited by the underscore character.
   */
  fun underscore(
    camelCaseWord: String?,
    vararg delimiterChars: Char
  ): String? {
    if (camelCaseWord == null) {
      return null
    }
    var result = camelCaseWord.trim { it <= ' ' }
    if (result.isEmpty()) {
      return ""
    }
    result = result.replace("([A-Z]+)([A-Z][a-z])".toRegex(), "$1_$2")
    result = result.replace("([a-z\\d])([A-Z])".toRegex(), "$1_$2")
    result = result.replace('-', '_')
    if (delimiterChars != null) {
      for (delimiterChar in delimiterChars) {
        result = result.replace(delimiterChar, '_')
      }
    }
    return result.lowercase(Locale.getDefault())
  }

  /**
   * Returns a copy of the input with the first character converted to uppercase and the remainder to lowercase.
   *
   * @param words the word to be capitalized
   * @return the string with the first character capitalized and the remaining characters lowercased
   */
  fun capitalize(words: String?): String? {
    if (words == null) {
      return null
    }
    val result = words.trim { it <= ' ' }
    if (result.isEmpty()) {
      return ""
    }
    if (result.length == 1) {
      return result.uppercase(Locale.getDefault())
    }
    return "" + result.get(0).uppercaseChar() + result.substring(1).lowercase(Locale.getDefault())
  }

  /**
   * Capitalizes the first word and turns underscores into spaces and strips trailing "_id" and any supplied removable tokens.
   * Like [.titleCase], this is meant for creating pretty output.
   *
   *
   * Examples:
   *
   * <pre>
   * inflector.humanize(&quot;employee_salary&quot;)       #=&gt; &quot;Employee salary&quot;
   * inflector.humanize(&quot;author_id&quot;)             #=&gt; &quot;Author&quot;
  </pre> *
   *
   *
   *
   * @param lowerCaseAndUnderscoredWords the input to be humanized
   * @param removableTokens              optional array of tokens that are to be removed
   * @return the humanized string
   *
   * @see .titleCase
   */
  fun humanize(
    lowerCaseAndUnderscoredWords: String?,
    vararg removableTokens: String
  ): String? {
    if (lowerCaseAndUnderscoredWords == null) {
      return null
    }
    var result = lowerCaseAndUnderscoredWords.trim { it <= ' ' }
    if (result.length == 0) {
      return ""
    }
    // Remove a trailing "_id" token
    result = result.replace("_id$".toRegex(), "")
    // Remove all of the tokens that should be removed
    if (removableTokens != null) {
      for (removableToken in removableTokens) {
        result = result.replace(removableToken.toRegex(), "")
      }
    }
    result = result.replace("_+".toRegex(), " ") // replace all adjacent underscores with a single space
    return capitalize(result)
  }

  /**
   * Capitalizes all the words and replaces some characters in the string to create a nicer looking title. Underscores are
   * changed to spaces, a trailing "_id" is removed, and any of the supplied tokens are removed. Like
   * [.humanize], this is meant for creating pretty output.
   *
   *
   * Examples:
   *
   * <pre>
   * inflector.titleCase(&quot;man from the boondocks&quot;)       #=&gt; &quot;Man From The Boondocks&quot;
   * inflector.titleCase(&quot;x-men: the last stand&quot;)        #=&gt; &quot;X Men: The Last Stand&quot;
  </pre> *
   *
   *
   *
   * @param words           the input to be turned into title case
   * @param removableTokens optional array of tokens that are to be removed
   * @return the title-case version of the supplied words
   */
  fun titleCase(
    words: String?,
    vararg removableTokens: String
  ): String {
    val humanized = requireNotNull(humanize(words, *removableTokens)) {
      "humanize returned null in titleCase for input '$words'"
    }
    return Companion.replaceAllWithUppercase(humanized, "\\b([a-z])", 1) // change first char of each word to uppercase
  }

  /**
   * Turns a non-negative number into an ordinal string used to denote the position in an ordered sequence, such as 1st, 2nd,
   * 3rd, 4th.
   *
   * @param number the non-negative number
   * @return the string with the number and ordinal suffix
   */
  fun ordinalize(number: Int): String {
    var remainder = number % 100
    val numberStr = number.toString()
    if (11 <= number && number <= 13) {
      return numberStr + "th"
    }
    remainder = number % 10
    if (remainder == 1) {
      return numberStr + "st"
    }
    if (remainder == 2) {
      return numberStr + "nd"
    }
    if (remainder == 3) {
      return numberStr + "rd"
    }
    return numberStr + "th"
  }

  // ------------------------------------------------------------------------------------------------
  // Management methods
  // ------------------------------------------------------------------------------------------------
  /**
   * Determine whether the supplied word is considered uncountable by the [pluralize][.pluralize] and
   * [singularize][.singularize] methods.
   *
   * @param word the word
   * @return true if the plural and singular forms of the word are the same
   */
  fun isUncountable(word: String?): Boolean {
    if (word == null) {
      return false
    }
    val trimmedLower = word.trim { it <= ' ' }.lowercase(Locale.getDefault())
    return this.uncountables.contains(trimmedLower)
  }

  fun addPluralize(
    rule: String,
    replacement: String?
  ) {
    val pluralizeRule: Rule = Inflector.Rule(rule, replacement)
    this.plurals.addFirst(pluralizeRule)
  }

  fun addSingularize(
    rule: String,
    replacement: String?
  ) {
    val singularizeRule: Rule = Inflector.Rule(rule, replacement)
    this.singulars.addFirst(singularizeRule)
  }

  fun addIrregular(
    singular: String,
    plural: String
  ) {
    require(singular.isNotEmpty()) { "singular must not be empty" }
    require(plural.isNotEmpty()) { "plural must not be empty" }

    val singularRemainder = if (singular.length > 1) singular.substring(1) else ""
    val pluralRemainder = if (plural.length > 1) plural.substring(1) else ""
    addPluralize("(" + singular[0] + ")" + singularRemainder + "$", "$1$pluralRemainder")
    addSingularize("(" + plural[0] + ")" + pluralRemainder + "$", "$1$singularRemainder")
  }

  fun addUncountable(vararg words: String?) {
    if (words.isEmpty()) {
      return
    }
    for (word in words) {
      if (word != null) {
        uncountables.add(word.trim { it <= ' ' }.lowercase(Locale.getDefault()))
      }
    }
  }

  /**
   * Completely remove all rules within this inflector.
   */
  fun clear() {
    this.uncountables.clear()
    this.plurals.clear()
    this.singulars.clear()
  }

  private fun initialize() {
    val inflect = this
    inflect.addPluralize("$", "s")
    inflect.addPluralize("s$", "s")
    inflect.addPluralize("(ax|test)is$", "$1es")
    inflect.addPluralize("(octop|vir)us$", "$1i")
    inflect.addPluralize("(octop|vir)i$", "$1i") // already plural
    inflect.addPluralize("(alias|status)$", "$1es")
    inflect.addPluralize("(bu)s$", "$1ses")
    inflect.addPluralize("(buffal|tomat)o$", "$1oes")
    inflect.addPluralize("([ti])um$", "$1a")
    inflect.addPluralize("([ti])a$", "$1a") // already plural
    inflect.addPluralize("sis$", "ses")
    inflect.addPluralize("(?:([^f])fe|([lr])f)$", "$1$2ves")
    inflect.addPluralize("(hive)$", "$1s")
    inflect.addPluralize("([^aeiouy]|qu)y$", "$1ies")
    inflect.addPluralize("(x|ch|ss|sh)$", "$1es")
    inflect.addPluralize("(matr|vert|ind)ix|ex$", "$1ices")
    inflect.addPluralize("([m|l])ouse$", "$1ice")
    inflect.addPluralize("([m|l])ice$", "$1ice")
    inflect.addPluralize("^(ox)$", "$1en")
    inflect.addPluralize("(quiz)$", "$1zes")
    // Need to check for the following words that are already pluralized:
    inflect.addPluralize("(people|men|children|sexes|moves|stadiums)$", "$1") // irregulars
    inflect.addPluralize("(oxen|octopi|viri|aliases|quizzes)$", "$1") // special rules

    inflect.addSingularize("s$", "")
    inflect.addSingularize("(s|si|u)s$", "$1s") // '-us' and '-ss' are already singular
    inflect.addSingularize("(n)ews$", "$1ews")
    inflect.addSingularize("([ti])a$", "$1um")
    inflect.addSingularize("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1$2sis")
    inflect.addSingularize("(^analy)ses$", "$1sis")
    inflect.addSingularize("(^analy)sis$", "$1sis") // already singular, but ends in 's'
    inflect.addSingularize("([^f])ves$", "$1fe")
    inflect.addSingularize("(hive)s$", "$1")
    inflect.addSingularize("(tive)s$", "$1")
    inflect.addSingularize("([lr])ves$", "$1f")
    inflect.addSingularize("([^aeiouy]|qu)ies$", "$1y")
    inflect.addSingularize("(s)eries$", "$1eries")
    inflect.addSingularize("(m)ovies$", "$1ovie")
    inflect.addSingularize("(x|ch|ss|sh)es$", "$1")
    inflect.addSingularize("([m|l])ice$", "$1ouse")
    inflect.addSingularize("(bus)es$", "$1")
    inflect.addSingularize("(o)es$", "$1")
    inflect.addSingularize("(shoe)s$", "$1")
    inflect.addSingularize("(cris|ax|test)is$", "$1is") // already singular, but ends in 's'
    inflect.addSingularize("(cris|ax|test)es$", "$1is")
    inflect.addSingularize("(octop|vir)i$", "$1us")
    inflect.addSingularize("(octop|vir)us$", "$1us") // already singular, but ends in 's'
    inflect.addSingularize("(alias|status)es$", "$1")
    inflect.addSingularize("(alias|status)$", "$1") // already singular, but ends in 's'
    inflect.addSingularize("^(ox)en", "$1")
    inflect.addSingularize("(vert|ind)ices$", "$1ex")
    inflect.addSingularize("(matr)ices$", "$1ix")
    inflect.addSingularize("(quiz)zes$", "$1")

    inflect.addIrregular("person", "people")
    inflect.addIrregular("man", "men")
    inflect.addIrregular("child", "children")
    inflect.addIrregular("sex", "sexes")
    inflect.addIrregular("move", "moves")
    inflect.addIrregular("stadium", "stadiums")

    inflect.addUncountable("equipment", "information", "rice", "money", "species", "series", "fish", "sheep")
  }

  companion object {
    val instance: Inflector = Inflector()

    /**
     * Utility method to replace all occurrences given by the specific backreference with its uppercased form, and remove all
     * other backreferences.
     *
     *
     * The Java [regular expression processing][Pattern] does not use the preprocessing directives `\l`,
     * `&#92;u`, `\L`, and `\U`. If so, such directives could be used in the replacement string
     * to uppercase or lowercase the backreferences. For example, `\L1` would lowercase the first backreference, and
     * `&#92;u3` would uppercase the 3rd backreference.
     *
     *
     * @param input
     * @param regex
     * @param groupNumberToUppercase
     * @return the input string with the appropriate characters converted to upper-case
     */
    internal fun replaceAllWithUppercase(
      input: String,
      regex: String,
      groupNumberToUppercase: Int
    ): String {
      val underscoreAndDotPattern = Pattern.compile(regex)
      val matcher = underscoreAndDotPattern.matcher(input)
      // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
      val sb = StringBuffer()
      while (matcher.find()) {
        matcher.appendReplacement(sb, matcher.group(groupNumberToUppercase).uppercase(Locale.getDefault()))
      }
      matcher.appendTail(sb)
      return sb.toString()
    }
  }
}
