package it.neckar.open.i18n

import it.neckar.open.annotations.JavaFriendly
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.inCurlyBraces
import it.neckar.open.i18n.resolve.FallbackTextResolver
import it.neckar.open.i18n.resolve.TextKeyTextResolver
import kotlin.jvm.JvmStatic

/**
 * A text service resolves a text key and formats the message with additional (but optional) parameters
 */
interface TextService {
  /**
   * Resolves the text for the given key and locale.
   * Formats the text using the provided [parameters] - if there are any.
   */
  operator fun get(
    key: TextKey,
    i18nConfiguration: I18nConfiguration,
    parameters: Map<String, Any>?
  ): String

  operator fun get(
    key: TextKey,
    i18nConfiguration: I18nConfiguration,
  ): String {
    return get(key, i18nConfiguration, null)
  }

  /**
   * Resolves an enumeration.
   *
   * Only the name of the enum is used - unless the category is provided
   */
  operator fun get(
    enumValue: Enum<*>,
    i18nConfiguration: I18nConfiguration,
    /**
     * The optional category for this enumeration
     */
    category: String? = null,
    parameters: Map<String, Any>? = null,
  ): String {
    return get(enumValue.toTextKey(category), i18nConfiguration, parameters)
  }

  companion object {
    operator fun invoke(): DefaultTextService {
      return DefaultTextService()
    }

    operator fun invoke(firstResolver: TextResolver, secondResolver: TextResolver? = null): DefaultTextService {
      return DefaultTextService().also {
        it.addTextResolver(firstResolver)

        if (secondResolver != null) {
          it.addTextResolver(secondResolver)
        }
      }
    }

    @JavaFriendly
    @JvmStatic
    fun withFallback(firstResolver: TextResolver): DefaultTextService {
      return DefaultTextService().also {
        it.addTextResolver(firstResolver)
        it.addFallbackTextResolver()
      }
    }
  }
}

/**
 * Creates a text key for an enum value.
 */
fun Enum<*>.toTextKey(category: String?): TextKey {
  val baseKey = this.name

  val key: String = if (category == null) {
    baseKey
  } else {
    "$baseKey.$category"
  }

  //TODO introduce cache!
  return TextKey(key)
}

/**
 * A service that manages various [TextResolver]s.
 *
 * The first [TextResolver] which does provide a non-null text 'wins'.
 *
 * Therefore, it is usually a good idea to use [addTextResolverAtFirst] that place
 * the new resolver at the first position.
 */
class DefaultTextService : TextService {
  /**
   * The list of registered resolvers. They are processed from first to last.
   */
  private val textResolvers = mutableListOf<TextResolver>()

  /**
   * Retrieves the text for the given key and locale.
   * Returns the key itself as fallback if there has no value been found.
   */
  override fun get(key: TextKey, i18nConfiguration: I18nConfiguration, parameters: Map<String, Any>?): String {
    val resolved = resolve(key, i18nConfiguration)

    //apply the parameters, if there are any
    if (parameters.isNullOrEmpty()) {
      return resolved
    }

    //Replace the message
    var replaced = resolved
    parameters.forEach {
      //TODO add number formatting!
      replaced = replaced.replace(it.key.inCurlyBraces(), it.value.toString())
    }
    return replaced
  }

  private fun resolve(key: TextKey, i18nConfiguration: I18nConfiguration): String {
    textResolvers.fastForEach { resolver ->
      resolver.resolve(key, i18nConfiguration)?.let {
        return it
      }
    }

    return key.key
  }


  /**
   * Adds a [TextResolver].
   *
   * Note that [resolver] overrules any previously added [TextResolver] if it returns a non-null value for a given [TextKey]
   */
  fun addTextResolverAtFirst(resolver: TextResolver) {
    textResolvers.add(0, resolver)
  }

  /**
   * Adds a text resolver at the *last* position. The newly added text resolver
   * will be queries *last* - after all other already registered resolvers.
   */
  fun addTextResolver(resolver: TextResolver) {
    textResolvers.add(resolver)
  }

  /**
   * Removes a [TextResolver].
   *
   * [TextResolver]s are processed in the order they are added.
   * The first [TextResolver] which does return a non-null text 'wins'.
   */
  fun removeTextResolver(resolver: TextResolver) {
    textResolvers.remove(resolver)
  }
}

/**
 * Resolves this [TextKey] using the given [textService]
 */
fun TextKey.resolve(textService: TextService, i18nConfiguration: I18nConfiguration, parameters: Map<String, Any>? = null): String {
  return textService[this, i18nConfiguration, parameters]
}

/**
 * Adds the [TextKeyTextResolver] as text provider to the text service
 */
fun DefaultTextService.addTextKeyTextResolver() {
  addTextResolver(TextKeyTextResolver)
}

/**
 * Adds the [FallbackTextResolver] as text provider to the text service
 */
fun DefaultTextService.addFallbackTextResolver() {
  addTextResolver(FallbackTextResolver)
}


/**
 * Wraps this text resolver into an [FallbackTextResolver]
 */
fun TextResolver.orFallbackText(): TextService {
  return TextService(this, FallbackTextResolver)
}
