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

import java.util.Properties

/**
 * JVM implementation: resolves git info from the injected deploy metadata.
 *
 * Chain: system property (local override, e.g. set by a run task) → environment variable
 * (baked into service images by Jib at image build) → the `META-INF/app-git-info.properties`
 * resource (packed exclusively into CLI leaf fat-jars by `configureServiceShadowJar`) →
 * [VersionInformation.UnknownGitValue].
 */
internal actual fun resolveGitInfo(property: GitProperty): String {
  return System.getProperty(property.systemProperty)
    ?: System.getenv(property.envVar)
    ?: findFatJarGitInfoValue(property.propertyKey)
    ?: VersionInformation.UnknownGitValue
}

private fun findFatJarGitInfoValue(propertyKey: String): String? {
  return fatJarGitInfo?.getProperty(propertyKey)?.ifEmpty { null }
}

/**
 * The git info resource packed into CLI leaf fat-jars at packaging time, or null when absent.
 *
 * The file is created only when a fat jar is assembled — it never exists in library jars or
 * module outputs, so the volatile values cannot churn any build input. Services in containers
 * resolve via the environment variables instead.
 */
private val fatJarGitInfo: Properties? by lazy {
  VersionInformation::class.java.getResourceAsStream("/META-INF/app-git-info.properties")?.use { stream ->
    Properties().apply { load(stream) }
  }
}
