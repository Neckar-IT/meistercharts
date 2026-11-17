/**
 * Imports
 */

import type { Meta, StoryObj } from "@storybook/react-vite";
import { BarChartStacked } from "./BarChartStacked";

/**
 * Default Config
 */

const config = {
  data: {
    categories: [
      { label: "Monday", values: [2, 8, 4, 6] },
      { label: "Tuesday", values: [40, 2, 2, 5] },
      { label: "Wednesday", values: [20, 10, 5, 5] },
      { label: "Thursday", values: [5, 5, 8, 5] },
      { label: "Friday", values: [5, 20, 10, 5] },
    ],
  },
};

/**
 * Meta Information
 */

const meta = { title: "Bar Chart Stacked" } satisfies Meta;

export default meta;

/**
 * Stories
 */

export const Minimal: StoryObj<typeof meta> = {
  render: () => <BarChartStacked configuration={config} />,
};
