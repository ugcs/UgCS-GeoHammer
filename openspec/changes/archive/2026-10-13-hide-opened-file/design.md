## Context

Requirements are in `specs/chart-visibility/spec.md`; motivation and scope in `proposal.md`.

Before this change the two chart types built their headers differently, and the GPR canvas reserved a 60 px band
(`Model.TOP_MARGIN`) above the radargram for the file name, the close icon and the markers:

```
SensorLineChart root (VBox, padding 10)        GPRChart root (VBox)
+--------------------------------------+       +--------------------------------------+
| HBox top: [x] name           <space> |       | Canvas                               |
+--------------------------------------+       |  y~10..30  [x] name   <- header      |
| StackPane chartsContainer            |       |            CloseGprChartButton (aux) |
|   flags/pins over the plot top       |       |            drawFileName()            |
+--------------------------------------+       |  y~30..60  line markers, flags,      |
| selectionRect (unmanaged)            |       |            pins, ruler toggle        |
+--------------------------------------+       |  ---- TOP_MARGIN = 60 ----           |
                                               |  radargram, rulers                   |
                                               +--------------------------------------+
```

- Closing differed: `SensorLineChart.close()` removed its root and published `FileClosedEvent`; `GPRChart.close()`
  stopped its `PaintLimiter` and published `FileClosedEvent`. `ProfileView` reacts with `Model.removeChart`. Removing
  the last line also closes the file (`RemoveLineButton`, `SensorLineChart`).
- Map layers, crop and map-click selection read `model.getFileManager().getFiles()` directly.
- Charts are created only when a file is loaded (`Model.initChart` from `Loader`); `Model.updateChartFile` re-keys the
  `charts` map on rename because `SgyFile` equality is based on its `java.io.File`.
- Flags and selection pins were drawn differently on the two chart types: GPR used a 25 px filled flag path and dashed
  XOR lines in the top band; the sensor chart used an 18 px pole with a cloth and solid lines over the plot top.

## Goals / Non-Goals

**Goals:**
- One header implementation shared by both chart types.
- A header API that grows by exposing controls, not by adding constructor callbacks.
- One place to ask whether a file is collapsed, used by map layers, crop and selection.
- One term throughout: a file is *collapsed* or *expanded* (never "hidden"/"visible").
- The same marker look and position on GPR and sensor charts, from shared constants.

**Non-Goals:**
- Additional header icons or an API for adding them.
- Moving GPR canvas elements other than the name and close icon (markers, ruler toggles) into JavaFX.
- Pixel-exact alignment of the sensor-chart flag line with the flag pole (see Risks).

## Decisions

### Shared header and root layout in `Chart`

```
Chart root (VBox, padding bottom 10, style class data-chart added by Model)
+-------------------------------------------------------------------+
| ChartHeader (HBox, padding 10 10 0 10, spacing 10, center-left)    |
|  [close]  name*                      <grow>                 [eye]  |
+-------------------------------------------------------------------+
| content node from subclass           managed/visible = !collapsed  |
|   SensorLineChart -> VBox(chartsContainer, selectionRect),         |
|                      padding 10 10 0 10                            |
|   GPRChart        -> VBox(canvas)                                  |
+-------------------------------------------------------------------+
```

- New `ChartHeader` view (package `chart`): close button, name label (style class `chart-name`), a growing spacer and
  the eye toggle at the right edge. Layout values are set in code; the name style is in `components.css`.
- `Chart` builds the root `VBox(header)` once; subclasses pass their content node to `setContent` from their
  constructors and no longer build a root. `getRootNode()` is a final `VBox` accessor in `Chart`, which removes the
  `(VBox)` cast in `Model.createChart`.
- `Chart.updateChartName()` owns the file name (`*` prefix for unsaved). `SensorLineChart` calls it where it updated
  its own label; `GPRChart` calls it on each draw, where it used to paint the name. `SensorLineChart`'s label and
  `GPRChart.drawFileName` are removed.
- Clicking the header selects the chart: the click bubbles to the root handler (`model.selectChart`), as clicks on
  the chart body do.

Alternative considered: keep each chart's header and add a collapse icon to both (AWT aux element for GPR, JavaFX for
sensor). Rejected: two implementations of every header control.

### Header API: exposed controls and properties

```
ChartHeader
  BooleanProperty collapsedProperty()   eye toggle (bidirectional) and header double click write it
  Button getCloseButton()               owner sets onAction
  void setName(String)

Chart
  header.collapsedProperty() listener -> content managed/visible, ProfileScroll disable, volatile copy,
                                         repaint, FileCollapsedEvent
  header.getCloseButton().setOnAction(e -> close())
```

- The header owns the collapsed view state as a `BooleanProperty`. The eye `ToggleButton` (style class `flat`) is
  bound to it bidirectionally and shows `EYE` / `EYE_OFF` (new SVG constants in `ResourceImageHolder`) with the
  tooltip "Collapse" / "Expand". A primary-button double click on the header flips the property.
