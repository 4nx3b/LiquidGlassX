# Liquid Glass (Backdrop)

![frontPhoto](artworks/banner.jpg)

A customizable Liquid Glass effect library for Compose Multiplatform.

## Docs

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kyant0/backdrop)](https://central.sonatype.com/artifact/io.github.kyant0/backdrop)

[Documentation](https://kyant.gitbook.io/backdrop)

## Performance notes

This fork applies performance-focused changes on top of upstream, with no visual downgrades:

- Saturation is fused into the lens shader, saving a separate color-filter pass when combining
  `vibrancy()`/`colorControls()` with `lens()`.
- Effect invalidation is scoped to the draw node, and backdrops are not redrawn while stationary.
- Highlight/shadow decoration layers are cached and only re-recorded when their inputs change.
- Per-corner radii are written into a reused scratch buffer instead of a new array per effect
  rebuild (relevant during animations and gestures).
- The lens shaders select corner radii with branchless steps instead of a divergent branch, and
  skip the radial depth gradient when `depthEffect` is disabled, saving an inverse square root
  per fragment.
- [BackdropRendering.scale](backdrop/src/commonMain/kotlin/com/kyant/backdrop/BackdropEffectScope.kt)
  globally scales the render resolution of backdrop effects (clamped to `0.1f..3f`) — lowering it
  is an escape hatch for low-end devices.
- The catalog's adaptive-luminance demo samples the backdrop at 5×5 directly on the GPU instead of
  reading the full layer back to the CPU each frame.

## Components

The library does not include any high-level components; you will need to create your own.
Below are some example components:

- [LiquidButton](/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidButton.kt)
- [LiquidToggle](/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidToggle.kt)
- [LiquidSlider](/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidSlider.kt)
- [LiquidBottomTabs](/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidBottomTabs.kt)

## Demo

- [Backdrop Catalog](./androidApp/release/androidApp-release.apk)

![Screenshots of Backdrop Catalog](artworks/catalog_app.jpg)
