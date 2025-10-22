package it.neckar.open.annotations

import kotlinx.serialization.SerialInfo
import kotlin.reflect.KClass

/**
 * Used when serializing a subclass of a sealed interface as the sealed interface itself.
 * This forces the serializer to always include type information (even if there is only one possible implementation).
 */
@Retention(AnnotationRetention.BINARY)
@Target(
  AnnotationTarget.PROPERTY,
  AnnotationTarget.TYPE,
)
@MustBeDocumented
@SerialInfo
annotation class ForcePolymorphic(
  /**
   * The concrete type that is "really" used - the subclass of the sealed interface this annotation is applied to
   */
  val concreteType: KClass<*>
)