- The close icon is a `Button` (style class `icon`, `close.png` graphic) exposed via `getCloseButton()`.
- The close button and the eye consume their mouse clicks, so they neither select the chart through the root handler
  nor reach the header's double-click handler. Clicks on the name or free header area bubble: a single click selects
  the chart, a double click also toggles it.
- `Chart` subscribes and mirrors the value into a `volatile` field, because map layers read it on the render thread
  where a JavaFX property must not be read. Future header controls follow the same pattern: the header exposes the
  control or a property, the owner subscribes; the constructor stays argument-free.

Alternatives considered:
- Callbacks passed to the constructor (`Runnable onToggleCollapse, Runnable onClose`): every new control adds a
  parameter and couples the header to the chart's methods. Rejected.
- Passing the `Chart` to the header: the view would depend on its controller (cyclic). Rejected.

### Single close path

`Chart.close()` (public, used by the header, `RemoveLineButton` and `SensorLineChart` last-line removal): confirm
unsaved changes, run subclass cleanup (`dispose()`; GPR stops its `PaintLimiter`), publish `FileClosedEvent`. Container
removal stays in `Model.removeChart` via `ProfileView`. The former `Chart.close(boolean)` and its only caller
`Model.closeAllCharts` were unused and are removed. `CloseGprChartButton` is deleted and dropped from
`GPRChart.updateAuxElements`.

`Chart.dispose()` is public so that `Model.initChart` can dispose the chart it replaces when a file is reopened;
otherwise the replaced `GPRChart` stays alive through its running `PaintLimiter` timer.

### Collapsed state on the chart, queried through `Model`

- On a change of the header's `collapsedProperty`, `Chart` sets content `managed`/`visible`, disables the chart's
  `ProfileScroll` (it stays in the profile area; `.profile-scroll:disabled` dims it, since the canvas-drawn scroll
  gets no disabled look from the theme), updates its `volatile` copy, repaints the chart and publishes
  `FileCollapsedEvent(file, collapsed)`. `Chart.isCollapsed()` returns the `volatile` copy.
- `Model.isCollapsed(SgyFile)` returns `chart != null && chart.isCollapsed()`; `Model.getExpandedFiles()` lists open
  files that are not collapsed. Map layers, map-click selection and `TraceCutter` use only these `Model` methods. There
  is no `Model.setCollapsed`: only the header changes the state.
- Why on the chart and not a `Set<SgyFile>` in `Model`: `SgyFile` hash/equality changes on rename, so a set would need
  re-keying; the `charts` map already handles that, and close drops the state with the chart. Charts are only
  recreated on load, where resetting to expanded is the required behavior.
- `GPRChart` skips drawing while collapsed; expanding repaints it. Zoom and scroll state live in
  `ScrollableData`/`ProfileField` and are untouched, so the view returns as it was.

`FileCollapsedEvent` instead of `WhatChanged(justdraw)`: `GridLayer` caches its rendering and does not re-render on
`justdraw`, and making it do so would re-render grids on every `justdraw` (published from many places, e.g. line changes
while scrolling a chart). `GpsTrack`, `GridLayer` and `RadarMap` re-render on `FileCollapsedEvent`; their render
completion repaints the map, which also redraws `FoundTracesLayer`.

### Consumers of collapsed state (all on the FX thread or the map render thread, read-only)

| Consumer | Change |
|---|---|
| `GpsTrack.drawTrack` | skip collapsed files; re-render on `FileCollapsedEvent` |
| `FoundTracesLayer.draw` / `mousePressed` | skip aux elements and user selections whose trace belongs to a collapsed file |
| `GridLayer.drawOnMapField` | skip collapsed files, including the selected one; re-render on `FileCollapsedEvent` |
| `RadarMap.drawCircles` | skip collapsed GPR files (intensities stay cached); re-render on `FileCollapsedEvent` |
| `Model.selectNearestTrace` | search expanded files only |
| `TraceCutter.applyCropLines` | pass expanded files to `TraceTransform.cropLines` |
| `TraceCutInitializer.initialRect` | fit to expanded files only |
| `QualityLayer` | unchanged (issues are not attributable per file) |

Map layers render on their own render-queue thread and read the collapsed flag without locking; the flag is `volatile`
and is set before `FileCollapsedEvent` is published, so the render it triggers sees the new state.

### GPR canvas sizing

The GPR canvas size is derived from the chart's content height (`vbox`), which no longer equals the root height
because the root also holds the header. `GPRChart` registers its size listeners once in `init()`: on its own content
height (plain listener, same lifetime as the chart) and on the profile area width (`ProfileView.getCenter()`) through a
`WeakChangeListener` whose target is held in a field. `ProfileView` no longer adds listeners on every file selection.
The weak listener keeps the long-lived profile area from holding dropped charts; together with disposing replaced
charts in `Model.initChart`, a closed or replaced GPR chart is not kept alive.

### Unified markers

