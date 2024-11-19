package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests annotated with this annotation will have logging disabled.
 */
@ExtendWith(DisableLoggingCondition::class)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DisableLogging
