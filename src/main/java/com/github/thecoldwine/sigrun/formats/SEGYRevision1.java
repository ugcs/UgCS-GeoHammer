package com.github.thecoldwine.sigrun.formats;

import com.github.thecoldwine.sigrun.common.BinaryHeader;
import com.github.thecoldwine.sigrun.common.Reportable;
import com.github.thecoldwine.sigrun.common.Utilities;
import com.github.thecoldwine.sigrun.serialization.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.thecoldwine.sigrun.common.TraceHeader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

/**
 * Created by maksenov on 2017-03-30.
 */
public class SEGYRevision1 implements SEGYRevision {
    private static final Logger log = LoggerFactory.getLogger(SEGYRevision1.class);

    @Override
    public Charset charset() {
        return Charset.forName("Cp1047");
    }

    private <B, H> B fillFormatUsingReflection(
            B builderInstance,
            Class<B> builderClass,
            Class<H> headerClass) {
        Field[] fields = headerClass.getDeclaredFields();

        for (Field f : fields) {
            f.setAccessible(true);

            if (f.isAnnotationPresent(Reportable.class)) {
                Reportable reportable = f.getAnnotation(Reportable.class);

                FormatEntry formatEntry = FormatEntry.create(reportable.startPosition(), reportable.endPosition());
                String formatMethodName = Utilities.makeFormatMethodName(f.getName());

                try {
                    Method m = builderClass.getMethod(formatMethodName, FormatEntry.class);
                    m.invoke(builderInstance, formatEntry);
                } catch (NoSuchMethodException e) {
                    log.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        return builderInstance;
    }

    @Override
    public BinaryHeaderFormat binaryHeaderFormat() {
        BinaryHeaderFormatBuilder builder = BinaryHeaderFormatBuilder.aBinaryHeaderFormat();

        fillFormatUsingReflection(builder, BinaryHeaderFormatBuilder.class, BinaryHeader.class);

        return builder.build();
    }

    @Override
    public TraceHeaderFormat traceHeaderFormat() {
        TraceHeaderFormatBuilder builder = TraceHeaderFormatBuilder.aTraceHeaderFormat();

        fillFormatUsingReflection(builder, TraceHeaderFormatBuilder.class, TraceHeader.class);

        return builder.build();
    }
}
