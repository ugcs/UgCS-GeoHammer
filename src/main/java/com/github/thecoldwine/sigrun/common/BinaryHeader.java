package com.github.thecoldwine.sigrun.common;

@SuppressWarnings("UnusedDeclaration")
public class BinaryHeader {
    public static final int BIN_HEADER_LENGTH = 400;

    /* Job identification code */
    @Reportable(value = "Job Id", startPosition = 0, endPosition = 4)
    private Integer jobId;
    /* Line code. For 3-D poststack data, this will typically contain the in-line code */
    @Reportable(value = "Line Number", startPosition = 4, endPosition = 8)
    private Integer lineNumber;
    /* Reel code */
    @Reportable(value = "Reel Number", startPosition = 8, endPosition = 12)
    private Integer reelNumber;
    /* Number of data traces per ensemble. Mandatory for prestack data  */
    @Reportable(value = "Data Traces Per Ensemble", startPosition = 12, endPosition = 14)
    private Short dataTracesPerEnsemble;
    /* Number of auxiliary traces per ensemble. Mandatory for prestack data */
    @Reportable(value = "Number Of Auxiliary Traces Per Ensemble", startPosition = 14, endPosition = 16)
    private Short auxiliaryTracesPerEnsemble;
    /* Sample interval in microseconds */
    @Reportable(value = "Sample Interval In Microseconds", startPosition = 16, endPosition = 18)
    private short sampleInterval;
    /* Sample interval in microseconds of original field recording */
    @Reportable(value = "Sample Interval In Microseconds Of Original Field Recording", startPosition = 18,
            endPosition = 20)
    private Short sampleIntervalOfOFR;
    /* Number of samples per data trace */
    @Reportable(value = "Samples Per Data Trace", startPosition = 20, endPosition = 22)
    private short samplesPerDataTrace;
    /* Number of samples per data trace for original field recording */
    @Reportable(value = "Samples Per Data Trace Of Original Field Recording", startPosition = 22, endPosition = 24)
    private Short samplesPerDataTraceOfOFR;
    /* Data sample format code */
    @Reportable(value = "Data Sample Code", startPosition = 24, endPosition = 26)
    private DataSample dataSampleCode;
    /* Ensemble fold */
    @Reportable(value = "Ensemble Fold", startPosition = 26, endPosition = 28)
    private short ensembleFold;
    /* Trace sorting code */
    @Reportable(value = "Trace Sorting Code", startPosition = 28, endPosition = 30)
    private TraceSorting traceSorting;
    /* Vertical sum code
            1 = no sum
            2 = two sum
            ..
            N = (verticalSum - 1) sum
    */
    @Reportable(value = "Vertical Sum Code", startPosition = 30, endPosition = 32)
    private short verticalSumCode;
    /* Sweep frequency at start (Hz) */
    @Reportable(value = "Sweep Frequency At Start", startPosition = 32, endPosition = 34)
    private short sweepFrequencyAtStart;
    /* Sweep frequency at end (Hz) */
    @Reportable(value = "Sweep Frequency At End", startPosition = 34, endPosition = 36)
    private short sweepFrequencyAtEnd;
    /* Sweep length (ms) */
    @Reportable(value = "Sweep Length", startPosition = 36, endPosition = 38)
    private short sweepLength;
    /* Sweep type code */
    @Reportable(value = "Sweep Type Code", startPosition = 38, endPosition = 40)
    private SweepTypeCode sweepTypeCode;
    /* Trace number of a sweep channel */
    @Reportable(value = "Trace Number", startPosition = 40, endPosition = 42)
    private short traceNumber;
    /* Sweep trace taper length in milliseconds at start if tapered */
    @Reportable(value = "Taper Length At Start", startPosition = 42, endPosition = 44)
    private short taperLengthAtStart;
    /* Sweep trace taper length in milliseconds at end if tapered */
    @Reportable(value = "Taper Length At End", startPosition = 44, endPosition = 46)
    private short taperLengthAtEnd;
    /* Taper type */
    @Reportable(value = "Taper Type", startPosition = 46, endPosition = 48)
    private TaperType taperType;
    /* Correlated data traces
    * IN SEG Y THIS FIELD HAS LENGTH TWO BYTE!
    * 1 = no
    * 2 = yes
    * */
    @Reportable(value = "Correlated Data Traces", startPosition = 48, endPosition = 50)
    private short dataTracesCorrelated;
    /* Binary gain recovered
    * IN SEG Y THIS FIELD HAS LENGTH TWO BYTE!
    * 1 = yes
    * 2 = no
    * */
    @Reportable(value = "Binary Gain Recovered", startPosition = 50, endPosition = 52)
    private short binaryGainRecovered;
    /* Amplitude recovery method:
    * 1 = none
    * 2 = spherical divergence
    * 3 = AGC
    * 4 = other
    * */
    @Reportable(value = "Amplitude Recovery Method", startPosition = 52, endPosition = 54)
    private short amplitudeRecoveryMethod;
    /* Measurement system
    * 1 = Meters
    * 2 = Feet
    * */

