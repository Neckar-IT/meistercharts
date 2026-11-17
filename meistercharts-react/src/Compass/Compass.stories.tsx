/**
 * Imports
 */

import type { Meta, StoryObj } from "@storybook/react-vite";
import { Compass } from "./Compass";

/**
 * Meta Information
 */

const meta = { title: "Compass" } satisfies Meta;

export default meta;

/**
 * Stories
 */

export const Minimal: StoryObj<{ currentValue: number }> = {
  args: {
    currentValue: 15,
  },
  render: ({ currentValue }: { currentValue: number }) => <Compass configuration={{ data: { currentValue } }} />,
};
