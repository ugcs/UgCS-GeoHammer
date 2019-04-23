package com.github.thecoldwine.sigrun.serialization;

public class TraceHeaderFormat {
	public final FormatEntry traceSequenceNumberWLFormat;
	public final FormatEntry traceSequenceNumberWSFormat;
	public final FormatEntry originalFieldRecordNumberFormat;
	public final FormatEntry traceNumberWOFRFormat;
	public final FormatEntry energySourcePointNumberFormat;
	public final FormatEntry ensembleNumberFormat;
	public final FormatEntry traceNumberWEnsembleFormat;
	public final FormatEntry traceIdentificationCodeFormat;
	public final FormatEntry numberOfVerticallySummedTracesFormat;
	public final FormatEntry numberOfHorizontallyStackedTracesFormat;
	public final FormatEntry dataUseFormat;
	public final FormatEntry distanceFromTheCenterOfSPFormat;
	public final FormatEntry receiverGroupElevationFormat;
	public final FormatEntry surfaceElevationAtSourceFormat;
	public final FormatEntry sourceDepthBelowSurfaceFormat;
	public final FormatEntry datumElevationAtReceiverGroupFormat;
	public final FormatEntry datumElevationAtSourceFormat;
	public final FormatEntry waterDepthAtSourceFormat;
	public final FormatEntry waterDepthAtGroupFormat;
	public final FormatEntry scalarForElevationsFormat;
	public final FormatEntry scalarForCoordinatesFormat;
	public final FormatEntry sourceXFormat;
	public final FormatEntry sourceYFormat;
	public final FormatEntry groupXFormat;
	public final FormatEntry groupYFormat;
	public final FormatEntry coordinateUnitsCodeFormat;
	public final FormatEntry weatheringVelocityFormat;
	public final FormatEntry subweatheringVelocityFormat;
	public final FormatEntry upholeTimeAtSourceInMsFormat;
	public final FormatEntry upholeTimeAtGroupInMsFormat;
	public final FormatEntry sourceStaticCorrectionInMsFormat;
	public final FormatEntry groupStaticCorrectionInMsFormat;
	public final FormatEntry totalStaticAppliedInMsFormat;
	public final FormatEntry lagTimeAFormat;
	public final FormatEntry lagTimeBFormat;
	public final FormatEntry delayRecordingTimeFormat;
	public final FormatEntry muteTimeStartFormat;
	public final FormatEntry muteTimeEndFormat;
	public final FormatEntry numberOfSamplesFormat;
	public final FormatEntry sampleIntervalInMcsFormat;
	public final FormatEntry gainTypeForInstrumentsFormat;
	public final FormatEntry instrumentGainConstantFormat;
	public final FormatEntry instrumentEarlyOrInitialGainFormat;
	public final FormatEntry correlatedFormat;
	public final FormatEntry sweepFrequencyAtStartFormat;
	public final FormatEntry sweepFrequencyAtEndFormat;
	public final FormatEntry sweepLengthInMillisecondsFormat;
	public final FormatEntry sweepTypeFormat;
	public final FormatEntry sweepTraceTaperLengthAtStartInMillisecondsFormat;
	public final FormatEntry sweepTraceTaperLengthAtEndInMillisecondsFormat;
	public final FormatEntry taperTypeFormat;
	public final FormatEntry aliasFilterFrequencyFormat;
	public final FormatEntry aliasFilterSlopeFormat;
	public final FormatEntry notchFilterFrequencyFormat;
	public final FormatEntry notchFilterSlopeFormat;
	public final FormatEntry lowCutFrequencyFormat;
	public final FormatEntry highCutFrequencyFormat;
	public final FormatEntry lowCutSlopeFormat;
	public final FormatEntry highCutSlopeFormat;
	public final FormatEntry yearDataRecordedFormat;
	public final FormatEntry dayOfYearFormat;
	public final FormatEntry hourOfDayFormat;
	public final FormatEntry minuteOfHourFormat;
	public final FormatEntry secondOfMinuteFormat;
	public final FormatEntry timeBasisCodeFormat;
	public final FormatEntry traceWeightingFactorFormat;
	public final FormatEntry geophoneGroupNumberOfRollSwitchPositionOneFormat;
	public final FormatEntry geophoneGroupNumberOfTraceNumberOneWOFRFormat;
	public final FormatEntry geophoneGroupNumberOfLastTraceWOFRFormat;
	public final FormatEntry gapSizeFormat;
	public final FormatEntry overTravelFormat;
	public final FormatEntry xOfCDPPositionFormat;
	public final FormatEntry yOfCDPPositionFormat;
	public final FormatEntry inLineNumberFormat;
	public final FormatEntry crossLineNumberFormat;
	public final FormatEntry shotpointNumberFormat;
	public final FormatEntry scalarForSPNumberFormat;
	public final FormatEntry traceValuesMUFormat;
	public final FormatEntry transductionConstantFormat;
	public final FormatEntry transductionUnitsFormat;
	public final FormatEntry deviceTraceIdentifierFormat;
	public final FormatEntry scalarToBeAppliedToTimesFormat;
	public final FormatEntry sourceTypeOrientationFormat;
	public final FormatEntry sourceEnergyDirectionFormat;
	public final FormatEntry sourceMeasurementFormat;
	public final FormatEntry sourceMeasurementUnitFormat;

