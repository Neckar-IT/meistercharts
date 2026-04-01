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
package it.neckar.open.annotations

/**
 * Data categories classified by lifecycle, scope, and purpose.
 *
 * Used with [DataCategory] to annotate code elements that create, manage,
 * or represent data of a specific category.
 */
enum class DataCategoryType {
  /**
   * Technical platform operation data.
   *
   * Global scope, stable lifecycle, rarely changed.
   * Examples: system tenant, service accounts, internal admin accounts, E2E system user.
   */
  PlatformData,

  /**
   * Domain reference and catalog data.
   *
   * Global scope, versioned and migratable.
   * Examples: manufacturers, component types, cable types, classifications.
   */
  GlobalReferenceData,

  /**
   * Minimal starting state created per tenant at registration.
   *
   * Tenant scope, created once then maintained.
   * Examples: tenant company record, default admin, default roles, base settings.
   */
  TenantBootstrapData,

  /**
   * Real operative data created by users during normal business operations.
   *
   * Tenant scope, continuously growing.
   * Examples: projects, customers, quotes, configurations.
   */
  TenantBusinessData,

  /**
   * Data for product demos and showcases.
   *
   * Tenant or global (demo tenant) scope, replaceable and re-generable.
   * Examples: demo user, demo projects, showcase configurations.
   */
  DemoData,

  /**
   * Data for learning and first use / onboarding.
   *
   * Tenant scope, optional and deletable.
   * Examples: example projects, example customers, example quotes.
   */
  OnboardingData,

  /**
   * Data for automated tests and E2E scenarios.
   *
   * Isolated tenant scope, short-lived and resettable.
   * Examples: E2E user, test fixtures, test projects.
   */
  TestData,
}
