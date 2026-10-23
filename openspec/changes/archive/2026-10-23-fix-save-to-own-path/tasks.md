# Tasks

## 1. Save to own path

- [x] 1.1 Change the `checkNotOpened` message to "File is open in GeoHammer: <path>. Choose another name or folder."
  without changing its logic; verify: no "File in use" / "Close target file first" text remains
  (`grep -rn "File in use" src/main`)
- [x] 1.2 In `saveToCsv`, delegate a self-target to `saveFile(csvFile)` before `checkNotOpened` and the rename steps;
  verify manually: open a CSV/.asc file, edit, "Save to.." with the suggested path → no error, file content updated,
  unsaved mark cleared, grid still shown
- [x] 1.3 Verify manually that refusals still work: two CSV files open, "Save to.." of one onto the other → the new
  message, both files unchanged; GPR file, "Save to.." → "Single file" onto its own path → the new message, source
  file timestamp unchanged

## 2. Verify

- [x] 2.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings
- [x] 2.2 Invoke the `code-review` skill on the uncommitted changes with this change's proposal and specs as the
  completeness reference, fix critical and major issues, and report the scores and any issues left open
