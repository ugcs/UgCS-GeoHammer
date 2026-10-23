# Proposal

## Why

"Save to.." on a CSV-type file (including MagDrone .asc) fails with "File in use. Close target file first" when the
chosen path is the file's own path (#1012). The check that protects other open files also counts the file being
saved, and the save dialog suggests exactly that path by default, so accepting the defaults always ends in this
error. The message reads like an operating-system lock, which led support to suspect OneDrive.

## What Changes

- "Save to.." on a CSV-type file whose target is the file's own path saves the file in place, exactly like "Save".
- Every other "Save to.." target that is a file open in GeoHammer is still refused, as today: another open file, or
  the own path of a GPR, sonar or NMEA file (their source files are never rewritten).
- The refusal message no longer suggests an operating-system lock or tells the user to close the file.

## Non-goals

- Changing the default name or folder suggested by the "Save to.." dialog.
- Detecting operating-system file locks or paths that point to the same file through links or short names.
- Changing what "Save to.." does for GPR, sonar and NMEA files (export of a copy) or for CSV files (save as).

## Capabilities

### New Capabilities
- `file-saving`: saving open files with "Save" and "Save to..": in-place saves, saving under a new path and which
  target paths are refused.

### Modified Capabilities

## Impact

- `Saver` ("Save to.." handlers and the open-file check).
- No format, API or dependency changes. Quick Start wiki note on "Save to.." should be updated by support.
