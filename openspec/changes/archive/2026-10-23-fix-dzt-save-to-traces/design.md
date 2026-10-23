# Design

## Context

A `DztFile` holds every channel's source file traces (`DztChannel.getTraces()`); the selected channel's list is also
the file's `traces`. The chart sees the meta-mapped `getTraces()` (`TraceList`): meta row `i` points at source file
trace `meta.getTraceIndex(i)`. Crops and line removals edit meta rows only.

```
file traces (channel.getTraces())   0   1   2   3   4   5   6   7
                                    x   x   x  [3] [4] [5] [6] [7]   <- first line removed
meta rows (getTraces())                         0   1   2   3   4

save(file, [0, 5))   DztWriter.writeChannels  -> file traces 0..4          (wrong traces)
                     DztFile.buildDzgMappings -> meta 0..4 -> file traces 3..7
```

All callers pass ranges in meta space: `Saver.saveToSingleFile` (`save(file)` = `[0, numTraces())` on a copy),
`Saver.saveToSeparateFiles` (`getLineRanges()`), `ScriptExecutor.copy` (one line range). `GprFile.save` is
correct because it iterates `getTraces()`.

The sample window is meta state too: every change (`TraceTransform` sample crop, `HorizontalProfile` level ground,
`DztFile.selectChannel`, undo via `SgyFileWithMeta.Snapshot.restoreFile`) ends in `syncMeta()`, and
`TraceFile.syncMeta` applies `meta.getSampleRange()` only to `traces`, i.e. the selected channel. Other channels keep
no range or a stale one from an earlier selection, so a multi-channel save writes them with different sample windows.

The writer puts `trace.getIndex()` into the first value of each trace: the source file index for the selected
channel, a stale value for others. The reader skips this value.

## Goals / Non-Goals

**Goals:**
- One array of source file trace indices, derived once from the meta range, drives both the traces written to the
  DZT and the index mappings written to the `.dzg`.
- All channels of a DZT file follow the meta sample range in memory, so every consumer (save, channel switch) sees
  the same window.

**Non-Goals:**
- Marks in the DZT file, the DZT undo desync and other formats (see proposal Non-goals).
- Changing the `SgyFile.save(File, IndexRange)` contract or its callers.

## Decisions

### 1. `DztFile.save` maps the range to source file trace indices once

`DztFile.save(file, range)` builds `int[] fileTraceIndices` with
`fileTraceIndices[index] = getFileTraceIndex(range.from() + index)` (this already handles a missing meta). It passes
the array to `DztWriter.write(channels, traceIndices)` and builds the `.dzg` mappings as
`(index, fileTraceIndices[index])` for every `index` the `.dzg` has a record for. `buildDzgMappings` takes the array
and loses its meta / no-meta branches.

The writer writes `channel.getTraces().get(traceIndices[index])` for every channel at saved file position `index` and
validates each trace index against every channel's size (replacing the range check). Naming follows `DzgFile`:
`index` is a position in the saved file, a trace index points into the source file.

Alternatives:
- Wrap each channel in a meta-mapped list view and keep passing the range. Rejected: it adds a view type used once,
  and the writer would still accept an `IndexRange` whose meaning depends on what it was handed.
- `DztWriter.write(dztFile, range)`, with the writer mapping the range through `getFileTraceIndex`. Rejected: the
  writer and `DztFile` depend on each other, the writer has to know about meta, validation needs meta space, and the
  `.dzt` and the `.dzg` derive the mapping separately again, which is how #1030 happened.

With one array, the DZT and the `.dzg` cannot describe different traces.

### 2. Saved traces are indexed from zero

The writer writes the saved file position `index` as the first value of each trace instead of `trace.getIndex()`.
This matches the indices that `DzgFile.save` writes through the mappings (`replaceIndex(record, index)`) and makes
the value the same for all channels.

### 3. `DztFile.syncMeta` applies the sample range to all channels

`DztFile` overrides `syncMeta()`: call `super.syncMeta()` (positions, selected channel), then set
`meta.getSampleRange()` on the traces of every channel. Fixing it at the sync point rather than in the writer keeps
in-memory state consistent for every path that reads other channels, including undo restore, which already calls
`syncMeta()` after restoring meta.

`DztWriter.writeHeaders` keeps deriving each channel's `rh_nsamp` from that channel's traces; with all channels
synced it now reflects the meta window for each of them.

### 4. Threads, events, undo

No new threads or events. Save keeps running in the `Saver` background task. `syncMeta` runs where it is called
today. No file data is mutated by saving, so there is no snapshot, `setUnsaved` or `tracesChanged` work; the
`syncMeta` override sets the same derived state the base method already sets, and is restored by the existing meta
snapshot.

`Channel.getTraces()` no longer declares `IOException`: no channel implementation throws it.

### 5. Test

New `src/test/java/com/ugcs/geohammer/format/dzt/DztFileTest` (new test package mirroring `format/dzt`). In a
`@TempDir` it writes a small synthetic 2-channel DZT with distinct sample values per trace and channel and a `.dzg`
with one record per trace, opens it with `DztFile.open` (no Spring context needed without marks), removes the first
traces from meta, then:
- `save(file)`: reopened channels contain exactly the kept traces in order; the saved traces and the saved `.dzg`
  are indexed from zero, and each saved trace has the position of the same source trace.
- `save(file, range)` with a meta sub-range (the separate-lines path): same checks for that range.
- sample crop through meta + `syncMeta()`, then `save(file)`: both reopened channels have the cropped sample count
  and values.

## Risks / Trade-offs

- [Files with more traces than the first value holds (16-bit samples, > 65535 traces) wrap the index] → Same as
  today with source file indices; the reader ignores the value.
- [Writing a mark-free first value may differ from what GSSI tools wrote there] → Marks are a non-goal; the original
  value is already discarded on read, so saved files lose nothing more than before.
- [Non-selected channels now carry a sample range] → It is the meta range the selected channel already uses;
  `selectChannel` re-applies it on switch.
