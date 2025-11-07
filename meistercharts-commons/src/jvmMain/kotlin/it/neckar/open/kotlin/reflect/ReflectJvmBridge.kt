package it.neckar.open.kotlin.reflect

import kotlin.reflect.KClass

/**
 * Transforms a (Kotlin) fully qualified class name into a format suitable for `Class.forName()`.
 *
 * This method processes a fully qualified class name (FQCN) and converts it into a format
 * that is compatible with `Class.forName()`, especially handling inner class names correctly.
 * It identifies the part of the FQCN that represents classes (including inner classes),
 * which start with an uppercase letter, and transforms the dots (.) to dollar signs ($)
 * for these class names. This is essential because Java uses dollar signs to distinguish
 * inner classes in its internal naming convention.
 *
 * @param fqName The fully qualified class name to be transformed. It can be a name of
 *               a top-level class or an inner class.
 * @return A string representing the transformed class name, suitable for use with
 *         `Class.forName()`. If the input does not represent an inner class, it returns
 *         the original name.
 */
fun fqNameToClassForName(fqName: String): String {
  val parts = fqName.split('.')

  // Find the index where the name parts start with an uppercase letter.
  val classIndex = parts.indexOfFirst { it.isNotEmpty() && it[0].isUpperCase() }

  // If no part starts with an uppercase letter, return the original name.
  if (classIndex == -1) return fqName

  // Reassemble the name, replacing dots with dollar signs from the found index onwards.
  // A dot is added between the package name and the first class name.
  return parts.subList(0, classIndex).joinToString(".") +
    "." +
    parts.subList(classIndex, parts.size).joinToString("$")
}

/**
 * Creates a new KClass instance for the given fully qualified class name.
 */
fun classForName(className: String): KClass<*> {
  val fqNameWithoutNullable = className.removeSuffix("?")   //Remove the potential nullable suffix
  val name = fqNameToClassForName(fqNameWithoutNullable)
  return Class.forName(name).kotlin
}
