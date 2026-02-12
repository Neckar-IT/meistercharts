package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.event.Level

/**
 * Tests annotated with this annotation will have logging disabled.
 *
 * Set [mute] to `true` to completely suppress all logging output including ERROR
 * (useful when a test intentionally triggers exceptions that produce noisy stack traces).
 */
@ExtendWith(DisableLoggingCondition::class)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DisableLogging(

  /**
   * The maximum log level that is still visible. Ignored if [mute] is `true`.
   */
  val level: Level = Level.ERROR,

  /**
   * If `true`, suppresses all logging output including ERROR (sets Level.OFF).
   */
  val mute: Boolean = false,
)
