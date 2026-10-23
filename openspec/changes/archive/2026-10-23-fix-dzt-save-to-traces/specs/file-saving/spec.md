## ADDED Requirements

### Requirement: Save to on a GPR file writes the kept traces

"Save to.." on a GPR file, to a single file or to separate lines, SHALL write exactly the traces shown in the chart,
in chart order and numbered from zero, with every channel limited to the file's sample range. Positions saved
alongside the traces SHALL belong to the same traces.

#### Scenario: First line removed, single file
- **WHEN** the user removes the first line of a DZT file and saves it with "Save to.." → "Single file"
- **THEN** the saved file contains only the remaining traces, in chart order, numbered from zero
- **AND** each saved trace has the GPS position of the same trace in the source file

#### Scenario: Separate lines after a crop
- **WHEN** the user removes the first line of a DZT file and saves it with "Save to.." → "Separate lines"
- **THEN** each line file contains only that line's traces, numbered from zero, with their own GPS positions

#### Scenario: Multi-channel sample crop
- **WHEN** the user crops the sample range of a multi-channel DZT file and saves it with "Save to.."
- **THEN** every channel in the saved file contains only the cropped sample range
