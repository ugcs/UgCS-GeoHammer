# Tasks

## 1. Measurement bar layout

- [x] 1.1 In `RulerMeasurementView`, change `DISTANCE_VALUE_SAMPLE` to `"0000.00"` (min width only, so longer values
  still widen the label instead of being clipped); verify in the running app (`mvn javafx:run`, ruler on) that
  dragging the ruler from under 10 to over 1000 m leaves the unit selector and heading in place
- [x] 1.2 Right-align the distance label (`Pos.CENTER_RIGHT`); verify in the app that the gap between the last digit
  and the unit selector is the same for 1.23 and 1234.56
- [x] 1.3 Replace the unit combo box's fixed `65` width with a `unit-combo-box` style class in `components.css` that
  reduces the cell and arrow-button padding, and a fixed `UNIT_COMBO_WIDTH = 48` (the skin's computed width is at
  least 100 until the popup is first shown); verify in the app (luma and
  luma-dark) that `km` and `mi` show in full next to the arrow and the selector is narrower than before

## 2. Verify

- [x] 2.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings
- [x] 2.2 Invoke the `code-review` skill on the uncommitted changes with this change's proposal and specs as the
  completeness reference, fix critical and major issues, and report the scores and any issues left open
