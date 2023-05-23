/*
 * MIT License
 * <p>
 * Copyright (c) 2017 Ralf Stuckert
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
