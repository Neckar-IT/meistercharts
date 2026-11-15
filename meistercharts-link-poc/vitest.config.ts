import { sharedVitestTestOptions } from "@neckar-hub/vite-config";
import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    ...sharedVitestTestOptions,
    environment: "jsdom",
    setupFiles: ["./src/canvasStub.ts"],
    include: ["./src/**/*.test.ts"],
  },
});
