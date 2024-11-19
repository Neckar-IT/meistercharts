package it.neckar.open.test.utils

import it.neckar.open.test.utils.ByTypeArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import javax.annotation.Nonnull
import kotlin.reflect.KClass

/**
 * Marks parameterized tests that are filled with methods/fields of a given type
 *
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ArgumentsSource(
  ByTypeArgumentsProvider::class
)
annotation class ByTypeSource(
  /**
   * The type of the field or return type of the method
   */
  @Nonnull val type: KClass<*>,
)
