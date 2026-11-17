import { storybookManagerHead, storybookPreviewHead } from "@neckar-hub/vite-config";
import type { StorybookConfig } from "@storybook/react-vite";

const config: StorybookConfig = {
  framework: { name: "@storybook/react-vite", options: { builder: { viteConfigPath: ".storybook/vite.config.ts" } } },
  stories: ["../src/**/*.stories.tsx"],
  core: { disableTelemetry: true },
  // No story names a `component`, so the argTypes react-docgen reads from it have no reader.
  typescript: { reactDocgen: false },
  managerHead: storybookManagerHead,
  previewHead: storybookPreviewHead,
};

export default config;
