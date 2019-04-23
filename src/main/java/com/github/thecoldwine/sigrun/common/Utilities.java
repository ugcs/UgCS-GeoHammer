package com.github.thecoldwine.sigrun.common;

/**
 * Created by aksen on 2017-03-30.
 */
public class Utilities {
    public static String makeFormatMethodName(String srcFieldName) {
        StringBuilder sb = new StringBuilder(srcFieldName);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.insert(0, "with");
        sb.append("Format");

        return sb.toString();
    }
}
