/**
 * Imports
 */

import * as meistercharts from "@meistercharts/meistercharts";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { BulletChart } from "./BulletChart";

/**
 * Mock meistercharts api
 */

const setConfig = vi.fn();
const dispose = vi.fn();

vi.mock("@meistercharts/meistercharts", () => ({
  createBulletChart: vi.fn(() => ({
    setConfiguration: setConfig,
    holder: window.document.createElement("div"),
    dispose,
  })),
}));

/**
 * base config for bulletchart
 */

const bulletChartConfig = {
  categories: [
    { current: 15, barStart: 10, barEnd: 20, label: "Revenue" },
    { current: 30, barStart: 27, barEnd: 45, label: "Expenses" },
  ],
};

/**
 * Set up User Interactions
 */

const user = userEvent.setup();

/**
 * Tests for the BulletChart component
 */

describe("BulletChart", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("calls meistercharts api to render the chart and set initial config", () => {
    render(<BulletChart configuration={bulletChartConfig} />);

    expect(meistercharts.createBulletChart).toHaveBeenCalledOnce();
    expect(setConfig).toHaveBeenCalledWith(bulletChartConfig);
  });

  it("should call the setConfiguration function when props change", async () => {
    const Usage = () => {
      const [configuration, setConfiguration] = useState<meistercharts.BulletChartConfiguration>(
        () => bulletChartConfig,
      );

      return (
        <>
          <BulletChart configuration={configuration} />

          <button
            onClick={() => {
              setConfiguration({ ...bulletChartConfig, barColors: ["#ff0000"] });
            }}
          >
            Change Color
          </button>
        </>
      );
    };

    render(<Usage />);

    expect(setConfig).toHaveBeenCalledWith(bulletChartConfig);

    await user.click(screen.getByText("Change Color"));

    expect(setConfig).toHaveBeenCalledWith({
      ...bulletChartConfig,
      barColors: ["#ff0000"],
    });
  });

  it("should pass through the className to the chart container", () => {
    render(<BulletChart className="test-class" configuration={bulletChartConfig} />);

    expect(screen.getByTestId("meistercharts-react-container")).toHaveClass("test-class");
  });

  it("should set the style prop on the chart container", () => {
    render(<BulletChart configuration={bulletChartConfig} style={{ color: "#ff0000" }} />);

    expect(screen.getByTestId("meistercharts-react-container")).toHaveStyle({
      color: "#ff0000",
    });
  });
});
