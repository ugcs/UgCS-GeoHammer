package com.github.thecoldwine.sigrun.serialization;

@SuppressWarnings({"WeakerAccess", "UnusedDeclaration"})
public class BinaryHeaderFormatBuilder {
    private FormatEntry jobIdFormat;
    private FormatEntry lineNumberFormat;
    private FormatEntry reelNumberFormat;
    private FormatEntry dataTracesPerEnsembleFormat;
    private FormatEntry auxiliaryTracesPerEnsembleFormat;
    private FormatEntry sampleIntervalFormat;
    private FormatEntry sampleIntervalOfOFRFormat;
    private FormatEntry samplesPerDataTraceFormat;
    private FormatEntry samplesPerDataTraceOfOFRFormat;
    private FormatEntry dataSampleCodeFormat;
    private FormatEntry ensembleFoldFormat;
    private FormatEntry traceSortingFormat;
    private FormatEntry verticalSumCodeFormat;
    private FormatEntry sweepFrequencyAtStartFormat;
    private FormatEntry sweepFrequencyAtEndFormat;
    private FormatEntry sweepLengthFormat;
    private FormatEntry sweepTypeCodeFormat;
    private FormatEntry traceNumberFormat;
    private FormatEntry taperLengthAtStartFormat;
    private FormatEntry taperLengthAtEndFormat;
    private FormatEntry taperTypeFormat;
    private FormatEntry dataTracesCorrelatedFormat;
    private FormatEntry binaryGainRecoveredFormat;
    private FormatEntry amplitudeRecoveryMethodFormat;
    private FormatEntry measurementSystemFormat;
    private FormatEntry impulseSignalPolarityFormat;
    private FormatEntry vibratoryPolarityCodeFormat;
    private FormatEntry segyFormatRevNumberFormat;
    private FormatEntry fixedLengthTraceFlagFormat;
    private FormatEntry numberOf3200ByteFormat;

    private BinaryHeaderFormatBuilder() {
    }

    public static BinaryHeaderFormatBuilder aBinaryHeaderFormat() {
        return new BinaryHeaderFormatBuilder();
    }

