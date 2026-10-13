## 1. Shared chart header

- [x] 1.1 Add `ChartHeader` (close button, name label with the `chart-name` style, growing spacer); verify it compiles
  and the style class is defined in `components.css`
- [x] 1.2 Move root construction into `Chart`: root `VBox(header)` plus the content node passed via `setContent`, final
  `getRootNode()`; remove the `(VBox)` cast in `Model.createChart`; verify both chart types still open and display
- [x] 1.3 Move file-name handling (`*` for unsaved) into `Chart.updateChartName()`; remove `SensorLineChart`'s own
  label and `GPRChart.drawFileName`; verify the name and `*` update after an edit and after save/save-as
- [x] 1.4 Unify close in `Chart.close()` (confirm unsaved, `dispose()`, publish `FileClosedEvent`); remove the unused
  `Chart.close(boolean)` and `Model.closeAllCharts`; dispose the replaced chart in `Model.initChart`; switch
  `RemoveLineButton` and `SensorLineChart` last-line removal to it; delete `CloseGprChartButton`; verify closing from the header, cancelling on unsaved, and closing by removing the
  last line for both chart types
- [x] 1.5 Adapt `SensorLineChart` content: padding on the content node, `init()` sizes `chartsContainer` directly,
  drag-selection bounds relative to the content node; verify drag zoom draws the rectangle in the right place
- [x] 1.6 Size the GPR canvas from its content height: `GPRChart` registers its size listeners once in `init()`
  (content height, and the profile area width via a weak listener) instead of `ProfileView` adding them on every
  selection; verify the radargram fits, and collapsing and expanding a chart below a GPR chart resizes the canvas back
  without a window resize

## 2. Collapse and collapsed state

- [x] 2.1 Header API: `ChartHeader` owns `collapsedProperty()` and exposes `getCloseButton()`; header controls consume
  their clicks; `Chart` subscribes (content `managed`/`visible`, `volatile` copy, repaint, `FileCollapsedEvent`) and
  sets the close action; verify collapsing leaves only the header, charts below move up, and collapsing a non-current
  file keeps the current file
- [x] 2.2 Eye toggle: add `EYE` / `EYE_OFF` SVG icons to `ResourceImageHolder` and a `ToggleButton` at the right edge
  of the header, bound to `collapsedProperty()`, tooltips "Collapse" / "Expand"; verify it collapses and expands the
  chart and shows a crossed eye while collapsed
- [x] 2.3 Toggle the collapsed state on a primary-button double click on the free header area; verify double-clicking
  the header collapses and expands the chart, and double-clicking a button does not
- [x] 2.4 Disable the chart's `ProfileScroll` while collapsed (kept visible, dimmed via `.profile-scroll:disabled`) and
  enable it on expand; verify with the current chart that the scroll stays, is dimmed and cannot be dragged
- [x] 2.5 Skip GPR drawing while collapsed and repaint on expand; verify zoom and position are unchanged after
  collapse/expand for GPR and sensor charts
- [x] 2.6 Add `Model.isCollapsed(SgyFile)` delegating to the chart and `Model.getExpandedFiles()`; verify that clicking
  a collapsed header selects the file and leaves it collapsed, and that the file is not marked unsaved

## 3. Collapsed files on the map, in crop and in selection

- [x] 3.1 `GpsTrack`: skip collapsed files and re-render on `FileCollapsedEvent`; verify the track disappears on
  collapse and returns on expand
- [x] 3.2 `FoundTracesLayer`: skip flags and user selection markers of collapsed files in `draw` and `mousePressed`;
  verify flags of a collapsed file are neither drawn nor clickable on the map
- [x] 3.3 `GridLayer`: skip grids of collapsed files, including the selected file, and re-render on
  `FileCollapsedEvent`; verify a computed grid disappears on collapse and returns on expand
- [x] 3.4 `RadarMap`: skip collapsed GPR files when drawing and re-render on `FileCollapsedEvent`; verify the
  amplitude overlay of a collapsed GPR file disappears and returns on expand
- [x] 3.5 `Model.selectNearestTrace`: search expanded files only; verify a map click near a collapsed track selects the
  nearest expanded trace (or nothing when all files are collapsed)
- [x] 3.6 `TraceCutter.applyCropLines` and `TraceCutInitializer.initialRect`: use expanded files only; verify a crop
  over a collapsed and an expanded track leaves the collapsed file unchanged, the initial area fits expanded tracks,
  and undo restores only the cropped files
- [x] 3.7 Verify quality issues stay on the map when a file is collapsed

## 4. Unified markers and compact GPR layout

- [x] 4.1 Move the GPR canvas margins to `ProfileField` (`TOP_MARGIN = 20`, `BOTTOM_MARGIN = 55`) and remove
  `Model.TOP_MARGIN`; verify the radargram starts right below the header
- [x] 4.2 Add `Chart.MARKER_OFFSET`; place GPR flags and selection pins at `ProfileField.TOP_MARGIN + MARKER_OFFSET`
  and derive the sensor-chart flag and pin offsets from it; verify icon bottoms and line starts sit at the same height
  on both chart types
- [x] 4.3 Unify the flag: `FoundPlace.FLAG_POLE_HEIGHT`, `ShapeHolder.FLAG` rebuilt to the sensor-chart geometry, pole
  / cloth outline / line in the flag color, selected flag filled with `FoundPlace.SELECTED_COLOR` and drawn 2 px, solid
  lines instead of dashed XOR; verify flags look the same on both chart types, selected and not
- [x] 4.4 Selection pin line on GPR: solid, colored by selection type (`ClickPlace.USER_COLOR` / `AUTO_COLOR`); verify
  user and automatic selections look the same on both chart types
- [x] 4.5 Anchor the distance-axis unit toggle to the bottom ruler (`TOGGLE_BOTTOM_OFFSET`); verify it is visible and
  clickable
- [x] 4.6 Draw the depth-axis unit toggle rotated 90° counter clockwise, centered on the radargram right of the axis
  labels; rename converters to `SampleConverter` / `TimeConverter` (labels `samples` / `ns`) and the toggle accessors
  to `getToggle()`; verify it is visible, readable and switches units on click
- [x] 4.7 Verify in the running app: flags and pins match between a GPR and a sensor chart, map flags use the new
  shape, and the distance-axis labels do not overlap the unit toggle

## 5. Specs

- [x] 5.1 Update `spec/overview.md`: chart header (close, name, eye, double click) and its exposed controls,
  `Model.isCollapsed` / `getExpandedFiles`, session-only collapsed state, `FileCollapsedEvent`, shared marker and GPR
  margin constants; verify the text matches the implementation

## 6. Verify

- [x] 6.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings
- [x] 6.2 If the `code-review` skill is available, invoke it (not the `code-review:code-review` plugin) on the
  uncommitted changes with the change's proposal and specs as the completeness reference, fix critical and major
  issues, and report the scores and any issues left open
