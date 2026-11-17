/**
 * Imports
 */

import type { Meta, StoryObj } from "@storybook/react-vite";
import { Histogram } from "./Histogram";

/**
 * Default Data
 */
const data = {
  categories: [
    { label: "Monday", values: [2] },
    { label: "Tuesday", values: [30] },
    { label: "Wednesday", values: [20] },
    { label: "Thursday", values: [25] },
    { label: "Friday", values: [20] },
    { label: "Saturday", values: [35] },
    { label: "Sunday", values: [45] },
  ],
};

/**
 * Meta Information
 */

const meta = { title: "Histogram" } satisfies Meta;

export default meta;

/**
 * Stories
 */

export const Minimal: StoryObj<typeof meta> = {
  render: () => <Histogram configuration={{ data }} />,
};
