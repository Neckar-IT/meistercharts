/**
 * Imports
 */

import type { Story } from "@ladle/react";
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
 * Stories
 */

export const Minimal: Story = () => <Histogram configuration={{ data }} />;
