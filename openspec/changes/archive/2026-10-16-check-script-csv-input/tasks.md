# Tasks

## 1. Shared input check

- [x] 1.1 Add `read_input_csv(input_path, **read_csv_kwargs)` to `scripts/script_utils.py` with the checks and the
  error of design decisions 1–3; verify with a quick run on copies of the #989 RTR file, the RTKLib test file
  `src/test/resources/TestData/Parsers/FixedColumnWidth/Valid.pos`, a headerless numeric CSV and a plain CSV that
  the first three fail with the expected reason and the plain one is read

## 2. Script migration

- [x] 2.1 Switch `export_geosoft_database.py` to `read_input_csv`; verify its read step finds all numeric columns of
  a tab-separated copy of a MagDrone ASC file with the header on the first line
- [x] 2.2 Switch `data_decimation.py`, `auto_time_lag_correction.py` (keeping `dtype=str, keep_default_na=False`) and
  `uxo_anomaly_marker.py`; verify no `detect_separator` + `read_csv` pair on the input remains in them
- [x] 2.3 Switch the four MagNIMBUS scripts, keeping the `try` around the read in the single-sensor script (design
  decision 4); verify no `detect_separator` + `read_csv` pair on the input remains in them
- [x] 2.4 Run data decimation from the command line on the #989 RTR file and on a plain CSV; verify the first exits
  with code 1 and the error, leaving the file byte-identical, and the second is decimated as before

## 3. Integration check

- [x] 3.1 In GeoHammer, run the Geosoft export or data decimation on the #989 RTR file; verify the script error shown
  in the app is the new message and the file stays unchanged

## 4. Verify

- [x] 4.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings; verify the build
  succeeds with no new warnings in the changed files
- [x] 4.2 If the `code-review` skill is available, invoke it (not the `code-review:code-review` plugin) on the
  uncommitted changes with this change's proposal and specs as the completeness reference; fix critical and major
  issues and report the scores and any issues left open
