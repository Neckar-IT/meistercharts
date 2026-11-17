/**
 * Imports
 */

import type { Meta, StoryObj } from "@storybook/react-vite";
import { BarChartGrouped } from "./BarChartGrouped";

/**
 * Default Config
 */

const config = {
  data: {
    categories: [
      { label: "Monday", values: [2, 20, 30, 40] },
      { label: "Tuesday", values: [30, 32, 15, 50] },
      { label: "Wednesday", values: [20, 25, 35, 50] },
      { label: "Thursday", values: [20, 25, 35, 50] },
      { label: "Friday", values: [20, 25, 12, 40] },
    ],
  },
};

/**
 * Meta Information
 */

const meta = { title: "Bar Chart Grouped" } satisfies Meta;

export default meta;

/**
 * Stories
 */

export const Minimal: StoryObj<typeof meta> = {
  render: () => <BarChartGrouped configuration={config} />,
};
