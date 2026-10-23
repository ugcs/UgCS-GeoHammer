# Proposal

## Why

"Save to.." on a DZT file writes the wrong traces after the file was cropped (#1030). The save range is given in the
index space of the traces shown in the chart, but the DZT writer indexes the traces of the source file, while the
`.dzg` is built through the chart-to-source mapping. After the first line is cropped or removed, the saved radargram
starts with the removed traces and its GPS positions belong to other traces. The same happens for every line saved
to separate files and for scripts run on a single line of a DZT file.

## What Changes

- "Save to.." on a DZT file (single file or separate lines) writes exactly the traces shown in the chart, in chart
  order, with the GPS positions of the same traces in the `.dzg`.
- Saved traces are indexed from zero, consistent with the trace indices in the saved `.dzg`.
- The sample range of a DZT file applies to all its channels, not only to the selected one, so every channel of a
  saved multi-channel file is limited to that range.

## Non-goals

- Writing marks or line boundaries into the DZT file: they stay in the `.geohammer` sidecar only.
- Fixing undo of sample-changing operations (background removal, elevation, scripts) on DZT files, which leaves the
  selected channel with the pre-undo samples; tracked separately.
- Changing what "Save to.." does for SEG-Y, sonar, NMEA or CSV files.
- Copying the whole source file for scripts run without a line (it still ignores crops, as for SEG-Y).

## Capabilities

### New Capabilities

### Modified Capabilities
- `file-saving`: adds what "Save to.." writes for a GPR file: the traces shown in the chart, with matching
  positions and the file's sample range on every channel.

## Impact

- `DztFile` (save range mapping, sample range sync across channels) and `DztWriter` (writes given traces, saved
  trace indices).
- Callers are unchanged: `Saver` ("Save to.." single file and separate lines) and `ScriptExecutor` (line export).
- No format, API or dependency changes; files saved before the fix are not affected on reopen.
