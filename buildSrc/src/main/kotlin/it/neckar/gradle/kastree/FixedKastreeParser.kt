package it.neckar.gradle.kastree

import kastree.ast.Node
import kastree.ast.psi.Converter
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * Copied from [kastree.ast.psi.Parser].
 * Necessary to update the kotlin log config for JDK 9+
 */
open class FixedKastreeParser(val converter: Converter = Converter) {
  protected val project: Project by lazy {
    KotlinCoreEnvironment.createForProduction(
      Disposer.newDisposable(),
      CompilerConfiguration().also {
        it.put(
          CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
          PrintingMessageCollector(System.err, MessageRenderer.PLAIN_RELATIVE_PATHS, false)
        )
      },
      EnvironmentConfigFiles.JVM_CONFIG_FILES
    ).project
  }

  fun parseFile(code: String, throwOnError: Boolean = true): Node.File {
    return converter.convertFile(parsePsiFile(code).also { file ->
      if (throwOnError) {
        file.collectDescendantsOfType<PsiErrorElement>().let {
          if (it.isNotEmpty()) {
            println("Error parsing code:")
            println(code)
            println("Errors found")
            it.forEach {
              println("$it")
              println("\tDescription: ${it.errorDescription}")
              println("\tText: ${it.text}")
              println("\tText Range: ${it.textRange}")
              println("\tContext: ${it.context}")

              var context = it.context
              while (context != null) {
                println("\t$context - ${context.textOffset}")
                context = context.context
              }

              println("\tText Offset: ${it.textOffset}")
            }

            throw ParseError(file, it)
          }
        }
      }
    })
  }

  fun parsePsiFile(code: String): KtFile =
    PsiManager.getInstance(project).findFile(LightVirtualFile("temp.kt", KotlinFileType.INSTANCE, code)) as KtFile

  data class ParseError(
    val file: KtFile,
    val errors: List<PsiErrorElement>,
  ) : IllegalArgumentException("Failed with ${errors.size} errors, first: ${errors.first().errorDescription}")

  companion object : FixedKastreeParser() {
    init {
      // To hide annoying warning on Windows
      System.setProperty("idea.use.native.fs.for.win", "false")
    }
  }

}
