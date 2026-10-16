## Why

A script gets a copy of the processed file as it is on disk. The bundled CSV scripts read it as a plain table, with
the header on the first line. Some formats have other lines first: a MagDrone ASC RTR file starts with
`Raw track data`, MagDrone ASC and RTKLib files may start with `%` comments. The scripts then take such a line as the
header and fail with a misleading error: "Export as Geosoft (Oasis montaj) database" on an RTR file reported
"no numeric columns to export". Issue #989.

Converting files to a common format in GeoHammer, or checking their template before a run, was rejected: a script
must see the file as it is on disk, and reading it is the script's job.

## What Changes

- The bundled CSV scripts read their input through one shared routine that checks the file before reading it.
- When the file does not start with its header line, the script stops with an error that names the file and the
  reason: comment lines before the header, a line before the header, or no header line.
- Files that start with their header line are read as before.
- The Geosoft export detects the separator of the file instead of assuming a comma, so it reads tab- and
  semicolon-separated files.

## Non-goals

- Reading files with lines before the header: the script still rejects them; supporting them is a separate change.
- Checks in GeoHammer before a script starts: no Java changes.
- User scripts: they keep reading their input as they do; they can use the shared routine.
- Fixed-width files beyond the error: RTKLib files are rejected because their header is a comment line.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `script-execution`: adds how bundled CSV scripts report an input file that does not start with its header line.

## Impact

- `scripts/script_utils.py`: new routine that checks the first lines of the input and reads it as a table.
- Scripts switched to it: `export_geosoft_database.py`, `data_decimation.py`, `auto_time_lag_correction.py`,
  `uxo_anomaly_marker.py`, `MagNIMBUS_data_correction.py`, `MagNIMBUS_gradient_calculation.py`,
  `MagNIMBUS_gradiometer_distance_depth_weight_estimation.py`,
  `MagNIMBUS_single_sensor_distance_depth_weight_estimation.py`.
- Reply on #989: RTR files are not supported by the bundled CSV scripts yet.
