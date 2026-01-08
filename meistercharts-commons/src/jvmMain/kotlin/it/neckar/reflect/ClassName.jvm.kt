package it.neckar.reflect

import it.neckar.open.kotlin.reflect.classForName
import kotlin.reflect.KClass

/**
 * Converts the ClassName to a KClass
 */
fun ClassName.asKClass(): KClass<*> {
  return classForName(value)
}
