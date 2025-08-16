package it.neckar.commons.logback.loki

import com.github.loki4j.logback.Loki4jAppender
import com.github.loki4j.logback.PipelineConfigAppenderBase.BatchCfg

fun Loki4jAppender.configureBatch(config: BatchCfg.() -> Unit) {
  val batchCfg = BatchCfg()
  batchCfg.config()
  setBatch(batchCfg)
}
