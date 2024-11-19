package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.ExtendWith

/**
 * Skip tests on Windows
 *
 */
@ExtendWith(DisableIfNotLinuxCondition::class)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class SkipWindows(val reason: String) {
}
