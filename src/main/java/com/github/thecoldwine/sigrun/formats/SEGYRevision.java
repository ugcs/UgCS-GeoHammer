package com.github.thecoldwine.sigrun.formats;

import com.github.thecoldwine.sigrun.serialization.BinaryHeaderFormat;
import com.github.thecoldwine.sigrun.serialization.TraceHeaderFormat;

import java.nio.charset.Charset;

/**
 * SEG-Y file revision.
 * Contains information about Charset, binary header and trace header formats.
 *
 * Created by maksenov on 2017-03-30.
 */
public interface SEGYRevision {
    Charset charset();
    BinaryHeaderFormat binaryHeaderFormat();
    TraceHeaderFormat traceHeaderFormat();
}
