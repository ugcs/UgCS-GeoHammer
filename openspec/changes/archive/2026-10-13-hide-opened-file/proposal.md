## Why

With several files open, the profile area and the map get crowded, and the only way to get a file out of the way is to
close it. Users need to temporarily collapse a file — reduce its chart to the header and remove its track from the
map — and keep it out of map-based line cropping, without closing it (#815).

Moving the GPR file name into a shared header also frees the top band of the GPR chart, which makes it possible to
give GPR and sensor charts the same layout of flags and selection markers.

## What Changes

- Every chart (GPR and sensor) gets one shared header: close button, file name and an eye toggle at the right edge.
  The GPR close button and file name move off the canvas into this header.
- Collapsing a chart (with the eye toggle or by double-clicking the header) hides its plot area and leaves only the
  header; expanding restores it. The eye shows an open eye while the chart is expanded and a crossed eye while it is
  collapsed. The profile scroll of a collapsed chart stays visible but is disabled.
- A collapsed file is not shown on the map: its track, flags, grid and GPR radar overlay are not drawn, and clicking the
  map does not select its traces.
- Map line cropping ignores collapsed files, and the initial crop area is fitted to expanded files only.
- Collapsing does not affect selection: a collapsed file stays selectable and can remain the current file.
- Collapsed state lives for the session only; reopening a file or restarting the app shows it expanded.
- GPR and sensor charts show flags and selected-trace markers the same way: same position below the plot top, same
  flag shape and colors, solid marker lines.
- The GPR chart gets a compact layout: smaller top and bottom margins, the depth-axis unit toggle drawn vertically
  next to the axis, the distance-axis unit toggle anchored to the chart bottom.

## Non-goals

- Collapsed state does not affect processing: gridding, quality checks, scripts, filters, saving and MCP tools work on
  collapsed files as on any other.
- Quality-check issues on the map stay visible when a file is collapsed.
- Collapsed state is not persisted (not in meta, not in settings).
- No additional header icons and no API for adding them.
- Sub-pixel alignment of the sensor-chart flag line with the flag pole.

## Capabilities

### New Capabilities

- `chart-visibility`: collapsing and expanding an opened file — the chart header, chart collapse, and how a collapsed
  file is treated on the map, in line cropping and in selection — plus the consistent look of chart markers and the
  GPR chart layout that the shared header enables.

### Modified Capabilities

None.

## Impact

- Charts: `Chart` (shared header and root layout, single close path, `MARKER_OFFSET`), `SensorLineChart`, `GPRChart`
  (canvas sizing), `ProfileScroll`; new `ChartHeader` view; `CloseGprChartButton` removed.
- GPR layout: `ProfileField` (`TOP_MARGIN`, `BOTTOM_MARGIN`; `Model.TOP_MARGIN` removed), `LeftRulerController`,
  `HorizontalRulerController`.
- Markers: `FoundPlace`, `ClickPlace`, `ShapeHolder` (flag shape, also used on the map).
- Model: `Model` (collapsed-file query, chart root handling in `createChart`); new `FileCollapsedEvent`.
- Map: `GpsTrack`, `FoundTracesLayer`, `GridLayer`, `RadarMap`, `TraceCutter`, `TraceCutInitializer`;
  `Model.selectNearestTrace`.
- View: `ProfileView` (GPR sizing listeners removed); eye icons (`EYE`, `EYE_OFF`) in `ResourceImageHolder`; header and
  profile-scroll styles in the components stylesheet.
- No file format, persistence or MCP API changes.
