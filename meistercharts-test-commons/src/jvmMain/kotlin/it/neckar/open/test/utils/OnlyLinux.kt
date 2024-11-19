package it.neckar.open.test.utils

import it.neckar.open.test.utils.DisableIfNotLinuxCondition
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Run tests only on Linux
 *
 */
@ExtendWith(DisableIfNotLinuxCondition::class)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class OnlyLinux
