package it.neckar.open.kotlin.serializers

import kotlin.reflect.KClass

actual fun <S : Any> KClass<S>.verifyPlausibleForSerialization() {
  //Reflection not available in JS
}
