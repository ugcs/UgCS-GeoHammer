# Proposal

## Why

The script panel reads the `scripts` folder only on startup and when another file is selected, so a script added
while GeoHammer runs (copied by the user or created by an agent through MCP `create_script`) is not offered until the
user switches files or restarts. One unreadable JSON description also empties the whole list. Issue #982.

## What Changes

- The script list in the script panel is read from the `scripts` folder each time the user opens the script dropdown,
  so newly added scripts appear without a restart or a file switch.
- Reloading the list keeps the selected script and the values typed into its parameter inputs.
- A script whose JSON description cannot be read is left out of the list and logged; the other scripts stay
  available. This applies to the script panel and to the MCP script tools, which share the loader.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `script-execution`: adds requirements on which scripts are offered — scripts added while GeoHammer runs and scripts
  with unreadable descriptions.

## Non-goals

- No watching of the `scripts` folder: the list is not updated while the dropdown stays closed.
- Edits of the selected script's JSON description do not rebuild its parameter inputs; the user reselects the script
  to see them.
- No manual refresh button.
- MCP script tools already read the folder on every call; their behavior changes only for unreadable descriptions.

## Impact

- Java: `ScriptExecutionTool` (dropdown reload, selection keeping), `JsonScriptMetadataLoader` (skip unreadable
  descriptions).
- MCP `list_scripts`, `get_script`, `run_script`: an unreadable description no longer fails the call; that script is
  reported as not found.
