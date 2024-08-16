package it.neckar.open.annotations.serialization

/**
 * Marked classes are inlined when serialized:
 * They must only have one value (that is serialized).
 *
 * This value is serialized directly. The classname of this class is *not* serialized
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.CLASS,
)
@Deprecated("Too complicated to use. Use ::isSerializedAsPrimitive() instead")
@MustBeDocumented
annotation class SerializedInline
