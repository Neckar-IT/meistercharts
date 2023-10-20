package it.neckar.ksp.boxing

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class BoxingVerifierProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    environment.logger.info("Creating ${BoxingVerifierProcessor::class.simpleName} for ${environment.platforms}")
    return BoxingVerifierProcessor(environment.codeGenerator, environment.logger)
  }
}
