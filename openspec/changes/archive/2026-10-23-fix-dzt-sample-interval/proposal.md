## Why

The time ruler of a GSSI DZT file shows a wrong ns scale: a 50 ns, 512-sample file is labeled 0–25 ns (#1043). The DZT
sample interval is reported as the whole recorded time window instead of the time of one sample, and the same value
makes the 3D projection of DZT files about half as deep as it should be. Integer picoseconds also lose up to 10% on the
short windows of high-frequency antennas.

## What Changes

- The time of one sample is derived from each file's recorded time window: SEG-Y from its sample interval header field,
  DZT from the range divided by the samples per scan of the selected channel.
- The time ruler spans the file's recorded time window, with fractional sample times kept until tick labels are drawn.
- The 3D projection uses the corrected sample time for depth, so DZT profiles get their true depth.
- Depth scales in centimeters per sample, in air and in ground, are computed from the sample time the same way for
  all GPR formats. DZT files no longer report the ground scale for air, which made samples per meter and the
  altitude-based ground surface line √εr times too deep. The DZT ground scale uses the same soil speed as SEG-Y
  instead of the header's dielectric.
- **BREAKING** (MCP): `get_gpr_info` reports the sample interval in nanoseconds under the key `sampleIntervalNanos`,
  replacing `sampleInterval` (picoseconds for SEG-Y, the time window in ns for DZT).

## Non-goals

- Showing the DZT time-zero offset (`rhf_position`) on the ruler; the ruler still starts at 0 ns at the top sample.
- Shifting DZT sample times by the skipped first word of each scan.
- Ruler ticks below 1 ns or changes to tick spacing and labels.
- Using the DZT header dielectric (`rhf_epsr`) or depth range (`rhf_depth`) for depth scales.
- Changes to file reading or saving.

## Capabilities

### New Capabilities
- `gpr-time-scale`: time of a GPR sample derived from the file, shown on the time ruler, used for projection depth and
  depth scales, and reported to MCP agents.

### Modified Capabilities

## Impact

- `TraceFile.getSampleInterval()` (int, picoseconds) becomes `getSampleIntervalNanos()` (double, nanoseconds), with
  implementations in `GprFile` and `DztFile`.
- Consumers: `LeftRulerController.TimeConverter`, `TraceProfileService`, `ProjectionController`, `GetGprInfo` (MCP).
- Depth scales (`getSamplesToCmAir/Grn`) move from `GprFile` and `DztFile` into `TraceFile`; `getSamplesPerMeter`
  feeds the ground surface line (`HorizontalProfile`).
- No changes to persisted formats, undo, events or file data.
