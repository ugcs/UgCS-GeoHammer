package com.github.thecoldwine.sigrun.serialization;

import com.github.thecoldwine.sigrun.common.*;
import com.github.thecoldwine.sigrun.converters.ByteANumberConverter;

@SuppressWarnings({"WeakerAccess", "ConstantConditions"})
public class BinaryHeaderReader extends AbstractReader {
    public final BinaryHeaderFormat format;

    public BinaryHeaderReader(BinaryHeaderFormat format) {
        this.format = format;
    }

    public BinaryHeader read(final byte[] buffer) {
        assert buffer.length == BinaryHeader.BIN_HEADER_LENGTH;
        final BinaryHeader binaryHeader = new BinaryHeader();

        if (format.jobIdFormat != null)
            binaryHeader.setJobId(ByteANumberConverter.byteAToInt(buffer, format.jobIdFormat.posStart));

        if (format.lineNumberFormat != null)
            binaryHeader.setLineNumber(ByteANumberConverter.byteAToInt(buffer, format.lineNumberFormat.posStart));

        if (format.reelNumberFormat != null)
            binaryHeader.setReelNumber(ByteANumberConverter.byteAToInt(buffer, format.reelNumberFormat.posStart));

        if (format.dataTracesPerEnsembleFormat != null)
            binaryHeader.setDataTracesPerEnsemble(ByteANumberConverter.byteAToShort(buffer, format.dataTracesPerEnsembleFormat.posStart));

        if (format.auxiliaryTracesPerEnsembleFormat != null)
            binaryHeader.setAuxiliaryTracesPerEnsemble(ByteANumberConverter.byteAToShort(buffer, format.auxiliaryTracesPerEnsembleFormat.posStart));

        if (format.sampleIntervalFormat != null)
            binaryHeader.setSampleInterval(ByteANumberConverter.byteAToShort(buffer, format.sampleIntervalFormat.posStart));

        if (format.sampleIntervalOfOFRFormat != null)
            binaryHeader.setSampleIntervalOfOFR(ByteANumberConverter.byteAToShort(buffer, format.sampleIntervalOfOFRFormat.posStart));

        if (format.samplesPerDataTraceFormat != null)
            binaryHeader.setSamplesPerDataTrace(ByteANumberConverter.byteAToShort(buffer, format.samplesPerDataTraceFormat.posStart));

        if (format.samplesPerDataTraceOfOFRFormat != null)
            binaryHeader.setSamplesPerDataTraceOfOFR(ByteANumberConverter.byteAToShort(buffer, format.samplesPerDataTraceOfOFRFormat.posStart));

        if (format.dataSampleCodeFormat != null)
            binaryHeader.setDataSampleCode(DataSample.create(ByteANumberConverter.byteAToShort(buffer, format.dataSampleCodeFormat.posStart)));

        if (format.ensembleFoldFormat != null)
            binaryHeader.setEnsembleFold(ByteANumberConverter.byteAToShort(buffer, format.ensembleFoldFormat.posStart));

        if (format.traceSortingFormat != null)
            binaryHeader.setTraceSorting(TraceSorting.create(ByteANumberConverter.byteAToShort(buffer, format.traceSortingFormat.posStart)));

        if (format.verticalSumCodeFormat != null)
            binaryHeader.setVerticalSumCode(ByteANumberConverter.byteAToShort(buffer, format.verticalSumCodeFormat.posStart));

        if (format.sweepFrequencyAtStartFormat != null)
            binaryHeader.setSweepFrequencyAtStart(ByteANumberConverter.byteAToShort(buffer, format.sweepFrequencyAtStartFormat.posStart));

        if (format.sweepFrequencyAtEndFormat != null)
            binaryHeader.setSweepFrequencyAtEnd(ByteANumberConverter.byteAToShort(buffer, format.sweepFrequencyAtEndFormat.posStart));

        if (format.sweepLengthFormat != null)
            binaryHeader.setSweepLength(ByteANumberConverter.byteAToShort(buffer, format.sweepLengthFormat.posStart));

        if (format.sweepTypeCodeFormat != null)
            binaryHeader.setSweepTypeCode(SweepTypeCode.create(ByteANumberConverter.byteAToShort(buffer, format.sweepTypeCodeFormat.posStart)));

        if (format.traceNumberFormat != null)
            binaryHeader.setTraceNumber(ByteANumberConverter.byteAToShort(buffer, format.traceNumberFormat.posStart));

        if (format.taperLengthAtStartFormat != null)
            binaryHeader.setTaperLengthAtStart(ByteANumberConverter.byteAToShort(buffer, format.taperLengthAtStartFormat.posStart));

        if (format.taperLengthAtEndFormat != null)
            binaryHeader.setTaperLengthAtEnd(ByteANumberConverter.byteAToShort(buffer, format.taperLengthAtEndFormat.posStart));

        if (format.taperTypeFormat != null)
            binaryHeader.setTaperType(TaperType.create(ByteANumberConverter.byteAToShort(buffer, format.taperTypeFormat.posStart)));

        if (format.dataTracesCorrelatedFormat != null)
            binaryHeader.setDataTracesCorrelated(ByteANumberConverter.byteAToShort(buffer, format.dataTracesCorrelatedFormat.posStart));

        if (format.binaryGainRecoveredFormat != null)
            binaryHeader.setBinaryGainRecovered(ByteANumberConverter.byteAToShort(buffer, format.binaryGainRecoveredFormat.posStart));

        if (format.amplitudeRecoveryMethodFormat != null)
            binaryHeader.setAmplitudeRecoveryMethod(ByteANumberConverter.byteAToShort(buffer, format.amplitudeRecoveryMethodFormat.posStart));

        if (format.measurementSystemFormat != null)
            binaryHeader.setMeasurementSystem(ByteANumberConverter.byteAToShort(buffer, format.measurementSystemFormat.posStart));

        if (format.impulseSignalPolarityFormat != null)
            binaryHeader.setImpulseSignalPolarity(ByteANumberConverter.byteAToShort(buffer, format.impulseSignalPolarityFormat.posStart));

        if (format.vibratoryPolarityCodeFormat != null)
            binaryHeader.setVibratoryPolarityCode(ByteANumberConverter.byteAToShort(buffer, format.vibratoryPolarityCodeFormat.posStart));

        if (format.segyFormatRevNumberFormat != null)
            binaryHeader.setSegyFormatRevNumber(ByteANumberConverter.byteAToShort(buffer, format.segyFormatRevNumberFormat.posStart));

        if (format.fixedLengthTraceFlagFormat != null)
            binaryHeader.setFixedLengthTraceFlag(ByteANumberConverter.byteAToShort(buffer, format.fixedLengthTraceFlagFormat.posStart));

        if (format.numberOf3200ByteFormat != null)
            binaryHeader.setNumberOf3200Byte(ByteANumberConverter.byteAToShort(buffer, format.numberOf3200ByteFormat.posStart));

        this.fireProgressEvent(buffer.length);

        return binaryHeader;
    }
}
