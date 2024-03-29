package it.neckar.gradle

import getOrPut
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.configuration.ConsoleOutput

/**
 * Represents an ansi console (with colors)
 */
class AnsiConsole(val gradle: Gradle) {
  constructor(project: Project) : this(project.gradle)


  val plain: Boolean = gradle.startParameter.consoleOutput == ConsoleOutput.Plain

  fun black(content: Any): CharSequence {
    return withColor(content.toString(), Color.Black)
  }

  fun red(content: Any): CharSequence {
    return withColor(content.toString(), Color.Red)
  }

  fun green(content: Any): CharSequence {
    return withColor(content.toString(), Color.Green)
  }

  fun yellow(content: Any): CharSequence {
    return withColor(content.toString(), Color.Yellow)
  }

  fun blue(content: Any): CharSequence {
    return withColor(content.toString(), Color.Blue)
  }

  fun magenta(content: Any): CharSequence {
    return withColor(content.toString(), Color.Magenta)
  }

  fun cyan(content: Any): CharSequence {
    return withColor(content.toString(), Color.Cyan)
  }

  fun white(content: Any): CharSequence {
    return withColor(content.toString(), Color.White)
  }

  fun orange(content: Any): CharSequence {
    return withColor(content.toString(), Color.Orange)
  }

  fun gray(content: Any): CharSequence {
    return withColor(content.toString(), Color.Gray)
  }

  private fun withColor(content: Any, color: Color): CharSequence {
    return if (plain) {
      content.toString()
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
    Gray("90", "100"),
    Orange("38;5;208", "48;5;208"),
  }

  companion object {
    /**
     * Starts the ANSI code
     */
    const val ESC: String = "\u001B"

    /**
     * Control Sequence Introducer
     * Begins a control sequence
     * https://en.wikipedia.org/wiki/ANSI_escape_code#CSIsection
     */
    const val CSI: String = "["

    /**
     * Resets all ansi attributes
     */
    const val RESET: String = "${ESC}${CSI}0m"
  }
}

/**
 * Returns the AnsiConsole for this project
 */
val Project.console: AnsiConsole
  get() {
    return getOrPut("ansiConsole") {
      return AnsiConsole(gradle)
    }
  }
