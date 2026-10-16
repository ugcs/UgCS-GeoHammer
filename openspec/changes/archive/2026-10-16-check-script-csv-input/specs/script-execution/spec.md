# Spec Delta

## ADDED Requirements

### Requirement: Bundled CSV scripts reject input without the header on the first line

A bundled script that reads its input as a CSV table SHALL check the first lines of the file before reading it. When
the first line is empty or starts with `%` or `#`, when it has no separator while the next line has, or when its
values look like data instead of column names, the script SHALL stop with an error that names the file and the
reason, and SHALL NOT change the file.

#### Scenario: Marker line before the header
- **WHEN** the user runs "Export as Geosoft (Oasis montaj) database" on a MagDrone ASC RTR file whose first line is
  `Raw track data`
- **THEN** the script fails with an error saying that a line precedes the header, instead of "no numeric columns to
  export"

#### Scenario: Comment lines before the header
- **WHEN** the user runs data decimation on a MagDrone ASC file that starts with `%` comment lines
- **THEN** the script fails with an error saying that comment lines precede the header, and the file data is unchanged

#### Scenario: Header in a comment line
- **WHEN** the user runs data decimation on an RTKLib position file
- **THEN** the script fails with an error saying that comment lines precede the header

#### Scenario: No header line
- **WHEN** the user runs data decimation on a comma-separated file whose first line holds numbers
- **THEN** the script fails with an error saying that the file has no header line

#### Scenario: File with the header on the first line
- **WHEN** the user runs data decimation on a file whose first line is its header
- **THEN** the script reads and decimates it as before

### Requirement: Geosoft export reads the separator of the file

The Geosoft export SHALL read the file with the separator used in it, so that files separated by tabs or semicolons
are exported with all their numeric columns.

#### Scenario: Tab-separated file
- **WHEN** the user exports a tab-separated MagDrone ASC file with the header on the first line to a Geosoft database
- **THEN** the database contains a channel for every numeric column of the file
