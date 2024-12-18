package it.neckar.open.test.utils.matchers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlin.contracts.contract

/**
 * Casts this JsonNode to an ObjectNode
 */
fun JsonNode.asObjectNode(): ObjectNode {
  contract {
    returns() implies (this@asObjectNode is ObjectNode)
  }

  return this as ObjectNode
}
