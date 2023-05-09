package it.neckar.open.kotlin.lang

import it.neckar.open.unit.other.Inclusive

/**
 * Deletes the suffix - if there is one
 */
fun StringBuilder.deleteSuffix(toDelete: String) {
  if (endsWith(toDelete)) {
    val lengthToDelete = toDelete.length
    deleteRange(length - lengthToDelete, length)
  }
}

/**
 * Returns the index of the char - closes to the center of this string.
 * This method can be used to split a string into (as equal as possible) parts.
 */
fun String.centerIndexOf(char: Char): Int {
  val firstFoundIndex = this.indexOf(char)
  if (firstFoundIndex == -1) {
    //nothing found
    return -1
  }

  //The currently best index
  var currentlyBestIndex = firstFoundIndex


  var currentIndex = firstFoundIndex
  while (currentIndex > -1) {
    currentIndex = this.indexOf(char, currentIndex + 1)

    if (currentIndex == -1) {
      //no char found anymore, return the currently best index
      return currentlyBestIndex
    }

    //decide which one is the best
    val center = this.length / 2.0

    val distanceCurrent = (currentIndex - center).abs()
    val distanceCurrentlyBest = (currentlyBestIndex - center).abs()

    if (distanceCurrent < distanceCurrentlyBest) {
      currentlyBestIndex = currentIndex
    }
  }

  return currentlyBestIndex
}

operator fun String.Companion.invoke(arrays: IntArray, offset: Int = 0, size: Int = arrays.size - offset): String {
  val sb = StringBuilder()
  for (n in offset until offset + size) {
    sb.append(arrays[n].toChar()) // @TODO: May not work the same! In JS: String.fromCodePoint
  }
  return sb.toString()
}

////////////////////////////////////
////////////////////////////////////

private val formatRegex = Regex("%([-]?\\d+)?(\\w)")

fun String.splitKeep(regex: Regex): List<String> {
  val str = this
  val out = arrayListOf<String>()
  var lastPos = 0
  for (part in regex.findAll(this)) {
    val prange = part.range
    if (lastPos != prange.start) {
      out += str.substring(lastPos, prange.start)
    }
    out += str.substring(prange)
    lastPos = prange.endInclusive + 1
  }
  if (lastPos != str.length) {
    out += str.substring(lastPos)
  }
  return out
}

private val replaceNonPrintableCharactersRegex by lazy { Regex("[^ -~]") }
fun String.replaceNonPrintableCharacters(replacement: String = "?"): String {
  return this.replace(replaceNonPrintableCharactersRegex, replacement)
}

fun String.indexOfOrNull(char: Char, startIndex: Int = 0): Int? = this.indexOf(char, startIndex).takeIf { it >= 0 }

fun String.lastIndexOfOrNull(char: Char, startIndex: Int = lastIndex): Int? =
  this.lastIndexOf(char, startIndex).takeIf { it >= 0 }

fun String.splitInChunks(size: Int): List<String> {
  val out = arrayListOf<String>()
  var pos = 0
  while (pos < this.length) {
    out += this.substring(pos, kotlin.math.min(this.length, pos + size))
    pos += size
  }
  return out
}

fun String.substr(start: Int): String = this.substr(start, this.length)

fun String.substr(start: Int, length: Int): String {
  val low = (if (start >= 0) start else this.length + start).coerceIn(0, this.length)
  val high = (if (length >= 0) low + length else this.length + length).coerceIn(0, this.length)
  return if (high >= low) this.substring(low, high) else ""
}

inline fun String.eachBuilder(transform: StringBuilder.(Char) -> Unit): String = buildString {
  @Suppress("ReplaceManualRangeWithIndicesCalls") // Performance reasons? Check that plain for doesn't allocate
  for (n in 0 until this@eachBuilder.length) transform(this, this@eachBuilder[n])
}

inline fun String.transform(transform: (Char) -> String): String = buildString {
  @Suppress("ReplaceManualRangeWithIndicesCalls") // Performance reasons? Check that plain for doesn't allocate
  for (n in 0 until this@transform.length) append(transform(this@transform[n]))
}

