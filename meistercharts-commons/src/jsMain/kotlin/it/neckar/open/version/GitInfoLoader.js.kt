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
package it.neckar.open.version

/**
 * JS implementation: resolves a single git property from the injected deploy metadata.
 *
 * Chain: `globalThis.__APP_GIT_INFO__[propertyKey]` (a plain object filled at serve time —
 * Ktor templating or the nginx entrypoint) → `<meta name="gitHash">` (hash only) →
 * [VersionInformation.UnknownGitValue]. Non-browser runtimes (Node, tests) resolve to the
 * fallback without throwing: `globalThis` exists everywhere, `document` is guarded.
 */
internal actual fun resolveGitProperty(property: GitProperty): String {
  return findAppGitInfoValue(property.propertyKey)
    ?: findMetaAppGitHashValue(property)
    ?: VersionInformation.UnknownGitValue
}

private fun findAppGitInfoValue(propertyKey: String): String? {
  val appGitInfo: dynamic = js("globalThis.__APP_GIT_INFO__")
  if (appGitInfo == null) {
    return null
  }
  //No smart cast on dynamic: bind to a static String before calling Kotlin extensions
  val value: String = appGitInfo[propertyKey] as? String ?: return null
  return sanitizeInjectedValue(value)
}

/**
 * Treats an empty string (envsubst ran without the env var) and an unreplaced `${…}` placeholder
 * (HTML served without the injecting entrypoint, e.g. local webpack dev server) as absent.
 */
private fun sanitizeInjectedValue(value: String): String? {
  return if (value.isEmpty() || value.startsWith("\${")) null else value
}

private fun findMetaAppGitHashValue(property: GitProperty): String? {
  return when (property) {
    GitProperty.Hash -> {
      val documentOrNull: dynamic = js("typeof document !== 'undefined' ? document : null")
      if (documentOrNull == null) {
        return null
      }
      val metaElement = documentOrNull.querySelector("meta[name='gitHash']")
      if (metaElement == null) {
        return null
      }
      //No smart cast on dynamic: bind to a static String before calling Kotlin extensions
      val content: String = metaElement.getAttribute("content") as? String ?: return null
      sanitizeInjectedValue(content)
    }

    GitProperty.CommitDateTime -> null
  }
}
