package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.ExtendWith

/**
 * Marks tests that require a Chrome browser running in Docker (or locally).
 *
 * These tests are disabled by default and only run when explicitly enabled via:
 * - Environment variable: `CHROME_TESTS_ENABLED=true`
 * - Gradle property: `-PchromeTests`
 *
 * ## Usage
 *
 * Start Chrome in Docker:
 * ```bash
 * docker run -d -p 9222:9222 zenika/alpine-chrome --no-sandbox --remote-debugging-address=0.0.0.0 --remote-debugging-port=9222 about:blank
 * ```
 *
 * Run tests with environment variable:
 * ```bash
 * CHROME_TESTS_ENABLED=true ./gradlew :your:module:test
 * ```
 *
 * Run tests with Gradle property:
 * ```bash
 * ./gradlew :your:module:test -PchromeTests
 * ```
 */
@ExtendWith(DockerChromeTestCondition::class)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DockerChromeTest(
  /**
   * Optional reason reported by JUnit when tests are disabled.
   */
  val reason: String = "Docker Chrome tests disabled (set CHROME_TESTS_ENABLED=true or -PchromeTests to enable)",
)
