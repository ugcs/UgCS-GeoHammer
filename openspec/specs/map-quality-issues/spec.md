# map-quality-issues Specification

## Purpose

Defines how quality control issues, the survey track and selected map elements are shown on the map, so that issues
stay visible and distinguishable from the track, from selections, from each other and from the map background at any
zoom.

## Requirements

### Requirement: Quality issues are drawn above the survey track

Quality issues SHALL be drawn above the survey track of every file, including the selected file and its selected
line. The track SHALL NOT cover any part of an issue.

#### Scenario: Altitude issue on the selected file
- **WHEN** the quality check finds an Altitude AGL issue on a line of the selected file
- **THEN** the issue stripe is drawn over the track line it follows, and the track does not cover it

#### Scenario: Data issue on the selected line
- **WHEN** a data issue lies on the selected line
- **THEN** the data marker is drawn over the selected line

### Requirement: Quality issues have a minimum on-screen size

Quality issues SHALL keep their size in meters while that size is visible, and SHALL NOT shrink below a minimum
on-screen size when zoomed out: altitude stripes and gap areas SHALL extend about 1 px beyond their geometry on every
side, and data markers SHALL have a radius of at least 3 px.

#### Scenario: Zoomed out over a large survey
- **WHEN** the map is zoomed out so far that an altitude stripe is narrower than a pixel in meters
- **THEN** the stripe is still drawn about 2 px wide along its line

#### Scenario: Zoomed in
- **WHEN** the map is zoomed in so that an altitude stripe is several pixels wide in meters
- **THEN** the stripe width follows its size in meters, with only a 1 px margin on each side

#### Scenario: Data marker zoomed out
- **WHEN** the map is zoomed out so that a data issue radius is smaller than 3 px in meters
- **THEN** the data marker is drawn with a 3 px radius

### Requirement: Fixed, distinguishable map colors

Quality issues, the survey track and selected map elements SHALL use fixed colors that the user cannot change.
Issues SHALL be drawn opaque in warm colors, so their colors do not change with the background: Altitude AGL issues
orange-red, line distance gaps yellow, data issues a round orange-red marker with a yellow ring. The survey track SHALL
use cool colors that no issue uses: the selected file cyan, other files a fainter cyan and thinner. Selected elements
SHALL share one magenta selection color: the selected line, drawn wider than the track, the selected ruler point and
the selected point of the area selection. Area selection edges SHALL be white, so they are not confused with gaps.

#### Scenario: Altitude issue next to the track
- **WHEN** an Altitude AGL issue is shown on a line of the selected file
- **THEN** the orange-red issue is clearly different from the cyan track

#### Scenario: Selected line
- **WHEN** the user selects a line of a file that has several lines
- **THEN** the selected line is drawn magenta and wider, clearly different from the other cyan lines of the file

#### Scenario: Gap next to the selected line
- **WHEN** a line distance gap is shown next to the selected line
- **THEN** the yellow gap is clearly different from the magenta selected line

#### Scenario: Data issue on an altitude issue
- **WHEN** a data issue lies on an Altitude AGL issue stripe
- **THEN** the yellow ring outlines the data marker on the orange-red stripe

#### Scenario: Data issue inside a gap
- **WHEN** a data issue lies inside a line distance gap area
- **THEN** the orange-red marker is visible on the yellow gap

#### Scenario: Area selection over gaps
- **WHEN** an area selection is drawn while line distance gaps are shown
- **THEN** its white edges are clearly different from the yellow gaps, and its selected point is magenta

### Requirement: Survey track lines are outlined

Every survey track line SHALL be drawn with a thin, darker, semi-transparent outline of its own color, so that the
line stays readable over both light and dark backgrounds.

#### Scenario: Track over light ground
- **WHEN** the track is shown over light imagery or a light grid region
- **THEN** each track line has a darker edge that separates it from the background

#### Scenario: All track kinds
- **WHEN** the map shows the selected file with a selected line and another open file
- **THEN** the lines of the selected file, the selected line and the lines of the other file are all outlined

### Requirement: Holes in gap areas are shown

When a line distance gap area surrounds a covered region, the covered region SHALL be shown as a hole: the map and
grid below it SHALL stay visible.

#### Scenario: Covered island inside a gap
- **WHEN** the gap area found by the line distance check has a hole where lines cover the survey
- **THEN** the gap is filled around the hole and the map inside the hole is not covered
