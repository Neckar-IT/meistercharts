package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.ExtendWith

/**
 *
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TempFile(val value: String = "")
