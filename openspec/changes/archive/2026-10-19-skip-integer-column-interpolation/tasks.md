## 1. Skip integer columns in the load fill

- [x] 1.1 Change `MissingValues.getColumnsToFill` to take the file's `List<GeoData>` instead of the schema, and keep a
  displayed non-Line column only when, over all rows, it has at least one null cell and at least one `Double` cell;
  stop scanning a column once both are found. Update the call in `fillGeoDataValues`. Verify by reading the code
  against design.md decisions 1 and 2
- [x] 1.2 Run the app (`mvn javafx:run`) and open a CSV with gaps in an integer column (e.g. `10`, empty, `20`) and in a
  decimal column (e.g. `10.0`, empty, `20.0`); verify the integer gap stays empty and the decimal gap is filled with 15
- [x] 1.3 In the same file, add a column with whole numbers and gaps in one line and a decimal value in another line;
  reopen and verify its gaps are filled in both lines. Save the file and verify the integer column's gaps are written
  as empty cells
- [x] 1.4 Add `CsvFileTest` covering the scenarios of "Integer columns are not interpolated" through `CsvFile.open` and
  `save` with a test template and CSV in `src/test/resources`; verify `mvn test -Dtest=CsvFileTest` passes

## 2. Verify

- [x] 2.1 Run `mvn clean package` and fix new compilation errors and Error Prone/NullAway warnings in changed code
- [x] 2.2 Invoke the `code-review` skill on the uncommitted changes with this change's proposal and specs as the
  completeness reference; fix critical and major issues and report the scores and any issues left open