    @Reportable(value = "Measurement System", startPosition = 54, endPosition = 56)
    private short measurementSystem;
    /* Impulse signal polarity
    * 1 = Increase in pressure or upward geophone case movement gives negative number of tape
    * 2 = Increase in pressure or upward geophone case movement gives positive number of tape
    * */
    @Reportable(value = "Impulse Signal Polarity", startPosition = 56, endPosition = 58)
    private short impulseSignalPolarity;
    /* Vibratory polarity code
    * 1 = 337.5 to 22.5
    * 2 = 22.5  to 67.5
    * 3 = 67.5  to 112.5
    * 4 = 112.5 to 157.5
    * 5 = 157.5 to 202.5
    * 6 = 202.5 to 247.5
    * 7 = 247.5 to 292.5
    * 8 = 292.5 to 337.5
    * */
    @Reportable(value = "Vibratory Polarity Code", startPosition = 58, endPosition = 60)
    private short vibratoryPolarityCode;
    /* SEG Y Format Revision Number. This is a 16-bit unsigned value with a Q-point between the first and second
    * bytes. Thus for SEG Y Revision 1.0, as defined in this document, this will be recorded as 0100(16). This field
    * is mandatory for all versions of SEG Y, although a value of zero indicates "traditional" SEG Y conforming to the
    * 1975 standard.
    * */
    @Reportable(value = "SEG Y Format Revision Number", startPosition = 300, endPosition = 302)
    private short segyFormatRevNumber;
    /* Fixed length trace flag. A value of one indicates that all traces in this SEG Y file are guaranteed to have
    * the same sample interval and number of samples, as specified in Textual File Header bytes 3217-3218 and 3221-3222.
    * A value of zero indicates that the length of the traces in the file may vary and the number of samples in bytes
    * 115-116 if the Trace Header must be examined to determine the actual length of each trace. This field is mandatory
    * for all versions of SEG Y, although a value if zero indicates "traditional SEG Y" conforming to the 1975 standard.
    * */
    @Reportable(value = "Fixed Length Trace Flag", startPosition = 302, endPosition = 304)
    private short fixedLengthTraceFlag;
    /* Number of 3200-byte, Extended Textual File Header records following the Binary Header. A value of zero indicates
    * there are no Extended Textual File Header records (i. e. this file has no Textual File Headers). A value of -1
    * indicates that there are a variable number of Expended Textual File Header records and the end of the Extended
    * File Header is denoted by an ((SEG: EndText)) stanza in the final record. A positive values indicates that there
    * are exactly that many Extended File Header records. Note that, although the exact number of Extended Textual File
    * Header records may be a useful piece of information, it not always ne known at the time the Binary Header is
    * written an it is not mandatory that a positive value be recorded here. This field is mandatory for all versions of
    * SEG Y, although a value of zero indicates "traditional" SEG Y conforming to the 1975 standard.
    * */
    @Reportable(value = "Number Of 3200 Byte", startPosition = 304, endPosition = 306)
    private short numberOf3200Byte;

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Integer getReelNumber() {
        return reelNumber;
    }

    public void setReelNumber(Integer reelNumber) {
        this.reelNumber = reelNumber;
    }

