/**
 * Imports
 */

import type { Meta, StoryObj } from "@storybook/react-vite";
import { useEffect, useState } from "react";
import type { ThemeId } from "../utils";
import { TimeLineChart } from "./TimeLineChart";
import { useTimeLineChartController } from "./useTimeLineChartController";

/**
 * Helpers
 */

function generateData() {
  const timestamp = Date.now();
  const value1 = Math.sin(((timestamp / 10000) % 10) * Math.PI) * 10 + 50;
  const value2 = Math.cos(((timestamp / 10000) % 10) * Math.PI) * 25 + 50;

  return { timestamp, decimalValues: [value1, value2] };
}

/**
 * Meta Information
 */

const meta = { title: "Time Line Chart" } satisfies Meta;

export default meta;

/**
 * Stories
 */

export const Minimal: StoryObj<typeof meta> = {
  render: () => {
    const controller = useTimeLineChartController();

    useEffect(() => {
      controller.setUpDemo();
      controller.play();
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return <TimeLineChart controller={controller} />;
  },
};

interface ThemeStoryProps {
  theme1: ThemeId;
  theme2: ThemeId;
}

export const Theme: StoryObj<ThemeStoryProps> = {
  args: {
    theme1: "Dark",
    theme2: "NeckarIT",
  },
  argTypes: {
    theme1: {
      options: ["Dark", "Default", "NeckarIT", "Debug"],
      control: { type: "select" },
    },
    theme2: {
      options: ["Dark", "Default", "NeckarIT", "Debug"],
      control: { type: "select" },
    },
  },
  render: (args: ThemeStoryProps) => {
    const controller1 = useTimeLineChartController();
    const controller2 = useTimeLineChartController();

    useEffect(() => {
      controller1.setUpDemo();
      controller1.play();
      controller2.setUpDemo();
      controller2.play();
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
      <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
        <TimeLineChart controller={controller1} theme={args.theme1} />
        <TimeLineChart controller={controller2} theme={args.theme2} />
      </div>
    );
  },
};

interface I18nStoryProps {
  locale1: string;
  timeZone1: string;
  locale2: string;
  timeZone2: string;
}

const timeZones = ["Europe/Berlin", "Antarctica/Rothera", "America/Chicago", "Asia/Aqtau"];
const locales = ["en-US", "de-DE", "fr-ch"];

export const I18n: StoryObj<I18nStoryProps> = {
  args: {
    timeZone1: timeZones[0],
    locale1: locales[0],
    timeZone2: timeZones[1],
    locale2: locales[1],
  },
  argTypes: {
    timeZone1: {
      options: [...timeZones],
      control: { type: "select" },
    },
    locale1: {
      options: [...locales],
      control: { type: "select" },
    },
    timeZone2: {
      options: [...timeZones],
      control: { type: "select" },
    },
    locale2: {
      options: [...locales],
      control: { type: "select" },
    },
  },
  render: (args: I18nStoryProps) => {
    const controller1 = useTimeLineChartController();
    const controller2 = useTimeLineChartController();

    useEffect(() => {
      controller1.setUpDemo();
      controller1.play();
      controller2.setUpDemo();
      controller2.play();
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const i18nConfiguration1 = {
      timeZone: args.timeZone1,
      textLocale: args.locale1,
      formatLocale: args.locale1,
    };
    const i18nConfiguration2 = {
      timeZone: args.timeZone2,
      textLocale: args.locale2,
      formatLocale: args.locale2,
    };
    return (
      <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
        <TimeLineChart controller={controller1} i18nConfiguration={i18nConfiguration1} />
        <TimeLineChart controller={controller2} i18nConfiguration={i18nConfiguration2} />
      </div>
    );
  },
};

export const OwnData: StoryObj<typeof meta> = {
  render: () => {
    const controller = useTimeLineChartController();

    // generate data
    useEffect(() => {
      const interval = window.setInterval(() => {
        controller.addSample(generateData());
      }, 50);

      controller.play();
      return () => {
        window.clearInterval(interval);
      };
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
      <TimeLineChart
        configuration={{
          decimalDataSeries: [
            { id: 0, name: "sin" },
            { id: 1, name: "cos" },
          ],
        }}
        controller={controller}
      />
    );
  },
};

const timeLineChartConfiguration = {
  decimalDataSeries: [
    { id: 0, name: "sin" },
    { id: 1, name: "cos" },
  ],
};

export const ChangingState: StoryObj<typeof meta> = {
  render: () => {
    const controller = useTimeLineChartController();

    const [_, setCounter] = useState(0);

    // generate data
    useEffect(() => {
      // play on mount
      controller.play();

      const interval = window.setInterval(() => {
        controller.addSample(generateData());
        // trigger a re-render
        setCounter((prev) => prev + 1);
      }, 500);
      return () => {
        window.clearInterval(interval);
      };
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
      <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
        <div style={{ flexGrow: "1" }}>
          <TimeLineChart configuration={timeLineChartConfiguration} controller={controller} />
        </div>
        <div style={{ display: "flex", flexDirection: "row", flexGrow: "0" }}>
          <button
            type="button"
            onClick={() => {
              controller.play();
            }}
          >
            Play
          </button>
          <button
            type="button"
            onClick={() => {
              controller.pause();
            }}
          >
            Pause
          </button>
        </div>
      </div>
    );
  },
};
