# Tasks

## 1. Event and analytics

- [x] 1.1 Add `FilterType` enum (`model/event`) with codes `lowpass`, `running_median`, `timelag`,
  `background_removal`, `flatten_surface` and `getCode()`; verify it compiles
- [x] 1.2 Add `FilterAppliedEvent(source, SgyFile file, FilterType filterType)` extending `BaseEvent`; verify it compiles
- [x] 1.3 Add `EventType.FILTER_APPLIED("geohammer-filter-applied")` and
  `EventsFactory.createFilterAppliedEvent(FilterType, String template)` setting `filter` (code) and `template`
- [x] 1.4 Add `FilterEventsAnalytics` `@Service` with a private `@EventListener` on `FilterAppliedEvent` sending the
  event with `Templates.getTemplateName(file)`

## 2. Series filters

- [x] 2.1 Add a `FilterType` parameter to `SensorLineChart.applyFilter`, pass it from `applyLowPass`, `applyTimeLag`,
  `applyRunningMedian`, and publish `FilterAppliedEvent` after the filtered column is created (not on early returns);
  verify in the app that Apply and Apply to all on N files send N events (debug log or Amplitude)

## 3. GPR filters

- [x] 3.1 Publish `FilterAppliedEvent(BACKGROUND_REMOVAL)` in `GprBackgroundTool.removeBackground` once per processed
  file inside the loop; verify opening a GPR file with removed background in meta sends no event
- [x] 3.2 Publish `FilterAppliedEvent(BACKGROUND_REMOVAL)` in MCP `RemoveGprBackground.invoke` after removal, not on
  the "already removed" return; verify via MCP `remove_gpr_background`
- [x] 3.3 Publish `FilterAppliedEvent(FLATTEN_SURFACE)` in `GprElevationTool.flattenSurface` after
  `groundProfile.flattenSurface(...)`; verify by running Flatten surface

## 4. Docs

- [x] 4.1 Add `FilterAppliedEvent` to the Events list in `spec/overview.md`; verify the entry matches the event

## 5. Verify

- [x] 5.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings
- [x] 5.2 Invoke the `code-review` skill on the uncommitted changes with proposal.md and
  `specs/usage-analytics/spec.md` as the completeness reference; fix critical and major issues and report the
  scores and any issues left open
