## Context

`TraceFile.getSampleInterval()` returns an `int` with no stated unit. `GprFile` returns the SEG-Y binary header field
(bytes 16–18), which GPR writers fill with picoseconds although the SEG-Y standard defines microseconds. Every consumer
divides by 1000 to get nanoseconds:

- `LeftRulerController.TimeConverter` — sample ↔ ns for the GPR time ruler (`VerticalRulerDrawer`, FX thread).
- `GprFile.getSamplesToCmGrn/Air` — depth scales in centimeters.
- `TraceProfileService` — `TraceProfile.setSampleIntervalNanos`, used by `GridService.getCellHeight` for projection
  depth (background, projection executor). Its comment says "microseconds", which the code contradicts.
- `ProjectionController.getPulseDelaySamples` — trace header delay (bytes 108–110) divided by the interval, so the delay
  is also read as picoseconds; DZT traces have no header and get 0.
- `GetGprInfo` — reports the raw value to MCP agents.

`DztFile` returns `(int) rhf_range`, the whole time window in ns, which is the bug in #1043. `DztReader` keeps
`rh_nsamp - 1` samples per trace (the first word of a scan is a trace index), while `DztFile.getSamplesToCm*` already
divide `rhf_depth` by `rh_nsamp`. Both DZT depth scales return that same ground value: `rhf_depth` is the depth range
GSSI computes with the header dielectric `rhf_epsr`, so the air scale is √εr times too small. `TraceFile.getSamplesPerMeter`
is `100 / getSamplesToCmAir()`, and `HorizontalProfile.buildSurface` multiplies altitude by it to place the ground
surface line, which therefore lands √εr times too deep for DZT.

## Goals / Non-Goals

**Goals:**
- One unit and type for sample time across `TraceFile` and its consumers, named after the unit.

**Non-Goals:**
- Widening other `int` time values (trace header delay, SEG-Y header fields) or the ruler's tick model.

## Decisions

**1. `double getSampleIntervalNanos()` replaces `int getSampleInterval()` on `TraceFile`.** Every consumer works in ns,
so the conversion from picoseconds happens once in `GprFile`, at the format boundary. A `double` keeps the fraction
that DZT needs (7.8 ps per sample for 8 ns over 1024 samples would truncate by 10%). The name matches the existing
`TraceProfile.getSampleIntervalNanos`, and the new signature makes the compiler flag every old call site, so none keeps
the picosecond meaning.
Alternative: keep `int` picoseconds and round in `DztFile`. Rejected: 2–3% error on 10 ns windows still shows a wrong
ns scale on high-frequency antennas.
Alternative: `double` picoseconds. Rejected: every consumer would keep its own `/ 1000`, and the unit would stay
implicit in the name.

**2. DZT sample time is `rhf_range / rh_nsamp` of the selected channel.** This matches `getSamplesToCm*`, which divide
the header depth by `rh_nsamp`, and the usual GSSI reading. It returns 0 when `rhf_range <= 0` or `rh_nsamp <= 0`.
Selecting another channel changes the result without extra state, since `selectedChannel()` is read on each call.

**3. Consumers guard a zero sample time.**
- `TimeConverter.convert(s, f)` returns `ceil(dt * s)` and `floor(dt * f)`, the same rounding as now without
  `/ 1000`, with a 1e-9 ns tolerance so double products such as `0.1 * 30 = 3.0000000000000004` do not move a SEG-Y
  tick by a whole ns. `back(ns)` returns `round(ns / dt)`, and 0 when `dt <= 0` (today it divides by an int that can
  be 0).
- `ProjectionController.getPulseDelaySamples` converts the delay to ns explicitly (`delay / 1000.0`), divides by `dt`
  and rounds; it keeps returning 0 when `dt <= 0` or there is no delay. For SEG-Y the result equals today's integer
  division except for rounding instead of truncation.
- `TraceProfileService` passes the value through unchanged and drops `1e-3` and the wrong comment.
- `TraceFile.getSamplesPerMeter` returns 0 when the air scale is not positive, instead of infinity, which
  `HorizontalProfile.buildSurface` would cast to an overflowing sample index.

**4. MCP key `sampleIntervalNanos` replaces `sampleInterval`.** The value's unit changes for SEG-Y and its meaning changes
for DZT, so a new key prevents agents from reading a new number under an old meaning. Every other physical quantity in
MCP results already carries its unit in the key (`durationSeconds`, `sampleRateHz`, `cmPerSampleInAir`,
`samplesPerMeter`); `sampleInterval` was the only exception. The suffix matches `getSampleIntervalNanos()`. The tool
description states "sample interval in nanoseconds".
Alternative: keep `sampleInterval` with the value in nanoseconds. Rejected: clients reading it get a SEG-Y value 1000
times smaller without any error.
Alternative: keep `sampleInterval` in picoseconds for compatibility. Rejected: it leaves the only unitless physical
result key in place.

**5. Depth scales are concrete methods on `TraceFile`.** `getSamplesToCmAir/Grn` move from `GprFile` into
`TraceFile` as `SPEED_SM_NS_VACUUM` / `SPEED_SM_NS_SOIL` times `getSampleIntervalNanos() / 2`, and the overrides in
`GprFile` and `DztFile` are removed. Both formats then share one model, built on the sample time this change fixes,
and the speed constants become private to `TraceFile`.
Alternative: DZT ground scale from the header dielectric (`SPEED_SM_NS_VACUUM / sqrt(rhf_epsr)`). Rejected: SEG-Y has
no such field, so the two formats would disagree; the only consumer of the ground scale is MCP `cmPerSampleInGround`,
and the projection takes its permittivity from the user.
Alternative: keep `rhf_depth / rh_nsamp` for the ground scale. Rejected: same model split, and `rhf_depth` is 0 in some
headers.

**6. No data, undo or event handling.** The value is read from headers on demand; nothing is mutated, so no snapshot,
`setUnsaved` or `tracesChanged()` is involved and no event is published. The ruler picks up the change on its next
repaint; the projection on its next `updateTraceProfile`.

## Risks / Trade-offs

- [SEG-Y files whose header holds true microseconds] They were already scaled as picoseconds and stay so. → Accepted:
  unchanged behavior, outside this fix.
- [MCP clients reading `sampleInterval`] The key disappears. → Accepted: the tool is described for agents at call time,
  and the old value was wrong for DZT.
- [DZT files recorded with εr other than 9] Their MCP `cmPerSampleInGround` changes from the header's dielectric to the
  shared soil speed. → Accepted: one model for all formats; no view uses the ground scale.
- [Off-by-one sample in DZT times] Displayed sample `i` is scan slot `i + 1`, so times are early by one sample
  (about 0.1 ns on a 50 ns window), below the ruler's 1 ns resolution. → Accepted, listed as a non-goal.
