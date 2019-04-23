package com.github.thecoldwine.sigrun.serialization;

public class BinaryHeaderFormat {
    public final FormatEntry jobIdFormat;
    public final FormatEntry lineNumberFormat;
    public final FormatEntry reelNumberFormat;
    public final FormatEntry dataTracesPerEnsembleFormat;
    public final FormatEntry auxiliaryTracesPerEnsembleFormat;
    public final FormatEntry sampleIntervalFormat;
    public final FormatEntry sampleIntervalOfOFRFormat;
    public final FormatEntry samplesPerDataTraceFormat;
    public final FormatEntry samplesPerDataTraceOfOFRFormat;
    public final FormatEntry dataSampleCodeFormat;
    public final FormatEntry ensembleFoldFormat;
    public final FormatEntry traceSortingFormat;
    public final FormatEntry verticalSumCodeFormat;
    public final FormatEntry sweepFrequencyAtStartFormat;
    public final FormatEntry sweepFrequencyAtEndFormat;
    public final FormatEntry sweepLengthFormat;
    public final FormatEntry sweepTypeCodeFormat;
    public final FormatEntry traceNumberFormat;
    public final FormatEntry taperLengthAtStartFormat;
    public final FormatEntry taperLengthAtEndFormat;
    public final FormatEntry taperTypeFormat;
    public final FormatEntry dataTracesCorrelatedFormat;
    public final FormatEntry binaryGainRecoveredFormat;
    public final FormatEntry amplitudeRecoveryMethodFormat;
    public final FormatEntry measurementSystemFormat;
    public final FormatEntry impulseSignalPolarityFormat;
    public final FormatEntry vibratoryPolarityCodeFormat;
    public final FormatEntry segyFormatRevNumberFormat;
    public final FormatEntry fixedLengthTraceFlagFormat;
    public final FormatEntry numberOf3200ByteFormat;

    public BinaryHeaderFormat(FormatEntry jobIdFormat,
                              FormatEntry lineNumberFormat,
                              FormatEntry reelNumberFormat,
                              FormatEntry dataTracesPerEnsembleFormat,
                              FormatEntry auxiliaryTracesPerEnsembleFormat,
                              FormatEntry sampleIntervalFormat,
                              FormatEntry sampleIntervalOfOFRFormat,
                              FormatEntry samplesPerDataTraceFormat,
                              FormatEntry samplesPerDataTraceOfOFRFormat,
                              FormatEntry dataSampleCodeFormat,
                              FormatEntry ensembleFoldFormat,
                              FormatEntry traceSortingFormat,
                              FormatEntry verticalSumCodeFormat,
                              FormatEntry sweepFrequencyAtStartFormat,
                              FormatEntry sweepFrequencyAtEndFormat,
                              FormatEntry sweepLengthFormat,
                              FormatEntry sweepTypeCodeFormat,
                              FormatEntry traceNumberFormat,
                              FormatEntry taperLengthAtStartFormat,
                              FormatEntry taperLengthAtEndFormat,
                              FormatEntry taperTypeFormat,
                              FormatEntry dataTracesCorrelatedFormat,
                              FormatEntry binaryGainRecoveredFormat,
                              FormatEntry amplitudeRecoveryMethodFormat,
                              FormatEntry measurementSystemFormat,
                              FormatEntry impulseSignalPolarityFormat,
                              FormatEntry vibratoryPolarityCodeFormat,
                              FormatEntry segyFormatRevNumberFormat,
                              FormatEntry fixedLengthTraceFlagFormat,
                              FormatEntry numberOf3200ByteFormat) {
        this.jobIdFormat = jobIdFormat;
        this.lineNumberFormat = lineNumberFormat;
        this.reelNumberFormat = reelNumberFormat;
        this.dataTracesPerEnsembleFormat = dataTracesPerEnsembleFormat;
        this.auxiliaryTracesPerEnsembleFormat = auxiliaryTracesPerEnsembleFormat;
        this.sampleIntervalFormat = sampleIntervalFormat;
        this.sampleIntervalOfOFRFormat = sampleIntervalOfOFRFormat;
        this.samplesPerDataTraceFormat = samplesPerDataTraceFormat;
        this.samplesPerDataTraceOfOFRFormat = samplesPerDataTraceOfOFRFormat;
        this.dataSampleCodeFormat = dataSampleCodeFormat;
        this.ensembleFoldFormat = ensembleFoldFormat;
        this.traceSortingFormat = traceSortingFormat;
        this.verticalSumCodeFormat = verticalSumCodeFormat;
        this.sweepFrequencyAtStartFormat = sweepFrequencyAtStartFormat;
        this.sweepFrequencyAtEndFormat = sweepFrequencyAtEndFormat;
        this.sweepLengthFormat = sweepLengthFormat;
        this.sweepTypeCodeFormat = sweepTypeCodeFormat;
        this.traceNumberFormat = traceNumberFormat;
        this.taperLengthAtStartFormat = taperLengthAtStartFormat;
        this.taperLengthAtEndFormat = taperLengthAtEndFormat;
        this.taperTypeFormat = taperTypeFormat;
        this.dataTracesCorrelatedFormat = dataTracesCorrelatedFormat;
        this.binaryGainRecoveredFormat = binaryGainRecoveredFormat;
        this.amplitudeRecoveryMethodFormat = amplitudeRecoveryMethodFormat;
        this.measurementSystemFormat = measurementSystemFormat;
        this.impulseSignalPolarityFormat = impulseSignalPolarityFormat;
        this.vibratoryPolarityCodeFormat = vibratoryPolarityCodeFormat;
        this.segyFormatRevNumberFormat = segyFormatRevNumberFormat;
        this.fixedLengthTraceFlagFormat = fixedLengthTraceFlagFormat;
        this.numberOf3200ByteFormat = numberOf3200ByteFormat;
    }

    public void validate() {
        final FormatEntry[] mandatoryFields = {
                sampleIntervalFormat,
                samplesPerDataTraceFormat,
                dataSampleCodeFormat,
        };

        for (FormatEntry e : mandatoryFields) {
            if (e == null) {
                throw new SEGYFormatException("Missing one of mandatory fields in binary header format");
            }
        }
    }
}
