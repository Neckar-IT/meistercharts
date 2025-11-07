package it.neckar.open.kotlin.serializers

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.reflect.KClass

actual fun <S : Any> KClass<S>.verifyPlausibleForSerialization() {
  //Reflection not available in JS
}

@Deprecated("Not available in JS", level = DeprecationLevel.ERROR)
actual fun SerialDescriptor.guessClass(): KClass<*> {
  throw UnsupportedOperationException("Class guessing is not supported in JS")
}

@Deprecated("Not available in JS", level = DeprecationLevel.ERROR)
actual fun SerialDescriptor.guessClassName(): String {
  throw UnsupportedOperationException("Class name guessing is not supported in JS")
}
