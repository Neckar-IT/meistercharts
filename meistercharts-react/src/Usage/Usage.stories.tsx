/**
 * Imports
 */

import type { Meta, StoryObj } from "@storybook/react-vite";
import "./styles.css";
import { BarChartGrouped, type BarChartGroupedConfiguration } from "../BarChartGrouped";
/**
 * Chart Config
 */

const config: BarChartGroupedConfiguration = {
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

const meta = { title: "Usage" } satisfies Meta;

export default meta;

/**
 * Stories
 */

export const NoDiv: StoryObj<typeof meta> = {
  render: () => {
    return <BarChartGrouped configuration={config} />;
  },
};

export const Div: StoryObj<typeof meta> = {
  render: () => {
    return (
      <div style={{ backgroundColor: "lightblue" }}>
        <BarChartGrouped configuration={config} />
      </div>
    );
  },
};

export const DivFixedHeight: StoryObj<typeof meta> = {
  render: () => {
    return (
      <div style={{ height: "768px", backgroundColor: "lightblue" }}>
        <BarChartGrouped configuration={config} />
      </div>
    );
  },
};

export const DivSmallHeightWithClass: StoryObj<typeof meta> = {
  render: () => {
    return (
      <div
        style={{
          backgroundColor: "lightblue",
          overflow: "hidden",
        }}
      >
        <BarChartGrouped className="small" configuration={config} />
      </div>
    );
  },
};

export const DivSmallHeightWithStyle: StoryObj<typeof meta> = {
  render: () => {
    return (
      <div
        style={{
          backgroundColor: "lightblue",
          overflow: "hidden",
        }}
      >
        <BarChartGrouped configuration={config} style={{ height: "75px", minHeight: "75px" }} />
      </div>
    );
  },
};

export const Flex: StoryObj<typeof meta> = {
  render: () => {
    return (
      <div style={{ display: "flex", backgroundColor: "lightblue" }}>
        <div style={{ width: "50%", backgroundColor: "red" }} />
        <BarChartGrouped configuration={config} />
      </div>
    );
  },
};

export const FlexFixedHeight: StoryObj<typeof meta> = {
  render: () => {
    return (
      <div style={{ display: "flex", backgroundColor: "lightblue", height: "400px" }}>
        <div style={{ width: "50%", backgroundColor: "red" }} />
        <BarChartGrouped configuration={config} />
      </div>
    );
  },
};

export const Grid: StoryObj<typeof meta> = {
  render: () => {
    return (
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
          backgroundColor: "lightblue",
        }}
      >
        <div style={{ backgroundColor: "red" }} />
        <BarChartGrouped configuration={config} />
      </div>
    );
  },
};

export const GridFixedHeight: StoryObj<typeof meta> = {
  render: () => {
    return (
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
          backgroundColor: "lightblue",
          height: "500px",
        }}
      >
        <div style={{ backgroundColor: "red" }} />
        <BarChartGrouped configuration={config} />
      </div>
    );
  },
};