    public Short getDataTracesPerEnsemble() {
        return dataTracesPerEnsemble;
    }

    public void setDataTracesPerEnsemble(Short dataTracesPerEnsemble) {
        this.dataTracesPerEnsemble = dataTracesPerEnsemble;
    }

    public Short getAuxiliaryTracesPerEnsemble() {
        return auxiliaryTracesPerEnsemble;
    }

    public void setAuxiliaryTracesPerEnsemble(Short auxiliaryTracesPerEnsemble) {
        this.auxiliaryTracesPerEnsemble = auxiliaryTracesPerEnsemble;
    }

    public short getSampleInterval() {
        return sampleInterval;
    }

    public void setSampleInterval(short sampleInterval) {
        this.sampleInterval = sampleInterval;
    }

    public Short getSampleIntervalOfOFR() {
        return sampleIntervalOfOFR;
    }

    public void setSampleIntervalOfOFR(Short sampleIntervalOfOFR) {
        this.sampleIntervalOfOFR = sampleIntervalOfOFR;
    }

    public short getSamplesPerDataTrace() {
        return samplesPerDataTrace;
    }

    public void setSamplesPerDataTrace(short samplesPerDataTrace) {
        this.samplesPerDataTrace = samplesPerDataTrace;
    }

    public Short getSamplesPerDataTraceOfOFR() {
        return samplesPerDataTraceOfOFR;
    }

    public void setSamplesPerDataTraceOfOFR(Short samplesPerDataTraceOfOFR) {
        this.samplesPerDataTraceOfOFR = samplesPerDataTraceOfOFR;
    }

    public DataSample getDataSampleCode() {
        return dataSampleCode;
    }

    public void setDataSampleCode(DataSample dataSampleCode) {
        this.dataSampleCode = dataSampleCode;
    }

    public short getEnsembleFold() {
        return ensembleFold;
    }

    public void setEnsembleFold(short ensembleFold) {
        this.ensembleFold = ensembleFold;
    }

    public TraceSorting getTraceSorting() {
        return traceSorting;
    }

    public void setTraceSorting(TraceSorting traceSorting) {
        this.traceSorting = traceSorting;
    }

    public short getVerticalSumCode() {
        return verticalSumCode;
    }

    public void setVerticalSumCode(short verticalSumCode) {
        this.verticalSumCode = verticalSumCode;
    }

    public short getSweepFrequencyAtStart() {
        return sweepFrequencyAtStart;
    }

    public void setSweepFrequencyAtStart(short sweepFrequencyAtStart) {
        this.sweepFrequencyAtStart = sweepFrequencyAtStart;
    }

    public short getSweepFrequencyAtEnd() {
        return sweepFrequencyAtEnd;
    }

    public void setSweepFrequencyAtEnd(short sweepFrequencyAtEnd) {
        this.sweepFrequencyAtEnd = sweepFrequencyAtEnd;
    }

    public short getSweepLength() {
        return sweepLength;
    }

    public void setSweepLength(short sweepLength) {
        this.sweepLength = sweepLength;
    }

    public SweepTypeCode getSweepTypeCode() {
        return sweepTypeCode;
    }

    public void setSweepTypeCode(SweepTypeCode sweepTypeCode) {
        this.sweepTypeCode = sweepTypeCode;
    }

    public short getTraceNumber() {
        return traceNumber;
    }

    public void setTraceNumber(short traceNumber) {
        this.traceNumber = traceNumber;
    }

    public short getTaperLengthAtStart() {
        return taperLengthAtStart;
    }

    public void setTaperLengthAtStart(short taperLengthAtStart) {
        this.taperLengthAtStart = taperLengthAtStart;
    }

    public short getTaperLengthAtEnd() {
        return taperLengthAtEnd;
    }

    public void setTaperLengthAtEnd(short taperLengthAtEnd) {
        this.taperLengthAtEnd = taperLengthAtEnd;
    }

    public TaperType getTaperType() {
        return taperType;
    }

    public void setTaperType(TaperType taperType) {
        this.taperType = taperType;
    }

    public short getDataTracesCorrelated() {
        return dataTracesCorrelated;
    }

