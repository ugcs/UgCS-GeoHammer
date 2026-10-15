# Design

## Context

`ScriptExecutionTool.updateView()` reads `scripts/*.json` through `ScriptMetadataLoader`, filters by OS and template,
and refills `scriptsMetadataSelector` via `restoreScriptSelection()`. It runs in the constructor and from
`ToolView.selectFile`, which returns early when the same file is selected again — so the list goes stale while the
file stays the same. `restoreScriptSelection()` selects the script saved in `Settings` for the template; selecting
fires the combo's `onAction`, which calls `updateParametersBox` and rebuilds the inputs from stored values.

MCP `ScriptTool.findScript` and `ListScripts` call the same loader on every request, so they are already fresh.
`JsonScriptMetadataLoader` throws on the first unreadable `*.json`, failing the whole list for both callers.

No file data changes in this change: no snapshot, undo, `setUnsaved` or cache invalidation is involved.

## Goals / Non-Goals

**Goals:**
- Fresh script list whenever the dropdown is opened, without touching the selected script's inputs.
- One unreadable description no longer hides the rest.

**Non-Goals:**
- Folder watching, rebuilding inputs of a changed script, any change to `updateView()`'s file-switch path.

## Decisions

**Reload on `scriptsMetadataSelector.setOnShowing`, on the FX thread.** The handler reloads, refilters and replaces
the combo items before the popup is laid out, so the user sees the fresh list immediately. Reading ~15 small JSON
files takes milliseconds; `updateView()` already does the same read on the FX thread. Alternatives: a `WatchService`
on `scripts/` (rejected: a background thread, ~10 s polling delay on macOS, debouncing of `.py`/`.json` pairs and
editor temp files, all for a list nobody is looking at); loading on the executor (rejected: the popup would show the
stale list first and then jump); a refresh button (rejected: not discoverable).

**Keep the selection by filename, without firing `onAction`.** Before replacing items, remember the current value's
`filename()`; after, select the reloaded item with the same filename, or clear the selection if it is gone.
`ScriptMetadata` is a record compared by value, so matching on the old instance would drop the selection when the JSON
changed. The replacement runs inside a `ReentranceGuard` (`util`), and the `onAction` handler returns early while the
guard is latched, so neither `updateParametersBox` nor the `Settings` write runs and the typed values stay. When the
selection is cleared because the script is gone, the handler calls `updateParametersBox(null)` explicitly to remove the
inputs and hide Apply buttons.

The list-building part of `updateView()` (load → OS filter → template filter) is extracted into one private method
used by both `updateView()` and the showing handler, so the filtering rules stay in one place. The error dialog stays
in `updateView()` only; the showing handler logs a failure to read the folder itself (e.g. folder missing) and keeps the
current items.

**While a script runs**, `disableAndShowProgress()` disables `inputContainer`, which holds the dropdown, so it cannot
be opened and the showing handler does not run. No extra check is needed.

**Skip unreadable descriptions in `JsonScriptMetadataLoader`.** Catch the per-file `IOException` (Jackson parse and
mapping errors are `IOException`s), `log.warn` with the file name and the exception, and continue. Errors opening the
directory still propagate. This follows "bad data records: log and skip" and fixes the shared loader once for the panel
and MCP. The interface signature stays the same.

## Risks / Trade-offs

- [JavaFX sizes the popup height before syncing the list item count (`ComboBoxListViewSkin` updates it only in
  `prefWidth`), and later reconfiguration only grows the popup, so changing items in `onShowing` leaves it sized for
  the old count] → after replacing items, call `prefWidth` on the skin's popup content to sync the count before the
  popup is sized. Reloading earlier (e.g. on mouse press) does not help: no layout pass reaches the popup before
  it is shown.
- [An agent that writes a description Jackson cannot map now gets "Script not found" from `get_script`/`run_script`
  instead of the parse error] → the warning in the log names the file; `create_script` builds the JSON itself, so this
  hits only hand-edited files.
- [Reloading on every open re-reads all descriptions] → negligible at the current folder size.
