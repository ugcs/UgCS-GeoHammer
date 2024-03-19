package com.ugcs.gprvisualizer.app.parcers;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.ugcs.gprvisualizer.app.parcers.exceptions.ColumnsMatchingException;
import com.ugcs.gprvisualizer.app.parcers.fixedcolumnwidth.FixedColumnWidthParser;
import com.ugcs.gprvisualizer.app.yaml.Template;

public class FixedColumnWidthParserTests extends BaseParsersTest {

        @Test
        void validPos() throws IOException {
            var path = YamlTestDataFolder + ColumnFixedWidthFolder + "ValidTemplate.yaml";
            String file = new String(Files.readAllBytes(Paths.get(path)));
            Template template = deserializer.load(file);

            var parser = new FixedColumnWidthParser(template);

            try {
                parser.parse(Paths.get(FCWTestDataFolder + "Valid.pos").toAbsolutePath().toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        void invalidColumnsLengths() throws IOException {
            var path = YamlTestDataFolder + ColumnFixedWidthFolder + "InvalidColumnsLengthsTemplate.yaml";

            String file = new String(Files.readAllBytes(Paths.get(path)));
            Template template = deserializer.load(file);

            var parser = new FixedColumnWidthParser(template);

            assertThrows(ColumnsMatchingException.class, () -> {
                parser.parse(Paths.get(FCWTestDataFolder + "Valid.pos").toAbsolutePath().toString());
            });
        }
    }