## Purpose

Lets users temporarily collapse an opened file — reduce its chart to a header and remove it from the map and from map
line cropping — without closing it, and keeps the charts of different file types consistent in how they show the file
name, flags and selected traces.

## ADDED Requirements

### Requirement: Chart header

Every opened file's chart, GPR and sensor alike, SHALL show a header with a close button, the file name and, at the
right edge, an eye toggle. The file name SHALL be prefixed with `*` while the file has unsaved changes. Clicking the
header outside its buttons SHALL select the chart, as clicking the chart body does.

#### Scenario: Header on a GPR chart
- **WHEN** the user opens a GPR file
- **THEN** its chart shows a header with the close button, the file name and the eye toggle above the radargram, and
  the file name is no longer drawn inside the radargram area

#### Scenario: Header on a sensor chart
- **WHEN** the user opens a CSV, sonar or NMEA file
- **THEN** its chart shows the same header with the close button, the file name and the eye toggle

#### Scenario: Unsaved marker
- **WHEN** the user modifies a file without saving
- **THEN** the header shows the file name prefixed with `*`

### Requirement: Close from the header

The header close button SHALL close the file. If the file has unsaved changes, the user SHALL be asked to confirm
first, and cancelling SHALL keep the file open.

#### Scenario: Close a saved file
- **WHEN** the user clicks the close button of a file without unsaved changes
- **THEN** the file is closed and its chart and map track are removed

#### Scenario: Cancel closing an unsaved file
- **WHEN** the user clicks the close button of a file with unsaved changes and cancels the confirmation
- **THEN** the file stays open and unchanged

### Requirement: Collapse and expand a chart

The eye toggle and a double click on the header outside its buttons SHALL both switch the collapsed state of the file.
Collapsing SHALL hide the chart's plot area, leaving only its header; expanding SHALL restore the plot area with the
same zoom and position as before collapsing. The eye SHALL show an open eye while the chart is expanded and a crossed
eye while it is collapsed. While the chart is collapsed, its profile scroll SHALL stay shown but disabled (dimmed and
not draggable); expanding SHALL enable it again. Collapsing SHALL NOT modify the file or mark it as unsaved.

#### Scenario: Collapse a chart
- **WHEN** the user clicks the eye toggle of an expanded chart
- **THEN** only the chart header remains in the profile area, the charts below move up and the eye shows a crossed eye

#### Scenario: Expand a chart
- **WHEN** the user clicks the eye toggle of a collapsed chart
- **THEN** the plot area is shown again with the zoom and position it had before collapsing and the eye shows an open
  eye

#### Scenario: Toggle with a double click on the header
- **WHEN** the user double-clicks the header outside its buttons
- **THEN** the chart is collapsed if it was expanded or expanded if it was collapsed, and its file becomes the current
  file (the first click selects it, as a single click on the header does)

#### Scenario: GPR chart above follows
- **WHEN** a chart below a GPR chart is collapsed and then expanded
- **THEN** the GPR chart returns to its previous size without overlapping the chart below

#### Scenario: Profile scroll of a collapsed chart
- **WHEN** the user collapses the chart of the current file
- **THEN** its profile scroll stays in place, is shown dimmed and cannot be dragged until the chart is expanded

#### Scenario: Collapse does not change the file
- **WHEN** the user collapses the chart of a saved file
- **THEN** the file is still shown as saved

### Requirement: Collapsed files on the map

The map SHALL NOT draw the track, flags, grid or GPR amplitude (radar) overlay of a collapsed file, and clicking the
map SHALL NOT select a trace of a collapsed file. Quality-check issues SHALL remain on the map regardless of collapsed
files. Expanding the file's chart SHALL show its track, flags, grid and radar overlay again.

#### Scenario: Track disappears when collapsed
- **WHEN** the user collapses a file's chart
- **THEN** the file's track, flags, grid and radar overlay are no longer drawn on the map

#### Scenario: Track reappears when expanded
- **WHEN** the user expands a collapsed file's chart
- **THEN** the file's track, flags, grid and radar overlay are drawn on the map again

#### Scenario: Map click ignores collapsed files
- **WHEN** the user clicks the map next to a collapsed file's track, and an expanded file's track is farther away
- **THEN** the nearest trace of the expanded file is selected, not a trace of the collapsed file

