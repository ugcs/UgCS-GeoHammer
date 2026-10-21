# map-ruler Specification

## Purpose

Defines the measurement bar shown under the map while the ruler is on: the distance and heading it displays and how
its layout stays stable as the measured values change.

## Requirements

### Requirement: Distance field has a fixed width

The distance value SHALL be shown in a field wide enough for `0000.00`. While the shown distance fits that width, a
change of the value SHALL NOT move the distance unit selector or the heading. A longer value SHALL be shown in full,
widening the field.

#### Scenario: Value gains digits
- **WHEN** the ruler is dragged so the distance changes from 5.00 to 1234.56 in the selected unit
- **THEN** the distance unit selector and the heading stay in the same position

#### Scenario: Value exceeds the reserved width
- **WHEN** the distance is 15000.00 in the selected unit
- **THEN** the full value is shown and the unit selector and heading move right to make room

### Requirement: Distance value is right-aligned

The distance value SHALL be aligned to the right edge of its field, so the gap between the last digit and the unit
selector SHALL be the same for any value that fits the field.

#### Scenario: Short and long values
- **WHEN** the distance is shown as 1.23 and then as 1234.56
- **THEN** in both cases the last digit is the same distance from the unit selector

### Requirement: Compact distance unit selector

The distance unit selector SHALL be only as wide as needed to show the longest unit label and its drop-down arrow.

#### Scenario: Longest unit selected
- **WHEN** a two-letter unit such as km is selected
- **THEN** the label is shown in full next to the drop-down arrow, with no wide empty space in the selector
