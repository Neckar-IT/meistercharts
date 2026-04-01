/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.commons.kotlin.js

import kotlinx.browser.window
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Represents a key when accessing local storage
 */
value class LocalStorageKey(val value: String)

/**
 * Represents a prefix for a Key
 * @see LocalStorageKey
 */
value class LocalStorageKeyPrefix(val value: String)

object LocalStorageSupport {
  /**
   * The encoder that is used for local storage
   */
  private val localStorageEncoder = Json {
    prettyPrint = false
  }

  /**
   * Loads a value from local storage for the given key
   */
  fun <T> loadFromLocalStorage(key: LocalStorageKey, serializer: KSerializer<T>): T? {
    val valueAsString = window.localStorage.getItem(key.value)
    //console.log("loading $key from local storage --> ", valueAsString)

    if (valueAsString != null && valueAsString != "null") {
      return try {
        localStorageEncoder.decodeFromString(serializer, valueAsString)
      } catch (e: Exception) {
        console.warn("Could not load $key due to ${e.message}", e)
        console.info("retrieved content:", valueAsString)
        null
      }
    }

    return null
  }

  /**
   * Saves the value to local storage under the given key
   */
  inline fun <reified T> saveToLocalStorage(key: LocalStorageKey, value: T) {
    saveToLocalStorage(key, value, serializer())
  }

  fun <T> saveToLocalStorage(key: LocalStorageKey, value: T, serializer: KSerializer<T>) {
    val valueAsString = localStorageEncoder.encodeToString(serializer, value)
    window.localStorage.setItem(key.value, valueAsString)
    //console.log("saving $key to local storage --> ", valueAsString)
  }

  inline fun <reified T> saveToLocalStorageOptional(key: LocalStorageKey, value: T?) {
    saveToLocalStorage(key, value, serializer())
  }

  /**
   * Saves the provided value to local storage under the given key.
   *
   * If null is provided as value, the key will be removed from local storage
   */
  fun <T> saveToLocalStorageOptional(key: LocalStorageKey, value: T?, serializer: KSerializer<T>) {
    // Option 'ALL' will be selected
    if (value == null) {
      window.localStorage.removeItem(key.value)
    } else {
      saveToLocalStorage(key, value, serializer)
    }
  }

  /**
   * Deletes the value under the given key from local storage (if there is any)
   */
  fun deleteFromLocalStorage(key: LocalStorageKey) {
    window.localStorage.removeItem(key.value)
    //console.log("Removing from local storage for key $key")
  }
}
