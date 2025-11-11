package it.neckar.open.kotlin.lang

import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

/**
 * Returns true if this is an interface.
 *
 * Not implemented in Kotlin at the moment: https://youtrack.jetbrains.com/issue/KT-17661/KClass-doesnt-have-isInterface-and-isEnum
 */
val KClass<*>.isInterface: Boolean get() = this.java.isInterface

val KClass<*>.isCollection: Boolean
  get() {
    return Collection::class.java.isAssignableFrom(this.java)
  }

/**
 * Returns true if this is an enum class.
 *
 * Think about using [asEnumClassOrNull] to help with casting.
 */
fun KClass<*>.isEnum(): Boolean {
  return this.java.isEnum
}

/**
 * Casts this KClass to an enum class or returns null if it is not an enum class.
 */
@Suppress("UNCHECKED_CAST")
fun KClass<*>.asEnumClassOrNull(): KClass<out Enum<*>>? {
  return if (this.isEnum()) this as KClass<out Enum<*>> else null
}

/**
 * Casts this KClass to an enum class or throws an exception if it is not an enum class.
 */
fun KClass<*>.asEnumClass(): KClass<out Enum<*>> {
  return asEnumClassOrNull() ?: throw IllegalArgumentException("Class [$this] is not an enum class")
}

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
inline val <T : Any> KClass<T>.enumValues: Array<out Enum<*>>
  get() {
    require(this.isEnum()) { "[$this] is not an enum class" }
    return asEnumClass().enumEntries
  }

/**
 * Returns the enum entries for this class.
 */
val <T : Enum<T>> KClass<T>.enumEntries: Array<T>
  get() {
    require(this.isEnum()) { "[$this] is not an enum class" }
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
 * Returns true if this type has any star projections
 */
fun KType.hasAnyStarProjection(): Boolean {
  return this.arguments.any { it.type == null }
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

/**
 * Returns the (qualified) annotation name for the given annotation class.
 */
fun KClass<*>.findAnnotationByName(qualifiedName: String): Annotation? {
  return annotations.firstOrNull { it.annotationClass.qualifiedName == qualifiedName }
}

inline fun <reified T: Annotation> KClass<*>.findAnnotationByName(): Annotation? {
  return findAnnotationByName(T::class.qualifiedName.requireNotNull { "Could not find qualified name for ${T::class}" })
}

/**
 * Finds the property for the given constructor parameter
 */
fun <TYPE : Any> KClass<TYPE>.getBackingProperty(constructorParameter: KParameter): KProperty1<TYPE, *> {
  return getProperty(valName = constructorParameter.name.requireNotNull { "Name not found for $constructorParameter" })
}

/**
 * Returns the property for the given name.
 */
fun <TYPE : Any> KClass<TYPE>.getProperty(valName: String): KProperty1<TYPE, *> {
  return findProperty(valName)
    ?: throw IllegalArgumentException("Could not find callable for parameter [$valName] in class [${simpleNameNonNull}]. Available members: ${this.members.joinToString { it.name }}")
}

/**
 * Returns the value of the property for the given instance.
 */
fun <T : Any> T.getPropertyValueForced(propertyName: String): Any? {
  val clazz = this::class

  val property = clazz.getProperty(propertyName)
  return property.getValueForced(this)
}

fun <T : Any> T.findPropertyValueForced(propertyName: String): Any? {
  val clazz = this::class
  val property = clazz.findProperty(propertyName) ?: return null
  return property.getValueForced(this)
}

/**
 * Finds the property for the given name.
 * Returns null if no property is found.
 */
fun <TYPE : Any> KClass<TYPE>.findProperty(valName: String): KProperty1<TYPE, *>? {
  return memberProperties
    .firstOrNull { it.name == valName }
    ?.also {
      it.isAccessible = true
    }
}

/**
 * Returns true if the property is marked as transient.
 */
fun KProperty1<out Any, *>.isTransient(): Boolean {
  val javaField = javaField.requireNotNull { "Could not find javaField for $this" }
  return Modifier.isTransient(javaField.modifiers)
}

/**
 * Returns true if the property has a backing field.
 * This is the case if it is not a val with a custom getter or a var with a custom getter/setter.
 */
val KProperty1<out Any, *>.hasBackingField: Boolean
  get() {
    return this.javaField != null
  }

/**
 * Returns the value of the property for the given instance.
 */
fun KProperty1<out Any, *>.getValueForced(instance: Any): Any? {
  @Suppress("UNCHECKED_CAST")
  this as KProperty1<Any, *>
  this.isAccessible = true //Make sure we can access the property

  if (isConst) {
    //For const properties, we can call the getter directly
    return this.getter.call()
  }

  return this.get(instance)
}
