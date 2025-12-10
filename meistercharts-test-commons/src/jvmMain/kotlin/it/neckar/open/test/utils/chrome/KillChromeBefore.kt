package it.neckar.open.test.utils.chrome

import org.junit.jupiter.api.extension.ExtendWith

/**
 * Annotation that kills all Chrome processes before each test.
 *
 * Usage:
 * ```
 * @KillChromeBefore
 * class MyTest {
 *   @Test
 *   fun testSomething() {
 *     // All Chrome processes are killed before this test runs
 *   }
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(KillChromeExtension::class)
annotation class KillChromeBefore

