package it.neckar.open.provider

import it.neckar.open.collections.Cache
import it.neckar.open.collections.cache
import it.neckar.open.kotlin.lang.getModulo
import it.neckar.open.kotlin.lang.getModuloOrElse
import kotlin.jvm.JvmStatic
import kotlin.reflect.KProperty0

/**
 * Returns values for a given index.
 * Does *not* have a size. In most cases it is helpful to use a [SizedProvider] instead
 *
 * ATTENTION: Do not replace this interface with lambdas. Lambdas do not support primitive types (at least in JVM 1.8).
 * Therefore, the index is always boxed!
 *
 * @param IndexContext The type for the index.
 * This should be either:
 * - a value class wrapping an index
 * - an annotation annotated with [MultiProviderIndexContextAnnotation] - if a value class seems to be overkill
 * To avoid boxing this value is *not* used as parameter for [valueAt] (but should in theory).
 *
 * Please create extension methods for [valueAt] that take the value class as parameter and delegate accordingly.
 */
fun interface MultiProvider<in IndexContext, out T> : MultiProvider1<IndexContext, T, Any> {
  /**
   * Retrieves the value at the given [index].
   *
   * Please use extension methods with the correct type instead (if possible)
   */
  fun valueAt(index: Int): T

  override fun valueAt(index: Int, param1: Any): T {
    return valueAt(index)
  }

  companion object {
    /**
     * Wraps the given list into a [MultiProvider].
     * Does *NOT* support empty lists
     */
    @JvmStatic
    fun <IndexContext, T> forListModulo(values: List<T>): MultiProvider<IndexContext, T> {
      return MultiProvider { index -> values.getModulo(index) }
    }

    @JvmStatic
    fun <IndexContext, T> forListModuloProvider(values: List<() -> T>): MultiProvider<IndexContext, T> {
      return MultiProvider { index -> values.getModulo(index).invoke() }
    }

    @JvmStatic
    fun <IndexContext, T> forListModuloProvider(values: List<() -> T>, fallback: T): MultiProvider<IndexContext, T> {
      return MultiProvider { index -> values.getModuloOrElse(index) { fallback }.invoke() }
    }

    @JvmStatic
    fun <IndexContext, T> modulo(vararg values: T): MultiProvider<IndexContext, T> {
      return forListModulo(values.toList())
    }

    @JvmStatic
    fun <IndexContext, T> moduloProvider(vararg valueProviders: () -> T): MultiProvider<IndexContext, T> {
      return moduloProvider(valueProviders.toList())
    }

    @JvmStatic
    fun <IndexContext, T> moduloProvider(valueProviders: List<() -> T>): MultiProvider<IndexContext, T> {
      return MultiProvider { index -> valueProviders.getModulo(index).invoke() }
    }

    /**
     * Wraps the given list into a [MultiProvider].
     * Returns the fallback value if the given [values] list is empty
     */
    @JvmStatic
    fun <IndexContext, T> forListModulo(values: List<T>, fallback: T): MultiProvider<IndexContext, T> {
      if (values.isEmpty()) {
        return always(fallback)
      }

      return forListModulo(values)
    }

    /**
     * Returns the values from the list - and null for all indices that are not contained in the given list.
     * Supports empty lists
     */
    @JvmStatic
    fun <IndexContext, T> forListOrNull(values: List<T>): MultiProvider<IndexContext, T?> {
      return MultiProvider { index -> values.getOrNull(index) }
    }

    /**
     * Returns the values from the list - and the fallback for all indices that are not contained in the given list.
     * Supports empty lists
     */
    @JvmStatic
    fun <IndexContext, T> forListOr(values: List<T>, fallback: T): MultiProvider<IndexContext, T> {
      return MultiProvider { index -> values.getOrNull(index) ?: fallback }
    }

    /**
     * Returns the values from the list - and calls the [fallbackProvider] for all indices that are not contained in the given list.
     *
     * Attention: Do not create new objects in the [fallbackProvider] or use [cached] to avoid unnecessary object instantiation
     */
    @JvmStatic
    fun <IndexContext, T> forListOr(values: List<T>, fallbackProvider: () -> T): MultiProvider<IndexContext, T> {
      return MultiProvider { index -> values.getOrNull(index) ?: fallbackProvider() }
    }

    /**
     * Returns the values of the list or throws an exception if there is no element in the list at the requested index.
     */
    @JvmStatic
    fun <IndexContext, T> forListOrException(values: List<T>): MultiProvider<IndexContext, T> {
      return MultiProvider { index -> values[index] }
    }

    /**
     * Always returns the given value - for all indices
     */
    @JvmStatic
    fun <IndexContext, T> always(value: T): MultiProvider<IndexContext, T> {
      return MultiProvider { value }
    }

    @JvmStatic
    fun <IndexContext, T> alwaysProvider(valueProvider: () -> T): MultiProvider<IndexContext, T> {
      return MultiProvider { valueProvider() }
    }

    /**
     * Creates new instances for each index - as required.
     * The provider is called for each index exactly once. The returned value is then cached
     */
    @JvmStatic
    fun <IndexContext, T> cached(provider: (index: Int) -> T): CachedMultiProvider<IndexContext, T> {
      return MultiProvider<IndexContext, T> { provider(it) }.cached(100)
    }

    /**
     * Throws an exception if called.
     * This object should only be used if it is ensured that [valueAt] is not called.
     */
    @JvmStatic
    fun <IndexContext, T> empty(): MultiProvider<IndexContext, T> {
      return forListOrException(emptyList())
    }

    /**
     * Returns a multi provider that always returns null
     */
    @JvmStatic
    fun alwaysNull(): MultiProvider<Any, Nothing?> {
      return MultiProvider { null }
    }

    /**
     * Wraps the given lambda into a multi provider
     */
    operator fun <IndexContext, T> invoke(lambda: (index: Int) -> T): MultiProvider<IndexContext, T> {
      return MultiProvider { index -> lambda(index) }
    }

    /**
     * Creates a new multi provider that delegates all calls to the current value of this property
     */
    fun <IndexContext, T> delegating(provider: () -> MultiProvider<IndexContext, T>): MultiProvider<IndexContext, T> {
      return MultiProvider { index -> provider().valueAt(index) }
    }
  }
}

