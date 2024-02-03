package it.neckar.open.kotlin.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.elementNames

/**
 * Returns true if this serial kind is a primitive
 */
@OptIn(ExperimentalSerializationApi::class)
val SerialKind.isPrimitive: Boolean
  get() {
    return when (this) {
      PrimitiveKind.BOOLEAN -> true
      PrimitiveKind.BYTE -> true
      PrimitiveKind.CHAR -> true
      PrimitiveKind.DOUBLE -> true
      PrimitiveKind.FLOAT -> true
      PrimitiveKind.INT -> true
      PrimitiveKind.LONG -> true
      PrimitiveKind.SHORT -> true
      PrimitiveKind.STRING -> true
      else -> false
    }
  }


/**
 * Returns the required element names for this descriptor
 * Skips elements that are optional (have a default value)
 */
@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.requiredElementNames(): List<String> {
  return elementNames.filterIndexed { index, _ ->
    isElementOptional(index).not()
  }
}

/**
 * Returns the serial descriptor for the element with the given name
 */
@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.getElementDescriptorByName(name: String): SerialDescriptor {
  return getElementDescriptor(getElementIndex(name))
}
