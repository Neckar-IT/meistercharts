/**
 * Imports
 */

import type { Meta, StoryObj } from "@storybook/react-vite";
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
 * Meta Information
 */

const meta = { title: "Bullet Chart" } satisfies Meta;

export default meta;

/**
 * Stories
 */

export const Minimal: StoryObj<typeof meta> = {
  render: () => <BulletChart configuration={config} />,
};

export const CustomHeight: StoryObj<typeof meta> = {
  render: () => <BulletChart configuration={config} style={{ height: "300px" }} />,
};