fun String.parseInt(): Int = when {
  this.startsWith("0x", ignoreCase = true) -> this.substring(2).toLong(16).toInt()
  this.startsWith("0o", ignoreCase = true) -> this.substring(2).toLong(8).toInt()
  this.startsWith("0b", ignoreCase = true) -> this.substring(2).toLong(2).toInt()
  else -> this.toInt()
}

//val String.quoted: String get() = this.quote()
fun String.toCharArray() = CharArray(length) { this@toCharArray[it] }

fun String.escape(): String {
  val out = StringBuilder()
  for (n in 0 until this.length) {
    val c = this[n]
    when (c) {
      '\\' -> out.append("\\\\")
      '"' -> out.append("\\\"")
      '\n' -> out.append("\\n")
      '\r' -> out.append("\\r")
      '\t' -> out.append("\\t")
      in '\u0000'..'\u001f' -> {
        out.append("\\x")
        out.append(Hex.encodeCharLower(c.toInt().extract(4, 4)))
        out.append(Hex.encodeCharLower(c.toInt().extract(0, 4)))
      }
      else -> out.append(c)
    }
  }
  return out.toString()
}

fun String.uescape(): String {
  val out = StringBuilder()
  for (n in 0 until this.length) {
    val c = this[n]
    when (c) {
      '\\' -> out.append("\\\\")
      '"' -> out.append("\\\"")
      '\n' -> out.append("\\n")
      '\r' -> out.append("\\r")
      '\t' -> out.append("\\t")
      else -> if (c.isPrintable()) {
        out.append(c)
      } else {
        out.append("\\u")
        out.append(Hex.encodeCharLower(c.toInt().extract(12, 4)))
        out.append(Hex.encodeCharLower(c.toInt().extract(8, 4)))
        out.append(Hex.encodeCharLower(c.toInt().extract(4, 4)))
        out.append(Hex.encodeCharLower(c.toInt().extract(0, 4)))
      }
    }
  }
  return out.toString()
}

fun String.unescape(): String {
  val out = StringBuilder()
  var n = 0
  while (n < this.length) {
    val c = this[n++]
    when (c) {
      '\\' -> {
        val c2 = this[n++]
        when (c2) {
          '\\' -> out.append('\\')
          '"' -> out.append('\"')
          'n' -> out.append('\n')
          'r' -> out.append('\r')
          't' -> out.append('\t')
          'u' -> {
            val chars = this.substring(n, n + 4)
            n += 4
            out.append(chars.toInt(16).toChar())
          }
          else -> {
            out.append("\\$c2")
          }
        }
      }
      else -> out.append(c)
    }
  }
  return out.toString()
}

fun String?.uquote(): String = if (this != null) "\"${this.uescape()}\"" else "null"
fun String?.quote(): String = if (this != null) "\"${this.escape()}\"" else "null"

fun String.isQuoted(): Boolean = this.startsWith('"') && this.endsWith('"')
fun String.unquote(): String = if (isQuoted()) this.substring(1, this.length - 1).unescape() else this

val String?.quoted: String get() = this.quote()
val String.unquoted: String get() = this.unquote()


/**
 * Returns true if this string contains all given strings.
 *
 * Does *not* check the order
 */
fun String.containsAll(elements: Iterable<String>, ignoreCase: Boolean = false): Boolean {
  return elements.all {
    this.contains(it, ignoreCase)
  }
}

/**
 * Returns a string that is safe to be used as file name
 */
fun String.encodeForFileName(): String {
  return InvalidForFileName.replace(this, "_")
}

/**
 * Regex that contains invalid elements for a file name
 */
private val InvalidForFileName: Regex = Regex("[:\\\\/*\"?|<>']")

/**
 * This is far from perfect.
 * TODO: Improve using this information: https://www.w3.org/TR/CSS2/syndata.html#characters
 */
private val InvalidForCssIdentifier: Regex = Regex("[:\\\\/*\"?|<>' ]")

/**
 * Returns a valid CSS identifier
 */
