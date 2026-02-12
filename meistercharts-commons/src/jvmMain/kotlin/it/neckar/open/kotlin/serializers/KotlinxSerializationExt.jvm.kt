package it.neckar.open.kotlin.serializers

import it.neckar.open.kotlin.lang.asKClass
import it.neckar.open.kotlin.lang.findPropertyValueForced
import it.neckar.open.kotlin.lang.getAllSealedSubclasses
import it.neckar.open.kotlin.lang.isSealed
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.serializer
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
 * Returns all discriminator values from the @SerialName annotations of all sealed subclasses.
 * This is useful for creating filter parameters that match discriminator values of a sealed class hierarchy.
 *
 * @return List of serial names from all concrete (non-interface) subclasses
 * @throws IllegalArgumentException if this class is not sealed
 * @throws IllegalStateException if any subclass is missing a @SerialName annotation
 */
fun <T : Any> KClass<T>.getDiscriminatorValues(): List<String> {
  require(isSealed) { "[$this] must be sealed to extract discriminator values" }

  return getAllSealedSubclasses()
    .map { subclass ->
      subclass.findSerialName() ?: throw IllegalStateException("Subclass [${subclass.simpleName}] of sealed type [$simpleName] is missing @SerialName annotation")
    }
}

/**
 * Returns true if the class is annotated as serializable
 */
fun KClass<*>.isAnnotatedAsSerializable(): Boolean {
  return findAnnotations(kotlinx.serialization.Serializable::class).isNotEmpty()
}

/**
 * Returns the type descriptors for the type parameters of this descriptor - if there are any
 */
fun SerialDescriptor.getTypeDescriptors(): List<SerialDescriptor> {
  val typeParameterDescriptors = this.findPropertyValueForced("typeParameterDescriptors") ?: return emptyList()

  @Suppress("UNCHECKED_CAST")
  return (typeParameterDescriptors as Array<SerialDescriptor>).toList()
}


/**
 * Tries to find the serial descriptor for the given type (includes generic types)
 */
fun KType.serialDescriptor(): SerialDescriptor {
  return serializer(this).descriptor
}

private val emptySerializersModule = EmptySerializersModule()

/**
 * Tries to find the serializer for the given type (includes generic types)
 */
fun KType.serializer(): KSerializer<*> {
  try {
    return emptySerializersModule.serializer(this)
  } catch (e: Exception) {
    throw IllegalStateException("Cannot find serializer for type ${this}", e)
  }
}
