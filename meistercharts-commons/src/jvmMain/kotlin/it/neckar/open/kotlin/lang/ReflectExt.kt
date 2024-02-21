package it.neckar.open.kotlin.lang

import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Returns true if this is an interface.
 *
 * Not implemented in Kotlin at the moment: https://youtrack.jetbrains.com/issue/KT-17661/KClass-doesnt-have-isInterface-and-isEnum
 */
val KClass<*>.isInterface: Boolean get() = this.java.isInterface

val KClass<*>.isEnum: Boolean get() = this.java.isEnum

val KClass<*>.isObject: Boolean get() = this.objectInstance != null

/**
 * Returns the simple name, including the name of the enclosing class if this is an inner class.
 */
val KClass<*>.simpleNameWithEnclosing: String
  get() {
    val simpleName = this.simpleName ?: throw IllegalStateException("simpleName is null for $this")

    val enclosingClass: Class<*> = this.java.enclosingClass ?: return simpleName
    return "${enclosingClass.simpleName}.$simpleName"
  }

/**
 * Returns the enum values for this class.
 */
val <T : Any> KClass<T>.enumValues: Array<T>
  get() {
    return this.java.enumConstants ?: throw IllegalStateException("enumConstants is null for $this")
  }

fun KType.isSealedInterface(): Boolean {
  return asKClass().isSealed
}

/**
 * Returns the class of the type
 * Throws an exception if the classifier is null
 */
fun KType.asKClass(): KClass<*> {
  return this.classifier.requireNotNull() as KClass<*>
}

/**
 * Returns the sealed interface for this class
 */
fun KClass<*>.findSealedInterface(): KClass<*> {
  val sealedInterfaces = supertypes.firstOrNull {
    //Is a sealed interface?
    it.isSealedInterface()
  } ?: throw IllegalArgumentException("Could not find sealed interface for ${this::class} in super types")

  return sealedInterfaces.asKClass()
}
