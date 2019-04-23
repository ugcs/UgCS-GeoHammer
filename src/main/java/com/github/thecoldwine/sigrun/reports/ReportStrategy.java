package com.github.thecoldwine.sigrun.reports;

import com.github.thecoldwine.sigrun.common.BinaryHeader;
import com.github.thecoldwine.sigrun.common.TextHeader;
import com.github.thecoldwine.sigrun.common.TraceHeader;

import java.io.IOException;
import java.io.OutputStream;

@Deprecated
@SuppressWarnings({"SameParameterValue", "WeakerAccess", "UnusedDeclaration"})
public abstract class ReportStrategy {
    public final static String DEFAULT_SEPARATOR = ",";

    private String customSeparator;
    private boolean printHeaderSupported = false;
    protected BinaryHeader binaryHeader;
    protected TextHeader textHeader;

    public ReportStrategy() {
        this(null, null);
    }

    public ReportStrategy(String separator) {
        this(null, null);
        this.customSeparator = separator;
    }

    public ReportStrategy(BinaryHeader binaryHeader, TextHeader textHeader) {
        this.binaryHeader = binaryHeader;
        this.textHeader = textHeader;
    }

    public final BinaryHeader getBinaryHeader() {
        return binaryHeader;
    }

    public final void setBinaryHeader(BinaryHeader binaryHeader) {
        this.binaryHeader = binaryHeader;
    }

    public final TextHeader getTextHeader() {
        return textHeader;
    }

    public final void setTextHeader(TextHeader textHeader) {
        this.textHeader = textHeader;
    }

    public final String getSeparator() {
        if (customSeparator != null && !customSeparator.equals(""))
            return customSeparator;
        else
            return DEFAULT_SEPARATOR;
    }

    public final void setCustomSeparator(String customSeparator) {
        this.customSeparator = customSeparator;

    }

    public boolean isPrintHeaderSupported() {
        return printHeaderSupported;
    }

    protected void setPrintHeaderSupported(boolean printHeaderSupported) {
        this.printHeaderSupported = printHeaderSupported;
    }

    public abstract void processTraceHeader(TraceHeader traceHeader, OutputStream outputStream) throws IOException;

    public abstract void printHeader(OutputStream outputStream) throws IOException;
}
