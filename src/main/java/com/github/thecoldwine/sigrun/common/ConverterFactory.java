package com.github.thecoldwine.sigrun.common;

import com.github.thecoldwine.sigrun.converters.IBM360Converter;
import com.github.thecoldwine.sigrun.converters.IEEEConverter;
import com.github.thecoldwine.sigrun.converters.SeismicValuesConverter;
import com.github.thecoldwine.sigrun.converters.TC2BIConverter;
import com.github.thecoldwine.sigrun.converters.TC4BIConverter;

public class ConverterFactory {
    public static SeismicValuesConverter getConverter(DataSample sample) {
        switch (sample) {
            case IBM_FP:
                return new IBM360Converter();
            case IEEE_FP:
                return new IEEEConverter();
            case TC_4B_I:
            	return new TC4BIConverter();
            case TC_2B_I:
            	return new TC2BIConverter();
            default:
                throw new UnsupportedOperationException("Converter is not implemented yet for " + sample.getCode() + " " + sample.getDescription() + " " + sample.getSize());
        }
    }
}
