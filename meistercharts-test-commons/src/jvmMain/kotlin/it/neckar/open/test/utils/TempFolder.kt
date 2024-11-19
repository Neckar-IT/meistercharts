package it.neckar.open.test.utils

/**
 * Marks a parameter as a temporary folder
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TempFolder
