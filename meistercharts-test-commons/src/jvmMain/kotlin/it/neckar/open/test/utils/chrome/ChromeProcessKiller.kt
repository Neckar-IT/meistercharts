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
package it.neckar.open.test.utils.chrome

import it.neckar.logging.Logger
import it.neckar.logging.LoggerDelegate
import it.neckar.open.lang.Os
import java.io.IOException

/**
 * Utility to kill Chrome processes on various operating systems.
 */
object ChromeProcessKiller {

  /**
   * Kills all Chrome/Chromium processes.
   * Works on Windows, Mac, and Linux.
   */
  fun killAllChromeProcesses() {
    try {
      when {
        Os.isWindows -> killChromeOnWindows()
        Os.isMac -> killChromeOnMac()
        Os.isLinux -> killChromeOnLinux()
        else -> logger.warn("Warning: Unsupported OS for killing Chrome processes: ${System.getProperty("os.name")}")
      }
    } catch (e: IOException) {
      logger.error("Warning: Failed to kill Chrome processes: ${e.message}", e)
    } catch (e: Exception) {
      logger.error(" Unexpected error while killing Chrome processes: ${e.message}", e)
    }
  }

  private fun killChromeOnWindows() {
    // Kill chrome.exe and chromedriver.exe
    executeCommand("taskkill", "/F", "/IM", "chrome.exe", "/T")
    executeCommand("taskkill", "/F", "/IM", "chromedriver.exe", "/T")
  }

  private fun killChromeOnMac() {
    // Kill Chrome and Chromium
    executeCommand("pkill", "-9", "Chrome")
    executeCommand("pkill", "-9", "Chromium")
    executeCommand("pkill", "-9", "chromedriver")
  }

  private fun killChromeOnLinux() {
    // Kill chrome, chromium, and google-chrome processes
    executeCommand("pkill", "-i", "-9", "chrome")
    executeCommand("pkill", "-i", "-9", "chromium")
    executeCommand("pkill", "-i", "-9", "google-chrome")
    executeCommand("pkill", "-i", "-9", "chromedriver")
  }

  /**
   * Executes a command and waits for it to complete.
   * Ignores errors (e.g., if no processes were found to kill).
   */
  private fun executeCommand(vararg command: String) {
    try {
      val process = ProcessBuilder(*command)
        .redirectErrorStream(true)
        .start()

      when (val exitCode = process.waitFor()) {
        0 -> logger.info("Successfully executed: ${command.joinToString(" ")}")
        1 -> {
          val out = process.inputStream.bufferedReader().readText()

          if (out.isEmpty()) {
            //This is fine - no process to kill
            logger.info("No processes found [command: ${command.joinToString(" ")}]")
            return
          }

          logger.error("Command ${command.joinToString(" ")} failed with exit code 1: $out")
        }

        else -> {
          // Exit code != 0 is often just "no process found", which is fine
          val error = process.errorStream.bufferedReader().readText()
          logger.warn("Command ${command.joinToString(" ")} exited with code $exitCode: $error")
        }
      }
    } catch (e: IOException) {
      // Ignore - command might not exist or no processes to kill
      logger.error("Command ${command.joinToString(" ")} failed: ${e.message}", e)
    }
  }

  private val logger: Logger by LoggerDelegate()
}
