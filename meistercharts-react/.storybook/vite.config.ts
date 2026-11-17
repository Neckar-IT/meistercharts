import { serveTimeInjection } from "@neckar-hub/vite-config";
import { defineConfig } from "vite";

/** The Vite configuration of the stories: the package's own vite.config.ts builds the library in lib mode, Storybook an application. */
export default defineConfig({
  plugins: [serveTimeInjection()],
});
