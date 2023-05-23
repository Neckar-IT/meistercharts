package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.ExtendWith

/**
 * Disables tests when run headless
 *
 */
@ExtendWith(DisableIfHeadlessCondition::class)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DisableIfHeadless
