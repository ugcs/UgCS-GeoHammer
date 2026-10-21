## Why

Quality control issues are hard to see on the map (#465). The Altitude AGL issue is red and is drawn below the red
track of the selected file, the gap issue is yellow like the selected line, and the purple data issue blends into
the grid palettes. Altitude stripes are sized in meters only, so on large surveys viewed zoomed out they shrink below
a pixel and vanish under the track. Letting the user pick issue colors would hide the problem instead of fixing it,
so the defaults and the issue rendering are fixed instead.

## What Changes

- New default colors for the survey track: selected file cyan, other files a fainter cyan. Every track line gets a
  thin darker outline, so it stays readable over light and dark backgrounds. The track uses cool colors only; warm
  colors are reserved for errors.
- One magenta selection color for the selected line, the selected ruler point and the selected point of the area
  selection. Area selection edges become white, so they are not confused with yellow gaps.
- New default colors for quality issues: Altitude AGL orange-red, line distance gap yellow, data issue an orange-red
  marker with a yellow ring. The colors were chosen and checked against satellite imagery and all grid palettes,
  including red-green color blindness.
- Quality issues are drawn above the survey track.
- Quality issues keep a minimum on-screen size at any zoom: stripes and gap areas are at least about 2 px wider than
  their geometry, data markers have a radius of at least 3 px.
- Holes inside a gap area are shown as holes instead of being filled.
- Colors that map layers draw with (track, selection, quality issues, ruler, area selection) are defined in one place;
  view colors (map background, "no GPS" text) stay in the map view.

## Non-goals

- User-selectable colors for issues or the track.
- Outlines, casings or fill patterns for issues (only track lines are outlined).
- Changes to the quality checks themselves: thresholds, issue geometry and issue sizes in meters stay the same.
- Thinning or fading the track when lines are dense at low zoom.
- Changes to flag or map background colors.

## Capabilities

### New Capabilities
- `map-quality-issues`: how quality issues, the survey track and selected map elements are shown on the map: drawing
  order, minimum on-screen size, distinguishable colors, track outlines and gap holes.

### Modified Capabilities

## Impact

- Map layers: `GpsTrack` (track colors and outline), `QualityLayer` (colors, drawing, minimum size), `MapView` (layer
  order, each layer drawn on its own graphics copy; its background and "no GPS" text colors stay as they are),
  `MapRuler` and `TraceCutter` (colors from the shared definitions, selection color), `FoundTracesLayer` (unused point
  color removed).
- Quality service: issues carry a type instead of a color; `QualityColors` is removed. The three checks pass the type.
- No changes to persisted formats, settings, undo, events or MCP tools.
