@file:OptIn(KspExperimental::class)

package it.neckar.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import kotlin.reflect.KClass

/**
 * Returns all function declarations that have the specified annotation or have a parameter with the specified annotation.
 */
fun <T : Annotation> Resolver.getFunctionsWithAnnotationOrAnnotatedParameters(annotationKlass: KClass<T>): Sequence<KSFunctionDeclaration> {
  //Collect all functions in all files. Top level functions, functions from classes and its companion objects
  val allFunctions = getAllFiles().flatMap { ksFile ->
    ksFile.declarations.flatMap { declaration ->
      when (declaration) {
        is KSFunctionDeclaration -> { //Function declared in a file
          sequenceOf(declaration)
        }

        is KSClassDeclaration -> {
          val companionObjectFunctions = declaration.declarations.flatMap {
            if (it.isCompanionObject()) {
              it.getDeclaredFunctions()
            } else {
              emptySequence()
            }
          }

          //Function declared in a class
          val declaredFunctions = declaration.getDeclaredFunctions()

          //Combine the results
          declaredFunctions + companionObjectFunctions
        }

        else -> emptySequence()
      }
    }
  }

  //Find the relevant functions that have the given annotations (somewhere)
  return allFunctions.filter { functionDeclaration ->
    functionDeclaration.hasAnnotationInDeclarationOrParameters(annotationKlass)
  }
}

/**
 * Returns the super function - if there is one
 */
fun Resolver.findSuperFunction(function: KSFunctionDeclaration): KSFunctionDeclaration? {
  // Get the containing class of the function
  val containingClass = function.findContainingClass() ?: return null

  // Iterate over all super types
  for (superType in containingClass.superTypes) {
    // Resolve the type declaration of the super type
    val superTypeDeclaration = superType.resolve().declaration

    // If it's a class or interface, check its functions
    if (superTypeDeclaration is KSClassDeclaration) {
      for (superFunction in superTypeDeclaration.getDeclaredFunctions()) {
        // Check if the function in the super type matches our function

        if (overrides(function, superFunction)) {
          return superFunction
        }
      }
    }
  }

  return null
}

/**
 * Returns the containing class of this function (if there is one).
 * Will return null for top level functions
 */
fun KSFunctionDeclaration.findContainingClass(): KSClassDeclaration? {
  return parentDeclaration as? KSClassDeclaration
}

/**
 * Returns the type of the parameter in the super function
 */
fun Resolver.findSuperTypeParameterType(parameter: KSValueParameter): KSTypeReference? {
  // Get the function that contains the parameter
  val function: KSFunctionDeclaration = parameter.parent as? KSFunctionDeclaration ?: return null

  val superFunction = findSuperFunction(function) ?: return null
  return getSuperParameter(function, superFunction, parameter)
}
