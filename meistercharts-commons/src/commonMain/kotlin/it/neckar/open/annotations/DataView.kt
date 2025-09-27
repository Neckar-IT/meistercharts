package it.neckar.open.annotations

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER

/**
 * Annotation that tags classes as "view" elements.
 *
 * "Views" represent a specific view on data - for a specific use case (e.g. table entries).
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY) //Is required to allow KSP plugin to find the annotation when calling `KSAnnotated.annotations`
@Target(CLASS, FUNCTION, PROPERTY, CONSTRUCTOR, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class DataView(val useCaseDescription: String)
