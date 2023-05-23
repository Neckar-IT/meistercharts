package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.ExtendWith

/**
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ExtendWith(LocaleExtension::class)
annotation class WithLocale(val value: String)
