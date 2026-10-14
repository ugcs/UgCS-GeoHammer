# Proposal

## Why

Scripts run on a temporary copy of the open file, so they do not know the source file: output files are named after
the copy (with a random numeric tail removed by a heuristic that also cuts real timestamps) and the RSS script
cannot write its CSV next to the source SGY. Column names of semantic columns (latitude, longitude, line) reach
scripts through hidden environment variables that are not declared anywhere, so a script's inputs cannot be read
from its JSON description. Issue #981.

## What Changes

- Script parameters declared in the JSON description get a `visible` flag. A parameter that is not visible is not
  shown in the script panel, not stored in settings, not listed or accepted by the MCP script tools, and its value is
  always resolved by GeoHammer.
- A `COLUMN_NAME` parameter may declare a `semantic` (`Latitude`, `Longitude`, `Line`, `Altitude`, `Altitude AGL`,
  `TMI`, `Mark`). Its value is the column with that semantic in each processed file. Such parameters are not visible
  by default; a script author may make one visible so the user can pick another column.
- New parameter type `ORIGINAL_PATH`: the path of the source file of each processed file. It is never visible.
- Scripts that name their output files after the input (`UAWOS_RSS_processing`, both MagNIMBUS distance/depth/weight
  estimation scripts, `export_geosoft_database`, `mbes_patch_test_route`) declare `original-path` and name outputs
  after the source file.
- `UAWOS_RSS_processing` drops its output folder parameter and always writes its CSV next to the source SGY file.
- Scripts that read semantic columns (`MagNIMBUS_data_correction`, `auto_time_lag_correction`,
  `MagNIMBUS_single_sensor_distance_depth_weight_estimation`, `export_geosoft_database`, `uxo_anomaly_marker`) declare
  them as semantic `COLUMN_NAME` parameters.
- **BREAKING**: the `GEOHAMMER_COLUMN_*` environment variables and `script_utils.semantic_column()` are removed.
  Scripts created earlier through MCP `create_script` that rely on them fall back to the default column names.
- **BREAKING**: `script_utils.normalize_input_stem()` is removed. Output files of sources whose name ends with a long
  number (e.g. a timestamp) keep that number in their name.
- The MCP `create_script` contract describes semantic and original path parameters instead of the environment
  variables.

## Capabilities

### New Capabilities

- `script-execution`: how GeoHammer runs Python scripts on open files — which parameters a script receives, how
  their values are resolved per processed file, and which parameters users and agents may set.

### Modified Capabilities

None.

## Non-goals

- Output folder parameters of the export scripts stay as they are: visible and required.
- No "next to each data file" mode for output folders.
- No protection against output name clashes when "Apply to all" writes files with equal names to one folder.
- No migration of user scripts created through MCP; the release is a pre-release.
- Edits of script JSON files in the installation folder are not preserved on update; `visible` is a tool for script
  authors, not a user setting.

## Impact

- Java: `ScriptParameter`, `ScriptMetadata`, `ScriptExecutor`, `ScriptExecutionTool`, MCP `ScriptTool`, `RunScript`,
  `CreateScript`.
- Python: `scripts/script_utils.py`; JSON descriptions and argument parsing of the nine scripts listed above.
- Output file names of the five output-writing scripts change for sources with a long numeric name tail.
