# Tasks

## 1. Parameter model

- [x] 1.1 Add `ORIGINAL_PATH` to `ScriptParameter.ParameterType` and nullable `visible` and `semantic` fields to the
  `ScriptParameter` record with `isVisible()` (explicit value, else hidden for semantic and `ORIGINAL_PATH`); verify
  the existing `JsonScriptMetadataLoaderTest` still passes
- [x] 1.2 Extend `ScriptParameter.validate()`: `semantic` only on `COLUMN_NAME` and one of `Semantic.getName()`, not
  combined with `default_value`; `ORIGINAL_PATH` never visible; a required hidden regular parameter has a default;
  verify a JSON with `ORIGINAL_PATH` + `"visible": true` shows "Invalid script metadata" in the panel

## 2. Per-file resolution and removal of environment variables

- [x] 2.1 In `ScriptExecutor.execute` build the per-file argument map from the declared parameters (original path,
  semantic header or semantic name, hidden default, given value), then append undeclared given values; verify by
  running `uxo_anomaly_marker` after group 4 on a file whose template maps latitude to `lat`
- [x] 2.2 Remove `createColumnVariables`, `COLUMN_VARIABLE_PREFIX` and the `environment` overload of
  `CommandExecutor.executeCommand`; verify no references remain (`grep -rn GEOHAMMER_COLUMN src scripts` is empty)

## 3. Panel and MCP

- [x] 3.1 `ScriptExecutionTool`: create inputs only for visible parameters, so hidden values are neither shown nor
  stored; preselect the file's semantic column for visible semantic `COLUMN_NAME` parameters when the stored value is
  not a column of the file; verify in the app with RSS (no output folder input) and a temporary visible semantic column
- [x] 3.2 MCP `ScriptTool.describeScript`: list visible parameters only; verify `list_scripts` shows no
  `original-path` for the RSS script
- [x] 3.3 MCP `RunScript.invoke`: reject a request that sets a hidden parameter, naming it in the error; verify by
  calling `run_script` with `original-path` in `params`
- [x] 3.4 MCP `CreateScript`: add `visible` and `semantic` to the parameter schema and replace the `semantic_column()`
  advice in the contract with semantic `COLUMN_NAME` and `ORIGINAL_PATH` parameters; verify the tool description no
  longer mentions `semantic_column` or `GEOHAMMER_COLUMN`

## 4. Scripts

- [x] 4.1 `script_utils.py`: remove `COLUMN_VARIABLE_PREFIX`, `semantic_column`, the semantic name constants and
  `normalize_input_stem`; verify `grep -rn "semantic_column\|normalize_input_stem" scripts` lists only the scripts
  updated in 4.2-4.4
- [x] 4.2 `UAWOS_RSS_processing`: replace `output-dir` with `original-path` in the JSON, add `--original-path` to
  argparse and write `<source stem>_processed.csv` to `output_dir or dirname(original_path)`; verify a run in the app
  writes the CSV next to the source SGY
- [x] 4.3 Declare `original-path` in the JSON and argparse of both MagNIMBUS estimation scripts,
  `export_geosoft_database` and `mbes_patch_test_route`, and name their outputs after it; verify each script compiles
  (`python3 -m py_compile`) and its output name keeps a numeric tail of the source name
- [x] 4.4 Declare semantic column parameters in the JSON and argparse of `MagNIMBUS_data_correction`,
  `auto_time_lag_correction`, `MagNIMBUS_single_sensor_distance_depth_weight_estimation`, `export_geosoft_database`
  (Latitude, Longitude; Line for auto time lag) and `uxo_anomaly_marker` (existing `lat-col`/`lon-col`), defaulting to
  the semantic names in argparse; verify each script compiles and `auto_time_lag_correction` runs on a MagNIMBUS file

## 5. Verify

- [x] 5.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings
- [x] 5.2 Invoke the `code-review` skill on the uncommitted changes with proposal.md and specs/script-execution as the
  completeness reference, fix critical and major issues, and report the scores and any issues left open
