/**
 * Imports
 */

import * as meistercharts from "@meistercharts/meistercharts";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { LineChartSimple, type LineChartSimpleConfiguration } from "./LineChartSimple";

/**
 * Mock meistercharts api
 */

const setData = vi.fn();
const setStyle = vi.fn();
const dispose = vi.fn();

vi.mock("@meistercharts/meistercharts", () => ({
  createLineChartSimple: vi.fn(() => ({
    setData,
    setStyle,
    holder: window.document.createElement("div"),
    dispose,
  })),
}));

/**
 * base config for the line chart
 */

const config: LineChartSimpleConfiguration = {
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
 * Set up User Interactions
 */

const user = userEvent.setup();

/**
 * Tests for the LineChartSimple component
 */

describe("LineChartSimple", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("should call the meistercharts api to render the histogram", () => {
    render(<LineChartSimple configuration={config} />);

    expect(meistercharts.createLineChartSimple).toHaveBeenCalledOnce();
    expect(setData).toHaveBeenCalledWith(config.data);
    expect(setStyle).toHaveBeenCalledWith(config.style ?? {});
  });

  it("should call the setData and setStyle functions when config prop changes", async () => {
    const Usage = () => {
      const [configuration, setConfiguration] = useState<LineChartSimpleConfiguration>(config);

      return (
        <>
          <LineChartSimple configuration={configuration} />

          <button
            onClick={() => {
              setConfiguration({
                ...config,
                style: { lineStyles: [{ lineStyle: { color: "#ff0000" } }] },
              });
            }}
          >
            Change Color
          </button>
        </>
      );
    };

    render(<Usage />);

    expect(setData).toHaveBeenCalledWith(config.data);
    expect(setStyle).toHaveBeenCalledWith({});

    await user.click(screen.getByText("Change Color"));

    expect(setData).toHaveBeenCalledWith(config.data);
    expect(setStyle).toHaveBeenCalledWith({
      lineStyles: [{ lineStyle: { color: "#ff0000" } }],
    });
  });

  it("should pass through the className to the chart container", () => {
    render(<LineChartSimple className="test-class" configuration={config} />);

    expect(screen.getByTestId("meistercharts-react-container")).toHaveClass("test-class");
  });

  it("should set the style prop on the chart container", () => {
    render(<LineChartSimple configuration={config} style={{ color: "#ff0000" }} />);

    expect(screen.getByTestId("meistercharts-react-container")).toHaveStyle({
      color: "#ff0000",
    });
  });
});