    public void setDataTracesCorrelated(short dataTracesCorrelated) {
        this.dataTracesCorrelated = dataTracesCorrelated;
    }

    public short getBinaryGainRecovered() {
        return binaryGainRecovered;
    }

    public void setBinaryGainRecovered(short binaryGainRecovered) {
        this.binaryGainRecovered = binaryGainRecovered;
    }

    public short getAmplitudeRecoveryMethod() {
        return amplitudeRecoveryMethod;
    }

    public void setAmplitudeRecoveryMethod(short amplitudeRecoveryMethod) {
        this.amplitudeRecoveryMethod = amplitudeRecoveryMethod;
    }

    public short getMeasurementSystem() {
        return measurementSystem;
    }

    public void setMeasurementSystem(short measurementSystem) {
        this.measurementSystem = measurementSystem;
    }

    public short getImpulseSignalPolarity() {
        return impulseSignalPolarity;
    }

    public void setImpulseSignalPolarity(short impulseSignalPolarity) {
        this.impulseSignalPolarity = impulseSignalPolarity;
    }

    public short getVibratoryPolarityCode() {
        return vibratoryPolarityCode;
    }

    public void setVibratoryPolarityCode(short vibratoryPolarityCode) {
        this.vibratoryPolarityCode = vibratoryPolarityCode;
    }

    public short getSegyFormatRevNumber() {
        return segyFormatRevNumber;
    }

    public void setSegyFormatRevNumber(short segyFormatRevNumber) {
        this.segyFormatRevNumber = segyFormatRevNumber;
    }

    public short getFixedLengthTraceFlag() {
        return fixedLengthTraceFlag;
    }

    public void setFixedLengthTraceFlag(short fixedLengthTraceFlag) {
        this.fixedLengthTraceFlag = fixedLengthTraceFlag;
    }

    public short getNumberOf3200Byte() {
        return numberOf3200Byte;
    }

    public void setNumberOf3200Byte(short numberOf3200Byte) {
        this.numberOf3200Byte = numberOf3200Byte;
    }

    @Override
    public String toString() {
        return "BinaryHeader:" +
                "\njobId                       " + jobId +
                "\nlineNumber                  " + lineNumber +
                "\nreelNumber                  " + reelNumber +
                "\ndataTracesPerEnsemble       " + dataTracesPerEnsemble +
                "\nauxiliaryTracesPerEnsemble " + auxiliaryTracesPerEnsemble +
                "\nsampleInterval              " + sampleInterval +
                "\nsampleIntervalOfOFR         " + sampleIntervalOfOFR +
                "\nsamplesPerDataTrace         " + samplesPerDataTrace +
                "\nsamplesPerDataTraceOfOFR    " + samplesPerDataTraceOfOFR +
                "\ndataSampleCode              " + dataSampleCode +
                "\nensembleFold                " + ensembleFold +
                "\ntraceSorting                " + traceSorting +
                "\nverticalSumCode             " + verticalSumCode +
                "\nsweepFrequencyAtStart       " + sweepFrequencyAtStart +
                "\nsweepFrequencyAtEnd         " + sweepFrequencyAtEnd +
                "\nsweepLength                 " + sweepLength +
                "\nsweepTypeCode               " + sweepTypeCode +
                "\ntraceNumber                 " + traceNumber +
                "\ntaperLengthAtStart          " + taperLengthAtStart +
                "\ntaperLengthAtEnd            " + taperLengthAtEnd +
                "\ntaperType                   " + taperType +
                "\ndataTracesCorrelated        " + dataTracesCorrelated +
                "\nbinaryGainRecovered         " + binaryGainRecovered +
                "\namplitudeRecoveryMethod     " + amplitudeRecoveryMethod +
                "\nmeasurementSystem           " + measurementSystem +
                "\nimpulseSignalPolarity       " + impulseSignalPolarity +
                "\nvibratoryPolarityCode       " + vibratoryPolarityCode +
                "\nsegyFormatRevNumber         " + segyFormatRevNumber +
                "\nfixedLengthTraceFlag        " + fixedLengthTraceFlag +
                "\nnumberOf3200Byte            " + numberOf3200Byte + '}';
    }
}
