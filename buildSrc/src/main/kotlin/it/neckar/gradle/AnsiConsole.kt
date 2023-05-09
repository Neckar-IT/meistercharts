package it.neckar.gradle

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.configuration.ConsoleOutput

/**
 * Represents an ansi console (with colors)
 */
class AnsiConsole(val gradle: Gradle) {
  constructor(project: Project) : this(project.gradle)


  val plain: Boolean = gradle.startParameter.consoleOutput == ConsoleOutput.Plain

  fun black(content: CharSequence): CharSequence {
    return withColor(content, Color.Black)
  }

  fun red(content: CharSequence): CharSequence {
    return withColor(content, Color.Red)
  }

  fun green(content: CharSequence): CharSequence {
    return withColor(content, Color.Green)
  }

  fun yellow(content: CharSequence): CharSequence {
    return withColor(content, Color.Yellow)
  }

  fun blue(content: CharSequence): CharSequence {
    return withColor(content, Color.Blue)
  }

  fun magenta(content: CharSequence): CharSequence {
    return withColor(content, Color.Magenta)
  }

  fun cyan(content: CharSequence): CharSequence {
    return withColor(content, Color.Cyan)
  }

  fun white(content: CharSequence): CharSequence {
    return withColor(content, Color.White)
  }

  private fun withColor(content: CharSequence, color: Color): CharSequence {
    return if (plain) {
      content
    } else {
      "$ESC${CSI}${color.foreground}m${content}$RESET"
    }
  }

  enum class Color(val foreground: String, val background: String) {
    Black("30", "40"),
    Red("31", "41"),
    Green("32", "42"),
    Yellow("33", "43"),
    Blue("34", "44"),
    Magenta("35", "45"),
    Cyan("36", "46"),
    White("37", "47"),
  }

  companion object {
    /**
     * Starts the ANSI code
     */
    val ESC: String = "\u001B"

    /**
     * Control Sequence Introducer
     * Begins a controle sequence
     * https://en.wikipedia.org/wiki/ANSI_escape_code#CSIsection
     */
    val CSI: String = "["

    /**
     * Resets all ansi attributes
     */
    val RESET: String = "${ESC}${CSI}0m"
  }
}

val Project.console: AnsiConsole
  get() {
    return AnsiConsole(this.gradle)
  }
