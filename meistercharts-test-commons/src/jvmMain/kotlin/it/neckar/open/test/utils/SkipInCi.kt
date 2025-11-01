package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.ExtendWith


/**
 * Skips annotated tests when running inside the CI environment.
 */
@ExtendWith(DisableIfInCiCondition::class)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class SkipInCi(
  /**
   * Optional reason reported by JUnit when a test is disabled.
   */
  val reason: String = "Disabled because running inside CI",
)