fun String.encodeForCssIdentifier(): String {
  return InvalidForCssIdentifier.replace(this, "_")
}

/**
 * Wraps a single line of text into multiple lines. Words are identified by [wrapOn]
 * Leading and trailing spaces for each line as stripped
 */
fun String.wrap(
  /**
   * The max length of a line
   */
  maxLineLength: Int = 80,
  /**
   * Wrap on this string - usually an space
   */
  wrapOn: Char = ' ',
): List<String> {
  require(maxLineLength > 1) {
    "Invalid wrap length: $maxLineLength"
  }


  //The collected lines
  val lines = mutableListOf<String>()

  //The start index of the current line
  @Inclusive var currentLineStartIndex = 0

  //Iterate while there chars left
  while (currentLineStartIndex <= lastIndex) {
    //check if this

    @Inclusive val currentLineMaxEndIndex = (currentLineStartIndex + maxLineLength - 1)

    //Check if this is the last line, just add the complete line
    if (currentLineMaxEndIndex >= lastIndex) {
      //last line, just take everything
      lines.add(substring(currentLineStartIndex))
      break
    }

    //Find the last wrap index (if there is one)
    val wrapIndex: Int = lastIndexOf(wrapOn, currentLineMaxEndIndex)

    //Check if there has been a valid wrap index found
    if (wrapIndex > currentLineStartIndex) {
      //We found a valid index to wrap

      //Add the line
      lines.add(substring(currentLineStartIndex, wrapIndex))

      //Prepare for the next line, skip the char with the wrap string
      currentLineStartIndex = wrapIndex + 1
    } else {
      //Take the complete line
      lines.add(substring(currentLineStartIndex, currentLineMaxEndIndex + 1))

      //Prepare for the next line, skip the char with the wrap string
      currentLineStartIndex = currentLineMaxEndIndex + 1

      //check if the next char is - by coincidence - the wrapOn char, wrap if this is the case
      if (currentLineStartIndex <= lastIndex && get(currentLineStartIndex) == wrapOn) {
        currentLineStartIndex++
      }
    }
  }

  return lines
}

/**
 * Returns this if this is not null. Returns [ifNull] if this is null.
 */
fun String?.ifNull(ifNull: String): String {
  return this ?: ifNull
}

/**
 * Returns this char sequence if it is not null, not empty, and doesn't consist solely of whitespace characters, or the result of calling defaultValue function otherwise.
 */
inline fun String?.ifBlank(defaultValue: () -> String): String {
  return if (isNullOrBlank()) defaultValue() else this
}

/**
 * Returns the given default value if this is null or blank
 */
fun String?.ifBlank(defaultValue: String): String {
  return if (isNullOrBlank()) defaultValue else this
}

/**
 * Returns this string or null if this string is empty
 */
fun String?.nullIfEmpty(): String? {
  return this.takeIf {
    !it.isNullOrEmpty()
  }
}

/**
 * Returns this string or null if this string is blank
 */
fun String?.nullIfBlank(): String? {
  return this.takeIf {
    !it.isNullOrBlank()
  }
}

/**
 * Returns a checkbox char
 */
fun Boolean.toCheckboxChar(): String {
  return if (this) "\u2611" else "\u2610"
}

fun String.inCurlyBraces(): String {
  return "{$this}"
}


/**
 * Shortens the string to [maxLength]; in such case, appends the [ellipsis] (typically "…" ).
 *
 * For example, returns "ABCD…" when called for "ABCDEFHG".(5, "…")
 */
fun String.restrictLengthWithEllipsis(maxLength: Int, ellipsis: String = "…"): String =
  if (this.length <= maxLength) this
  else this.substring(0, maxLength - ellipsis.length) + ellipsis

/**
 * Returns true if the string starts with a latin letter a-z or A-Z
 */
fun String.startsWithLetter(): Boolean = this.contains(regexStartsWithLetter)

private val regexStartsWithLetter = "^[a-zA-Z]".toRegex()


/**
 * Returns true if all lines are blank
 */
fun List<String>.allBlank(): Boolean {
  return all { it.isBlank() }
}
