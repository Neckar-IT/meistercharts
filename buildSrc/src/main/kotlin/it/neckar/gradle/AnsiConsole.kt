package it.neckar.gradle

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.extra

/**
 * Represents an ansi console (with colors); holds only [plain], so a task action can capture it for the configuration cache.
 */
class AnsiConsole(val plain: Boolean) {

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
      "$esc${csi}${color.foreground}m${content}$Reset"
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
     * The console of [gradle]: plain when Gradle runs with `--console=plain`.
     */
    fun of(gradle: Gradle): AnsiConsole {
      return AnsiConsole(gradle.startParameter.isPlainConsole)
    }

    /**
     * Starts the ANSI code
     */
    const val esc: String = "\u001B"

    /**
     * Control Sequence Introducer
     * Begins a control sequence
     * https://en.wikipedia.org/wiki/ANSI_escape_code#CSIsection
     */
    const val csi: String = "["

    /**
     * Resets all ansi attributes
     */
    const val Reset: String = "${esc}${csi}0m"
  }
}

/**
 * Returns the AnsiConsole for this project.
 * Cached in project extra properties to avoid creating multiple instances.
 */
val Project.console: AnsiConsole
  get() {
    val key = "ansiConsole"
    if (extra.has(key)) {
      return extra.get(key) as AnsiConsole
    }
    val value = AnsiConsole.of(gradle)
    extra.set(key, value)
    return value
  }

/**
 * Alias for [Project.console]
 */
inline val Project.ansiConsole: AnsiConsole
  get() = console
