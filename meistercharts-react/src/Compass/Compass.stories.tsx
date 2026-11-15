/**
 * Imports
 */

import type { Story } from "@ladle/react";
import { Compass } from "./Compass";

/**
 * Stories
 */

export const Minimal: Story<{ currentValue: number }> = ({ currentValue }) => (
  <Compass configuration={{ data: { currentValue } }} />
);
Minimal.args = {
  currentValue: 15,
};
