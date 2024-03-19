package com.ugcs.gprvisualizer.app.parcers;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.ugcs.gprvisualizer.app.parcers.exceptions.UnknownSegyTypeException;
import com.ugcs.gprvisualizer.app.parcers.segylog.SegYLogParser;

@Disabled
public class SegyParserTests extends BaseParsersTest {

        @Test
        void validSegy() {
            var parser = new SegYLogParser(null);

            try {
                parser.parse(Paths.get(SegyTestDataFolder + "2020-07-29-14-37-42-gpr.sgy").toAbsolutePath().toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        @Test
        void unknownSegy() {
            var parser = new SegYLogParser(null);

            assertThrows(UnknownSegyTypeException.class, () -> {
                parser.parse(Paths.get(SegyTestDataFolder + "2020-07-29-14-37-42-unknown.sgy").toAbsolutePath().toString());
            });
        }
    }
