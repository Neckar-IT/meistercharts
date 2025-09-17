package it.neckar.open.test.utils

import assertk.*
import assertk.assertions.support.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.JacksonYAMLParseException
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import java.io.StringWriter
import java.net.URL
import java.nio.charset.Charset
import javax.annotation.Nonnull

typealias YamlNode = JsonNode

/**
 * YAML Utils for comparing YAML strings or files.
 */
object YamlUtils {
  @JvmStatic
  @JvmOverloads
  fun assertYamlEquals(expected: URL, actual: String, charset: Charset = Charsets.UTF_8) {
    assertYamlEquals(AssertUtils.toString(expected, charset), actual)
  }


  @JvmStatic
  @JvmOverloads
  fun assertYamlEquals(
    expected: String?, actual: String?,
    actualTreeModifier: YamlNode.() -> Unit = {},
  ) {
    // Null/blank guard
    if (expected.isNullOrBlank() || actual.isNullOrBlank()) {
      val exp = formatYaml(expected ?: "").trim()
      val act = formatYaml(actual ?: "").trim()
      assertThat(exp).fail(exp, act)
    }

    try {
      // YAML parser
      val yamlMapper = ObjectMapper(
        YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
      )
      // JSON serializer for canonical comparison
      val jsonMapper = ObjectMapper()

      val expectedTree = yamlMapper.readTree(expected)
      val actualTree = yamlMapper.readTree(actual).also(actualTreeModifier)

      if (expectedTree != actualTree) {
        val expectedJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedTree)
        val actualJson   = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualTree)

        val result = JSONCompare.compareJSON(expectedJson, actualJson, JSONCompareMode.STRICT)

        if (result.failed()) {
          System.err.println("           Pointer               | Expected                                -- Actual")
          System.err.println("---------------------------------------------------------------------------------------------------")

          // field failures with values
          result.fieldFailures.forEach {
            System.err.println(
              "Failure: ${it.field.padEnd(23)} | ${it.expected?.toString()?.padEnd(40)} -- ${it.actual?.toString()?.padEnd(40)}"
            )
          }
          // missing fields (present in expected, absent in actual)
          result.fieldMissing.forEach { fieldComparisonFailure ->
            System.err.println("Missing: ${fieldComparisonFailure.field.padEnd(23)} | ${"present".padEnd(40)} -- ${"absent".padEnd(40)}")
          }
          // unexpected fields (absent in expected, present in actual)
          result.fieldUnexpected.forEach { fieldComparisonFailure ->
            System.err.println("Unexpected: ${fieldComparisonFailure.field.padEnd(20)} | ${"absent".padEnd(40)} -- ${"present".padEnd(40)}")
          }

          val expYaml = formatYaml(expected ?: "").trim()
          val actYaml = formatYaml(yamlMapper.writeValueAsString(actualTree)).trim()
          assertThat(expYaml).fail(expYaml, actYaml)
        }
      }
    } catch (e: JacksonYAMLParseException) {
      throw AssertionError("YAML parsing error (${e.message})\nActual:\n${formatYaml(actual ?: "").trim()}", e)
    } catch (e: Exception) {
      throw AssertionError("YAML processing failed: ${e.message}\nActual:\n${formatYaml(actual ?: "").trim()}", e)
    }
  }


  @JvmStatic
  @Nonnull
  fun formatYaml(yaml: String?): String {
    return try {
      val mapper = ObjectMapper(
        YAMLFactory()
          .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER) // no '---'
      )
      val tree = mapper.readTree(yaml)
      val out = StringWriter()

      mapper.writerWithDefaultPrettyPrinter()
        .writeValue(out, tree)

      out.toString()
    } catch (_: Exception) {
      yaml.toString()
    }
  }
}

