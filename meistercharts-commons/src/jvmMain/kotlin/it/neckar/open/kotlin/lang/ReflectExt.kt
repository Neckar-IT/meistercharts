package it.neckar.open.kotlin.lang

import kotlin.reflect.KClass

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
