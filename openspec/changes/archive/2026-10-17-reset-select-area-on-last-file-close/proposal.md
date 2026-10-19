## Why

When the Select Area tool is on and the last open file is closed, the tool button stays pressed but becomes
disabled, and the crop polygon stays drawn on the empty map. The user cannot turn the tool off until another file is
opened (#995).

## What Changes

- Closing the last open file (via the chart close button or Close All) turns the Select Area tool off: the button is
  released and the crop polygon is removed from the map.
- Closing a file while other files remain open keeps the tool and its polygon as they are.

## Non-goals

- Turning the tool off when all remaining files are collapsed (they are still open; crop just has nothing to act on).
- A general rule that releases every map toggle tool when it gets disabled.
- Changing how the initial crop area is fitted or how cropping is applied.

## Capabilities

### New Capabilities
- `map-line-cropping`: the map Select Area tool — when it can be turned on and off and what happens to its crop area
  as files open and close.

### Modified Capabilities

## Impact

- `TraceCutter` (map layer providing the Select Area, Crop, Split, Merge and Undo buttons): reacts to file close.
- No changes to file data, undo, persisted formats or MCP tools.
