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
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import java.io.File
import java.io.IOException
import java.lang.reflect.Parameter
import javax.annotation.Nonnull

/**
 * Extension that fills a File parameter with a temporary file or folder.
 * Use [WithTempFiles] at the class/method and add [TempFolder] oder [TempFile] to the test method parameters
 */
class TemporaryFolderExtension : AbstractResourceProvidingExtension<TemporaryFolder>(TemporaryFolder::class.java) {

  override fun createResource(extensionContext: ExtensionContext): TemporaryFolder {
    return TemporaryFolder()
  }

  @Throws(ParameterResolutionException::class)
  override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
    if (super.supportsParameter(parameterContext, extensionContext)) {
      return true
    }
    if (parameterContext.parameter.type.isAssignableFrom(File::class.java)) {
      return if (parameterContext.parameter.isAnnotationPresent(TempFolder::class.java)) {
        true
      } else parameterContext.parameter.isAnnotationPresent(TempFile::class.java)
    }
    return if (parameterContext.parameter.type.isAssignableFrom(TemporaryFolder::class.java)) {
      true
    } else false
  }

  @Throws(ParameterResolutionException::class, IOException::class)
  override fun convertResourceForParameter(parameter: Parameter, resource: TemporaryFolder): Any {
    if (parameter.type.isAssignableFrom(TemporaryFolder::class.java)) {
      return resource
    }
    if (parameter.isAnnotationPresent(TempFolder::class.java)) {
      return resource.newFolder()
    }
    if (parameter.isAnnotationPresent(TempFile::class.java)) {
      val annotation = parameter.getAnnotation(TempFile::class.java)
      return if (annotation.value.isNotEmpty()) {
        resource.newFile(annotation.value)
      } else resource.newFile()
    }
    throw ParameterResolutionException("unable to resolve parameter for $parameter")
  }

  override fun cleanup(@Nonnull resource: TemporaryFolder) {
    resource.delete()
  }
}
