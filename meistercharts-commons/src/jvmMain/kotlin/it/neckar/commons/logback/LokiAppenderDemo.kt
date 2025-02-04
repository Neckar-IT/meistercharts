package it.neckar.commons.logback

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import org.slf4j.event.Level


fun main() {
  println("Hello, World!")

  LogbackConfigurer.configureLoggingConsoleAndLoki(
    app = "loki-appender-demo-app",
    hostname = "silver",
    levelForRoot = Level.INFO,
    levelForNeckarIt = Level.DEBUG
  ) {
    setBatchTimeoutMs(1000)
  }

  LokiAppenderDemo().runLoggingDemo()
  Thread.sleep(5000)
}

/**
 *
 */
class LokiAppenderDemo {
  fun runLoggingDemo() {
    logger.info("Hello, World! Info")
    logger.warn { "Hello, World! Warn" }
    logger.error("Hello, World! Error", IllegalStateException("This is an error"))
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("it.neckar.commons.logback.LokiAppenderDemo-Foobar")
  }
}
