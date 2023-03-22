/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.history

import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.fastFor
import kotlinx.serialization.Serializable
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmStatic

/**
 * Configuration for the history.
 *
 * Is used as central place where the number of data series and the natural insertion level are stored
 */
@Serializable
class HistoryConfiguration(
  /**
   * Configuration for all data series that contain a decimal value
   */
  val decimalConfiguration: HistoryDecimalConfiguration,
  /**
   * Configuration for all data series that contain an enum value (also booleans)
   */
  val enumConfiguration: HistoryEnumConfiguration,

  /**
   * Configuration for all data series that contain reference values
   */
  val referenceEntryConfiguration: HistoryReferenceEntryConfiguration,
) {

  /**
   * The total number of data series (of all types)
   */
  val totalDataSeriesCount: Int
    get() {
      return decimalDataSeriesCount + enumDataSeriesCount + referenceEntryDataSeriesCount
    }

  /**
   * The count of decimal data series
   */
  val decimalDataSeriesCount: Int
    get() {
      return decimalConfiguration.dataSeriesCount
    }

  /**
   * The count of enum data series
   */
  val enumDataSeriesCount: Int
    get() {
      return enumConfiguration.dataSeriesCount
    }

  /**
   * The count of object value data series
   */
  val referenceEntryDataSeriesCount: Int
    get() {
      return referenceEntryConfiguration.dataSeriesCount
    }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as HistoryConfiguration

    if (decimalConfiguration != other.decimalConfiguration) return false
    if (enumConfiguration != other.enumConfiguration) return false
    return referenceEntryConfiguration == other.referenceEntryConfiguration
  }

  override fun hashCode(): Int {
    var result = decimalConfiguration.hashCode()
    result = 31 * result + enumConfiguration.hashCode()
    result = 31 * result + referenceEntryConfiguration.hashCode()
    return result
  }

  /**
   * Dumps the history configuration
   */
  fun dump(): String {
    return buildString {
      appendLine("Decimals:")
      append(decimalConfiguration.dump())

      appendLine()
      appendLine("Enums:")
      append(enumConfiguration.dump())

      appendLine()
      appendLine("Reference Entries:")
      append(referenceEntryConfiguration.dump())
    }
  }

  companion object {
    /**
     * An empty history configuration without any data series
     */
    val empty: HistoryConfiguration = historyConfiguration { }

    /**
     * Creates a new history configuration with only decimal values
     */
    @JvmStatic
    fun onlyDecimals(decimalSeriesIds: IntArray, displayNames: Array<TextKey>): HistoryConfiguration {
      return HistoryConfiguration(
        HistoryDecimalConfiguration(
          dataSeriesIds = decimalSeriesIds,
          displayNames = displayNames.toList(),
          units = List(decimalSeriesIds.size) { HistoryUnit.None }
        ),
        HistoryEnumConfiguration.empty,
        HistoryReferenceEntryConfiguration.empty,
      )
    }
  }
}


/**
 * Creates a new history configuration
 */
fun historyConfiguration(config: HistoryConfigurationBuilder.() -> Unit): HistoryConfiguration {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  return HistoryConfigurationBuilder().also(config).build()
}

/**
 * Creates a configuration for a history chart with decimal, enum, and reference entry data series.
 *
 * @param decimalDataSeriesCount the number of decimal data series in the configuration
 * @param enumDataSeriesCount the number of enum data series in the configuration
 * @param referenceEntrySeriesCount the number of reference entry data series in the configuration
 * @param decimalDataSeriesInitializer a lambda expression used to initialize the configuration for each decimal data series
 * @param enumDataSeriesInitializer a lambda expression used to initialize the configuration for each enum data series
 * @param referenceEntryDataSeriesInitializer a lambda expression used to initialize the configuration for each reference entry data series
 * @return a `HistoryConfiguration` object that contains the configuration for the chart
 * @throws IllegalArgumentException if the number of created data series entries does not match the given count of data series
 */
fun historyConfiguration(
  decimalDataSeriesCount: Int,
  enumDataSeriesCount: Int,
  referenceEntrySeriesCount: Int,

  decimalDataSeriesInitializer: HistoryConfigurationBuilder.(dataSeriesIndex: DecimalDataSeriesIndex) -> Unit,
  enumDataSeriesInitializer: HistoryConfigurationBuilder.(dataSeriesIndex: EnumDataSeriesIndex) -> Unit,
  referenceEntryDataSeriesInitializer: HistoryConfigurationBuilder.(dataSeriesIndex: ReferenceEntryDataSeriesIndex) -> Unit,
): HistoryConfiguration {

  return historyConfiguration {
    decimalDataSeriesCount.fastFor {
      decimalDataSeriesInitializer(DecimalDataSeriesIndex(it))
    }
    enumDataSeriesCount.fastFor {
      enumDataSeriesInitializer(EnumDataSeriesIndex(it))
    }
    referenceEntrySeriesCount.fastFor {
      referenceEntryDataSeriesInitializer(ReferenceEntryDataSeriesIndex(it))
    }
  }.also {
    require(it.decimalDataSeriesCount == decimalDataSeriesCount) {
      "You need to create a decimal data series entry for each data series. Was ${it.decimalDataSeriesCount} but expected $decimalDataSeriesCount"
    }
    require(it.enumDataSeriesCount == enumDataSeriesCount) {
      "You need to create a enum data series entry for each data series. Was ${it.decimalDataSeriesCount} but expected $decimalDataSeriesCount"
    }
    require(it.referenceEntryDataSeriesCount == referenceEntrySeriesCount) {
      "You need to create a referenceEntry data series entry for each data series. Was ${it.decimalDataSeriesCount} but expected $decimalDataSeriesCount"
    }
  }
}

