package com.github.thecoldwine.sigrun.common;

/**
 * Created by maksenov on 03/02/15.
 */
public class SEGYStreamException extends RuntimeException {
    public SEGYStreamException(String message) {
        super(message);
    }

    public SEGYStreamException(String message, Throwable cause) {
        super(message, cause);
    }

    public SEGYStreamException(Throwable cause) {
        super(cause);
    }
}
