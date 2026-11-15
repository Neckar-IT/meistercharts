import * as meistercharts from "@meistercharts/meistercharts";

/**
 * Provides direct access to the meistercharts-library.
 *
 * Use with care.
 */
export { meistercharts };

/**
 * Available themes.
 */
export type ThemeId = meistercharts.ThemeId;

/**
 * Sets the default theme used for all charts.
 * Do this before rendering any charts.
 *
 * The theme may be overwritten on a per-chart basis.
 */
export function setDefaultTheme(theme: ThemeId) {
  meistercharts.setDefaultTheme(theme);
}

/**
 * Sets the default i18n configuration.
 *
 * This configuration may be overwritten on a per-chart basis.
 */
export function setDefaultI18nConfiguration(config: meistercharts.I18nConfiguration) {
  meistercharts.setDefaultI18nConfiguration(config);
}
