# gpr-time-scale Specification

## Purpose

Defines the time of one GPR sample, taken from the file's own header, and how it scales the time ruler, the depth of
projected profiles, depth scales in centimeters and the file summary reported to MCP agents.

## Requirements

### Requirement: Sample time is derived from the file

The system SHALL take the time of one sample from the opened GPR file, in nanoseconds with fractions kept. For SEG-Y
files it SHALL be the sample interval of the binary header, read as picoseconds. For DZT files it SHALL be the
recorded time range of the selected channel divided by that channel's samples per scan. A file whose header gives no
positive time or sample count SHALL have a sample time of zero.

#### Scenario: DZT file
- **WHEN** a DZT file with a range of 50 ns and 512 samples per scan is opened
- **THEN** its sample time is 0.09765625 ns

#### Scenario: SEG-Y file
- **WHEN** a SEG-Y file with a sample interval of 100 in its binary header is opened
- **THEN** its sample time is 0.1 ns

#### Scenario: DZT channel switch
- **WHEN** a two-channel DZT file has channel 1 with 50 ns over 512 samples and channel 2 with 100 ns over 512
  samples, and channel 2 is selected
- **THEN** its sample time is 0.1953125 ns

#### Scenario: Missing time in the header
- **WHEN** a DZT file with a range of 0 ns is opened
- **THEN** its sample time is zero and the file opens and draws without error

### Requirement: Time ruler spans the recorded time window

When the GPR time ruler is switched to ns, the system SHALL place each tick at the sample whose time equals the tick
value, using the file's sample time with fractions kept. A file with a sample time of zero SHALL show only the 0 ns
tick.

#### Scenario: DZT file in full view
- **WHEN** a DZT file with a range of 50 ns and 512 samples per scan is shown in full with the ruler in ns
- **THEN** the 40 ns tick is drawn at 80% of the profile height, and the bottom of the profile is at 49–50 ns

#### Scenario: Short time window
- **WHEN** a DZT file with a range of 8 ns and 1024 samples per scan is shown in full with the ruler in ns
- **THEN** the 4 ns tick is drawn at half of the profile height

#### Scenario: Samples ruler is unchanged
- **WHEN** the ruler is switched to samples
- **THEN** ticks show sample numbers as before, regardless of the sample time

### Requirement: Projection depth follows the sample time

The system SHALL convert sample positions of a projected GPR profile to depth using the file's sample time, so the
profile depth equals half the recorded time window multiplied by the wave speed in the ground. The initial sample offset
of the projection SHALL be the trace delay of the first trace, read as picoseconds, divided by the sample time and
rounded, and SHALL be zero when the file has no delay or a sample time of zero.

#### Scenario: DZT profile depth
- **WHEN** a line of a DZT file with a range of 50 ns is projected with a relative permittivity of 9
- **THEN** the projected profile is about 2.5 m deep, not about 1.3 m

#### Scenario: SEG-Y pulse delay
- **WHEN** a SEG-Y file with a sample interval of 100 ps and a first trace delay of 2000 is projected
- **THEN** the initial sample offset is 20

#### Scenario: DZT pulse delay
- **WHEN** a DZT file is projected
- **THEN** the initial sample offset is 0

### Requirement: Depth scales follow the sample time

The system SHALL compute the distance one sample covers from the file's sample time, the same way for every GPR
format: in air as 30 cm/ns times half the sample time, and in ground as 10 cm/ns times half the sample time. Samples
per meter SHALL be 100 divided by the distance in air in centimeters, and SHALL be zero when the sample time is zero.
The ground surface line derived from altitude SHALL use samples per meter.

#### Scenario: DZT depth scales
- **WHEN** a DZT file with a range of 50 ns and 512 samples per scan is opened
- **THEN** a sample covers 1.46484375 cm in air and 0.48828125 cm in ground, and there are about 68.27 samples per
  meter

#### Scenario: DZT dielectric is not used
- **WHEN** two DZT files with a range of 50 ns and 512 samples per scan differ only in the dielectric constant stored
  in their headers
- **THEN** both have the same depth scales

#### Scenario: SEG-Y depth scales
- **WHEN** a SEG-Y file with a sample interval of 100 in its binary header is opened
- **THEN** a sample covers 1.5 cm in air and 0.5 cm in ground, as before

#### Scenario: Zero sample time
- **WHEN** a file has a sample time of zero
- **THEN** its depth scales and samples per meter are zero, and its ground surface line is drawn without error

### Requirement: GPR summary reports the sample time in nanoseconds

The `get_gpr_info` MCP tool SHALL report the file's sample time in nanoseconds, with fractions kept, under the key
`sampleIntervalNanos`, and SHALL NOT report the key `sampleInterval`.

#### Scenario: DZT summary
- **WHEN** an agent calls `get_gpr_info` for a DZT file with a range of 50 ns and 512 samples per scan
- **THEN** the result has `sampleIntervalNanos` equal to 0.09765625 and no `sampleInterval` key

#### Scenario: SEG-Y summary
- **WHEN** an agent calls `get_gpr_info` for a SEG-Y file with a sample interval of 100 in its binary header
- **THEN** the result has `sampleIntervalNanos` equal to 0.1
