package it.neckar.open.kotlin.serializers

import it.neckar.open.kotlin.lang.asKClass
import it.neckar.open.kotlin.lang.isSealed
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSupertypes

actual fun <S : Any> KClass<S>.verifyPlausibleForSerialization() {
  if (isSealed) {
    //all sealed interfaces/classes are plausible targets
    return
  }

  val sealedSuperType: KType? = allSupertypes.firstOrNull { it.isSealed() }

  if (sealedSuperType!=null){
    //We have a sealed interface as supertype, use the sealed interface for serialization
    throw IllegalArgumentException("Use the sealed interface [${sealedSuperType.asKClass().simpleName}] as type for serialization instead of [${this.simpleName}].")
  }
}
