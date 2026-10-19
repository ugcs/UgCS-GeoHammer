## Why

On open, empty cells of every data column are filled by linear interpolation. For integer columns (counters, codes,
quality flags, satellite counts) this invents fractional values such as 13.333 that the source never could hold, and
saving writes them into the file (#1001).

## What Changes

- Empty cells of a data column are interpolated on open only when the column contains floating point values.
- A column contains floating point values when at least one of its numeric cells, anywhere in the file, is written
  with a decimal separator or an exponent (`10.0`, `10.5`, `1e3`). Integer-looking cells in such a column do not
  change that.
- Empty cells of integer data columns stay empty after open and are saved as empty cells.

## Non-goals

- Filling gaps of integer columns another way (repeating the previous value, rounding interpolated values).
- A template setting to declare a column's number type.
- Changes to latitude and longitude filling.
- Changes to how integer or floating point values are parsed, shown or written.

## Capabilities

### New Capabilities

### Modified Capabilities
- `csv-loading`: data columns are interpolated on load only when they contain floating point values; integer columns
  keep their empty cells.

## Impact

- `MissingValues` (fill of missing data values on load, called from `CsvFile`): selects only floating point data
  columns for filling.
- No changes to persisted formats, undo, events or MCP tools.
