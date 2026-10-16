## Context

`ScriptExecutor` writes the processed file to a temp copy in its template layout (`CsvFile.save`, or a plain copy
for GPR) and passes its path as the first argument. The eight bundled CSV scripts read it with
`pd.read_csv(path, sep=detect_separator(path))`; the Geosoft export calls `pd.read_csv(path)` without a separator.
None of them checks that the first line is the header. The process merges stderr into the output that GeoHammer
shows when a script fails (`CommandExecutor`, `redirectErrorStream`). See proposal.md for the failure.

What the copy starts with, for the formats in question:

```
MagDrone ASC RTR      Raw track data                 <- no separator, next line has 16 tabs
MagDrone ASC          % ... (when the device wrote a comment block)
RTKLib .pos           % program : ...                <- header itself is a % line, values fixed-width
plain CSV             Timestamp,Latitude,...         <- header first
```

## Goals / Non-Goals

**Goals:**
- One check, shared by all bundled CSV scripts, before pandas parses the file.
- An error that names the file and the reason, in the style the scripts already use (`Error: ...`, exit code 1).

**Non-Goals:**
- Skipping or restoring lines before the header (proposal non-goals).
- Changing what GeoHammer writes or checks.

## Decisions

### 1. `read_input_csv(input_path, **read_csv_kwargs)` in `script_utils.py`

Returns `(data, separator)`: the separator is still needed by the scripts that write the table back. It detects the
separator with `detect_separator`, checks the first two lines (decision 2), then calls
`pd.read_csv(input_path, sep=separator, **read_csv_kwargs)`. Extra kwargs keep script options such as
`dtype=str, keep_default_na=False` in auto time lag correction. pandas is imported inside the function, because
`script_utils` is also imported by scripts that do not depend on pandas (`mbes_patch_test_route.py`).

Alternative: a check-only function each script calls before its own `read_csv`. Rejected: scripts would still
repeat the separator detection and the read, and a new script could call one without the other.

### 2. Checks on the first two lines

Read with `utf-8-sig`, as `detect_separator` does. In order, the first failing check gives the reason:

| Check | Reason in the error | Catches |
|---|---|---|
| first line is empty or starts with `%` or `#` | comment lines precede the header | `%` blocks, RTKLib |
| first line has no separator and the second line has | a line precedes the header | `Raw track data` |
| more than half of the non-empty first-line values are numbers | the file has no header line | headerless CSV |

The "no separator" check is used instead of "the first line has fewer fields than the second": data lines ending
with a separator have one field more than the header, and the narrower check does not reject them. A file with a
single line or a single column passes the checks and is read as before.

### 3. Error reporting

`sys.exit(f"Error: {path}: {reason}; the header must be on the first line")`. `sys.exit` with a string prints it to
stderr and exits with code 1, the same outcome as the scripts' existing `print("Error: ...")` + `sys.exit(1)`, with
no traceback. The check runs before any output is written, so the file is never changed.

### 4. Script migration

Each script replaces `separator = detect_separator(path)` + `pd.read_csv(path, sep=separator, ...)` with
`data, separator = read_input_csv(path, ...)` and keeps its writes as they are. The Geosoft export gets the
separator this way too (spec: Geosoft export separator) and ignores it after reading.
`MagNIMBUS_single_sensor_distance_depth_weight_estimation.py` keeps its own `try` around the read for missing and
unreadable files; `sys.exit` raises `SystemExit`, which its `except Exception` does not catch.

## Risks / Trade-offs

- [A legitimate header made of numbers is reported as "no header line"] → Unlikely for survey data; the message names
  the file and the rule, so the cause is visible.
- [User scripts still fail with misleading errors] → Accepted (proposal non-goals); the routine is available to them.
- [RTR users still cannot export to Geosoft] → Accepted for now; reading files with lines before the header is a
  separate change.
