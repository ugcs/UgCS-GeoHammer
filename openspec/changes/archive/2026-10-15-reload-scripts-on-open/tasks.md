# Tasks

## 1. Skip unreadable descriptions

- [x] 1.1 In `JsonScriptMetadataLoader.loadScriptMetadata`, catch the per-file `IOException`, `log.warn` the file name
  with the exception and continue; keep directory errors propagating. Verify: with an invalid `broken.json` in
  `scripts/`, the panel and MCP `list_scripts` list all other scripts and no error dialog appears.

## 2. Reload the list when the dropdown opens

- [x] 2.1 Extract the load → OS filter → template filter part of `ScriptExecutionTool.updateView()` into one private
  method and use it from `updateView()`; verify the file-switch path still lists the same scripts as before.
- [x] 2.2 Add a `ReentranceGuard` field and make the `scriptsMetadataSelector` `onAction` handler return early while it
  is latched; verify selecting a script by hand still rebuilds its inputs and saves the last selection.
- [x] 2.3 Add a `setOnShowing` handler that, inside the guard, rebuilds the list, replaces the combo items and
  reselects the previous script by `filename()`; when it is gone, clear the selection and call
  `updateParametersBox(null)`. On a folder read failure, log and keep the current items. Verify by hand: copying a
  script pair into `scripts/` makes it appear on the next open; a typed parameter value survives reopening; deleting
  the selected script clears the selection and the inputs; the popup is sized correctly after the item count changes.

## 3. Verify

- [x] 3.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings.
- [x] 3.2 If the `code-review` skill is available, invoke it (not the `code-review:code-review` plugin) on the
  uncommitted changes with this change's proposal and specs as the completeness reference, fix critical and major
  issues, and report the scores and any issues left open.
