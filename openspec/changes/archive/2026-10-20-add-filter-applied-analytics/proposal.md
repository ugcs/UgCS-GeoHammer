# Proposal

## Why

Usage analytics shows which file types users open and which scripts they run, but not which processing filters they
apply. Without it we cannot tell which filters are used, on which instruments, and where to invest (#1006).

## What Changes

- Send a new analytics event `geohammer-filter-applied` each time a filter is applied to a file.
- Event properties: `filter` (filter id) and `template` (template name of the file, as in `geohammer-file-opened`).
- Covered filters:
  - series filters on data files: `lowpass`, `running_median`, `timelag`;
  - GPR processing: `background_removal`, `flatten_surface`.
- One event per processed file: "Apply to all" and background removal over all GPR files send one event per file.
- Events are sent regardless of where the filter was started (tool panel or MCP).

## Non-goals

- No filter parameters, series names or file names in the event.
- No distinction between the tool panel and MCP as the event source.
- Grid render filters (range, analytic signal, hill shading, smoothing), spread coordinates, gridding, reprojection
  and quality control are not tracked.
- Background removal restored on GPR file open is not an application by the user and sends no event.
- The existing `geohammer-script-execution-started` event is not changed.

## Capabilities

### New Capabilities
- `usage-analytics`: anonymous usage events the app sends about user actions (here: filter applications).

### Modified Capabilities

## Impact

- `analytics`: new event type, factory method and a listener that turns filter applications into analytics events.
- `model/event`: new event announcing that a filter was applied to a file.
- `SensorLineChart.applyFilter` and its `applyLowPass`/`applyTimeLag`/`applyRunningMedian` callers.
- GPR entry points: `GprBackgroundTool`, MCP `remove_gpr_background`, `GprElevationTool`.
- No new dependencies; Amplitude dashboards get a new event type.
