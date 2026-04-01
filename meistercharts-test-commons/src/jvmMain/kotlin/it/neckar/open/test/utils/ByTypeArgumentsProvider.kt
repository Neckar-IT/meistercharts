/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.AnnotationConsumer
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.stream.Stream
import kotlin.reflect.KClass

/**
 * Extracts test data from fields
 *
 */
class ByTypeArgumentsProvider : ArgumentsProvider, AnnotationConsumer<ByTypeSource> {
  private lateinit var type: KClass<*>

  override fun accept(annotation: ByTypeSource) {
    type = annotation.type
  }

  override fun provideArguments(context: ExtensionContext): Stream<Arguments> {
    val fieldsValues = getFieldsValues(context)
    val methodValues = getMethodValues(context)
    check(fieldsValues.isEmpty().not() || methodValues.isEmpty().not()) { "No data fields/methods found in ${context.requiredTestClass.name} for annotation <$type>" }
    return Stream.concat(fieldsValues.stream(), methodValues.stream())
  }

  private fun getFieldsValues(context: ExtensionContext): List<Arguments> {
    val entries: MutableList<Arguments> = ArrayList()

    for (declaredField in context.requiredTestClass.declaredFields) {
      if (declaredField.type == type.java) {

        //Warnings when marked as DataPoint
        for (declaredAnnotation in declaredField.declaredAnnotations) {
          if (declaredAnnotation.annotationClass.simpleName == "org.junit.experimental.theories.DataPoint") {
            System.err.println("Remove @DataPoint annotation at " + javaClass.name + "." + declaredField.name)
          }
        }
        try {
          val value = getValue(declaredField, context.testInstance.orElse(null))
          entries.add(toArguments(value))
        } catch (e: Exception) {
          throw RuntimeException("Error accessing " + declaredField.name, e)
        }
      }
    }
    return entries
  }

  private fun getMethodValues(context: ExtensionContext): List<Arguments> {
    val entries: MutableList<Arguments> = ArrayList()
    for (declaredMethod in context.requiredTestClass.declaredMethods) {
      if (declaredMethod.isSynthetic) {
        continue
      }
      if (declaredMethod.returnType == type.java) {
        //Warnings when marked as DataPoint
        for (declaredAnnotation in declaredMethod.declaredAnnotations) {
          if (declaredAnnotation.annotationClass.simpleName == "org.junit.experimental.theories.DataPoint") {
            System.err.println("Remove @DataPoint annotation at " + javaClass.name + "." + declaredMethod.name)
          }
        }
        try {
          val value = getValue(declaredMethod, context.testInstance.orElse(null))
          entries.add(toArguments(value))
        } catch (e: Exception) {
          throw RuntimeException(e)
        }
      }
    }
    return entries
  }

  companion object {
    private operator fun getValue(declaredField: Field, testInstance: Any?): Any {
      //Warning when not static!
      if (Modifier.isStatic(declaredField.modifiers)) {
        declaredField.isAccessible = true
        return declaredField[null]
      }
      checkNotNull(testInstance) { "Test instance required" }
      return declaredField[testInstance]
    }

    private operator fun getValue(declaredMethod: Method, testInstance: Any?): Any {
      //Warning when not static!
      if (Modifier.isStatic(declaredMethod.modifiers)) {
        return declaredMethod.invoke(null)
      }
      checkNotNull(testInstance) { "Test instance required" }
      return declaredMethod.invoke(testInstance)
    }

    private fun toArguments(item: Any): Arguments {
      if (item is Arguments) {
        return item
      }

      return if (item is Array<*>) {
        Arguments.arguments(*item)
      } else Arguments.arguments(item)
    }
  }
}
