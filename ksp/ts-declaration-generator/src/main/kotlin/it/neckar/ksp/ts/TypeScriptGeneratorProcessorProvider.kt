package it.neckar.ksp.ts

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class TypeScriptGeneratorProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): TypeScriptGeneratorProcessor {
    environment.logger.info("Creating ${TypeScriptGeneratorProcessor::class.simpleName} for ${environment.platforms}")
    return TypeScriptGeneratorProcessor(environment.codeGenerator, environment.logger, environment.options)
  }
}