/**
 * Returns a [MultiProvider] that delegates all calls to the current value of this property
 */
fun <IndexContext, T> KProperty0<MultiProvider<IndexContext, T>>.delegate(): MultiProvider<IndexContext, T> {
  return MultiProvider { index -> this@delegate.get().valueAt(index) }
}

/**
 * Maps the value.
 *
 * ATTENTION: Creates a new instance!
 *
 * ATTENTION: If using in styles, usually a delegate should be used first:
 *
 * `::baseProvider.delegate().map{...}`
 */
fun <IndexContext, T, R> MultiProvider<IndexContext, T>.mapped(function: (T) -> R): MultiProvider<IndexContext, R> {
  return MultiProvider.invoke {
    function(this.valueAt(it))
  }
}

fun <IndexContext, T> MultiProvider<IndexContext, () -> T>.resolved(): MultiProvider<IndexContext, T> {
  return MultiProvider.invoke {
    this.valueAt(it).invoke()
  }
}


/**
 * Marker annotation for annotations that can be used as IndexContent
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class MultiProviderIndexContextAnnotation


/**
 * Caches the results of a multi provider
 */
open class CachedMultiProvider<in IndexContext, T>(
  /**
   * The delegate that is used to create the multi
   */
  val delegate: MultiProvider<IndexContext, T>,
  /**
   * The maximum size of the cache
   */
  cacheSize: Int = 500,
) : MultiProvider<IndexContext, T> {

  /**
   * The cache
   */
  val cache: Cache<Int, T> = cache("CachedMultiProvider", cacheSize)

  override fun valueAt(index: Int): T {
    return cache.getOrStore(index) {
      delegate.valueAt(index)
    }
  }

  /**
   * Clears the cache
   */
  fun clear() {
    cache.clear()
  }
}

/**
 * Caches the multi provider
 */
fun <IndexContext, T> MultiProvider<IndexContext, T>.cached(cacheSize: Int = 100): CachedMultiProvider<IndexContext, T> {
  return CachedMultiProvider(this, cacheSize)
}
