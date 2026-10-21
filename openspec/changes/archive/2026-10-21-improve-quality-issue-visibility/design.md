## Context

See proposal.md for the problem. Current state:

- `MapView.afterPropertiesSet` paints layers in list order: satellite, radar, grid, `QualityLayer`, `GpsTrack`,
  `FoundTracesLayer`, `TraceCutter`, `MapRuler`. Quality issues are therefore under the track. `MapView.draw` passes
  one shared `Graphics2D` to every layer, so state a layer sets (stroke, transform) leaks into the layers after it.
- `QualityLayer.draw` paints issues in list order (line distance, data, altitude), so altitude stripes also cover data
  markers. Polygons are filled only (no stroke), with `Path2D.WIND_NON_ZERO`, so holes with the same winding as the
  shell get filled. Point issues have a 1.5 px minimum radius.
- `QualityIssue` carries a `java.awt.Color` set by the checks from `QualityColors` (service package).
- `GpsTrack.drawTraceLines` uses three inline color literals and draws every simplified segment with `drawLine`.
- Other map colors are inline `Color` constants in `MapView`, `MapRuler`, `TraceCutter` and `FoundTracesLayer`.

All drawing stays where it is today (map render paths, AWT `Graphics2D`); no threads, events, settings, file data or
undo are involved.

## Goals / Non-Goals

**Goals:**
- One source for the colors map layers draw with; view colors stay in the view.
- Issue color decided by the map layer, not by the quality service.
- Drawing order and minimum sizes from the spec.

**Non-Goals:**
- Antialiasing of the track or issues (unchanged).
- Changing issue geometry produced by the checks.

## Decisions

### Palette

Colors were chosen with a palette lab that rendered the map with Java2D over satellite crops (including a real
survey), a plain map background and all 12 grid spectra, and measured CIEDE2000 distances to the backgrounds and
between elements, with and without simulated deuteranopia and protanopia (values below are normal / deutan / protan).

| Element | Constant | Color | Alpha | Width |
|---|---|---|---|---|
| Selection | `SELECTION` | `#E821F8` | opaque | |
| Track, selected file | `TRACK` | `#39E5DA` | opaque | 2 px (unchanged) |
| Track, selected line | `SELECTED_LINE` | selection | opaque | 3 px (unchanged) |
| Track, other files | `OTHER_TRACK` | track color | 75% | 1 px (unchanged) |
| Altitude AGL issue | `ALTITUDE_ISSUE` | `#F65532` | opaque | geometry + 2 px stroke |
| Line distance gap | `LINE_DISTANCE_ISSUE` | `#FCEF26` | opaque | geometry + 2 px stroke |
| Data issue | `DATA_ISSUE` | altitude color | opaque | radius max(3 px, meters) |
| Data issue ring | `DATA_ISSUE_RING` | gap color | opaque | 2 px around the marker |
| Area selection edge / point / middle point | `TRACE_CUTTER_*` | white / white / gray | opaque | |
| Area selection selected point | `TRACE_CUTTER_SELECTED_POINT` | selection | opaque | |
| Ruler line / point | `RULER_LINE`, `RULER_POINT` | green / white (unchanged) | opaque | |
| Ruler selected point | `RULER_SELECTED_POINT` | selection | opaque | |

The palette splits roles by hue family: cool cyan for the track, magenta for anything selected, warm orange-red and
yellow for errors. Cyan is the usual recommendation for trajectories over satellite imagery and is the complement of
the error colors (track/altitude 63/38/37). The magenta selected line replaces the former cyan selected line, which
was indistinguishable from a cyan track (12/10/13, now 53/23/37); a saturated magenta was chosen over a lighter one
because it keeps more distance from the track for deuteranopes. Area selection edges move from yellow to white:
yellow was identical to the gap color (CIEDE2000 4). The data marker is the inverse of the two issue colors: on an
altitude stripe it shows as its yellow ring, inside a gap as an orange-red dot.

Opaque issues keep their color on any background (translucent altitude took on the ground color over red and orange
grid areas, and overlapping stripe rectangles would show darker seams). Alternatives tested and rejected: orange and
pinkish white tracks (pinkish white was too weak over light ground in a real survey); pink altitude with crimson gaps
(light pink and cyan collapse to the same gray under protanopia, 4); translucent altitude; outlines, casings and
hatching on issues (visual noise).

### Track outline

`GpsTrack.renderTraceLines` draws every line twice with round caps and joins: first an outline in the line color
darkened three times (`Color.darker()`, about 0.34 of the brightness) at 38% opacity and 2 px wider, then the line
itself. The outline lifts the worst-case contrast of the cyan track against its backgrounds from 7 to 18. The outline
color is derived from the line color in `GpsTrack`, so selected file, selected line and other files get matching
outlines without extra constants.

