@file:OptIn(KspExperimental::class)

package it.neckar.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.NonExistLocation
import com.google.devtools.ksp.symbol.Origin
import kotlin.contracts.contract
import kotlin.reflect.KClass

/**
 * Returns the fully qualified name - if available.
 * Falls back to the simple name
 */
fun KSDeclaration.fqName(): String {
  this.qualifiedName?.let {
    return it.asString()
  }

  return this.simpleName.asString()
}

/**
 * Formats the location
 */
fun Location.format(): String {
  return when (this) {
    is FileLocation -> "${this.filePath}:${this.lineNumber}"
    NonExistLocation -> "-- non existent location --"
  }
}

/**
 * The names of the kotlin primitive types (for the JVM)
 */
private val kotlinPrimitiveNames = setOf("kotlin.Int", "kotlin.Boolean", "kotlin.Char", "kotlin.Byte", "kotlin.Short", "kotlin.Long", "kotlin.Float", "kotlin.Double")

/**
 * Returns true if this declaration is a kotlin primitive type
 */
fun KSDeclaration.isPrimitive(): Boolean {
  val qualifiedName = this.qualifiedName ?: throw IllegalStateException("Could not find the qualified name for $this @ ${this.location.format()}")
  return it.neckar.ksp.kotlinPrimitiveNames.contains(qualifiedName.asString())
}

@OptIn(KspExperimental::class)
fun KSClassDeclaration.isKotlinClass(): Boolean {
  return origin == Origin.KOTLIN ||
    origin == Origin.KOTLIN_LIB ||
    isAnnotationPresent(Metadata::class)
}

private val kotlinPrimitiveArrayNames = setOf(
  "kotlin.IntArray",
  "kotlin.BooleanArray",
  "kotlin.CharArray",
  "kotlin.ByteArray",
  "kotlin.ShortArray",
  "kotlin.LongArray",
  "kotlin.FloatArray",
  "kotlin.DoubleArray"
)

/**
 * Returns true if this declaration is a primitive array
 */
fun KSDeclaration.isPrimitiveArray(): Boolean {
  val qualifiedName = this.qualifiedName?.asString()
  return qualifiedName in it.neckar.ksp.kotlinPrimitiveArrayNames
}

private val kotlinPrimitiveCollectionNames = setOf(
  "it.neckar.open.collections.DoubleArrayList",
  "it.neckar.open.collections.IntArrayList",
  "it.neckar.open.collections.FloatArrayList",
)

/**
 * Returns true if this declaration is a primitive collection
 */
fun KSDeclaration.isPrimitiveCollection(): Boolean {
  val qualifiedName = this.qualifiedName?.asString()
  return qualifiedName in it.neckar.ksp.kotlinPrimitiveCollectionNames
}

/**
 * Returns true if this is a companion object
 */
fun KSDeclaration.isCompanionObject(): Boolean {
  contract {
    returns(true) implies (this@isCompanionObject is KSClassDeclaration)
  }

  return this is KSClassDeclaration && this.isCompanionObject
}

/**
 * Returns true if the [KSDeclaration] is a value class (modifier [Modifier.VALUE] is set)
 */
fun KSDeclaration.isValueClass(): Boolean {
  contract {
    returns(true) implies (this@isValueClass is KSClassDeclaration)
  }

  if ((this is KSClassDeclaration).not()) {
    return false
  }

  return this.modifiers.contains(Modifier.VALUE)
}

fun <T : Annotation> Sequence<KSAnnotation>.containsAnnotation(annotationKlass: KClass<T>): Boolean {
  val annotationClassSimpleName = requireNotNull(annotationKlass.simpleName) { "Annotation simple class name must not be null for $annotationKlass" }
  return containsAnnotation(annotationClassSimpleName)
}

fun Sequence<KSAnnotation>.containsAnnotation(annotationClassSimpleName: String): Boolean {
  return any {
    it.shortName.asString() == annotationClassSimpleName
  }
}

