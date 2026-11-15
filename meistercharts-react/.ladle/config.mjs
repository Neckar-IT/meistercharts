/**
 * Ladle appends `appendToHead` after Vite expanded its placeholders, so the `${APP_IMAGE_DIGEST}` token stays raw for
 * /docker-entrypoint.d/40-inject-serve-time-values.sh of the showcase container and `serveTimeInjection()` in .ladle/vite.config.ts.
 */

/** @type {import('@ladle/react').UserConfig} */
export default {
  // An array: Ladle evaluates a function, its default included, with `new Function`, which `script-src 'self'` blocks; see `verifyLadleConfigs`.
  storyOrder: ["*"],
  appendToHead: `<meta name="imageDigest" content="\${APP_IMAGE_DIGEST}" />`,
  stories: ["./src/**/*.stories.tsx"],
  addons: {
    rtl: {
      enabled: false,
    },
    width: {
      enabled: true,
      options: {
        base: 400,
        sm: 640,
        md: 768,
        lg: 1024,
        xl: 1280,
        "2xl": 1536,
      },
      defaultState: 0,
    },
  },
  /**
   * Load the vite.config.ts specifically for ladle — the package's own one builds the library.
   */
  viteConfig: `${process.cwd()}/.ladle/vite.config.ts`,
};
