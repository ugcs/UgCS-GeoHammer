## ADDED Requirements

### Requirement: Integer columns are not interpolated

The system SHALL interpolate a data column only when it contains floating point values: at least one of its numeric
cells, in any row of the file, is written with a decimal separator or an exponent. Numeric cells written without them
SHALL NOT change that, and text cells SHALL NOT count either way. Empty cells of a data column without floating point
values SHALL stay empty after load and SHALL be saved as empty cells.

#### Scenario: Integer column with gaps
- **WHEN** a data column has values 10, empty, empty, 20 in consecutive rows of one line
- **THEN** the empty rows stay empty

#### Scenario: Whole numbers written with a decimal separator
- **WHEN** a data column has values 10.0, empty, 20.0 in consecutive evenly timed rows of one line
- **THEN** the empty row gets 15

#### Scenario: Integer and decimal values in one column
- **WHEN** a data column has values 10, empty, 11.5, empty, 12 in consecutive evenly timed rows of one line
- **THEN** the empty rows get 10.75 and 11.75

#### Scenario: Decimal values in another line
- **WHEN** a data column has only values written without a decimal separator in line 1, including 10, empty, 20 in
  evenly timed rows, and a value 3.5 in line 2
- **THEN** the empty row in line 1 gets 15

#### Scenario: Save keeps integer gaps
- **WHEN** a file with empty cells in an integer data column is opened and saved
- **THEN** the saved file has those cells empty

## MODIFIED Requirements

### Requirement: Missing data values are interpolated on load

When a CSV or fixed-width data file is opened, the system SHALL fill empty cells of its data columns that contain
floating point values by linear interpolation between the nearest known values of the same column. Data columns are
the columns shown as series: those declared as data values in the template or containing numeric values, excluding
position, date, time, timestamp, trace number, mark and line columns. Columns that are not data columns SHALL NOT be
changed by this fill.

#### Scenario: Gap between known values
- **WHEN** a data column has values 10.0 and 20.0 in two rows of the same line with two empty rows between them, and
  the rows are evenly spaced in time
- **THEN** the empty rows get 13.333… and 16.666…

#### Scenario: Non-data columns are left as they are
- **WHEN** a file has empty cells in its mark column
- **THEN** those cells stay empty after load

#### Scenario: File without gaps
- **WHEN** a file has no empty cells in its data columns
- **THEN** all values are loaded unchanged

### Requirement: Interpolation follows time when available

The system SHALL position rows on the row timestamp when interpolating, so filled values are proportional to the
elapsed time between known values. Rows without a timestamp SHALL be positioned between their neighbors' timestamps.
Rows sharing a timestamp SHALL be spread evenly by their order between that timestamp and the next distinct one.
When no row of a line has a timestamp, or all rows of a line share one timestamp, the system SHALL position rows by
their order in the file.

#### Scenario: Uneven time spacing
- **WHEN** a data column has 10.0 at time 0 s and 20.0 at time 10 s, with one empty row at time 2 s
- **THEN** the empty row gets 12

#### Scenario: File without times
- **WHEN** a line has no timestamps, and a data column has 10.0 and 20.0 with one empty row between them
- **THEN** the empty row gets 15

#### Scenario: Rows sharing a timestamp
- **WHEN** a data column has 10.0 at time 0 s, an empty row also at time 0 s, and 20.0 at time 1 s
- **THEN** the empty row gets 15

#### Scenario: All rows share one timestamp
- **WHEN** every row of a line has the same timestamp, and a data column has 10.0 and 20.0 with one empty row between
  them
- **THEN** the empty row gets 15

### Requirement: Interpolation stays within a survey line

The system SHALL NOT interpolate between values of different survey lines. Empty cells before the first known value of
a line SHALL take that first value, and empty cells after the last known value of a line SHALL take that last value.
A column with no known value in a line SHALL stay empty in that line.

#### Scenario: Line head and tail
- **WHEN** a line's first two rows and last row are empty in a data column, and its known values are 5.0 then 7.0
- **THEN** the first two rows get 5 and the last row gets 7

#### Scenario: Gap at a line boundary
- **WHEN** line 1 ends with 100.0 followed by empty rows, and line 2 starts with empty rows followed by 200.0
- **THEN** the empty rows of line 1 get 100 and the empty rows of line 2 get 200

#### Scenario: No known values in a line
- **WHEN** a data column with floating point values is empty in every row of a line
- **THEN** that column stays empty in every row of that line

### Requirement: Text cells are preserved

A cell holding non-numeric text SHALL keep its text, SHALL NOT be replaced by an interpolated value and SHALL NOT be
used as a known value for interpolation.

#### Scenario: Text between gaps
- **WHEN** a data column holds 5.0, empty, "N/A", empty, 7.0 in consecutive evenly timed rows of one line
- **THEN** the empty rows get 5.5 and 6.5, and the "N/A" cell keeps its text
