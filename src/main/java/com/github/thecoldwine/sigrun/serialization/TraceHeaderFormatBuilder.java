package com.github.thecoldwine.sigrun.serialization;

@SuppressWarnings({"WeakerAccess", "UnusedDeclaration"})
public class TraceHeaderFormatBuilder {
    public FormatEntry traceSequenceNumberWLFormat;
    public FormatEntry traceSequenceNumberWSFormat;
    public FormatEntry originalFieldRecordNumberFormat;
    public FormatEntry traceNumberWOFRFormat;
    public FormatEntry energySourcePointNumberFormat;
    public FormatEntry ensembleNumberFormat;
    public FormatEntry traceNumberWEnsembleFormat;
    public FormatEntry traceIdentificationCodeFormat;
    public FormatEntry numberOfVerticallySummedTracesFormat;
    public FormatEntry numberOfHorizontallyStackedTracesFormat;
    public FormatEntry dataUseFormat;
    public FormatEntry distanceFromTheCenterOfSPFormat;
    public FormatEntry receiverGroupElevationFormat;
    public FormatEntry surfaceElevationAtSourceFormat;
    public FormatEntry sourceDepthBelowSurfaceFormat;
    public FormatEntry datumElevationAtReceiverGroupFormat;
    public FormatEntry datumElevationAtSourceFormat;
    public FormatEntry waterDepthAtSourceFormat;
    public FormatEntry waterDepthAtGroupFormat;
    public FormatEntry scalarForElevationsFormat;
    public FormatEntry scalarForCoordinatesFormat;
    public FormatEntry sourceXFormat;
    public FormatEntry sourceYFormat;
    public FormatEntry groupXFormat;
    public FormatEntry groupYFormat;
    public FormatEntry coordinateUnitsCodeFormat;
    public FormatEntry weatheringVelocityFormat;
    public FormatEntry subweatheringVelocityFormat;
    public FormatEntry upholeTimeAtSourceInMsFormat;
    public FormatEntry upholeTimeAtGroupInMsFormat;
    public FormatEntry sourceStaticCorrectionInMsFormat;
    public FormatEntry groupStaticCorrectionInMsFormat;
    public FormatEntry totalStaticAppliedInMsFormat;
    public FormatEntry lagTimeAFormat;
    public FormatEntry lagTimeBFormat;
    public FormatEntry delayRecordingTimeFormat;
    public FormatEntry muteTimeStartFormat;
    public FormatEntry muteTimeEndFormat;
    public FormatEntry numberOfSamplesFormat;
    public FormatEntry sampleIntervalInMcsFormat;
    public FormatEntry gainTypeForInstrumentsFormat;
    public FormatEntry instrumentGainConstantFormat;
    public FormatEntry instrumentEarlyOrInitialGainFormat;
    public FormatEntry correlatedFormat;
    public FormatEntry sweepFrequencyAtStartFormat;
    public FormatEntry sweepFrequencyAtEndFormat;
    public FormatEntry sweepLengthInMillisecondsFormat;
    public FormatEntry sweepTypeFormat;
    public FormatEntry sweepTraceTaperLengthAtStartInMillisecondsFormat;
    public FormatEntry sweepTraceTaperLengthAtEndInMillisecondsFormat;
    public FormatEntry taperTypeFormat;
    public FormatEntry aliasFilterFrequencyFormat;
    public FormatEntry aliasFilterSlopeFormat;
    public FormatEntry notchFilterFrequencyFormat;
    public FormatEntry notchFilterSlopeFormat;
    public FormatEntry lowCutFrequencyFormat;
    public FormatEntry highCutFrequencyFormat;
    public FormatEntry lowCutSlopeFormat;
    public FormatEntry highCutSlopeFormat;
    public FormatEntry yearDataRecordedFormat;
    public FormatEntry dayOfYearFormat;
    public FormatEntry hourOfDayFormat;
    public FormatEntry minuteOfHourFormat;
    public FormatEntry secondOfMinuteFormat;
    public FormatEntry timeBasisCodeFormat;
    public FormatEntry traceWeightingFactorFormat;
    public FormatEntry geophoneGroupNumberOfRollSwitchPositionOneFormat;
    public FormatEntry geophoneGroupNumberOfTraceNumberOneWOFRFormat;
    public FormatEntry geophoneGroupNumberOfLastTraceWOFRFormat;
    public FormatEntry gapSizeFormat;
    public FormatEntry overTravelFormat;
    public FormatEntry xOfCDPPositionFormat;
    public FormatEntry yOfCDPPositionFormat;
    public FormatEntry inLineNumberFormat;
    public FormatEntry crossLineNumberFormat;
    public FormatEntry shotpointNumberFormat;
    public FormatEntry scalarForSPNumberFormat;
    public FormatEntry traceValuesMUFormat;
    public FormatEntry transductionConstantFormat;
    public FormatEntry transductionUnitsFormat;
    public FormatEntry deviceTraceIdentifierFormat;
    public FormatEntry scalarToBeAppliedToTimesFormat;
    public FormatEntry sourceTypeOrientationFormat;
    public FormatEntry sourceEnergyDirectionFormat;
    public FormatEntry sourceMeasurementFormat;
    public FormatEntry sourceMeasurementUnitFormat;

