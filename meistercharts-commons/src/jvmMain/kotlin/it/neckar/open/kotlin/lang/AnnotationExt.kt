package it.neckar.open.kotlin.lang

/**
 * Returns the annotation of the given typ. Throws an exception if the annotation is not present
 */
inline fun <reified T : Annotation> Class<out Annotation>.getAnnotationNonNull(annotationType: Class<T>): T {
  val found: T? = this.getAnnotation(annotationType)
  if (found != null) {
    return found
  }

  throw IllegalArgumentException("Annotation [${T::class.qualifiedName}] not found for [${this}]")
}
