# map-line-cropping Specification

## Purpose

Lets users draw a crop area on the map with the Select Area tool and crop survey lines of the open files to it, and
defines how the tool and its crop area behave as files are opened and closed.

## Requirements

### Requirement: Select Area turns off when the last file is closed

When the last open file is closed while the Select Area tool is on, the tool SHALL turn off: its button SHALL be shown
released and disabled, and the crop area SHALL no longer be drawn on the map. This SHALL hold for closing a single
file and for Close All.

#### Scenario: Closing the only open file
- **WHEN** one file is open, the Select Area tool is on, and the user closes that file
- **THEN** the Select Area button is released and disabled, and the crop area is no longer drawn on the map

#### Scenario: Close All
- **WHEN** several files are open, the Select Area tool is on, and the user chooses Close All
- **THEN** after all files are closed the Select Area button is released and disabled, and no crop area is drawn

#### Scenario: Opening a file afterwards
- **WHEN** the tool was turned off by closing the last file and the user opens a file
- **THEN** the Select Area button is released and enabled, and turning it on draws a new crop area fitted to the
  open files

### Requirement: Select Area stays on while files remain open

Closing a file while other files remain open SHALL NOT turn the Select Area tool off or change its crop area.

#### Scenario: Closing one of several files
- **WHEN** two files are open, the Select Area tool is on, and the user closes one of them
- **THEN** the Select Area button stays pressed and enabled, and the crop area stays where it was