	public TraceHeaderFormat(FormatEntry traceSequenceNumberWLFormat, FormatEntry traceSequenceNumberWSFormat,
			FormatEntry originalFieldRecordNumberFormat, FormatEntry traceNumberWOFRFormat,
			FormatEntry energySourcePointNumberFormat, FormatEntry ensembleNumberFormat,
			FormatEntry traceNumberWEnsembleFormat, FormatEntry traceIdentificationCodeFormat,
			FormatEntry numberOfVerticallySummedTracesFormat, FormatEntry numberOfHorizontallyStackedTracesFormat,
			FormatEntry dataUseFormat, FormatEntry distanceFromTheCenterOfSPFormat,
			FormatEntry receiverGroupElevationFormat, FormatEntry surfaceElevationAtSourceFormat,
			FormatEntry sourceDepthBelowSurfaceFormat, FormatEntry datumElevationAtReceiverGroupFormat,
			FormatEntry datumElevationAtSourceFormat, FormatEntry waterDepthAtSourceFormat,
			FormatEntry waterDepthAtGroupFormat, FormatEntry scalarForElevationsFormat,
			FormatEntry scalarForCoordinatesFormat, FormatEntry sourceXFormat, FormatEntry sourceYFormat,
			FormatEntry groupXFormat, FormatEntry groupYFormat, FormatEntry coordinateUnitsCodeFormat,
			FormatEntry weatheringVelocityFormat, FormatEntry subweatheringVelocityFormat,
			FormatEntry upholeTimeAtSourceInMsFormat, FormatEntry upholeTimeAtGroupInMsFormat,
			FormatEntry sourceStaticCorrectionInMsFormat, FormatEntry groupStaticCorrectionInMsFormat,
			FormatEntry totalStaticAppliedInMsFormat, FormatEntry lagTimeAFormat, FormatEntry lagTimeBFormat,
			FormatEntry delayRecordingTimeFormat, FormatEntry muteTimeStartFormat, FormatEntry muteTimeEndFormat,
			FormatEntry numberOfSamplesFormat, FormatEntry sampleIntervalInMcsFormat,
			FormatEntry gainTypeForInstrumentsFormat, FormatEntry instrumentGainConstantFormat,
			FormatEntry instrumentEarlyOrInitialGainFormat, FormatEntry correlatedFormat,
			FormatEntry sweepFrequencyAtStartFormat, FormatEntry sweepFrequencyAtEndFormat,
			FormatEntry sweepLengthInMillisecondsFormat, FormatEntry sweepTypeFormat,
			FormatEntry sweepTraceTaperLengthAtStartInMillisecondsFormat,
			FormatEntry sweepTraceTaperLengthAtEndInMillisecondsFormat, FormatEntry taperTypeFormat,
			FormatEntry aliasFilterFrequencyFormat, FormatEntry aliasFilterSlopeFormat,
			FormatEntry notchFilterFrequencyFormat, FormatEntry notchFilterSlopeFormat,
			FormatEntry lowCutFrequencyFormat, FormatEntry highCutFrequencyFormat, FormatEntry lowCutSlopeFormat,
			FormatEntry highCutSlopeFormat, FormatEntry yearDataRecordedFormat, FormatEntry dayOfYearFormat,
			FormatEntry hourOfDayFormat, FormatEntry minuteOfHourFormat, FormatEntry secondOfMinuteFormat,
			FormatEntry timeBasisCodeFormat, FormatEntry traceWeightingFactorFormat,
			FormatEntry geophoneGroupNumberOfRollSwitchPositionOneFormat,
			FormatEntry geophoneGroupNumberOfTraceNumberOneWOFRFormat,
			FormatEntry geophoneGroupNumberOfLastTraceWOFRFormat, FormatEntry gapSizeFormat,
			FormatEntry overTravelFormat, FormatEntry xOfCDPPositionFormat, FormatEntry yOfCDPPositionFormat,
			FormatEntry inLineNumberFormat, FormatEntry crossLineNumberFormat, FormatEntry shotpointNumberFormat,
			FormatEntry scalarForSPNumberFormat, FormatEntry traceValuesMUFormat,
			FormatEntry transductionConstantFormat, FormatEntry transductionUnitsFormat,
			FormatEntry deviceTraceIdentifierFormat, FormatEntry scalarToBeAppliedToTimesFormat,
			FormatEntry sourceTypeOrientationFormat, FormatEntry sourceEnergyDirectionFormat,
			FormatEntry sourceMeasurementFormat, FormatEntry sourceMeasurementUnitFormat) {
		this.traceSequenceNumberWLFormat = traceSequenceNumberWLFormat;
		this.traceSequenceNumberWSFormat = traceSequenceNumberWSFormat;
		this.originalFieldRecordNumberFormat = originalFieldRecordNumberFormat;
		this.traceNumberWOFRFormat = traceNumberWOFRFormat;
		this.energySourcePointNumberFormat = energySourcePointNumberFormat;
		this.ensembleNumberFormat = ensembleNumberFormat;
		this.traceNumberWEnsembleFormat = traceNumberWEnsembleFormat;
		this.traceIdentificationCodeFormat = traceIdentificationCodeFormat;
		this.numberOfVerticallySummedTracesFormat = numberOfVerticallySummedTracesFormat;
		this.numberOfHorizontallyStackedTracesFormat = numberOfHorizontallyStackedTracesFormat;
		this.dataUseFormat = dataUseFormat;
		this.distanceFromTheCenterOfSPFormat = distanceFromTheCenterOfSPFormat;
		this.receiverGroupElevationFormat = receiverGroupElevationFormat;
		this.surfaceElevationAtSourceFormat = surfaceElevationAtSourceFormat;
		this.sourceDepthBelowSurfaceFormat = sourceDepthBelowSurfaceFormat;
		this.datumElevationAtReceiverGroupFormat = datumElevationAtReceiverGroupFormat;
		this.datumElevationAtSourceFormat = datumElevationAtSourceFormat;
		this.waterDepthAtSourceFormat = waterDepthAtSourceFormat;
		this.waterDepthAtGroupFormat = waterDepthAtGroupFormat;
		this.scalarForElevationsFormat = scalarForElevationsFormat;
		this.scalarForCoordinatesFormat = scalarForCoordinatesFormat;
		this.sourceXFormat = sourceXFormat;
		this.sourceYFormat = sourceYFormat;
		this.groupXFormat = groupXFormat;
		this.groupYFormat = groupYFormat;
		this.coordinateUnitsCodeFormat = coordinateUnitsCodeFormat;
		this.weatheringVelocityFormat = weatheringVelocityFormat;
		this.subweatheringVelocityFormat = subweatheringVelocityFormat;
		this.upholeTimeAtSourceInMsFormat = upholeTimeAtSourceInMsFormat;
		this.upholeTimeAtGroupInMsFormat = upholeTimeAtGroupInMsFormat;
		this.sourceStaticCorrectionInMsFormat = sourceStaticCorrectionInMsFormat;
		this.groupStaticCorrectionInMsFormat = groupStaticCorrectionInMsFormat;
		this.totalStaticAppliedInMsFormat = totalStaticAppliedInMsFormat;
		this.lagTimeAFormat = lagTimeAFormat;
		this.lagTimeBFormat = lagTimeBFormat;
		this.delayRecordingTimeFormat = delayRecordingTimeFormat;
		this.muteTimeStartFormat = muteTimeStartFormat;
		this.muteTimeEndFormat = muteTimeEndFormat;
		this.numberOfSamplesFormat = numberOfSamplesFormat;
		this.sampleIntervalInMcsFormat = sampleIntervalInMcsFormat;
		this.gainTypeForInstrumentsFormat = gainTypeForInstrumentsFormat;
		this.instrumentGainConstantFormat = instrumentGainConstantFormat;
		this.instrumentEarlyOrInitialGainFormat = instrumentEarlyOrInitialGainFormat;
		this.correlatedFormat = correlatedFormat;
		this.sweepFrequencyAtStartFormat = sweepFrequencyAtStartFormat;
		this.sweepFrequencyAtEndFormat = sweepFrequencyAtEndFormat;
		this.sweepLengthInMillisecondsFormat = sweepLengthInMillisecondsFormat;
		this.sweepTypeFormat = sweepTypeFormat;
		this.sweepTraceTaperLengthAtStartInMillisecondsFormat = sweepTraceTaperLengthAtStartInMillisecondsFormat;
		this.sweepTraceTaperLengthAtEndInMillisecondsFormat = sweepTraceTaperLengthAtEndInMillisecondsFormat;
		this.taperTypeFormat = taperTypeFormat;
		this.aliasFilterFrequencyFormat = aliasFilterFrequencyFormat;
		this.aliasFilterSlopeFormat = aliasFilterSlopeFormat;
		this.notchFilterFrequencyFormat = notchFilterFrequencyFormat;
		this.notchFilterSlopeFormat = notchFilterSlopeFormat;
		this.lowCutFrequencyFormat = lowCutFrequencyFormat;
		this.highCutFrequencyFormat = highCutFrequencyFormat;
		this.lowCutSlopeFormat = lowCutSlopeFormat;
		this.highCutSlopeFormat = highCutSlopeFormat;
		this.yearDataRecordedFormat = yearDataRecordedFormat;
		this.dayOfYearFormat = dayOfYearFormat;
		this.hourOfDayFormat = hourOfDayFormat;
		this.minuteOfHourFormat = minuteOfHourFormat;
		this.secondOfMinuteFormat = secondOfMinuteFormat;
		this.timeBasisCodeFormat = timeBasisCodeFormat;
		this.traceWeightingFactorFormat = traceWeightingFactorFormat;
		this.geophoneGroupNumberOfRollSwitchPositionOneFormat = geophoneGroupNumberOfRollSwitchPositionOneFormat;
		this.geophoneGroupNumberOfTraceNumberOneWOFRFormat = geophoneGroupNumberOfTraceNumberOneWOFRFormat;
		this.geophoneGroupNumberOfLastTraceWOFRFormat = geophoneGroupNumberOfLastTraceWOFRFormat;
		this.gapSizeFormat = gapSizeFormat;
		this.overTravelFormat = overTravelFormat;
		this.xOfCDPPositionFormat = xOfCDPPositionFormat;
		this.yOfCDPPositionFormat = yOfCDPPositionFormat;
		this.inLineNumberFormat = inLineNumberFormat;
		this.crossLineNumberFormat = crossLineNumberFormat;
		this.shotpointNumberFormat = shotpointNumberFormat;
		this.scalarForSPNumberFormat = scalarForSPNumberFormat;
		this.traceValuesMUFormat = traceValuesMUFormat;
		this.transductionConstantFormat = transductionConstantFormat;
		this.transductionUnitsFormat = transductionUnitsFormat;
		this.deviceTraceIdentifierFormat = deviceTraceIdentifierFormat;
		this.scalarToBeAppliedToTimesFormat = scalarToBeAppliedToTimesFormat;
		this.sourceTypeOrientationFormat = sourceTypeOrientationFormat;
		this.sourceEnergyDirectionFormat = sourceEnergyDirectionFormat;
		this.sourceMeasurementFormat = sourceMeasurementFormat;
		this.sourceMeasurementUnitFormat = sourceMeasurementUnitFormat;
	}

	public void validate() {
		final FormatEntry[] mandatoryFields = { numberOfSamplesFormat };

		for (FormatEntry e : mandatoryFields) {
			if (e == null) {
				throw new SEGYFormatException("Missing one of mandatory fields in trace header format");
			}
		}
	}
}
