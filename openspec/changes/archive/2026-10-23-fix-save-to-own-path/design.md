# Design

## Context

All save actions live in `Saver` and run through `runAction` → `TaskRunner` on a background thread; failures are
shown by `Dialogs.showError("Can't save file", e)` with the exception message.

"Save to.." already means different things per file type:

```
CSV            onSaveTo -> saveToCsv(csvFile, toFile)
               "save as": csvFile.save(toFile), setUnsaved(false),
               model.updateChartFile (rebinds chart), FileRenameEvent(old -> new), justdraw

GPR/Sonar/NMEA onSaveTo -> menu -> saveToSingleFile / saveToSeparateFiles
               "export": writes a copy, the chart stays on the source file
```

`checkNotOpened(toFile)` compares `toFile` with every open file via `File.equals`, including the file being saved,
so a target equal to the file's own path is always refused.

Excluding the file from the check alone is not enough for CSV: `saveToCsv` would then publish a `FileRenameEvent`
with old == new path, and `GridLayer.moveResult` puts the grid result under the same key and then removes it
(`results.remove(oldFile, result)`), dropping the file's grid.

## Goals / Non-Goals

**Goals:**
- A CSV "Save to.." onto its own path goes through the same code path as "Save".
- All other open-file targets keep being refused by the existing check, only with a clearer message.

**Non-Goals:**
- No change to `FileRenameEvent` listeners; the rename path is simply not taken for a self-target.
- No collision check for the per-line files of "Multiple files with separate lines" (not checked today either).

## Decisions

**1. CSV self-target delegates to `saveFile`.**
In `saveToCsv`, when the target equals the file's own path, call `saveFile(csvFile)` and return before
`checkNotOpened` and the rename steps. `saveFile` is what "Save" runs: `save(file)`, `setUnsaved(false)`,
`justdraw`. No `updateChartFile`, no `FileRenameEvent`, so per-file state keyed by path (grids) is untouched.
*Alternative:* exclude the source in `checkNotOpened` and keep the rename path — rejected because of the
`GridLayer` key removal above and a pointless rename event.
*Alternative:* refuse self-target for all types with a better message — rejected: for CSV "Save to.." is
"save as", and the user has already confirmed "Replace" in the OS dialog.

**2. `checkNotOpened` keeps its logic and only changes the message.**
It still compares the target with every open file, including the file being saved, so the GPR, sonar and NMEA
self-target stays refused without an extra check. Message: "File is open in GeoHammer: <path>. Choose another name or
folder." — no "in use", no "close first"; it fits both another open file and a source-preserving self-target.
*Alternative:* skip the saved file in `checkNotOpened` and add a separate source check in `saveToSingleFile` —
rejected: two changes where none is needed, and the skip alone would let a GPR export overwrite its source.

**3. Path comparison stays `Objects.equals` on `File`.**
Paths from the file chooser and drag-and-drop are absolute, and `File.equals` is case-insensitive on Windows, which
covers #1012. Relative command-line paths, links and 8.3 names are not resolved (non-goal in the proposal).

Threads and events: everything stays on the `runAction` background thread as today. No data mutation, so no
snapshot/undo or cache invalidation; `setUnsaved(false)` and `justdraw` come from `saveFile`.

## Risks / Trade-offs

- [Self-target relies on path equality; a relative path, link or short name to the same file is treated as a
  different path] → for CSV this falls back to the existing rename path, which still writes the right content;
  for export paths the source is not protected in that case. Same as today; normalizing paths at file opening is
  a separate fix.
- [`checkNotOpened` reads the open-file list off the FX thread] → pre-existing, unchanged by this change.