/**
 * Returns true if the function itself or the return type is annotated with the provided annotation
 */
fun <T : Annotation> KSFunctionDeclaration.isReturnTypeAnnotated(annotationType: KClass<T>): Boolean {
  return annotations.containsAnnotation(annotationType) ||
    returnType?.annotations?.containsAnnotation(annotationType) == true
}

/**
 * Returns true if this parameter is annotated itself - or its type is annotated
 */
fun <T : Annotation> KSValueParameter.isAnnotatedOrTypeAnnotated(annotationKlass: KClass<T>): Boolean {
  return annotations.containsAnnotation(annotationKlass) || //the parameter itself is annotated
    type.annotations.containsAnnotation(annotationKlass) //the parameter type is annotated
}

/**
 * Returns true if this parameter has the type function
 */
fun <T : Annotation> KSValueParameter.isFunctionTypeWithAnnotated(annotationKlass: KClass<T>): Boolean {
  val resolvedType = this.type.resolve()

  if (resolvedType.isFunctionType) {
    return resolvedType.arguments.any {
      it.isAnnotationPresent(annotationKlass)
    }
  }

  return false
}

/**
 * Returns the parameter that matches the provided parameter.
 */
fun getSuperParameter(function: KSFunctionDeclaration, superFunction: KSFunctionDeclaration, parameter: KSValueParameter): KSTypeReference {
  // Match parameters by index and return the type
  val index = function.parameters.indexOf(parameter)

  // Match parameters by index and return the type
  return superFunction.parameters[index].type
}

fun findSuperParameter(function: KSFunctionDeclaration, superFunction: KSFunctionDeclaration?, parameter: KSValueParameter): KSTypeReference? {
  if (superFunction == null) {
    return null
  }

  return getSuperParameter(function, superFunction, parameter)
}

/**
 * Returns true if this function contains the provided annotation anywhere in the declaration or parameters
 */
fun <T: Annotation> KSFunctionDeclaration.hasAnnotationInDeclarationOrParameters(annotationKlass: KClass<T>): Boolean {
  return isReturnTypeAnnotated(annotationKlass) || //The method itself or return type is annotated
    parameters.any { parameter ->
      parameter.isAnnotatedAnywhere(annotationKlass)
    }
}

/**
 * Returns true if the parameter has the provided annotation anywhere.
 * * is annotated itself
 * * has annotation at the type
 * * is of function type and the function has an annotation somewhere
 */
fun <T : Annotation> KSValueParameter.isAnnotatedAnywhere(annotationKlass: KClass<T>): Boolean {
  return this.isAnnotatedOrTypeAnnotated(annotationKlass) ||
    this.isFunctionTypeWithAnnotated(annotationKlass)
}

fun KSAnnotated.isDeprecated(): Boolean {
  return annotations.any { it.shortName.asString() == "Deprecated" }
}

/**
 * Returns true if this property is a field - in a class
 */
fun KSPropertyDeclaration.isClassProperty(): Boolean {
  return this.parentDeclaration is KSClassDeclaration
}

/**
 * Returns true if this property is a field
 */
fun KSPropertyDeclaration.isTopLevelProperty(): Boolean {
  return this.parentDeclaration == null
}

/**
 * Returns the value type!
 * Must only be called for value classes
 */
fun KSClassDeclaration.getValueType(): KSTypeReference {
  require(this.isValueClass()) {
    "Declaration ${this.simpleName.asString()} is not a value class @ ${this.location.format()}"
  }

  val primaryConstructor = primaryConstructor ?: throw IllegalArgumentException("No primary constructor for ${simpleName.asString()} @ ${location.format()}")
  val constructorParameter = primaryConstructor.parameters.firstOrNull() ?: throw IllegalArgumentException("No constructor parameter for ${simpleName.asString()} @ ${location.format()}")

  return constructorParameter.type
}
