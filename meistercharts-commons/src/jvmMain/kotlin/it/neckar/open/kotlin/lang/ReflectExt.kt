package it.neckar.open.kotlin.lang

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

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

    val enclosingClass: KClass<*> = this.java.enclosingClass?.kotlin ?: return simpleName
    return "${enclosingClass.simpleNameWithEnclosing}.$simpleName"
  }

/**
 * Returns the simple name - throws an exception if it is null.
 */
val KClass<*>.simpleNameNonNull: String
  get() {
    return this.simpleName ?: throw IllegalStateException("simpleName is null for $this")
  }

/**
 * Returns the enum entries for this class.
 */
@Deprecated("Use enumEntries instead", ReplaceWith("enumEntries"))
inline val <T : Any> KClass<T>.enumValues: Array<T>
  get() {
    return this.enumEntries
  }

val <T : Any> KClass<T>.enumEntries: Array<T>
  get() {
    return this.java.enumConstants ?: throw IllegalStateException("enumConstants is null for $this")
  }

/**
 * Returns true if this type is sealed
 */
fun KType.isSealed(): Boolean {
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
 * Returns all ancestors
 */
fun KClass<*>.getAllAncestors(): Set<KType> {
  return supertypes.flatMap {
    buildSet {
      add(it)
      addAll(it.asKClass().getAllAncestors())
    }
  }.toSet()
}

/**
 * Returns all *subclasses* of this sealed interface.
 * Including subclasses of subinterfaces.
 *
 * Does *not* include sealed interfaces or sealed classes.
 */
fun <T : Any> KClass<T>.getAllSealedSubclasses(): List<KClass<out T>> {
  require(this.isSealed) { "[$this] must be sealed" }

  return sealedSubclasses.flatMap {
    if (it.isSealed) {
      it.getAllSealedSubclasses()
    } else {
      listOf(it)
    }
  }
}

/**
 * Returns the sealed interface for this class.
 * Throws an exception if the sealed interface is not found
 */
fun KClass<*>.getSealedInterface(): KClass<*> {
  return findSealedInterface() ?: throw IllegalArgumentException("Could not find sealed interface for [${this::simpleNameWithEnclosing}] in super types")
}

/**
 * Returns the sealed interface for this class or null if it is not found
 */
fun KClass<*>.findSealedInterface(): KClass<*>? {
  return supertypes.firstOrNull {
    //Is a sealed interface?
    it.isSealed()
  }?.asKClass()
}

/**
 * Returns all sealed interfaces that are ancestors for this class
 */
fun KClass<*>.findSealedInterfacesAncestors(): List<KType> {
  return getAllAncestors().filter {
    it.isSealed()
  }.map { it }
}

/**
 * Returns true if this class is a sealed class and shares a common ancestor with the other class.
 */
fun KClass<*>.hasCommonSealedAncestorWith(other: KClass<*>): Boolean {
  //Check if the sealed interfaces share a common ancestor
  val myAncestors = findSealedInterfacesAncestors()
  val otherAncestors = other.findSealedInterfacesAncestors()

  return myAncestors.any { it in otherAncestors }
}

/**
 * Returns the supertype or null if there is none.
 */
inline fun <reified SuperType : Any> KClass<*>.findSupertype(): KType? {
  return supertypes.firstOrNull {
    it.classifier == SuperType::class
  }
}

/**
 * Returns true if this class has a supertype of the given type.
 */
@Deprecated(("Inline!"), ReplaceWith("isSubclassOf(T::class)", "kotlin.reflect.full.isSubclassOf"))
inline fun <reified T : Any> KClass<*>.hasSupertype(): Boolean {
  return isSubclassOf(T::class)
}

/**
 * Returns true if this type is a Nothing type
 */
fun KType.isNothing(): Boolean {
  return this.classifier == Nothing::class
}

fun KType.isNotNothing(): Boolean {
  return isNothing().not()
}

/**
 * Returns the (simple) annotation name for the given annotation class.
 * E.g., for @JvmStatic, this returns "JvmStatic"
 */
inline fun <reified T : Annotation> getAnnotationName(): String {
  return T::class.simpleName ?: throw IllegalStateException("Could not find the simple name for ${T::class}")
}
