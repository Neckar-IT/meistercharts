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

  fun black(content: Any): String {
    return withColor(content.toString(), Color.Black)
  }

  fun red(content: Any): String {
    return withColor(content.toString(), Color.Red)
  }

  fun green(content: Any): String {
    return withColor(content.toString(), Color.Green)
  }

  fun yellow(content: Any): String {
    return withColor(content.toString(), Color.Yellow)
  }

  fun blue(content: Any): String {
    return withColor(content.toString(), Color.Blue)
  }

  fun magenta(content: Any): String {
    return withColor(content.toString(), Color.Magenta)
  }

  fun cyan(content: Any): String {
    return withColor(content.toString(), Color.Cyan)
  }

  fun white(content: Any): String {
    return withColor(content.toString(), Color.White)
  }

  fun orange(content: Any): String {
    return withColor(content.toString(), Color.Orange)
  }

  fun gray(content: Any): String {
    return withColor(content.toString(), Color.Gray)
  }

  /**
   * Creates a loading bar string with a carriage return (\r) to overwrite the previous loading bar string if there is one. Must be used in a loop.
   */
  fun loadingBar(current: Int, max: Int, color: Color): String {
    val currentString: String = "#".repeat(current)
    val freeSpaceString: String = " ".repeat(max - current)
    return withColor("\r[$currentString$freeSpaceString]", color)
  }

  private fun withColor(content: Any, color: Color): String {
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

/**
 * Alias for [Project.console]
 */
val Project.ansiConsole: AnsiConsole
  get() = console
