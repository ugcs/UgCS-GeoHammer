## 1. Map colors in one place

- [x] 1.1 Add `com.ugcs.geohammer.map.MapColors` with constants for the selection color, the track, quality issues
  (altitude, line distance, data, data ring), ruler and trace cutter, using the palette from design.md; verify it
  compiles
- [x] 1.2 Use the `MapColors` constants in `MapRuler` and `TraceCutter` (white cutter edge, selection color for
  selected points) and remove the unused `FoundTracesLayer` point color; verify with `grep` that no
  `Color.<NAME>` or `new Color(` literal is left in `MapRuler` and `TraceCutter`

## 2. Issue type instead of issue color

- [x] 2.1 Add `QualityIssue.Type { ALTITUDE, LINE_DISTANCE, DATA }`, store the type instead of the color in
  `QualityIssue`, `PointQualityIssue` and `PolygonQualityIssue`; verify `getColor()` no longer exists
- [x] 2.2 Pass the type from `AltitudeCheck`, `LineDistanceCheck` and `DataCheck`, delete `QualityColors`; verify
  `mvn compile` succeeds and no reference to `QualityColors` remains

## 3. Track rendering

- [x] 3.1 Use `MapColors` track colors in `GpsTrack.drawTraceLines` (other files translucent); verify visually that the
  selected file is cyan, the selected line magenta and other files a fainter cyan
- [x] 3.2 Draw each line in `GpsTrack.renderTraceLines` as one `Path2D` instead of per-segment `drawLine`; verify that a
  translucent other-file track shows no darker dots at segment joints
- [x] 3.3 Outline every track line in `GpsTrack.renderTraceLines`: draw the path first in the darkened line color at 38%
  opacity and 2 px wider, then the line, both with round caps and joins; verify visually that all track kinds have a
  darker edge over light ground

## 4. Quality issue rendering

- [x] 4.1 Move `qualityLayer` after `gpsTrackMap` and before `FoundTracesLayer` in `MapView`; verify issues are drawn
  over the track and flags over issues
- [x] 4.2 In `QualityLayer`, resolve colors from the issue type via `MapColors`, draw in passes (gaps, altitude, data),
  fill polygons with `WIND_EVEN_ODD` and stroke them with a 2 px same-color stroke, raise the point minimum radius to
  3 px and draw the data ring in the gap color, 2 px around the marker; verify visually that data markers are drawn
  over altitude stripes, gap holes stay uncovered and stripes stay visible when zoomed out
- [x] 4.3 Draw every layer in `MapView.draw` on its own `Graphics2D` copy and dispose it, and dispose the frame
  graphics; verify that flag outlines, area selection edges and ruler points keep their thin strokes while quality
  issues are shown

## 5. Docs

- [x] 5.1 Describe `MapColors` in `spec/overview.md` (Packages, `map`) as the place for layer drawing colors, with view
  colors kept in `MapView`; verify the line no longer claims that all map colors live there

## 6. Verify

- [x] 6.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings
- [x] 6.2 If the `code-review` skill is available, invoke it (not the `code-review:code-review` plugin) on the
  uncommitted changes with the change's proposal and specs as the completeness reference, fix critical and major
  issues, and report the scores and any issues left open
