/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.demo

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Verifies that every demo descriptor class in the descriptors source directory is registered
 * in [DemoDescriptors.descriptors].
 *
 * Works on the source files - a forgotten registration would otherwise silently hide the demo
 * from the demo application and from the E2E tests.
 */
class DemoDescriptorsRegistrationTest {
  companion object {
    /** Module-root-relative path to the demo package holding the descriptors. */
    private const val DemoPackagePath = "src/commonMain/kotlin/com/meistercharts/demo"

    /** The demo package of this module; Gradle runs the test in the module directory. */
    private fun resolveDemoPackageDir(): File = File(DemoPackagePath)
  }

  @Test
  fun `every demo descriptor class is registered in DemoDescriptors`() {
    val demoPackageDir = resolveDemoPackageDir()

    val descriptorsDir = demoPackageDir.resolve("descriptors")
    require(descriptorsDir.isDirectory) { "Descriptors directory not found: ${descriptorsDir.absolutePath}" }

    //Top-level, non-private descriptor classes: "class XxxDemoDescriptor :" / "class XxxDemoDescriptor("
    val descriptorClassRegex = Regex("""^(?:open |abstract )?class (\w+DemoDescriptor)\s*[:(<]""", RegexOption.MULTILINE)
    val abstractClassRegex = Regex("""^abstract class (\w+DemoDescriptor)""", RegexOption.MULTILINE)

    val declaredDescriptorClasses = descriptorsDir.walkTopDown()
      .filter { it.isFile && it.extension == "kt" }
      .flatMap { file ->
        val content = file.readText()
        val abstractClasses = abstractClassRegex.findAll(content).map { it.groupValues[1] }.toSet()
        descriptorClassRegex.findAll(content)
          .map { it.groupValues[1] }
          .filter { it !in abstractClasses }
      }
      .toSortedSet()

    check(declaredDescriptorClasses.size > 250) {
      "Suspiciously few descriptor classes found (${declaredDescriptorClasses.size}) - regex out of sync with the sources?"
    }

    val registrySource = demoPackageDir.resolve("DemoDescriptors.kt").readText()

    val unregistered = declaredDescriptorClasses.filter { className ->
      "$className(".toRegex(RegexOption.LITERAL).containsMatchIn(registrySource).not()
    }

    assertThat(unregistered, "descriptor classes missing in DemoDescriptors.descriptors").isEmpty()
  }

  @Test
  fun `all runnable demo names are unique`() {
    val duplicates = DemoDescriptors.runnableDemos
      .groupBy { it.uniqueDemoName() }
      .filterValues { it.size > 1 }
      .keys

    assertThat(duplicates, "duplicated unique demo names").isEmpty()
  }
}