    public BinaryHeaderFormatBuilder withJobIdFormat(FormatEntry jobIdFormat) {
        this.jobIdFormat = jobIdFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withLineNumberFormat(FormatEntry lineNumberFormat) {
        this.lineNumberFormat = lineNumberFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withReelNumberFormat(FormatEntry reelNumberFormat) {
        this.reelNumberFormat = reelNumberFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withDataTracesPerEnsembleFormat(FormatEntry dataTracesPerEnsembleFormat) {
        this.dataTracesPerEnsembleFormat = dataTracesPerEnsembleFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withAuxiliaryTracesPerEnsembleFormat(FormatEntry auxiliaryTracesPerEnsembleFormat) {
        this.auxiliaryTracesPerEnsembleFormat = auxiliaryTracesPerEnsembleFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withSampleIntervalFormat(FormatEntry sampleIntervalFormat) {
        this.sampleIntervalFormat = sampleIntervalFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withSampleIntervalOfOFRFormat(FormatEntry sampleIntervalOfOFRFormat) {
        this.sampleIntervalOfOFRFormat = sampleIntervalOfOFRFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withSamplesPerDataTraceFormat(FormatEntry samplesPerDataTraceFormat) {
        this.samplesPerDataTraceFormat = samplesPerDataTraceFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withSamplesPerDataTraceOfOFRFormat(FormatEntry samplesPerDataTraceOfOFRFormat) {
        this.samplesPerDataTraceOfOFRFormat = samplesPerDataTraceOfOFRFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withDataSampleCodeFormat(FormatEntry dataSampleCodeFormat) {
        this.dataSampleCodeFormat = dataSampleCodeFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withEnsembleFoldFormat(FormatEntry ensembleFoldFormat) {
        this.ensembleFoldFormat = ensembleFoldFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withTraceSortingFormat(FormatEntry traceSortingFormat) {
        this.traceSortingFormat = traceSortingFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withVerticalSumCodeFormat(FormatEntry verticalSumCodeFormat) {
        this.verticalSumCodeFormat = verticalSumCodeFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withSweepFrequencyAtStartFormat(FormatEntry sweepFrequencyAtStartFormat) {
        this.sweepFrequencyAtStartFormat = sweepFrequencyAtStartFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withSweepFrequencyAtEndFormat(FormatEntry sweepFrequencyAtEndFormat) {
        this.sweepFrequencyAtEndFormat = sweepFrequencyAtEndFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withSweepLengthFormat(FormatEntry sweepLengthFormat) {
        this.sweepLengthFormat = sweepLengthFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withSweepTypeCodeFormat(FormatEntry sweepTypeCodeFormat) {
        this.sweepTypeCodeFormat = sweepTypeCodeFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withTraceNumberFormat(FormatEntry traceNumberFormat) {
        this.traceNumberFormat = traceNumberFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withTaperLengthAtStartFormat(FormatEntry taperLengthAtStartFormat) {
        this.taperLengthAtStartFormat = taperLengthAtStartFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withTaperLengthAtEndFormat(FormatEntry taperLengthAtEndFormat) {
        this.taperLengthAtEndFormat = taperLengthAtEndFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withTaperTypeFormat(FormatEntry taperTypeFormat) {
        this.taperTypeFormat = taperTypeFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withDataTracesCorrelatedFormat(FormatEntry dataTracesCorrelatedFormat) {
        this.dataTracesCorrelatedFormat = dataTracesCorrelatedFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withBinaryGainRecoveredFormat(FormatEntry binaryGainRecoveredFormat) {
        this.binaryGainRecoveredFormat = binaryGainRecoveredFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withAmplitudeRecoveryMethodFormat(FormatEntry amplitudeRecoveryMethodFormat) {
        this.amplitudeRecoveryMethodFormat = amplitudeRecoveryMethodFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withMeasurementSystemFormat(FormatEntry measurementSystemFormat) {
        this.measurementSystemFormat = measurementSystemFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withImpulseSignalPolarityFormat(FormatEntry impulseSignalPolarityFormat) {
        this.impulseSignalPolarityFormat = impulseSignalPolarityFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withVibratoryPolarityCodeFormat(FormatEntry vibratoryPolarityCodeFormat) {
        this.vibratoryPolarityCodeFormat = vibratoryPolarityCodeFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withSegyFormatRevNumberFormat(FormatEntry segyFormatRevNumberFormat) {
        this.segyFormatRevNumberFormat = segyFormatRevNumberFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withFixedLengthTraceFlagFormat(FormatEntry fixedLengthTraceFlagFormat) {
        this.fixedLengthTraceFlagFormat = fixedLengthTraceFlagFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder withNumberOf3200ByteFormat(FormatEntry numberOf3200ByteFormat) {
        this.numberOf3200ByteFormat = numberOf3200ByteFormat;
        return this;
    }

    public BinaryHeaderFormatBuilder but() {
        return aBinaryHeaderFormat()
                .withJobIdFormat(jobIdFormat)
                .withLineNumberFormat(lineNumberFormat)
                .withReelNumberFormat(reelNumberFormat)
                .withDataTracesPerEnsembleFormat(dataTracesPerEnsembleFormat)
                .withAuxiliaryTracesPerEnsembleFormat(auxiliaryTracesPerEnsembleFormat)
                .withSampleIntervalFormat(sampleIntervalFormat)
                .withSampleIntervalOfOFRFormat(sampleIntervalOfOFRFormat)
                .withSamplesPerDataTraceFormat(samplesPerDataTraceFormat)
                .withSamplesPerDataTraceOfOFRFormat(samplesPerDataTraceOfOFRFormat)
                .withDataSampleCodeFormat(dataSampleCodeFormat)
                .withEnsembleFoldFormat(ensembleFoldFormat)
                .withTraceSortingFormat(traceSortingFormat)
                .withVerticalSumCodeFormat(verticalSumCodeFormat)
                .withSweepFrequencyAtStartFormat(sweepFrequencyAtStartFormat)
                .withSweepFrequencyAtEndFormat(sweepFrequencyAtEndFormat)
                .withSweepLengthFormat(sweepLengthFormat)
                .withSweepTypeCodeFormat(sweepTypeCodeFormat)
                .withTraceNumberFormat(traceNumberFormat)
                .withTaperLengthAtStartFormat(taperLengthAtStartFormat)
                .withTaperLengthAtEndFormat(taperLengthAtEndFormat)
                .withTaperTypeFormat(taperTypeFormat)
                .withDataTracesCorrelatedFormat(dataTracesCorrelatedFormat)
                .withBinaryGainRecoveredFormat(binaryGainRecoveredFormat)
                .withAmplitudeRecoveryMethodFormat(amplitudeRecoveryMethodFormat)
                .withMeasurementSystemFormat(measurementSystemFormat)
                .withImpulseSignalPolarityFormat(impulseSignalPolarityFormat)
                .withVibratoryPolarityCodeFormat(vibratoryPolarityCodeFormat)
                .withSegyFormatRevNumberFormat(segyFormatRevNumberFormat)
                .withFixedLengthTraceFlagFormat(fixedLengthTraceFlagFormat)
                .withNumberOf3200ByteFormat(numberOf3200ByteFormat);
    }

    public BinaryHeaderFormat build() {
        BinaryHeaderFormat binaryHeaderFormat = new BinaryHeaderFormat(jobIdFormat,
                lineNumberFormat,
                reelNumberFormat,
                dataTracesPerEnsembleFormat,
                auxiliaryTracesPerEnsembleFormat,
                sampleIntervalFormat,
                sampleIntervalOfOFRFormat,
                samplesPerDataTraceFormat,
                samplesPerDataTraceOfOFRFormat,
                dataSampleCodeFormat,
                ensembleFoldFormat,
                traceSortingFormat,
                verticalSumCodeFormat,
                sweepFrequencyAtStartFormat,
                sweepFrequencyAtEndFormat,
                sweepLengthFormat,
                sweepTypeCodeFormat,
                traceNumberFormat,
                taperLengthAtStartFormat,
                taperLengthAtEndFormat,
                taperTypeFormat,
                dataTracesCorrelatedFormat,
                binaryGainRecoveredFormat,
                amplitudeRecoveryMethodFormat,
                measurementSystemFormat,
                impulseSignalPolarityFormat,
                vibratoryPolarityCodeFormat,
                segyFormatRevNumberFormat,
                fixedLengthTraceFlagFormat,
                numberOf3200ByteFormat);
        binaryHeaderFormat.validate();

        return binaryHeaderFormat;
    }
}
