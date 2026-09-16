package it.neckar.gradle

/**
 * Quotes text for a POSIX shell, for values whose type admits shell syntax.
 */
object ShellQuoting {
  /** Single quotes, with every `'` inside closed, escaped and reopened. */
  fun singleQuoted(text: String): String {
    return "'" + text.replace("'", """'\''""") + "'"
  }
}
