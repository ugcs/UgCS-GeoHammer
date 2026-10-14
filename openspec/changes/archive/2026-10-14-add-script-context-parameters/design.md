# Design

## Context

See proposal.md - Why. Current flow of a script run:

```
ScriptExecutionTool (FX)  --params from panel inputs-->  ScriptCoordinator.submit
MCP RunScript (virtual)   --params from the request--->        |
                                                    shared virtual-thread executor
                                                               |
                                    for each file: ScriptExecutor.execute(file, metadata, params)
                                       - copy the file to a temp file (random numeric name tail)
                                       - argv: python script.py <copy> --<name> <value> ...
                                       - env:  GEOHAMMER_COLUMN_<SEMANTIC>=<header>  (always, all scripts)
                                       - load the copy back when the script replaces its input
```

- `ScriptParameter` is a Jackson record read from `scripts/*.json`; `ScriptMetadata` holds the parameter list and the
  required/value validation used by both the panel and MCP.
- The panel builds one input per parameter, stores every value in `Settings` per script and parameter name, and
  passes one value map to all files of an "Apply to all" run.
- MCP `ScriptTool.describeScript` lists all parameters; `RunScript` passes any name-value pair through.
- Scripts read semantic columns through `script_utils.semantic_column()`, which falls back to the semantic name when
  the variable is missing.

## Goals / Non-Goals

**Goals:**
- One place that turns declared parameters into per-file argument values: `ScriptExecutor`.
- Hidden parameters cannot be supplied from outside: panel, settings and MCP never carry them.

**Non-Goals:**
- No change to how scripts that replace their input are loaded back, snapshotted or undone.
- No change to undeclared parameters passed by MCP agents (still passed through as today).

## Decisions

### 1. Semantic columns as `COLUMN_NAME` + `semantic`, original path as a type

```json
{ "name": "lat-col", "type": "COLUMN_NAME", "semantic": "Latitude" },
{ "name": "original-path", "type": "ORIGINAL_PATH" }
```

The type says what the value is (a column header), `semantic` says where its default comes from. A visible semantic
column is an ordinary column dropdown, so the panel needs no new input. The original path is not a user input but
run context, so it is a type that is always hidden.

Alternatives:
- `@semantic:latitude` / `@original-path` macros in `default_value`: no new types, but the rule hides in a string,
  typos surface only at run time, and visibility would depend on the content of the default value.
- `COLUMN_SEMANTIC` and `ORIGINAL_FILE_PATH` types: validated, but `COLUMN_SEMANTIC` duplicates `COLUMN_NAME` in the
  panel and mixes the kind of value with its source.

Validation in `ScriptParameter.validate()`: `semantic` only on `COLUMN_NAME`, its value one of `Semantic.getName()`,
not combined with `default_value`; `ORIGINAL_PATH` with `visible: true` is invalid. A hidden parameter that is neither
semantic nor original path must have a default value when it is required.

### 2. `visible` flag with a kind-dependent default

`ScriptParameter` gets `@Nullable Boolean visible` and `isVisible()`: the explicit value, otherwise `false` for
semantic and `ORIGINAL_PATH` parameters and `true` for the rest. Named `visible` rather than `display`: `display`
sits next to `display_name` in the same object and already means "plotted in the chart" for columns.

### 3. Per-file resolution in `ScriptExecutor`

`ScriptExecutor.execute` (background thread, shared virtual-thread executor) builds the argument map for each file:

```
for each declared parameter:
  ORIGINAL_PATH            -> absolute path of sgyFile.getFile()
  semantic, no given value -> schema header with that semantic, else the semantic name
  hidden, other            -> default_value
  visible                  -> the given value (as today)
```

The map is built from the declared parameters first, then extended with undeclared values (MCP pass-through), so the
argument order follows the JSON description. `ScriptMetadata.getLineIndex` keeps reading the given values.
`createColumnVariables`, `COLUMN_VARIABLE_PREFIX` and the `environment` overload of `CommandExecutor.executeCommand`
are removed; the overload has no other caller.

### 4. Panel and MCP skip hidden parameters

- `ScriptExecutionTool.updateParametersBox` creates inputs only for visible parameters, so `extractScriptParams` and
  `storeParamValues` never see hidden ones. For a visible semantic column the initial selection is the stored value
  when the file has that column, otherwise the file's semantic column (`updateComboBoxIfChanged` falls back to it
  instead of `default_value`).
- `ScriptTool.describeScript` lists visible parameters only; `RunScript.invoke` rejects a request that sets a hidden
  parameter before validation, with the parameter name in the message.
- `CreateScript`: parameter schema gains `visible` and `semantic`; the contract text replaces the
  `semantic_column()` advice with semantic `COLUMN_NAME` parameters and describes `ORIGINAL_PATH` for naming outputs.
  The type enum picks up `ORIGINAL_PATH` from `ParameterType.values()`.

No new events; the panel keeps its existing `FileSelectedEvent` handling.

### 5. Scripts

| Script | original-path | semantic columns | other |
|---|---|---|---|
| UAWOS_RSS_processing | yes | - | output-dir removed from JSON; CSV next to the source |
| MagNIMBUS gradiometer estimation | yes | - | |
| MagNIMBUS single sensor estimation | yes | Latitude, Longitude | |
| export_geosoft_database | yes | Latitude, Longitude | |
| mbes_patch_test_route | yes | - | |
| MagNIMBUS_data_correction | - | Latitude, Longitude | |
| auto_time_lag_correction | - | Line, Latitude, Longitude | |
| uxo_anomaly_marker | - | Latitude, Longitude | existing `--lat-col`/`--lon-col` declared in JSON |

Each script declares the arguments in argparse with defaults that keep standalone runs working (`--original-path`
defaults to the input path, column arguments to the semantic names). Output names are built from the original path
without `normalize_input_stem`; `semantic_column`, `COLUMN_VARIABLE_PREFIX`, the semantic name constants and
`normalize_input_stem` are removed from `script_utils.py`. The RSS script keeps `--output-dir` in argparse as a
fallback for standalone runs.

## Risks / Trade-offs

- [Scripts created through MCP that call `semantic_column()` fail on import] → accepted for the pre-release; the
  `create_script` contract describes the new parameters.
- [Output names change for sources with a long numeric tail] → intended; listed as breaking in the proposal.
- ["Apply to all" with a visible semantic column passes the selected header to all files] → visible means the runner
  chooses; authors keep semantic columns hidden unless overriding is needed.
- [RSS cannot write when the source folder is read-only] → the script error names the folder; an output folder
  parameter can be added back to the JSON without changing the script.
- [JSON edits in the installation folder are lost on update] → `visible` is documented as an author setting.

## Migration Plan

No data migration. Bundled scripts and their JSON descriptions ship together with the Java change; user scripts
relying on the environment variables must declare semantic parameters.