    private TraceHeaderFormatBuilder() {
    }

    public static TraceHeaderFormatBuilder aTraceHeaderFormat() {
        return new TraceHeaderFormatBuilder();
    }

    public TraceHeaderFormatBuilder withTraceSequenceNumberWLFormat(FormatEntry traceSequenceNumberWLFormat) {
        this.traceSequenceNumberWLFormat = traceSequenceNumberWLFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withTraceSequenceNumberWSFormat(FormatEntry traceSequenceNumberWSFormat) {
        this.traceSequenceNumberWSFormat = traceSequenceNumberWSFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withOriginalFieldRecordNumberFormat(FormatEntry originalFieldRecordNumberFormat) {
        this.originalFieldRecordNumberFormat = originalFieldRecordNumberFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withTraceNumberWOFRFormat(FormatEntry traceNumberWOFRFormat) {
        this.traceNumberWOFRFormat = traceNumberWOFRFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withEnergySourcePointNumberFormat(FormatEntry energySourcePointNumberFormat) {
        this.energySourcePointNumberFormat = energySourcePointNumberFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withEnsembleNumberFormat(FormatEntry ensembleNumberFormat) {
        this.ensembleNumberFormat = ensembleNumberFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withTraceNumberWEnsembleFormat(FormatEntry traceNumberWEnsembleFormat) {
        this.traceNumberWEnsembleFormat = traceNumberWEnsembleFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withTraceIdentificationCodeFormat(FormatEntry traceIdentificationCodeFormat) {
        this.traceIdentificationCodeFormat = traceIdentificationCodeFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withNumberOfVerticallySummedTracesFormat(FormatEntry numberOfVerticallySummedTracesFormat) {
        this.numberOfVerticallySummedTracesFormat = numberOfVerticallySummedTracesFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withNumberOfHorizontallyStackedTracesFormat(FormatEntry numberOfHorizontallyStackedTracesFormat) {
        this.numberOfHorizontallyStackedTracesFormat = numberOfHorizontallyStackedTracesFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withDataUseFormat(FormatEntry dataUseFormat) {
        this.dataUseFormat = dataUseFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withDistanceFromTheCenterOfSPFormat(FormatEntry distanceFromTheCenterOfSPFormat) {
        this.distanceFromTheCenterOfSPFormat = distanceFromTheCenterOfSPFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withReceiverGroupElevationFormat(FormatEntry receiverGroupElevationFormat) {
        this.receiverGroupElevationFormat = receiverGroupElevationFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSurfaceElevationAtSourceFormat(FormatEntry surfaceElevationAtSourceFormat) {
        this.surfaceElevationAtSourceFormat = surfaceElevationAtSourceFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSourceDepthBelowSurfaceFormat(FormatEntry sourceDepthBelowSurfaceFormat) {
        this.sourceDepthBelowSurfaceFormat = sourceDepthBelowSurfaceFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withDatumElevationAtReceiverGroupFormat(FormatEntry datumElevationAtReceiverGroupFormat) {
        this.datumElevationAtReceiverGroupFormat = datumElevationAtReceiverGroupFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withDatumElevationAtSourceFormat(FormatEntry datumElevationAtSourceFormat) {
        this.datumElevationAtSourceFormat = datumElevationAtSourceFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withWaterDepthAtSourceFormat(FormatEntry waterDepthAtSourceFormat) {
        this.waterDepthAtSourceFormat = waterDepthAtSourceFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withWaterDepthAtGroupFormat(FormatEntry waterDepthAtGroupFormat) {
        this.waterDepthAtGroupFormat = waterDepthAtGroupFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withScalarForElevationsFormat(FormatEntry scalarForElevationsFormat) {
        this.scalarForElevationsFormat = scalarForElevationsFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withScalarForCoordinatesFormat(FormatEntry scalarForCoordinatesFormat) {
        this.scalarForCoordinatesFormat = scalarForCoordinatesFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSourceXFormat(FormatEntry sourceXFormat) {
        this.sourceXFormat = sourceXFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSourceYFormat(FormatEntry sourceYFormat) {
        this.sourceYFormat = sourceYFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withGroupXFormat(FormatEntry groupXFormat) {
        this.groupXFormat = groupXFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withGroupYFormat(FormatEntry groupYFormat) {
        this.groupYFormat = groupYFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withCoordinateUnitsCodeFormat(FormatEntry coordinateUnitsCodeFormat) {
        this.coordinateUnitsCodeFormat = coordinateUnitsCodeFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withWeatheringVelocityFormat(FormatEntry weatheringVelocityFormat) {
        this.weatheringVelocityFormat = weatheringVelocityFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSubweatheringVelocityFormat(FormatEntry subweatheringVelocityFormat) {
        this.subweatheringVelocityFormat = subweatheringVelocityFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withUpholeTimeAtSourceInMsFormat(FormatEntry upholeTimeAtSourceInMsFormat) {
        this.upholeTimeAtSourceInMsFormat = upholeTimeAtSourceInMsFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withUpholeTimeAtGroupInMsFormat(FormatEntry upholeTimeAtGroupInMsFormat) {
        this.upholeTimeAtGroupInMsFormat = upholeTimeAtGroupInMsFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSourceStaticCorrectionInMsFormat(FormatEntry sourceStaticCorrectionInMsFormat) {
        this.sourceStaticCorrectionInMsFormat = sourceStaticCorrectionInMsFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withGroupStaticCorrectionInMsFormat(FormatEntry groupStaticCorrectionInMsFormat) {
        this.groupStaticCorrectionInMsFormat = groupStaticCorrectionInMsFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withTotalStaticAppliedInMsFormat(FormatEntry totalStaticAppliedInMsFormat) {
        this.totalStaticAppliedInMsFormat = totalStaticAppliedInMsFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withLagTimeAFormat(FormatEntry lagTimeAFormat) {
        this.lagTimeAFormat = lagTimeAFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withLagTimeBFormat(FormatEntry lagTimeBFormat) {
        this.lagTimeBFormat = lagTimeBFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withDelayRecordingTimeFormat(FormatEntry delayRecordingTimeFormat) {
        this.delayRecordingTimeFormat = delayRecordingTimeFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withMuteTimeStartFormat(FormatEntry muteTimeStartFormat) {
        this.muteTimeStartFormat = muteTimeStartFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withMuteTimeEndFormat(FormatEntry muteTimeEndFormat) {
        this.muteTimeEndFormat = muteTimeEndFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withNumberOfSamplesFormat(FormatEntry numberOfSamplesFormat) {
        this.numberOfSamplesFormat = numberOfSamplesFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSampleIntervalInMcsFormat(FormatEntry sampleIntervalInMcsFormat) {
        this.sampleIntervalInMcsFormat = sampleIntervalInMcsFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withGainTypeForInstrumentsFormat(FormatEntry gainTypeForInstrumentsFormat) {
        this.gainTypeForInstrumentsFormat = gainTypeForInstrumentsFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withInstrumentGainConstantFormat(FormatEntry instrumentGainConstantFormat) {
        this.instrumentGainConstantFormat = instrumentGainConstantFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withInstrumentEarlyOrInitialGainFormat(FormatEntry instrumentEarlyOrInitialGainFormat) {
        this.instrumentEarlyOrInitialGainFormat = instrumentEarlyOrInitialGainFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withCorrelatedFormat(FormatEntry correlatedFormat) {
        this.correlatedFormat = correlatedFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSweepFrequencyAtStartFormat(FormatEntry sweepFrequencyAtStartFormat) {
        this.sweepFrequencyAtStartFormat = sweepFrequencyAtStartFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSweepFrequencyAtEndFormat(FormatEntry sweepFrequencyAtEndFormat) {
        this.sweepFrequencyAtEndFormat = sweepFrequencyAtEndFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSweepLengthInMillisecondsFormat(FormatEntry sweepLengthInMillisecondsFormat) {
        this.sweepLengthInMillisecondsFormat = sweepLengthInMillisecondsFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSweepTypeFormat(FormatEntry sweepTypeFormat) {
        this.sweepTypeFormat = sweepTypeFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSweepTraceTaperLengthAtStartInMillisecondsFormat(FormatEntry sweepTraceTaperLengthAtStartInMillisecondsFormat) {
        this.sweepTraceTaperLengthAtStartInMillisecondsFormat = sweepTraceTaperLengthAtStartInMillisecondsFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSweepTraceTaperLengthAtEndInMillisecondsFormat(FormatEntry sweepTraceTaperLengthAtEndInMillisecondsFormat) {
        this.sweepTraceTaperLengthAtEndInMillisecondsFormat = sweepTraceTaperLengthAtEndInMillisecondsFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withTaperTypeFormat(FormatEntry taperTypeFormat) {
        this.taperTypeFormat = taperTypeFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withAliasFilterFrequencyFormat(FormatEntry aliasFilterFrequencyFormat) {
        this.aliasFilterFrequencyFormat = aliasFilterFrequencyFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withAliasFilterSlopeFormat(FormatEntry aliasFilterSlopeFormat) {
        this.aliasFilterSlopeFormat = aliasFilterSlopeFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withNotchFilterFrequencyFormat(FormatEntry notchFilterFrequencyFormat) {
        this.notchFilterFrequencyFormat = notchFilterFrequencyFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withNotchFilterSlopeFormat(FormatEntry notchFilterSlopeFormat) {
        this.notchFilterSlopeFormat = notchFilterSlopeFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withLowCutFrequencyFormat(FormatEntry lowCutFrequencyFormat) {
        this.lowCutFrequencyFormat = lowCutFrequencyFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withHighCutFrequencyFormat(FormatEntry highCutFrequencyFormat) {
        this.highCutFrequencyFormat = highCutFrequencyFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withLowCutSlopeFormat(FormatEntry lowCutSlopeFormat) {
        this.lowCutSlopeFormat = lowCutSlopeFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withHighCutSlopeFormat(FormatEntry highCutSlopeFormat) {
        this.highCutSlopeFormat = highCutSlopeFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withYearDataRecordedFormat(FormatEntry yearDataRecordedFormat) {
        this.yearDataRecordedFormat = yearDataRecordedFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withDayOfYearFormat(FormatEntry dayOfYearFormat) {
        this.dayOfYearFormat = dayOfYearFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withHourOfDayFormat(FormatEntry hourOfDayFormat) {
        this.hourOfDayFormat = hourOfDayFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withMinuteOfHourFormat(FormatEntry minuteOfHourFormat) {
        this.minuteOfHourFormat = minuteOfHourFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSecondOfMinuteFormat(FormatEntry secondOfMinuteFormat) {
        this.secondOfMinuteFormat = secondOfMinuteFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withTimeBasisCodeFormat(FormatEntry timeBasisCodeFormat) {
        this.timeBasisCodeFormat = timeBasisCodeFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withTraceWeightingFactorFormat(FormatEntry traceWeightingFactorFormat) {
        this.traceWeightingFactorFormat = traceWeightingFactorFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withGeophoneGroupNumberOfRollSwitchPositionOneFormat(FormatEntry geophoneGroupNumberOfRollSwitchPositionOneFormat) {
        this.geophoneGroupNumberOfRollSwitchPositionOneFormat = geophoneGroupNumberOfRollSwitchPositionOneFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withGeophoneGroupNumberOfTraceNumberOneWOFRFormat(FormatEntry geophoneGroupNumberOfTraceNumberOneWOFRFormat) {
        this.geophoneGroupNumberOfTraceNumberOneWOFRFormat = geophoneGroupNumberOfTraceNumberOneWOFRFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withGeophoneGroupNumberOfLastTraceWOFRFormat(FormatEntry geophoneGroupNumberOfLastTraceWOFRFormat) {
        this.geophoneGroupNumberOfLastTraceWOFRFormat = geophoneGroupNumberOfLastTraceWOFRFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withGapSizeFormat(FormatEntry gapSizeFormat) {
        this.gapSizeFormat = gapSizeFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withOverTravelFormat(FormatEntry overTravelFormat) {
        this.overTravelFormat = overTravelFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withXOfCDPPositionFormat(FormatEntry xOfCDPPositionFormat) {
        this.xOfCDPPositionFormat = xOfCDPPositionFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withYOfCDPPositionFormat(FormatEntry yOfCDPPositionFormat) {
        this.yOfCDPPositionFormat = yOfCDPPositionFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withInLineNumberFormat(FormatEntry inLineNumberFormat) {
        this.inLineNumberFormat = inLineNumberFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withCrossLineNumberFormat(FormatEntry crossLineNumberFormat) {
        this.crossLineNumberFormat = crossLineNumberFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withShotpointNumberFormat(FormatEntry shotpointNumberFormat) {
        this.shotpointNumberFormat = shotpointNumberFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withScalarForSPNumberFormat(FormatEntry scalarForSPNumberFormat) {
        this.scalarForSPNumberFormat = scalarForSPNumberFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withTraceValuesMUFormat(FormatEntry traceValuesMUFormat) {
        this.traceValuesMUFormat = traceValuesMUFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withTransductionConstantFormat(FormatEntry transductionConstantFormat) {
        this.transductionConstantFormat = transductionConstantFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withTransductionUnitsFormat(FormatEntry transductionUnitsFormat) {
        this.transductionUnitsFormat = transductionUnitsFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withDeviceTraceIdentifierFormat(FormatEntry deviceTraceIdentifierFormat) {
        this.deviceTraceIdentifierFormat = deviceTraceIdentifierFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withScalarToBeAppliedToTimesFormat(FormatEntry scalarToBeAppliedToTimesFormat) {
        this.scalarToBeAppliedToTimesFormat = scalarToBeAppliedToTimesFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSourceTypeOrientationFormat(FormatEntry sourceTypeOrientationFormat) {
        this.sourceTypeOrientationFormat = sourceTypeOrientationFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSourceEnergyDirectionFormat(FormatEntry sourceEnergyDirectionFormat) {
        this.sourceEnergyDirectionFormat = sourceEnergyDirectionFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSourceMeasurementFormat(FormatEntry sourceMeasurementFormat) {
        this.sourceMeasurementFormat = sourceMeasurementFormat;
        return this;
    }

    public TraceHeaderFormatBuilder withSourceMeasurementUnitFormat(FormatEntry sourceMeasurementUnitFormat) {
        this.sourceMeasurementUnitFormat = sourceMeasurementUnitFormat;
        return this;
    }

    public TraceHeaderFormatBuilder but() {
        return aTraceHeaderFormat()
                .withTraceSequenceNumberWLFormat(traceSequenceNumberWLFormat)
                .withTraceSequenceNumberWSFormat(traceSequenceNumberWSFormat)
                .withOriginalFieldRecordNumberFormat(originalFieldRecordNumberFormat)
                .withTraceNumberWOFRFormat(traceNumberWOFRFormat)
                .withEnergySourcePointNumberFormat(energySourcePointNumberFormat)
                .withEnsembleNumberFormat(ensembleNumberFormat)
                .withTraceNumberWEnsembleFormat(traceNumberWEnsembleFormat)
                .withTraceIdentificationCodeFormat(traceIdentificationCodeFormat)
                .withNumberOfVerticallySummedTracesFormat(numberOfVerticallySummedTracesFormat)
                .withNumberOfHorizontallyStackedTracesFormat(numberOfHorizontallyStackedTracesFormat)
                .withDataUseFormat(dataUseFormat)
                .withDistanceFromTheCenterOfSPFormat(distanceFromTheCenterOfSPFormat)
                .withReceiverGroupElevationFormat(receiverGroupElevationFormat)
                .withSurfaceElevationAtSourceFormat(surfaceElevationAtSourceFormat)
                .withSourceDepthBelowSurfaceFormat(sourceDepthBelowSurfaceFormat)
                .withDatumElevationAtReceiverGroupFormat(datumElevationAtReceiverGroupFormat)
                .withDatumElevationAtSourceFormat(datumElevationAtSourceFormat)
                .withWaterDepthAtSourceFormat(waterDepthAtSourceFormat)
                .withWaterDepthAtGroupFormat(waterDepthAtGroupFormat)
                .withScalarForElevationsFormat(scalarForElevationsFormat)
                .withScalarForCoordinatesFormat(scalarForCoordinatesFormat)
                .withSourceXFormat(sourceXFormat)
                .withSourceYFormat(sourceYFormat)
                .withGroupXFormat(groupXFormat)
                .withGroupYFormat(groupYFormat)
                .withCoordinateUnitsCodeFormat(coordinateUnitsCodeFormat)
                .withWeatheringVelocityFormat(weatheringVelocityFormat)
                .withSubweatheringVelocityFormat(subweatheringVelocityFormat)
                .withUpholeTimeAtSourceInMsFormat(upholeTimeAtSourceInMsFormat)
                .withUpholeTimeAtGroupInMsFormat(upholeTimeAtGroupInMsFormat)
                .withSourceStaticCorrectionInMsFormat(sourceStaticCorrectionInMsFormat)
                .withGroupStaticCorrectionInMsFormat(groupStaticCorrectionInMsFormat)
                .withTotalStaticAppliedInMsFormat(totalStaticAppliedInMsFormat)
                .withLagTimeAFormat(lagTimeAFormat)
                .withLagTimeBFormat(lagTimeBFormat)
                .withDelayRecordingTimeFormat(delayRecordingTimeFormat)
                .withMuteTimeStartFormat(muteTimeStartFormat)
                .withMuteTimeEndFormat(muteTimeEndFormat)
                .withNumberOfSamplesFormat(numberOfSamplesFormat)
                .withSampleIntervalInMcsFormat(sampleIntervalInMcsFormat)
                .withGainTypeForInstrumentsFormat(gainTypeForInstrumentsFormat)
                .withInstrumentGainConstantFormat(instrumentGainConstantFormat)
                .withInstrumentEarlyOrInitialGainFormat(instrumentEarlyOrInitialGainFormat)
                .withCorrelatedFormat(correlatedFormat)
                .withSweepFrequencyAtStartFormat(sweepFrequencyAtStartFormat)
                .withSweepFrequencyAtEndFormat(sweepFrequencyAtEndFormat)
                .withSweepLengthInMillisecondsFormat(sweepLengthInMillisecondsFormat)
                .withSweepTypeFormat(sweepTypeFormat)
                .withSweepTraceTaperLengthAtStartInMillisecondsFormat(sweepTraceTaperLengthAtStartInMillisecondsFormat)
                .withSweepTraceTaperLengthAtEndInMillisecondsFormat(sweepTraceTaperLengthAtEndInMillisecondsFormat)
                .withTaperTypeFormat(taperTypeFormat)
                .withAliasFilterFrequencyFormat(aliasFilterFrequencyFormat)
                .withAliasFilterSlopeFormat(aliasFilterSlopeFormat)
                .withNotchFilterFrequencyFormat(notchFilterFrequencyFormat)
                .withNotchFilterSlopeFormat(notchFilterSlopeFormat)
                .withLowCutFrequencyFormat(lowCutFrequencyFormat)
                .withHighCutFrequencyFormat(highCutFrequencyFormat)
                .withLowCutSlopeFormat(lowCutSlopeFormat)
                .withHighCutSlopeFormat(highCutSlopeFormat)
                .withYearDataRecordedFormat(yearDataRecordedFormat)
                .withDayOfYearFormat(dayOfYearFormat)
                .withHourOfDayFormat(hourOfDayFormat)
                .withMinuteOfHourFormat(minuteOfHourFormat)
                .withSecondOfMinuteFormat(secondOfMinuteFormat)
                .withTimeBasisCodeFormat(timeBasisCodeFormat)
                .withTraceWeightingFactorFormat(traceWeightingFactorFormat)
                .withGeophoneGroupNumberOfRollSwitchPositionOneFormat(geophoneGroupNumberOfRollSwitchPositionOneFormat)
                .withGeophoneGroupNumberOfTraceNumberOneWOFRFormat(geophoneGroupNumberOfTraceNumberOneWOFRFormat)
                .withGeophoneGroupNumberOfLastTraceWOFRFormat(geophoneGroupNumberOfLastTraceWOFRFormat)
                .withGapSizeFormat(gapSizeFormat)
                .withOverTravelFormat(overTravelFormat)
                .withXOfCDPPositionFormat(xOfCDPPositionFormat)
                .withYOfCDPPositionFormat(yOfCDPPositionFormat)
                .withInLineNumberFormat(inLineNumberFormat)
                .withCrossLineNumberFormat(crossLineNumberFormat)
                .withShotpointNumberFormat(shotpointNumberFormat)
                .withScalarForSPNumberFormat(scalarForSPNumberFormat)
                .withTraceValuesMUFormat(traceValuesMUFormat)
                .withTransductionConstantFormat(transductionConstantFormat)
                .withTransductionUnitsFormat(transductionUnitsFormat)
                .withDeviceTraceIdentifierFormat(deviceTraceIdentifierFormat)
                .withScalarToBeAppliedToTimesFormat(scalarToBeAppliedToTimesFormat)
                .withSourceTypeOrientationFormat(sourceTypeOrientationFormat)
                .withSourceEnergyDirectionFormat(sourceEnergyDirectionFormat)
                .withSourceMeasurementFormat(sourceMeasurementFormat)
                .withSourceMeasurementUnitFormat(sourceMeasurementUnitFormat);
    }

    public TraceHeaderFormat build() {
        TraceHeaderFormat traceHeaderFormat = new TraceHeaderFormat(traceSequenceNumberWLFormat,
                traceSequenceNumberWSFormat,
                originalFieldRecordNumberFormat,
                traceNumberWOFRFormat,
                energySourcePointNumberFormat,
                ensembleNumberFormat,
                traceNumberWEnsembleFormat,
                traceIdentificationCodeFormat,
                numberOfVerticallySummedTracesFormat,
                numberOfHorizontallyStackedTracesFormat,
                dataUseFormat,
                distanceFromTheCenterOfSPFormat,
                receiverGroupElevationFormat,
                surfaceElevationAtSourceFormat,
                sourceDepthBelowSurfaceFormat,
                datumElevationAtReceiverGroupFormat,
                datumElevationAtSourceFormat,
                waterDepthAtSourceFormat,
                waterDepthAtGroupFormat,
                scalarForElevationsFormat,
                scalarForCoordinatesFormat,
                sourceXFormat,
                sourceYFormat,
                groupXFormat,
                groupYFormat,
                coordinateUnitsCodeFormat,
                weatheringVelocityFormat,
                subweatheringVelocityFormat,
                upholeTimeAtSourceInMsFormat,
                upholeTimeAtGroupInMsFormat,
                sourceStaticCorrectionInMsFormat,
                groupStaticCorrectionInMsFormat,
                totalStaticAppliedInMsFormat,
                lagTimeAFormat,
                lagTimeBFormat,
                delayRecordingTimeFormat,
                muteTimeStartFormat,
                muteTimeEndFormat,
                numberOfSamplesFormat,
                sampleIntervalInMcsFormat,
                gainTypeForInstrumentsFormat,
                instrumentGainConstantFormat,
                instrumentEarlyOrInitialGainFormat,
                correlatedFormat,
                sweepFrequencyAtStartFormat,
                sweepFrequencyAtEndFormat,
                sweepLengthInMillisecondsFormat,
                sweepTypeFormat,
                sweepTraceTaperLengthAtStartInMillisecondsFormat,
                sweepTraceTaperLengthAtEndInMillisecondsFormat,
                taperTypeFormat,
                aliasFilterFrequencyFormat,
                aliasFilterSlopeFormat,
                notchFilterFrequencyFormat,
                notchFilterSlopeFormat,
                lowCutFrequencyFormat,
                highCutFrequencyFormat,
                lowCutSlopeFormat,
                highCutSlopeFormat,
                yearDataRecordedFormat,
                dayOfYearFormat,
                hourOfDayFormat,
                minuteOfHourFormat,
                secondOfMinuteFormat,
                timeBasisCodeFormat,
                traceWeightingFactorFormat,
                geophoneGroupNumberOfRollSwitchPositionOneFormat,
                geophoneGroupNumberOfTraceNumberOneWOFRFormat,
                geophoneGroupNumberOfLastTraceWOFRFormat,
                gapSizeFormat,
                overTravelFormat,
                xOfCDPPositionFormat,
                yOfCDPPositionFormat,
                inLineNumberFormat,
                crossLineNumberFormat,
                shotpointNumberFormat,
                scalarForSPNumberFormat,
                traceValuesMUFormat,
                transductionConstantFormat,
                transductionUnitsFormat,
                deviceTraceIdentifierFormat,
                scalarToBeAppliedToTimesFormat,
                sourceTypeOrientationFormat,
                sourceEnergyDirectionFormat,
                sourceMeasurementFormat,
                sourceMeasurementUnitFormat);

        traceHeaderFormat.validate();

        return traceHeaderFormat;
    }
}