#### Scenario: Quality issues stay visible
- **WHEN** quality issues are shown on the map and the user collapses a file they were computed for
- **THEN** the quality issues remain on the map

### Requirement: Line cropping skips collapsed files

Map line cropping SHALL apply only to expanded files; collapsed files SHALL keep all their lines unchanged. The initial
crop area SHALL be fitted to the tracks of expanded files only.

#### Scenario: Crop leaves collapsed files untouched
- **WHEN** the user applies a map crop whose area covers tracks of both an expanded and a collapsed file
- **THEN** the expanded file's lines are cropped and the collapsed file's lines are unchanged

#### Scenario: Initial crop area fits expanded files
- **WHEN** the user enters crop mode while some files are collapsed
- **THEN** the initial crop area is placed around the tracks of the expanded files

#### Scenario: Undo after crop
- **WHEN** the user undoes a crop applied while a file was collapsed
- **THEN** the cropped expanded files are restored and the collapsed file is still unchanged

### Requirement: Selection is independent of collapsing

Collapsing or expanding a file with the eye toggle SHALL NOT change the current file or the selected trace. A
collapsed file SHALL remain selectable by clicking its header, and tools SHALL work on it as the current file.

#### Scenario: Collapse the current file
- **WHEN** the user collapses the chart of the current file
- **THEN** it stays the current file and the tool panel keeps showing its tools

#### Scenario: Collapse another file
- **WHEN** the user clicks the eye toggle of a file that is not the current file
- **THEN** the current file stays unchanged

#### Scenario: Select a collapsed file
- **WHEN** the user clicks the header of a collapsed file outside its buttons
- **THEN** that file becomes the current file and its chart stays collapsed

### Requirement: Session-only collapsed state

Collapsed state SHALL last only while the file is open. Reopening a file or restarting the application SHALL show its
chart expanded.

#### Scenario: Reopen a collapsed file
- **WHEN** the user closes a collapsed file and opens it again
- **THEN** its chart is expanded and its track is shown on the map

### Requirement: Consistent flag and selection markers

GPR and sensor charts SHALL draw flags and selected-trace markers the same way. On both chart types the bottom of the
marker icon and the start of its vertical marker line SHALL be at the same distance below the top of the plot area. A
flag SHALL be drawn as a pole with a notched cloth in the flag's color, with a solid marker line in the flag's color;
a selected flag SHALL have its cloth filled with the selection color and its outline, pole and marker line drawn
thicker. A selected trace SHALL be marked with the selection pin and a solid line in the color of the selection type
(user or automatic).

#### Scenario: Flag on both chart types
- **WHEN** a GPR file and a sensor file with flags are open
- **THEN** the flags have the same shape, colors and vertical position on both charts

#### Scenario: Selected flag
- **WHEN** the user selects a flag
- **THEN** on either chart type its cloth is filled with the selection color and its outline and marker line become
  thicker; selecting another flag returns the previous one to its normal look

#### Scenario: Selected trace
- **WHEN** the user selects a trace on a GPR or a sensor chart
- **THEN** the selection pin is shown at the same vertical position on both chart types, with a solid line in the
  user-selection color (or the automatic-selection color for automatic selections)

### Requirement: Compact GPR chart layout

The GPR chart SHALL use only a small margin above the radargram, so flags and selection markers are drawn over the
top of the radargram as on sensor charts. The depth-axis unit toggle SHALL be drawn vertically (rotated 90° counter
clockwise), centered on the radargram height to the right of the axis labels, and labelled `samples` or `ns`. The
distance-axis unit toggle SHALL stay at the bottom of the chart. Clicking either toggle SHALL switch its unit as before.

#### Scenario: Depth-axis unit toggle
- **WHEN** the user opens a GPR file
- **THEN** the depth-axis unit toggle is shown vertically next to the axis, centered on the radargram, and clicking it
  switches between samples and nanoseconds

#### Scenario: Distance-axis unit toggle
- **WHEN** the user opens a GPR file
- **THEN** the distance-axis unit toggle is shown below the distance axis inside the chart, and clicking it switches
  the distance unit
