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
package it.neckar.open.console


/**
 * Represents an ansi console (with colors), modified copy from buildSrc/src/main/kotlin/it/neckar/gradle/AnsiConsole.kt
 */
class AnsiConsole {
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
   * Returns the content with bold formatting.
   */
  fun bold(content: Any): String {
    return "$ESC${CSI}1m${content}$RESET"
  }

  fun clearScreen(): String {
    return "\u001B[2J"
  }

  fun resetColor(): String {
    return "\u001B[0m"
  }

  fun moveCursor(row: Int, column: Int): String {
    return "\u001B[${row};${column}H"
  }

  /**
   * Creates a loading bar string with a carriage return (\r) to overwrite the previous loading bar string if there is one. Must be used in a loop.
   */
  fun loadingBar(current: Int, max: Int, color: Color): String {
    val currentString: String = "#".repeat(current)
    val freeSpaceString: String = " ".repeat(max - current)
    return withColor("\r[$currentString$freeSpaceString]", color)
  }

  fun withColor(content: Any, color: Color): String {
    return "$ESC$CSI${color.foreground}m${content}$RESET"
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
    const val RESET: String = "$ESC${CSI}0m"
  }
}
