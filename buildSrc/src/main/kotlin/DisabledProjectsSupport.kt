import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import java.io.File

/**
 * Support for disabled projects
 */
data class DisabledProjectsSupport(
  val disabledProjectPrefixes: Set<String>,
  val forceEnabledProjectPrefixes: Set<String>,
) {
  fun isDisabled(projectPath: String): Boolean {
    val disabled = disabledProjectPrefixes.any {
      projectPath.startsWith(it)
    }
    val forceEnabled = forceEnabledProjectPrefixes.any {
      projectPath.startsWith(it)
    }

    return disabled && forceEnabled.not()
  }

  companion object {
    /**
     * Very lenient parser that should accept most JSON5 features
     */
    private val mapper: JsonMapper = JsonMapper.builder()
      .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
      .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
      .enable(JsonReadFeature.ALLOW_YAML_COMMENTS)
      .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
      .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
      .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
      .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
      .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
      .enable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS)
      .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
      .build()

    /**
     * Creates a new instance from the given configuration files.
     */
    fun load(
      /**
       * The (optional) file containing the disabled projects
       */
      disabledProjectsFile: File,
      /**
       * Special file that contains projects that can not be build on macOS
       */
      disabledOnMacOsFile: File,
    ): DisabledProjectsSupport {
      val disabledProjectPrefixes: MutableSet<String> = mutableSetOf()
      val forceEnabledProjectPrefixes: MutableSet<String> = mutableSetOf()

      if (disabledProjectsFile.isFile) {
        val json = mapper.readTree(disabledProjectsFile.readText())
        val forceEnabledNode: JsonNode? = json.get("forceEnabled")
        val disabledNode: JsonNode? = json.get("disabled")

        disabledNode?.map { it.textValue() }?.forEach {
          disabledProjectPrefixes.add(it)
        }

        forceEnabledNode?.map { it.textValue() }?.forEach {
          forceEnabledProjectPrefixes.add(it)
        }
      }

      require(disabledOnMacOsFile.exists()) { "File $disabledOnMacOsFile does not exist" }
      val isMacOs = System.getProperty("os.name").contains("Mac OS X")

      if (isMacOs) {
        mapper.readTree(disabledOnMacOsFile.readText()).map { it.textValue() }.forEach {
          println("||||--> $it")
          disabledProjectPrefixes.add(it)
        }
      }

      return DisabledProjectsSupport(disabledProjectPrefixes, forceEnabledProjectPrefixes)
    }

    fun empty(): DisabledProjectsSupport {
      return DisabledProjectsSupport(emptySet(), emptySet())
    }
  }
}
