## Context

`CsvFile.open` runs on the shared executor (from `Loader`) and calls `MissingValues.fillGeoDataValues` after the line
structure is final and before `setUnsaved(false)`. The fill selects columns in `getColumnsToFill(schema)`: every
displayed column except the Line semantic. It then walks the lines and interpolates each selected column that has
empty cells in that line.

The parser already keeps the number type of each cell: `Numbers.parseNumber` returns `Integer` or `Long` for tokens
without a decimal separator or exponent, and `Double` for everything else (`10.0`, `10.5`, `1e3`, and integers too long
for a `long`). The `ColumnSchema` has no number type, and nothing else uses one.

## Goals / Non-Goals

**Goals:**
- Decide float vs integer once per column from the values already loaded, with no schema or parser changes.

**Non-Goals:**
- Storing the detected number type on `Column` for other features.

## Decisions

**1. A column is floating point when any of its cells holds a `Double`.** The check reads `GeoData.getValue` and tests
`instanceof Double`; null and String cells are skipped. This matches the spec rule "written with a decimal separator or
an exponent" exactly, because that is what the parser turns into a `Double`.
Alternative: test whether values are whole (`v == Math.rint(v)`). Rejected: `10.0` would count as integer, but the
decision is that decimal notation marks the column as floating point.
Alternative: record the type in the parser (`FileColumn`, like `hasNumbers`) and carry it on `Column`. Rejected: it
widens `Column` for a single use, and `Loader.loadFrom` and other paths would need to keep it in sync.

**2. The check covers the whole file, before the per-line walk.** `getColumnsToFill` takes the `List<GeoData>` and
keeps a displayed non-Line column only when it has at least one empty cell and at least one `Double` cell, both over
all rows. The per-line walk is unchanged and still skips lines without gaps in a column. A per-line check would fill
the same column in some lines and not in others.
Scanning stops early for a column once both conditions are met; a column without gaps is never scanned for a `Double`.
The cost is at most one pass over rows times displayed columns per load, the same order as the existing gap check.

**3. No undo, unsaved or event handling.** As before, the fill is part of building the initial state inside `open`:
no snapshot, `setUnsaved(false)` stays after it, and no chart or cache sees the file before `open` returns.
Reload from disk (`Loader.loadFrom`) calls `open` and gets the same rule.

## Risks / Trade-offs

- [A float column whose values are all whole is saved as integers] `Text.formatNumber` writes `10.0` as `10`, so after
  a save and reopen such a column counts as integer. Its gaps were already filled and saved at the first open, so only
  lines that were entirely empty in that column remain empty, which they are under both rules. → Accepted.
- [Files saved by earlier versions] An integer column filled with fractional values by an earlier version now contains
  `Double` cells, so it counts as floating point. → Accepted: the file data is what it is; nothing is rewritten.
- [Integers longer than a `long`] They parse as `Double` and make the column floating point. → Accepted: not a realistic
  sensor value.
