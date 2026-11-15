/**
 * Imports
 */

import * as meistercharts from "@meistercharts/meistercharts";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { Compass, type CompassConfiguration } from "./Compass";

/**
 * Mock meistercharts api
 */

const setData = vi.fn();
const setStyle = vi.fn();
const dispose = vi.fn();

vi.mock("@meistercharts/meistercharts", () => ({
  createCompass: vi.fn(() => ({
    setData,
    setStyle,
    holder: window.document.createElement("div"),
    dispose,
  })),
}));

/**
 * base config for compass
 */

const compassConfig: CompassConfiguration = {
  data: {
    currentValue: 50,
  },
};

/**
 * Set up User Interactions
 */

const user = userEvent.setup();

/**
 * Tests for the Compass component
 */

describe("Compass", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("should call the meistercharts api to render the compass", () => {
    render(<Compass configuration={compassConfig} />);

    expect(meistercharts.createCompass).toHaveBeenCalledOnce();
    expect(setData).toHaveBeenCalledWith(compassConfig.data);
    expect(setStyle).toHaveBeenCalledWith(compassConfig.style ?? {});
  });

  it("should call the setData and setStyle functions when config prop changes", async () => {
    const Usage = () => {
      const [configuration, setConfiguration] = useState<CompassConfiguration>(compassConfig);
      return (
        <>
          <Compass configuration={configuration} />

          <button
            onClick={() => {
              setConfiguration({ data: { currentValue: 180 } });
            }}
          >
            Change Value
          </button>
        </>
      );
    };

    render(<Usage />);

    expect(setData).toHaveBeenCalledWith(compassConfig.data);
    expect(setStyle).toHaveBeenCalledWith(compassConfig.style ?? {});

    await user.click(screen.getByText("Change Value"));

    expect(setData).toHaveBeenCalledWith({ currentValue: 180 });
    expect(setStyle).toHaveBeenCalledWith({});
  });

  it("should pass through the className to the chart container", () => {
    render(<Compass className="test-class" configuration={compassConfig} />);

    expect(screen.getByTestId("meistercharts-react-container")).toHaveClass("test-class");
  });

  it("should set the style prop on the chart container", () => {
    render(<Compass configuration={compassConfig} style={{ color: "#ff0000" }} />);

    expect(screen.getByTestId("meistercharts-react-container")).toHaveStyle({
      color: "#ff0000",
    });
  });
});
