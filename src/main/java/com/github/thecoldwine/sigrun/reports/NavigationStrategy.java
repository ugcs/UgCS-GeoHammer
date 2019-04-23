package com.github.thecoldwine.sigrun.reports;

import com.github.thecoldwine.sigrun.common.BinaryHeader;
import com.github.thecoldwine.sigrun.common.TraceHeader;
import com.github.thecoldwine.sigrun.common.TextHeader;

import java.io.IOException;
import java.io.OutputStream;

@Deprecated
@SuppressWarnings({"WeakerAccess", "UnusedDeclaration"})
public class NavigationStrategy extends ReportStrategy {
    public static final int ESTIMATED_STRING_LENGTH = 512;

    public NavigationStrategy() {
        super();
    }

    public NavigationStrategy(String separator) {
        super(separator);
    }

    public NavigationStrategy(BinaryHeader binaryHeader, TextHeader textHeader) {
        super(binaryHeader, textHeader);
    }

    public NavigationStrategy(BinaryHeader binaryHeader, TextHeader textHeader, String separator) {
        super(binaryHeader, textHeader);
        setCustomSeparator(separator);
    }

    @Override
    public void processTraceHeader(TraceHeader traceHeader, OutputStream outputStream) throws IOException {

        outputStream.write((String.valueOf(binaryHeader.getLineNumber()) + getSeparator() + traceHeader.getCrossLineNumber() + getSeparator() + traceHeader.getInLineNumber() + getSeparator() + traceHeader.getShotpointNumber() + getSeparator() + traceHeader.getSourceX() + getSeparator() + traceHeader.getSourceY() + System.getProperty("line.separator")).getBytes("UTF-8"));
    }

    @Override
    public void printHeader(OutputStream outputStream) throws IOException {
        throw new UnsupportedOperationException("NavigationStrategy.printHeader not yet implemented.");
    }
}
