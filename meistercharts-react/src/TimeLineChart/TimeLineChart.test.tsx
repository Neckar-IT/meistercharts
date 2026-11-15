/**
 * Imports
 */

import * as meistercharts from "@meistercharts/meistercharts";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { TimeLineChartConfiguration } from "./TimeLineChart";
import { TimeLineChart } from "./TimeLineChart";
import { useTimeLineChartController } from "./useTimeLineChartController";

/**
 * Mock meistercharts api
 */

const setData = vi.fn();
const setDataSeries = vi.fn();
const setStyle = vi.fn();
const setUpDemo = vi.fn();
const addSample = vi.fn();
const addSamples = vi.fn();
const clearHistory = vi.fn();
const resetView = vi.fn();
const getZoom = vi.fn(() => ({ scaleX: 1, scaleY: 1 }));
const modifyZoom = vi.fn();
const getVisibleTimeRange = vi.fn(() => ({ start: 0, end: 1 }));
const dispose = vi.fn();
const setI18nConfiguration = vi.fn();
const setTheme = vi.fn();

vi.mock("@meistercharts/meistercharts", () => ({
  createTimeLineChart: vi.fn(() => ({
    holder: window.document.createElement("div"),
    setData,
    setDataSeries,
    setStyle,
    setI18nConfiguration,
    setUpDemo,
    addSample,
    addSamples,
    clearHistory,
    resetView,
    getZoom,
    modifyZoom,
    getVisibleTimeRange,
    dispose,
    setTheme,
  })),
  setI18nConfiguration: vi.fn(),
}));

/**
 * Set up User Interactions
 */

const user = userEvent.setup();

/**
 * Tests for the TimeLineChart component
 */

