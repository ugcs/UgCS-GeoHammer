package com.ugcs.geohammer.format.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ugcs.geohammer.format.GeoData;
import com.ugcs.geohammer.format.csv.parser.BaseParsersTest;
import com.ugcs.geohammer.model.ColumnSchema;
import com.ugcs.geohammer.model.template.Template;

class CsvFileTest extends BaseParsersTest {

    private static final double EPS = 1e-9;

    private static final String TEMPLATE_PATH = YamlTestDataFolder + YamlCsvFolder + "MissingValuesTemplate.yaml";

    private static final String FILE_PATH = CSVTestDataFolder + "missing-values.csv";

    @TempDir
    Path directory;

    @Test
    void open_integerColumnWithGaps_keepsGapsEmpty() throws IOException {
        CsvFile file = openFile();

        assertNull(getValue(file, 1, "IntCol"));
    }

    @Test
    void open_wholeNumbersWithDecimalSeparator_fillsGaps() throws IOException {
        CsvFile file = openFile();

        assertEquals(15.0, getNumber(file, 1, "FloatCol"), EPS);
        assertEquals(50.0, getNumber(file, 5, "FloatCol"), EPS);
    }

    @Test
    void open_integerAndDecimalValuesInColumn_fillsGaps() throws IOException {
        CsvFile file = openFile();

        assertEquals(10.75, getNumber(file, 1, "MixedCol"), EPS);
        assertEquals(14.0, getNumber(file, 5, "MixedCol"), EPS);
    }

    @Test
    void open_decimalValueInAnotherLine_fillsGapsInAllLines() throws IOException {
        CsvFile file = openFile();

        assertEquals(15.0, getNumber(file, 1, "CrossCol"), EPS);
        assertEquals(4.0, getNumber(file, 5, "CrossCol"), EPS);
    }

    @Test
    void open_filledValues_opensWithoutUnsavedChanges() throws IOException {
        CsvFile file = openFile();

        assertFalse(file.isUnsaved());
    }

    @Test
    void save_integerColumnWithGaps_writesEmptyCells() throws IOException {
        CsvFile file = openFile();
        File saved = directory.resolve("saved.csv").toFile();

        file.save(saved);

        List<String> lines = Files.readAllLines(saved.toPath());
        int intColumn = Arrays.asList(lines.getFirst().split(",")).indexOf("IntCol");
        assertEquals("", lines.get(2).split(",", -1)[intColumn]);
        assertEquals("15", lines.get(2).split(",", -1)[intColumn + 1]);
    }

    private CsvFile openFile() throws IOException {
        Template template = deserializer.load(Files.readString(Path.of(TEMPLATE_PATH)));
        template.init();
        CsvFile file = new CsvFile(null);
        file.open(new File(FILE_PATH), template);
        return file;
    }

    private static Object getValue(CsvFile file, int row, String header) {
        List<GeoData> values = file.getGeoData();
        ColumnSchema schema = GeoData.getSchema(values);
        return values.get(row).getValue(schema.getColumnIndex(header));
    }

    private static double getNumber(CsvFile file, int row, String header) {
        return ((Number)getValue(file, row, header)).doubleValue();
    }
}
