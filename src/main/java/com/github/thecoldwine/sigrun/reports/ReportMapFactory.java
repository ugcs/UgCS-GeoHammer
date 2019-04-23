package com.github.thecoldwine.sigrun.reports;

import com.github.thecoldwine.sigrun.common.Reportable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Deprecated
@SuppressWarnings({"WeakerAccess", "UnusedDeclaration"})
public class ReportMapFactory {
    public static <T> Map<String, Field> createReportMap(Class<T> clazz) {
        Map<String, Field> resultMap = new HashMap<String, Field>();
        Field[] fields = clazz.getFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Reportable.class)) {
                field.setAccessible(true);
                resultMap.put(field.getAnnotation(Reportable.class).value(), field);
            }
        }

        return resultMap;
    }
}