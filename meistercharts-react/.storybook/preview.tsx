import * as meistercharts from "@meistercharts/meistercharts";
import {
  type StorybookColorMode,
  storybookColorMode,
  storybookColorModeGlobalTypes,
  storybookInitialGlobals,
  storybookTailwindViewports,
} from "@neckar-hub/typescript-react-utils/storybook";
import type { Preview } from "@storybook/react-vite";
import { StrictMode, useEffect } from "react";

/** Wraps every story: the meistercharts theme of the toolbar global `theme`. */
function StoryFrame({ colorMode, children }: { colorMode: StorybookColorMode; children: React.ReactNode }): React.ReactElement {
  useEffect(() => {
    meistercharts.setDefaultTheme(colorMode === "dark" ? "Dark" : "Default");
  }, [colorMode]);

  return <StrictMode>{children}</StrictMode>;
}

const preview: Preview = {
  decorators: [
    (Story, context) => (
      <StoryFrame colorMode={storybookColorMode(context.globals)}>
        <Story />
      </StoryFrame>
    ),
  ],
  globalTypes: storybookColorModeGlobalTypes,
  initialGlobals: storybookInitialGlobals,
  parameters: {
    viewport: storybookTailwindViewports,
  },
};

export default preview;
