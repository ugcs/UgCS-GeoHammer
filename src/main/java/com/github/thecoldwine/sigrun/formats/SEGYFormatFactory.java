package com.github.thecoldwine.sigrun.formats;

/**
 * Factory of SEG-Y formats.
 *
 * Currently only rev 1 format is supported.
 *
 * Created by maksenov on 2017-03-30.
 */
public class SEGYFormatFactory {
    public static SEGYRevision Revision1() {
        return new SEGYRevision1();
    }
}
