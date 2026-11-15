import { serveTimeInjection } from "@neckar-hub/vite-config";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

/**
 * Vite configuration exclusively for the Ladle stories.
 *
 * The package's own vite.config.ts builds the library (lib mode, dts, externalised react and
 * @meistercharts/meistercharts). Ladle needs the opposite — an application build that bundles
 * those dependencies — so it gets a configuration of its own.
 */
export default defineConfig({
  plugins: [react(), serveTimeInjection()],
});
