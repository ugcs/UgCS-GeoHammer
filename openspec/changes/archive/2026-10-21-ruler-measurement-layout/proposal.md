# Proposal

## Why

The map ruler's measurement bar shifts while the ruler is dragged: the distance field is sized for `00.00`, so the unit
selector and heading move once the distance reaches 100, and the left-aligned value changes the gap before the unit as
the number of digits changes. The unit selector is also wider than its two-letter units need (#1007).

## What Changes

- The distance value field gets a fixed width that fits `0000.00`; values up to 9999.99 no longer move the unit
  selector or the heading.
- The distance value is right-aligned, so the gap between the last digit and the unit selector stays the same.
- The distance unit selector is narrower.

## Capabilities

### New Capabilities
- `map-ruler`: the map ruler's measurement bar — what it shows and how its layout behaves as values change.

### Modified Capabilities

None.

## Non-goals

- Distances above 9999.99 in the selected unit: the field grows to fit them and the bar shifts, as today.
- The heading field: its width and alignment stay as they are.
- Number formatting, units, unit conversion and the ruler's drawing on the map.
- Control heights and the bar's spacing and padding.

## Impact

- `com.ugcs.geohammer.map.RulerMeasurementView` only. No new dependencies, no data or persisted format changes.
