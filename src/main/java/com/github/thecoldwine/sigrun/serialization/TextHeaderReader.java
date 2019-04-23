package com.github.thecoldwine.sigrun.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.thecoldwine.sigrun.common.TextHeader;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class TextHeaderReader extends AbstractReader {
    private final static Logger log = LoggerFactory.getLogger(TextHeaderReader.class.getName());

    @SuppressWarnings("WeakerAccess")
    public final Charset charset;

    public TextHeaderReader(Charset charset) {
        log.info("Initializing text header");
        this.charset = charset;
    }

    public TextHeader read(byte[] buffer) {
        assert buffer.length == TextHeader.TEXT_HEADER_SIZE;

        final List<String> records = new ArrayList<String>(TextHeader.RECORDS_AMOUNT);

        for (int i = 0; i < TextHeader.RECORDS_AMOUNT; i++) {
            final int offset = i * TextHeader.STRING_LENGTH;
            final String record = new String(buffer, offset, TextHeader.STRING_LENGTH, charset);
            records.add(record);
        }

        this.fireProgressEvent(TextHeader.TEXT_HEADER_SIZE);

        return new TextHeader(records);
    }
}