/**
 * Creates a new history configuration for the given number of data series.
 *
 * IMPORTANT: It is necessary to call [HistoryConfigurationBuilder.enumDataSeries] from the given [dataSeriesInitializer]
 */
fun historyConfigurationOnlyDecimals(
  decimalDataSeriesCount: Int,
  dataSeriesInitializer: HistoryConfigurationBuilder.(dataSeriesIndex: DecimalDataSeriesIndex) -> Unit,
): HistoryConfiguration {
  contract {
    callsInPlace(dataSeriesInitializer, InvocationKind.UNKNOWN)
  }

  return historyConfiguration {
    decimalDataSeriesCount.fastFor {
      dataSeriesInitializer(DecimalDataSeriesIndex(it))
    }
  }
}

fun historyConfigurationOnlyEnums(
  enumDataSeriesCount: Int,
  dataSeriesInitializer: HistoryConfigurationBuilder.(dataSeriesIndex: EnumDataSeriesIndex) -> Unit,
): HistoryConfiguration {
  contract {
    callsInPlace(dataSeriesInitializer, InvocationKind.UNKNOWN)
  }

  return historyConfiguration {
    enumDataSeriesCount.fastFor {
      dataSeriesInitializer(EnumDataSeriesIndex(it))
    }
  }
}

fun historyConfigurationOnlyReferenceEntries(
  referenceEntryDataSeriesCount: Int,
  dataSeriesInitializer: HistoryConfigurationBuilder.(dataSeriesIndex: ReferenceEntryDataSeriesIndex) -> Unit,
): HistoryConfiguration {
  contract {
    callsInPlace(dataSeriesInitializer, InvocationKind.UNKNOWN)
  }

  return historyConfiguration {
    referenceEntryDataSeriesCount.fastFor {
      dataSeriesInitializer(ReferenceEntryDataSeriesIndex(it))
    }
  }
}

/**
 * Creates a default history configuration with the provided sizes.
 * Adds some default values
 */
fun createDefaultHistoryConfiguration(
  decimalValuesCount: Int,
  enumValuesCount: Int,
  referenceEntrySeriesCount: Int,

  enumProvider: (EnumDataSeriesIndex) -> HistoryEnum = { HistoryEnum.Boolean },
  referenceEntryStatusEnumProvider: (ReferenceEntryDataSeriesIndex) -> HistoryEnum = { HistoryEnum.Active },
): HistoryConfiguration = historyConfiguration(decimalValuesCount, enumValuesCount, referenceEntrySeriesCount,

  decimalDataSeriesInitializer = { dataSeriesIndex ->
    val dataSeriesId = DataSeriesId(dataSeriesIndex.value * 100)
    decimalDataSeries(dataSeriesId, TextKey.simple("DS.$dataSeriesId"), HistoryUnit.None)
  },
  enumDataSeriesInitializer = { dataSeriesIndex ->
    val dataSeriesId = DataSeriesId(dataSeriesIndex.value * 1000)
    enumDataSeries(dataSeriesId, TextKey.simple("DS.$dataSeriesId"), enumProvider(dataSeriesIndex))
  },
  referenceEntryDataSeriesInitializer = { dataSeriesIndex ->
    val dataSeriesId = DataSeriesId(dataSeriesIndex.value * 10_000)
    referenceEntryDataSeries(dataSeriesId, TextKey.simple("DS.$dataSeriesId"), referenceEntryStatusEnumProvider(dataSeriesIndex))
  })


/**
 * Builder for a history configuration.
 * Usually this class should not be used directly. Instead, use the method [historyConfigurationOnlyDecimals] with a lambda to configure the builder.
 */
class HistoryConfigurationBuilder {
  private val decimalConfigurationBuilder = HistoryDecimalConfiguration.Builder()
  private val enumConfigurationBuilder = HistoryEnumConfiguration.Builder()
  private val referenceEntryConfigurationBuilder = HistoryReferenceEntryConfiguration.Builder()

  fun decimalDataSeries(id: DataSeriesId, displayName: String, unit: HistoryUnit = HistoryUnit.None) {
    decimalConfigurationBuilder.decimalDataSeries(id, displayName, unit)
  }

  fun decimalDataSeries(id: DataSeriesId, displayName: TextKey, unit: HistoryUnit = HistoryUnit.None) {
    decimalConfigurationBuilder.decimalDataSeries(id, displayName, unit)
  }

  fun enumDataSeries(id: DataSeriesId, displayName: String, enumConfiguration: HistoryEnum) {
    enumConfigurationBuilder.enumDataSeries(id, TextKey.simple(displayName), enumConfiguration)
  }

  fun enumDataSeries(id: DataSeriesId, displayName: TextKey, enumConfiguration: HistoryEnum) {
    enumConfigurationBuilder.enumDataSeries(id, displayName, enumConfiguration)
  }

  fun referenceEntryDataSeries(id: DataSeriesId, displayName: String, statusEnum: HistoryEnum?) {
    referenceEntryConfigurationBuilder.referenceEntryDataSeries(id, displayName, statusEnum)
  }

  fun referenceEntryDataSeries(id: DataSeriesId, displayName: TextKey, statusEnum: HistoryEnum?) {
    referenceEntryConfigurationBuilder.referenceEntryDataSeries(id, displayName, statusEnum)
  }

  companion object {
    operator fun invoke(config: HistoryConfigurationBuilder.() -> Unit): HistoryConfiguration {
      return HistoryConfigurationBuilder()
        .also(config)
        .build()
    }
  }

  /**
   * Returns the history configuration
   */
  fun build(): HistoryConfiguration {
    return HistoryConfiguration(
      decimalConfigurationBuilder.build(),
      enumConfigurationBuilder.build(),
      referenceEntryConfigurationBuilder.build(),
    )
  }
}
