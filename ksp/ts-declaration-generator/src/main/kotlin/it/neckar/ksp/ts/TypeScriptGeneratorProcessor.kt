package it.neckar.ksp.ts

import com.google.devtools.ksp.getPropertyDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import java.io.File

class TypeScriptGeneratorProcessor(val codeGenerator: CodeGenerator, val logger: KSPLogger, val options: Map<String, String>) : SymbolProcessor {
  /**
   * Avoid repeated execution.
   * If the process shall create content for generated files, this must be updated.
   */
  private var runCount = 0

  override fun process(resolver: Resolver): List<KSAnnotated> {
    if (runCount > 0) {
      logger.info("Already run $runCount times. Skipping.")
      runCount++
      return emptyList()
    }
    runCount++

    logger.info("Processing symbols")
    logger.info("Options size: ${options.size}")
    options.forEach { (t, u) ->
      logger.info("Option $t = $u")
    }

    val namespace = options["namespace"]?.trim() ?: throw IllegalStateException("namespace not set")
    require(namespace.length > 4) {
      "namespace must be at least 4 characters long but was $namespace"
    }

    //The annotation name that are used to detekt symbols to export
    val annotationName = requireNotNull(options["annotationName"]) {
      "annotationName not set"
    }

    //The file that is created
    val typeScriptDefinitionFileName = requireNotNull(options["typeScriptDefinitionFileName"]) {
      "typeScriptDefinitionFileName not set"
    }
    val typeScriptFileName = requireNotNull(options["typeScriptFileName"]) {
      "typeScriptFileName not set"
    }

    codeGenerator.createNewFileByPath(Dependencies.ALL_FILES, typeScriptDefinitionFileName, extensionName = "")
      .bufferedWriter().use { writer ->
        writer.appendLine("export as namespace $namespace;")

        val visitor = ExportTypescriptDefinitionFileVisitor(writer, logger)
        val loggerVisitor = LoggerVisitor(codeGenerator, logger)

        resolver.getSymbolsWithAnnotation(annotationName).forEach {
          logger.info("Found annotated symbol $it of type ${it::class}")
          it.accept(visitor, GeneratingContext())
          it.accept(loggerVisitor, 0)
        }
      }

    codeGenerator.createNewFileByPath(Dependencies.ALL_FILES, typeScriptFileName, extensionName = "")
      .bufferedWriter().use { writer ->
        val visitor = ExportTypescriptFileVisitor(writer, logger)

        resolver.getSymbolsWithAnnotation(annotationName).forEach {
          logger.info("Found annotated symbol $it of type ${it::class}")
          it.accept(visitor, GeneratingContext())
        }
      }

    return emptyList()
  }
}


private fun collectFilesToExport(exportConfigFile: File): List<File> {
  val fileNames = exportConfigFile.readLines().filter { it.isNotBlank() }

  val baseDir = exportConfigFile.parentFile

  return fileNames.map {
    File(baseDir, it)
  }.onEach {
    require(it.exists()) {
      "File to export does not exist @ ${it.absolutePath}"
    }
  }
}