describe("TimeLineChart", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("should call the meistercharts api to render the time line chart", () => {
    render(<TimeLineChart />);

    expect(meistercharts.createTimeLineChart).toHaveBeenCalledOnce();
  });

  it("should call the setStyle function if chart style changes", async () => {
    const Usage = () => {
      const [configuration, setConfiguration] = useState<TimeLineChartConfiguration>({});

      return (
        <>
          <TimeLineChart configuration={configuration} />

          <button
            onClick={() => {
              setConfiguration({ style: { showToolbar: true } });
            }}
          >
            Show Toolbar
          </button>
        </>
      );
    };

    render(<Usage />);

    expect(setStyle).toHaveBeenCalledWith({});

    await user.click(screen.getByText("Show Toolbar"));

    expect(setStyle).toHaveBeenCalledWith({ showToolbar: true });
  });

  it("should call the setI18nConfiguration function if i18n-configuration changes", async () => {
    const id = "f0c8c06d-4ca0-46e1-a524-2e965139a5e9";
    const newConfiguration: meistercharts.I18nConfiguration = { timeZone: "Antarctica/Rothera" };
    const Usage = () => {
      const [configuration, setConfiguration] = useState<meistercharts.I18nConfiguration>({});

      return (
        <>
          <TimeLineChart i18nConfiguration={configuration} />
          <button
            data-testid={id}
            onClick={() => {
              setConfiguration(newConfiguration);
            }}
          >
            Change Time Zone
          </button>
        </>
      );
    };

    render(<Usage />);

    expect(setI18nConfiguration).toHaveBeenCalledWith({});

    await user.click(screen.getByTestId(id));

    expect(setI18nConfiguration).toHaveBeenCalledWith(newConfiguration);
  });

  it("should call the setTheme function if theme changes", async () => {
    const id = "92167690-1724-4acf-a06b-c7236a30f85c";
    const Usage = () => {
      const [theme, setTheme] = useState<meistercharts.ThemeId>("Default");

      return (
        <>
          <TimeLineChart theme={theme} />
          <button
            data-testid={id}
            onClick={() => {
              setTheme("Dark");
            }}
          >
            Change Theme
          </button>
        </>
      );
    };

    render(<Usage />);

    expect(setTheme).toHaveBeenCalledWith("Default");

    await user.click(screen.getByTestId(id));

    expect(setTheme).toHaveBeenCalledWith("Dark");
  });

  it("should call the setData function if history settings changes", async () => {
    const Usage = () => {
      const [configuration, setConfiguration] = useState<TimeLineChartConfiguration>({});

      return (
        <>
          <TimeLineChart configuration={configuration} />

          <button
            onClick={() => {
              setConfiguration({
                historySettings: {
                  durationBetweenSamples: 50,
                  guaranteedHistoryLength: 200,
                },
              });
            }}
          >
            Change History Settings
          </button>
        </>
      );
    };

    render(<Usage />);

    expect(setData).toHaveBeenCalledWith({});

    await user.click(screen.getByText("Change History Settings"));

    expect(setData).toHaveBeenCalledWith({
      historySettings: {
        durationBetweenSamples: 50,
        guaranteedHistoryLength: 200,
      },
    });
  });

  it("should call the setDataSeries function if data series changes", async () => {
    const Usage = () => {
      const [configuration, setConfiguration] = useState<TimeLineChartConfiguration>({});

      return (
        <>
          <TimeLineChart configuration={configuration} />

          <button
            onClick={() => {
              setConfiguration({
                decimalDataSeries: [
                  { id: 0, name: "sin" },
                  { id: 1, name: "cos" },
                ],
              });
            }}
          >
            Change History Settings
          </button>
        </>
      );
    };

    render(<Usage />);

    expect(setDataSeries).toHaveBeenCalledWith([], []);

    await user.click(screen.getByText("Change History Settings"));

    expect(setDataSeries).toHaveBeenCalledWith(
      [
        { id: 0, name: "sin" },
        { id: 1, name: "cos" },
      ],
      [],
    );
  });

  it("should call the addSample function on controller call", async () => {
    const Usage = () => {
      const controller = useTimeLineChartController();

      return (
        <>
          <TimeLineChart controller={controller} />

          <button
            onClick={() => {
              controller.addSample({ timestamp: 0, decimalValues: [0] });
            }}
          >
            Add Sample
          </button>
        </>
      );
    };

    render(<Usage />);

    await user.click(screen.getByText("Add Sample"));

    expect(addSample).toHaveBeenCalledWith({
      timestamp: 0,
      decimalValues: [0],
    });
  });

  it("should call the addSamples function on controller call", async () => {
    const Usage = () => {
      const controller = useTimeLineChartController();

      return (
        <>
          <TimeLineChart controller={controller} />

          <button
            onClick={() => {
              controller.addSamples([
                { timestamp: 0, decimalValues: [0] },
                { timestamp: 1, decimalValues: [1] },
              ]);
            }}
          >
            Add Samples
          </button>
        </>
      );
    };

    render(<Usage />);

    await user.click(screen.getByText("Add Samples"));

    expect(addSamples).toHaveBeenCalledWith([
      { timestamp: 0, decimalValues: [0] },
      { timestamp: 1, decimalValues: [1] },
    ]);
  });

  it("should call the clearHistory function on controller call", async () => {
    const Usage = () => {
      const controller = useTimeLineChartController();

      return (
        <>
          <TimeLineChart controller={controller} />

          <button
            onClick={() => {
              controller.clearHistory();
            }}
          >
            Clear History
          </button>
        </>
      );
    };

    render(<Usage />);

    await user.click(screen.getByText("Clear History"));

    expect(clearHistory).toHaveBeenCalled();
  });

  it("should call the resetView function on controller call", async () => {
    const Usage = () => {
      const controller = useTimeLineChartController();

      return (
        <>
          <TimeLineChart controller={controller} />

          <button
            onClick={() => {
              controller.resetView();
            }}
          >
            Reset View
          </button>
        </>
      );
    };

    render(<Usage />);

    await user.click(screen.getByText("Reset View"));

    expect(resetView).toHaveBeenCalled();
  });

  it("should call the getZoom function on controller call", async () => {
    const Usage = () => {
      const controller = useTimeLineChartController();

      return (
        <>
          <TimeLineChart controller={controller} />

          <button onClick={() => controller.getZoom()}>Get Zoom</button>
        </>
      );
    };

    render(<Usage />);

    await user.click(screen.getByText("Get Zoom"));

    expect(getZoom).toHaveBeenCalled();
    expect(getZoom).toHaveReturnedWith({ scaleX: 1, scaleY: 1 });
  });

  it("should call the modifyZoom function on controller call", async () => {
    const Usage = () => {
      const controller = useTimeLineChartController();

      return (
        <>
          <TimeLineChart controller={controller} />

          <button
            onClick={() => {
              controller.modifyZoom({ scaleX: 1.1, scaleY: 1.1 });
            }}
          >
            Modify Zoom
          </button>
        </>
      );
    };

    render(<Usage />);

    await user.click(screen.getByText("Modify Zoom"));

    expect(modifyZoom).toHaveBeenCalledWith({ scaleX: 1.1, scaleY: 1.1 }, undefined, undefined);
  });

  it("should call the getVisibleTimeRange function on controller call", async () => {
    const Usage = () => {
      const controller = useTimeLineChartController();

      return (
        <>
          <TimeLineChart controller={controller} />

          <button onClick={() => controller.getVisibleTimeRange()}>Get Visible Time Range</button>
        </>
      );
    };

    render(<Usage />);

    await user.click(screen.getByText("Get Visible Time Range"));

    expect(getVisibleTimeRange).toHaveBeenCalled();
    expect(getVisibleTimeRange).toHaveReturnedWith({ start: 0, end: 1 });
  });

  it("should pass through the className to the chart container", () => {
    render(<TimeLineChart className="test-class" />);

    expect(screen.getByTestId("meistercharts-react-container")).toHaveClass("test-class");
  });

  it("should set the style prop on the chart container", () => {
    render(<TimeLineChart style={{ color: "#ff0000" }} />);

    expect(screen.getByTestId("meistercharts-react-container")).toHaveStyle({
      color: "#ff0000",
    });
  });
});