### `MapColors` holds the layer palette

A final class `com.ugcs.geohammer.map.MapColors` with `public static final Color` constants for the colors that map
layers draw with: the selection color, the track, quality issues, trace cutter and ruler. Values that belong to one
palette reference each other (selected line and selected points use `SELECTION`, the other-file track derives from
`TRACK`, data marker colors reuse the issue colors). Stroke widths and sizes stay in their layers, since they are
geometry, not color.

View colors stay in the view: `MapView` itself paints the map background and the "no GPS" text, not through a layer,
so `BACKGROUND_COLOR` and the text color remain `MapView` constants. View styling and the layer palette change for
different reasons. `FoundTracesLayer` no longer sets a color before drawing: flags and selection pins set their own
colors, so its green point color was never visible and is removed.

Alternative: keep constants per layer and only change values. Rejected: the colors were chosen as one palette and must
stay consistent.

### `QualityIssue.Type` replaces the issue color

`QualityIssue` gets a nested enum `Type { ALTITUDE, LINE_DISTANCE, DATA }` and stores a `Type` instead of a `Color`
(`getType()` replaces `getColor()`). `PointQualityIssue` and `PolygonQualityIssue` take the type in their
constructors; `AltitudeCheck`, `LineDistanceCheck` and `DataCheck` pass their type. `QualityColors` is deleted.
`QualityLayer` maps the type to a `MapColors` constant with a switch expression.

Alternative: keep `Color` on the issue and point `QualityColors` at `MapColors`. Rejected: the service package would
start depending on the map package, and color is a view concern.

### Layer and issue drawing order

In `MapView`, `qualityLayer` moves after `gpsTrackMap` and before `FoundTracesLayer`, so issues are above the track and
flags stay above issues. `QualityLayer.draw` paints in passes by type: gaps, then altitude stripes, then data markers,
so markers are never covered by a stripe.

### Each layer draws on its own graphics copy

`MapView.draw` gives every layer its own `g2.create()` copy and disposes it afterwards, and disposes the base graphics
of the frame. A layer can then set strokes, transforms or hints without affecting later layers: `QualityLayer` sets a
2 px stroke that would otherwise thicken flag outlines, area selection edges and ruler points. A layer that throws
midway also cannot leave a half-applied transform behind. The copies share the frame image and cost a few state copies
per frame.

Alternative: each layer that changes state copies the graphics itself. Rejected: every new layer would have to
remember it.

### Minimum on-screen size

Polygons are filled and then stroked with a 2 px `BasicStroke` in the same opaque color, which adds about 1 px around
the geometry at any zoom without a visible outline. The checks and the issue model stay in meters. The point issue
minimum radius changes from 1.5 px to 3 px; the ring is a filled circle 2 px larger than the marker, drawn first.

Alternative: draw altitude issues as polylines with a pixel width. Rejected: it changes the issue model and the
checks for the same visible result.

### Gap holes

Polygon paths use `Path2D.WIND_EVEN_ODD`, which leaves interior rings empty whatever their orientation.

### Translucent other-file track

The other-file track is translucent. Drawing it segment by segment with `drawLine` would paint segment joints twice
and show darker dots. `GpsTrack.renderTraceLines` builds one `Path2D` per line from the simplified points and draws
it once (outline, then line); for the opaque tracks this changes nothing visible.

### Testing

The change is rendering only. A rendering test against a `BufferedImage` was dropped, since it tests UI drawing rather
than a unit; drawing order, gap holes and minimum sizes are verified visually during implementation.

## Risks / Trade-offs

- [Users are used to the red track] → The change is visible in release notes; warm colors now always mean an error,
  so the meaning of colors becomes clearer.
- [No color is visible on every grid] → With full-spectrum palettes some grid region always matches an issue hue;
  opacity, minimum sizes, the track outline and the data ring keep elements readable on the measured worst cases. The
  orange-red altitude color is weakest over the orange areas of Rainbow-type grids (CIEDE2000 7–9); `#FF2E2E` would
  raise this to 10 if needed.
- [Selected line vs track under deuteranopia] → Magenta and cyan differ by 23 for deuteranopes (12 against the
  other-file track); the selected line is also 1 px wider than the track and 2 px wider than other files' lines.
- [Other-file track at 75% opacity looks close to the selected file] → The selected file is told apart by width
  (2 px vs 1 px).
- [The outline doubles track rasterization] → Track rendering runs on the render queue only when the view or data
  changes, not on every repaint.
- [Dense track at low zoom still covers much of the grid] → Out of scope (proposal non-goal); can be a follow-up.
- [No automated test for the rendering] → Regressions show up visually on the map.
