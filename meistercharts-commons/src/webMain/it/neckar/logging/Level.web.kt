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
package it.neckar.logging

/**
 * Represents a log level.
 * Inspired by slf4j
 */
actual enum class Level {
  ERROR,
  WARN,
  INFO,
  DEBUG,
  TRACE,
  ;

  companion object {
    /**
     * Guesses the level from a string
     */
    fun guess(levelAsString: String?): Level? {
      if (levelAsString.isNullOrBlank()) return null

      val uppercase = levelAsString.trim().uppercase()

      //Check for direct hits
      when (uppercase) {
        "ERROR" -> return ERROR
        "WARN" -> return WARN
        "INFO" -> return INFO
        "DEBUG" -> return DEBUG
        "TRACE" -> return TRACE
        else -> {}
      }

      when {
        uppercase.startsWith("E") -> return ERROR
        uppercase.startsWith("W") -> return WARN
        uppercase.startsWith("I") -> return INFO
        uppercase.startsWith("D") -> return DEBUG
        uppercase.startsWith("T") -> return TRACE
        else -> {}
      }

      return null
    }
  }
}
