# Tasks

## 1. Sample range on all channels

- [x] 1.1 Override `DztFile.syncMeta()`: call `super.syncMeta()`, then set `meta.getSampleRange()` on the traces of
  every channel; verify by task 3.4

## 2. Save the kept traces

- [x] 2.1 Change `DztWriter.write` to take `int[] traceIndices` instead of an `IndexRange`: for saved file position
  `index` write `channel.getTraces().get(traceIndices[index])` of every channel, write `index` as the trace's first
  value, and validate every trace index against every channel's size; verify `mvn compile` passes
- [x] 2.2 In `DztFile.save(file, range)`, build `fileTraceIndices[index] = getFileTraceIndex(range.from() + index)`
  once and pass it to the writer and to `buildDzgMappings`, which maps `(index, fileTraceIndices[index])` for traces
  the `.dzg` has and drops its meta / no-meta branches; verify by tasks 3.2 and 3.3

## 3. Regression test

- [x] 3.1 Create `src/test/java/com/ugcs/geohammer/format/dzt/DztFileTest` with a `@TempDir` helper that writes a
  synthetic 2-channel DZT (distinct sample values per trace and channel) and a `.dzg` with one record per trace;
  verify a plain open reports both channels and all traces
- [x] 3.2 Test single-file save after removing the first traces from meta: reopened channels hold exactly the kept
  traces in order, the saved traces and the saved `.dzg` are indexed from zero, and each saved trace has the position
  of the same source trace; verify the test fails on the old code and passes with section 2
- [x] 3.3 Test `save(file, range)` with a meta sub-range of the cropped file (the separate-lines path) with the same
  checks; verify it passes
- [x] 3.4 Test sample crop: set a meta sample range, `syncMeta()`, `save(file)`; both reopened channels have the
  cropped sample count and values; verify it fails without task 1.1 and passes with it
- [x] 3.5 Run `mvn test` and verify no regressions

## 4. Verify

- [x] 4.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings
- [x] 4.2 If the `code-review` skill is available, invoke it (not the `code-review:code-review` plugin) on the
  uncommitted changes with this change's proposal and specs as the completeness reference, fix critical and major
  issues, and report the scores and any issues left open
