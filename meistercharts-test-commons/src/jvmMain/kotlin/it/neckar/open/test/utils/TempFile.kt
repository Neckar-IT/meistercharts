package it.neckar.open.test.utils

/**
 * Use [WithTempFiles] at the class/method and add [TempFolder] oder [TempFile] to the test method parameters
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TempFile(val value: String = "")
