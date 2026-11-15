import { resolve } from "path";
/// <reference types="vitest" />
import { sharedVitestTestOptions } from "@neckar-hub/vite-config";
import react from "@vitejs/plugin-react";
import dts from "unplugin-dts/vite";
import cssInjectedByJs from "vite-plugin-css-injected-by-js";
import { defineConfig } from "vitest/config";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    dts({
      exclude: ["./src/tests/**/*", "**/*.test.ts", "**/*.test.tsx", "**/*.spec.ts", "**/*.spec.tsx", "**/*.stories.tsx"],
      bundleTypes: true,
    }),
    cssInjectedByJs(),
  ],
  build: {
    sourcemap: true,
    lib: {
      entry: resolve(import.meta.dirname, "./src/index.ts"),
      formats: ["es"],
    },
    rolldownOptions: {
      external: ["react", "react/jsx.runtime", "react-dom", "@meistercharts/meistercharts"],
    },
  },
  test: {
    ...sharedVitestTestOptions,
    globals: true,
    environment: "jsdom",
    setupFiles: ["./src/__tests__/setup.ts"],
  },
});
