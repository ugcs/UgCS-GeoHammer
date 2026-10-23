## 1. Sample time in nanoseconds

- [x] 1.1 Replace `int getSampleInterval()` with `double getSampleIntervalNanos()` on `TraceFile`; in `GprFile` return
  the binary header interval divided by 1000.0 and use it in `getSamplesToCmGrn/Air` instead of reading the header;
  verify by reading the code against design.md decision 1
- [x] 1.2 In `DztFile` return `rhf_range / rh_nsamp` of the selected channel, or 0 when either is not positive; verify
  by reading the code against design.md decision 2 and the "Sample time is derived from the file" scenarios

## 2. Consumers

- [x] 2.1 Update `LeftRulerController.TimeConverter`: `convert` returns `ceil(dt * s)` and `floor(dt * f)`, `back`
  returns `round(ns / dt)` or 0 when `dt <= 0`; verify the project compiles and the samples converter is untouched
- [x] 2.2 Update `TraceProfileService` to pass `getSampleIntervalNanos()` to `setSampleIntervalNanos` and remove the
  `1e-3` factor and the "microseconds" comment; verify by reading the code
- [x] 2.3 Update `ProjectionController.getPulseDelaySamples` to convert the delay from ps to ns, divide by the sample
  time and round, returning 0 when the sample time is not positive or there is no delay; verify by reading the code
  against the "SEG-Y pulse delay" and "DZT pulse delay" scenarios
- [x] 2.4 In `GetGprInfo` report `sampleIntervalNanos` instead of `sampleInterval` and say "sample interval in
  nanoseconds" in the tool description; verify no `getSampleInterval(` call remains in `src/`
- [x] 2.5 Move `getSamplesToCmAir/Grn` into `TraceFile` as concrete methods built on `getSampleIntervalNanos()`, remove
  the overrides from `GprFile` and `DztFile`, and make the speed constants private; verify by reading the code against
  design.md decision 5 and the "Depth scales follow the sample time" scenarios
- [x] 2.6 Make `TraceFile.getSamplesPerMeter` return 0 when the air scale is not positive; verify by reading the code
  against the "Zero sample time" scenario

## 3. Check in the app

- [x] 3.1 Run the app (`mvn javafx:run`), open a DZT file and switch the ruler to ns; verify the ruler spans the
  file's recorded range (e.g. about 0–50 ns for 50 ns), and that it changes accordingly after selecting another
  channel of a multi-channel file
- [x] 3.2 Open a SEG-Y GPR file; verify the ns ruler and the depth scale show the same values as before the change

Cannot be tested: DZT projection depth and the DZT ground surface line need DZT files with position or altitude data,
and none are available.

## 4. Verify

- [x] 4.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings in changed code
- [x] 4.2 Invoke the `code-review` skill on the uncommitted changes with this change's proposal and specs as the
  completeness reference; fix critical and major issues and report the scores and any issues left open
