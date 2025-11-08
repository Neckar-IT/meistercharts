package it.neckar.open.kotlin.serializers

import it.neckar.open.annotations.serialization.SerializedType
import it.neckar.open.kotlin.lang.asKClass
import it.neckar.open.kotlin.lang.isSealed
import it.neckar.open.kotlin.reflect.classForName
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.findAnnotations

actual fun <S : Any> KClass<S>.verifyPlausibleForSerialization() {
  if (isSealed) {
    //all sealed interfaces/classes are plausible targets
    return
  }

  val sealedSuperType: KType? = allSupertypes.firstOrNull { it.isSealed() }

  if (sealedSuperType != null) {
    //We have a sealed interface as supertype, use the sealed interface for serialization
    throw IllegalArgumentException("Use the sealed interface [${sealedSuperType.asKClass().simpleName}] as type for serialization instead of [${this.simpleName}].")
  }
}

/**
 * Returns the serial name from the [SerialName] annotation.
 */
fun <T : Any> KClass<T>.findSerialName(): String? {
  return findAnnotations(SerialName::class).firstOrNull()?.value
}

/**
 * Returns the KClass that is guessed from the serial descriptor
 */
actual fun SerialDescriptor.guessClass(): KClass<*> {
  val guessClassName = guessClassName()
  return classForName(guessClassName)
}

actual fun SerialDescriptor.guessClassName(): String {
  return annotations
    .filterIsInstance<SerializedType>()
    .firstOrNull()?.type?.qualifiedName
    ?: serialName
}

/**
 * Returns true if the class is annotated as serializable
 */
fun KClass<*>.isAnnotatedAsSerializable(): Boolean {
  return findAnnotations(kotlinx.serialization.Serializable::class).isNotEmpty()
}