```
plot area top (sensor: chart plot top; GPR: ProfileField.TOP_MARGIN = 20)
   |
   |  flag cloth 15 x 10, notch at x 10
   |  pole FLAG_POLE_HEIGHT = 18
   v
 MARKER_OFFSET = 46   <- icon bottom and marker line start, both chart types
   |
   |  solid marker line to the bottom of the plot
```

- `Chart.MARKER_OFFSET` (46) is the distance from the plot top to the bottom of a flag or selection pin and the start
  of its marker line. GPR markers are placed at `ProfileField.TOP_MARGIN + MARKER_OFFSET`, i.e. over the top of the
  radargram as on sensor charts.
- Flag geometry: `FoundPlace.FLAG_POLE_HEIGHT` (18) is shared by `ShapeHolder.FLAG` (AWT path for GPR and the map,
  rebuilt to the sensor-chart geometry: pole plus a 15 x 10 notched cloth) and the JavaFX flag in `SensorLineChart`.
  `FoundPlace` and `ClickPlace` derive their icon rectangles from the shape and image sizes (`ICON_WIDTH`,
  `ICON_HEIGHT`).
- Colors and strokes: flag pole, cloth outline and marker line in the flag color, 1 px; a selected flag fills the
  cloth with `FoundPlace.SELECTED_COLOR` and draws outline, pole and line 2 px. The selection pin line uses
  `ClickPlace.USER_COLOR` / `AUTO_COLOR` by selection type on both chart types. The dashed XOR lines on GPR are
  replaced with solid lines.
- Sensor-chart offsets follow `MARKER_OFFSET`: flag icon `translateY = MARKER_OFFSET - FLAG_POLE_HEIGHT`, flag line
  `translateY = MARKER_OFFSET`; pin image `translateY = MARKER_OFFSET - iconHeight`, pin line
  `translateY = MARKER_OFFSET - iconHeight - 2` (starts just under the pin tip). The formulas differ because the marker
  `VBox` in `LineChartWithMarkers` stacks the line below the icon container, and the pin's `StackPane` takes its 32 px
  height there while the flag's `Pane` takes none.

Alternative considered: lay out sensor-chart markers explicitly in `LineChartWithMarkers.layoutPlotChildren` (plain
`Pane` marker node, icon at `MARKER_OFFSET - iconHeight`, line from `MARKER_OFFSET`, shared x anchor) instead of
`VBox` stacking plus `translateY`. It would remove the per-marker formulas and align pole and line at any stroke width.
Deferred: the current offsets match visually and the change is contained to the marker layout code.

### Compact GPR layout and unit toggles

- `ProfileField` owns the canvas margins: `TOP_MARGIN = 20` (was `Model.TOP_MARGIN = 60`, removed) and
  `BOTTOM_MARGIN = 55` (was a local 65). The freed top band was only used by the name, the close icon and markers,
  which moved to the header or over the radargram.
- Distance-axis toggle (`HorizontalRulerController`): anchored to the bottom ruler
  (`bottomRuleRect.y + height - TOGGLE_BOTTOM_OFFSET`) instead of being derived from the main rect height, which
  silently depended on the top margin.
- Depth-axis toggle (`LeftRulerController`): drawn rotated 90° counter clockwise, centered on the radargram height
  (`mainRect`) and `TOGGLE_RIGHT_OFFSET` from the right edge of the depth ruler, to the right of its labels. `getRect`
  returns the unrotated screen bounds, so hit testing is unchanged. Converters are renamed to `SampleConverter` /
  `TimeConverter` with labels `samples` / `ns`; toggles are exposed as `getToggle()`.

### Data, undo, caches

Collapsing changes no file data: no snapshot, no `setUnsaved`, no `tracesChanged`. Crop keeps its existing
snapshot/undo/reload handling through `TraceTransform`; it simply receives fewer files, so undo restores only the files
that were cropped. Marker and layout changes are drawing only.

### Planned refactor

Extracting the header and moving root construction into `Chart` restructured both chart classes. It was done as a
separate first step with no behavior change, before collapse was added.

## Risks / Trade-offs

- [`SensorLineChart` drag selection computed coordinates in the root's coordinate space; `selectionRect` moved into
  the content node] → bounds are computed relative to the content node; verify drag zoom manually.
- [Sensor-chart marker offsets rely on how the marker `VBox` stacks the icon container (the flag `Pane` takes no
  height, the pin `StackPane` does)] → offsets are derived from `MARKER_OFFSET`; the explicit marker layout above is
  the robust follow-up.
- [Sensor-chart flag line can be half a pixel off the pole for odd stroke widths (VBox centering plus pixel
  snapping)] → accepted (non-goal).
- [`ShapeHolder.FLAG` is also drawn on the map, so map flags take the new, smaller shape] → accepted for a consistent
  flag look.
- [The distance-axis label area is derived as the bottom ruler height minus 30, so it shrank with `BOTTOM_MARGIN`]
  → verify labels and the unit toggle do not overlap.
- [Collapsed current file shows no track highlight on the map] → accepted by spec (selection independent of
  collapsing).
- [The first click of a header double click selects the chart] → specified: double-clicking a non-current file's
  header makes it current and toggles it.
