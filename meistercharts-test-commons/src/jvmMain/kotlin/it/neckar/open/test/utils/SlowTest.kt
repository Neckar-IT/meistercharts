package it.neckar.open.test.utils

import org.junit.jupiter.api.Tag

/**
 * Marks a test as slow-running (typically >2 seconds).
 *
 * Tests annotated with @SlowTest are excluded from fast CI pipelines
 * (MR pipelines) but run on the main branch for full test coverage.
 *
 * Usage:
 * ```
 * @Test
 * @SlowTest
 * fun testComplexOperation() { ... }
 * ```
 *
 * Can also be applied to entire test classes:
 * ```
 * @SlowTest
 * class KspProcessorTest {
 *   @Test
 *   fun testProcessing1() { ... }
 * }
 * ```
 *
 * The tag name "slow-test" is used by Gradle to exclude these tests
 * when the skipSlowTests property is set (-PskipSlowTests).
 */
@Tag("slow-test")
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class SlowTest
