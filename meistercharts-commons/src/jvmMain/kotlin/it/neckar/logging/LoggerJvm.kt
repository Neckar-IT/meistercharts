package it.neckar.logging

/**
 * Represents a logger.
 * Is backed by [org.slf4j.Logger]
 */
actual typealias Logger = org.slf4j.Logger


actual fun Logger.isEnabledForLevel(level: Level): Boolean {
  return this.isEnabledForLevel(level)
}
