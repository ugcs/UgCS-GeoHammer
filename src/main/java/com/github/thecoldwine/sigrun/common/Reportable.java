package com.github.thecoldwine.sigrun.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings("UnusedDeclaration")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Reportable {
    String value();

    int startPosition() default 0;

    int endPosition() default 0;
}
