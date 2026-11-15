/**
 * Imports
 */

import * as meistercharts from "@meistercharts/meistercharts";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { Histogram, type HistogramConfiguration } from "./Histogram";

/**
 * Mock meistercharts api
 */

const setData = vi.fn();
const setStyle = vi.fn();
const dispose = vi.fn();

vi.mock("@meistercharts/meistercharts", () => ({
  createHistogram: vi.fn(() => ({
    setData,
    setStyle,
    holder: window.document.createElement("div"),
    dispose,
  })),
}));

/**
 * base config for histogram
 */

const config: HistogramConfiguration = {
  data: {
    categories: [
      { label: "Monday", values: [2] },
      { label: "Tuesday", values: [30] },
      { label: "Wednesday", values: [20] },
      { label: "Thursday", values: [25] },
      { label: "Friday", values: [20] },
      { label: "Saturday", values: [35] },
      { label: "Sunday", values: [45] },
    ],
  },
};

/**
 * Set up User Interactions
 */

const user = userEvent.setup();

/**
 * Test for the Histogram component
 */

describe("Histogram", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("should call the meistercharts api to render the histogram", () => {
    render(<Histogram configuration={config} />);

    expect(meistercharts.createHistogram).toHaveBeenCalledOnce();
    expect(setData).toHaveBeenCalledWith(config.data);
    expect(setStyle).toHaveBeenCalledWith(config.style ?? {});
  });

  it("should call the setData and setStyle functions when config prop changes", async () => {
    const Usage = () => {
      const [configuration, setConfiguration] = useState<HistogramConfiguration>(config);
      return (
        <>
          <Histogram configuration={configuration} />

          <button
            onClick={() => {
              setConfiguration({
                ...config,
                style: { barColors: [["#ff0000"]] },
              });
            }}
          >
            Change Value
          </button>
        </>
      );
    };

    render(<Usage />);

    expect(setData).toHaveBeenCalledWith(config.data);
    expect(setStyle).toHaveBeenCalledWith({});

    await user.click(screen.getByText("Change Value"));

    expect(setData).toHaveBeenCalledWith(config.data);
    expect(setStyle).toHaveBeenCalledWith({ barColors: [["#ff0000"]] });
  });

  it("should pass through the className to the chart container", () => {
    render(<Histogram className="test-class" configuration={config} />);

    expect(screen.getByTestId("meistercharts-react-container")).toHaveClass("test-class");
  });

  it("should set the style prop on the chart container", () => {
    render(<Histogram configuration={config} style={{ color: "#ff0000" }} />);

    expect(screen.getByTestId("meistercharts-react-container")).toHaveStyle({
      color: "#ff0000",
    });
  });
});
