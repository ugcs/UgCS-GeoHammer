# Design

## Context

Analytics events are built by `EventsFactory` and sent by `EventSender` (Amplitude, asynchronous). File-related
events follow a listener pattern: `FileOpenEventsAnalytics` listens to domain events (`FileOpenedEvent`) and turns
them into analytics events with the file template from `Templates.getTemplateName`.

Filter entry points (see proposal.md for scope):

```
 LowPassTool / RunningMedianTool / TimeLagTool --+
 MCP apply_filter -------------------------------+--> SensorLineChart.applyLowPass / applyRunningMedian / applyTimeLag
                                                              |
                                                              v
                                                 SensorLineChart.applyFilter(filter, filterType, series, suffix)

 GprBackgroundTool.removeBackground ---+
 MCP RemoveGprBackground --------------+--> TraceFile.removeBackground(undoModel)  <-- also GprFile.open (restore)
 GprElevationTool.flattenSurface ---------> HorizontalProfile.flattenSurface(traceFile, removeAirGap)
```

All of them run on background threads (`FilterToolView.submitAction` or MCP virtual threads). No file data, undo or
cache handling changes: the change only observes successful applications.

## Goals / Non-Goals

**Goals:**
- One analytics event per file a filter is applied to, from the tool panel and MCP alike.
- Keep analytics out of charts, tools and file formats: they announce a domain event, analytics listens.

**Non-Goals:**
- Refactoring the filter tools or unifying GPR and series filter entry points.
- Unit tests for the analytics listener: by decision, it only maps an event to two properties; the behavior is
  verified manually in the app.

## Decisions

### Domain event + analytics listener

Add `FilterAppliedEvent(source, SgyFile file, FilterType filterType)` in `model/event`, published via
`model.publishEvent(...)`. Add `FilterEventsAnalytics` (`@Service` in `analytics`) with an `@EventListener` that sends
`eventsFactory.createFilterAppliedEvent(filterType, Templates.getTemplateName(file))`.

The event carries the `SgyFile` itself, so the listener needs no `Model` lookup (unlike `FileOpenEventsAnalytics`,
whose event has only a `File`).

*Alternative:* inject `EventSender`/`EventsFactory` into each entry point, as `ScriptExecutor` does. Rejected: it pulls
analytics into a view (`SensorLineChart`), two tools and an MCP tool; the event keeps them unaware of analytics and
lets other listeners use it later.

The listener runs synchronously on the publishing background thread and touches no UI. `EventSender.send` is
asynchronous, so filters are not slowed down.

### Filter ids as an enum

Add `enum FilterType` (next to the event in `model/event`) with constants `LOW_PASS("lowpass")`,
`RUNNING_MEDIAN("running_median")`, `TIME_LAG("timelag")`, `BACKGROUND_REMOVAL("background_removal")`,
`FLATTEN_SURFACE("flatten_surface")` and `getCode()`. The analytics value is the code, never `ordinal()` or `name()`.
The ids match the MCP `apply_filter` names for series filters.

Add `EventType.FILTER_APPLIED("geohammer-filter-applied")` and `EventsFactory.createFilterAppliedEvent(FilterType,
String template)` setting `filter` and `template`.

### Series filters: publish in `SensorLineChart.applyFilter`

`applyFilter` gets a `FilterType` parameter; `applyLowPass`, `applyTimeLag`, `applyRunningMedian` pass their type.
The event is published at the end of `applyFilter`, after the filtered column is created, so early returns (empty
series name, series without chart) send nothing. This is the only point shared by the three tools and MCP
`apply_filter`, and "Apply to all" already calls it once per chart, giving one event per file.

*Alternative:* publish in each tool and in MCP `ApplyFilter`. Rejected: four call sites per filter and each must
replicate the "was it applied" check.

### GPR: publish at the user-action call sites, not in `TraceFile`

`TraceFile.removeBackground` is also called by `GprFile` on open to restore a removed background from meta, which
must not send an event. So the event is published by the callers that represent a user action:
- `GprBackgroundTool.removeBackground`: once per file inside the loop, only for files actually processed
  (`!isBackgroundRemoved()`).
- MCP `RemoveGprBackground.invoke`: after `removeBackground`, not on the "already removed" early return.
- `GprElevationTool.flattenSurface`: after `groundProfile.flattenSurface(...)`.

*Alternative:* a flag parameter on `TraceFile.removeBackground` to suppress the event on open. Rejected: the format
layer would publish Spring events, and a boolean flag on a data method is a hidden side effect.

## Risks / Trade-offs

- [A new GPR entry point forgets to publish the event] → GPR call sites are few; the spec lists tracked filters, and
  a new entry point is reviewed against it.
- [Template is null for an unknown file type] → `Event.withProperty` drops null values with a warning; the event is
  still sent with `filter` only. Acceptable, same as `geohammer-file-opened`.
- [Filter failing on one line still sends the event] → `applyFilter` logs per-line errors and still creates the
  column; the filter is considered applied, which matches what the user sees.
