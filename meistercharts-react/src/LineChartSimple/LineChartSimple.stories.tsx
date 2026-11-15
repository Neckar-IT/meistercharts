/**
 * Imports
 */

import { LineChartSimple } from "./LineChartSimple";

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
      { label: "Saturday", values: [84, 1, 64, 40] },
      { label: "Sunday", values: [20, 25, 12, 40] },
    ],
  },
};

/**
 * Stories
 */

export const Minimal = () => <LineChartSimple configuration={config} />;
