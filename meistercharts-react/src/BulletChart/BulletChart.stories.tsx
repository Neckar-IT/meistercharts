/**
 * Imports
 */

import { BulletChart } from "./BulletChart";

/**
 * Default Config
 */

const config = {
  categories: [
    { current: 15, barStart: 10, barEnd: 20, label: "Revenue" },
    { current: 30, barStart: 27, barEnd: 45, label: "Expenses" },
  ],
};

/**
 * Stories
 */

export const Minimal = () => <BulletChart configuration={config} />;

export const CustomHeight = () => <BulletChart configuration={config} style={{ height: "300px" }} />;
