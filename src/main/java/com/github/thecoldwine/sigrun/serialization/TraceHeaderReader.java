package com.github.thecoldwine.sigrun.serialization;

import com.github.thecoldwine.sigrun.common.CoordinateUnitsCode;
import com.github.thecoldwine.sigrun.common.TraceHeader;
import com.github.thecoldwine.sigrun.common.TraceIdentificationCode;
import com.github.thecoldwine.sigrun.converters.ByteANumberConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.thecoldwine.sigrun.common.GainTypeForInstruments;

import java.util.Arrays;

@SuppressWarnings("ConstantConditions")
public class TraceHeaderReader {
    private final static Logger log = LoggerFactory.getLogger(TraceHeaderReader.class.getName());

    @SuppressWarnings("WeakerAccess")
    public final TraceHeaderFormat format;

    public TraceHeaderReader(TraceHeaderFormat format) {
        this.format = format;
    }

    public TraceHeader read(byte[] buffer) {
        TraceHeader traceHeader = new TraceHeader();

        if (format.traceSequenceNumberWLFormat != null)
            traceHeader.setTraceSequenceNumberWL(ByteANumberConverter.byteAToInt(buffer, format.traceSequenceNumberWLFormat.posStart));

        if (format.traceSequenceNumberWSFormat != null)
            traceHeader.setTraceSequenceNumberWS(ByteANumberConverter.byteAToInt(buffer, format.traceSequenceNumberWSFormat.posStart));

        if (format.originalFieldRecordNumberFormat != null)
            traceHeader.setOriginalFieldRecordNumber(ByteANumberConverter.byteAToInt(buffer, format.originalFieldRecordNumberFormat.posStart));

        if (format.traceNumberWOFRFormat != null)
            traceHeader.setTraceNumberWOFR(ByteANumberConverter.byteAToInt(buffer, format.traceNumberWOFRFormat.posStart));

        if (format.energySourcePointNumberFormat != null)
            traceHeader.setEnergySourcePointNumber(ByteANumberConverter.byteAToInt(buffer, format.energySourcePointNumberFormat.posStart));

        if (format.ensembleNumberFormat != null)
            traceHeader.setEnsembleNumber(ByteANumberConverter.byteAToInt(buffer, format.ensembleNumberFormat.posStart));

        if (format.traceNumberWEnsembleFormat != null)
            traceHeader.setTraceNumberWEnsemble(ByteANumberConverter.byteAToInt(buffer, format.traceNumberWEnsembleFormat.posStart));

        if (format.traceIdentificationCodeFormat != null)
            traceHeader.setTraceIdentificationCode(TraceIdentificationCode.create(ByteANumberConverter.byteAToShort(buffer, format.traceIdentificationCodeFormat.posStart)));

        if (format.numberOfVerticallySummedTracesFormat != null)
            traceHeader.setNumberOfVerticallySummedTraces(ByteANumberConverter.byteAToShort(buffer, format.numberOfVerticallySummedTracesFormat.posStart));

        if (format.numberOfHorizontallyStackedTracesFormat != null)
            traceHeader.setNumberOfHorizontallyStackedTraces(ByteANumberConverter.byteAToShort(buffer, format.numberOfHorizontallyStackedTracesFormat.posStart));

        if (format.dataUseFormat != null)
            traceHeader.setDataUse(ByteANumberConverter.byteAToShort(buffer, format.dataUseFormat.posStart));

        if (format.distanceFromTheCenterOfSPFormat != null)
            traceHeader.setDistanceFromTheCenterOfSP(ByteANumberConverter.byteAToInt(buffer, format.distanceFromTheCenterOfSPFormat.posStart));

        if (format.receiverGroupElevationFormat != null)
            traceHeader.setReceiverGroupElevation(ByteANumberConverter.byteAToInt(buffer, format.receiverGroupElevationFormat.posStart));

        if (format.surfaceElevationAtSourceFormat != null)
            traceHeader.setSurfaceElevationAtSource(ByteANumberConverter.byteAToInt(buffer, format.surfaceElevationAtSourceFormat.posStart));

        if (format.sourceDepthBelowSurfaceFormat != null)
            traceHeader.setSourceDepthBelowSurface(ByteANumberConverter.byteAToInt(buffer, format.sourceDepthBelowSurfaceFormat.posStart));

        if (format.datumElevationAtReceiverGroupFormat != null)
            traceHeader.setDatumElevationAtReceiverGroup(ByteANumberConverter.byteAToInt(buffer, format.datumElevationAtReceiverGroupFormat.posStart));

        if (format.datumElevationAtSourceFormat != null)
            traceHeader.setDatumElevationAtSource(ByteANumberConverter.byteAToInt(buffer, format.datumElevationAtSourceFormat.posStart));

        if (format.waterDepthAtSourceFormat != null)
            traceHeader.setWaterDepthAtSource(ByteANumberConverter.byteAToInt(buffer, format.waterDepthAtSourceFormat.posStart));

        if (format.waterDepthAtGroupFormat != null)
            traceHeader.setWaterDepthAtGroup(ByteANumberConverter.byteAToInt(buffer, format.waterDepthAtGroupFormat.posStart));

        if (format.scalarForElevationsFormat != null)
            traceHeader.setScalarForElevations(ByteANumberConverter.byteAToShort(buffer, format.scalarForElevationsFormat.posStart));

        if (format.scalarForCoordinatesFormat != null)
            traceHeader.setScalarForCoordinates(ByteANumberConverter.byteAToShort(buffer, format.scalarForCoordinatesFormat.posStart));

//        if (format.sourceXFormat != null)
//            traceHeader.setSourceX(ByteANumberConverter.byteAToInt(buffer, format.sourceXFormat.posStart));
//
//        if (format.sourceYFormat != null)
//            traceHeader.setSourceY(ByteANumberConverter.byteAToInt(buffer, format.sourceYFormat.posStart));
        if (format.sourceXFormat != null)
            traceHeader.setSourceX(ByteANumberConverter.byteAToFloatIEEE754(buffer, format.sourceXFormat.posStart));

        if (format.sourceYFormat != null)
            traceHeader.setSourceY(ByteANumberConverter.byteAToFloatIEEE754(buffer, format.sourceYFormat.posStart));

        
        if (format.groupXFormat != null)
            traceHeader.setGroupX(ByteANumberConverter.byteAToInt(buffer, format.groupXFormat.posStart));

        if (format.groupYFormat != null)
            traceHeader.setGroupY(ByteANumberConverter.byteAToInt(buffer, format.groupYFormat.posStart));

        if (format.coordinateUnitsCodeFormat != null){
            traceHeader.setCoordinateUnitsCode(CoordinateUnitsCode.create(ByteANumberConverter.byteAToShortBigEndian(buffer, format.coordinateUnitsCodeFormat.posStart)));
        }

        if (format.weatheringVelocityFormat != null)
            traceHeader.setWeatheringVelocity(ByteANumberConverter.byteAToShort(buffer, format.weatheringVelocityFormat.posStart));

        if (format.subweatheringVelocityFormat != null)
            traceHeader.setSubweatheringVelocity(ByteANumberConverter.byteAToShort(buffer, format.subweatheringVelocityFormat.posStart));

        if (format.upholeTimeAtSourceInMsFormat != null)
            traceHeader.setUpholeTimeAtSourceInMs(ByteANumberConverter.byteAToShort(buffer, format.upholeTimeAtSourceInMsFormat.posStart));

        if (format.upholeTimeAtGroupInMsFormat != null)
            traceHeader.setUpholeTimeAtGroupInMs(ByteANumberConverter.byteAToShort(buffer, format.upholeTimeAtGroupInMsFormat.posStart));

        if (format.sourceStaticCorrectionInMsFormat != null)
            traceHeader.setSourceStaticCorrectionInMs(ByteANumberConverter.byteAToShort(buffer, format.sourceStaticCorrectionInMsFormat.posStart));

        if (format.groupStaticCorrectionInMsFormat != null)
            traceHeader.setGroupStaticCorrectionInMs(ByteANumberConverter.byteAToShort(buffer, format.groupStaticCorrectionInMsFormat.posStart));

        if (format.totalStaticAppliedInMsFormat != null)
            traceHeader.setTotalStaticAppliedInMs(ByteANumberConverter.byteAToShort(buffer, format.totalStaticAppliedInMsFormat.posStart));

        if (format.lagTimeAFormat != null)
            traceHeader.setLagTimeA(ByteANumberConverter.byteAToShort(buffer, format.lagTimeAFormat.posStart));

        if (format.lagTimeBFormat != null)
            traceHeader.setLagTimeB(ByteANumberConverter.byteAToShort(buffer, format.lagTimeBFormat.posStart));

        if (format.delayRecordingTimeFormat != null)
            traceHeader.setDelayRecordingTime(ByteANumberConverter.byteAToShort(buffer, format.delayRecordingTimeFormat.posStart));

        if (format.muteTimeStartFormat != null)
            traceHeader.setMuteTimeStart(ByteANumberConverter.byteAToShort(buffer, format.muteTimeStartFormat.posStart));

        if (format.muteTimeEndFormat != null)
            traceHeader.setMuteTimeEnd(ByteANumberConverter.byteAToShort(buffer, format.muteTimeEndFormat.posStart));

        if (format.numberOfSamplesFormat != null)
            traceHeader.setNumberOfSamples(ByteANumberConverter.byteAToShort(buffer, format.numberOfSamplesFormat.posStart));

        if (format.sampleIntervalInMcsFormat != null)
            traceHeader.setSampleIntervalInMcs(ByteANumberConverter.byteAToShort(buffer, format.sampleIntervalInMcsFormat.posStart));

        if (format.gainTypeForInstrumentsFormat != null)
            traceHeader.setGainTypeForInstruments(GainTypeForInstruments.create(ByteANumberConverter.byteAToShort(buffer, format.gainTypeForInstrumentsFormat.posStart)));

        if (format.instrumentGainConstantFormat != null)
            traceHeader.setInstrumentGainConstant(ByteANumberConverter.byteAToShort(buffer, format.instrumentGainConstantFormat.posStart));

        if (format.instrumentEarlyOrInitialGainFormat != null)
            traceHeader.setInstrumentEarlyOrInitialGain(ByteANumberConverter.byteAToShort(buffer, format.instrumentEarlyOrInitialGainFormat.posStart));

        if (format.correlatedFormat != null)
            traceHeader.setCorrelated(ByteANumberConverter.byteAToShort(buffer, format.correlatedFormat.posStart));

        if (format.sweepFrequencyAtStartFormat != null)
            traceHeader.setSweepFrequencyAtStart(ByteANumberConverter.byteAToShort(buffer, format.sweepFrequencyAtStartFormat.posStart));

        if (format.sweepFrequencyAtEndFormat != null)
            traceHeader.setSweepFrequencyAtEnd(ByteANumberConverter.byteAToShort(buffer, format.sweepFrequencyAtEndFormat.posStart));

        if (format.sweepLengthInMillisecondsFormat != null)
            traceHeader.setSweepLengthInMilliseconds(ByteANumberConverter.byteAToShort(buffer, format.sweepLengthInMillisecondsFormat.posStart));

        if (format.sweepTypeFormat != null)
            traceHeader.setSweepType(ByteANumberConverter.byteAToShort(buffer, format.sweepTypeFormat.posStart));

        if (format.sweepTraceTaperLengthAtStartInMillisecondsFormat != null)
            traceHeader.setSweepTraceTaperLengthAtStartInMilliseconds(ByteANumberConverter.byteAToShort(buffer, format.sweepTraceTaperLengthAtStartInMillisecondsFormat.posStart));

        if (format.sweepTraceTaperLengthAtEndInMillisecondsFormat != null)
            traceHeader.setSweepTraceTaperLengthAtEndInMilliseconds(ByteANumberConverter.byteAToShort(buffer, format.sweepTraceTaperLengthAtEndInMillisecondsFormat.posStart));

        if (format.taperTypeFormat != null)
            traceHeader.setTaperType(ByteANumberConverter.byteAToShort(buffer, format.taperTypeFormat.posStart));

        if (format.aliasFilterFrequencyFormat != null)
            traceHeader.setAliasFilterFrequency(ByteANumberConverter.byteAToShort(buffer, format.aliasFilterFrequencyFormat.posStart));

        if (format.aliasFilterSlopeFormat != null)
            traceHeader.setAliasFilterSlope(ByteANumberConverter.byteAToShort(buffer, format.aliasFilterSlopeFormat.posStart));

        if (format.notchFilterFrequencyFormat != null)
            traceHeader.setNotchFilterFrequency(ByteANumberConverter.byteAToShort(buffer, format.notchFilterFrequencyFormat.posStart));

        if (format.notchFilterSlopeFormat != null)
            traceHeader.setNotchFilterSlope(ByteANumberConverter.byteAToShort(buffer, format.notchFilterSlopeFormat.posStart));

        if (format.lowCutFrequencyFormat != null)
            traceHeader.setLowCutFrequency(ByteANumberConverter.byteAToShort(buffer, format.lowCutFrequencyFormat.posStart));

        if (format.highCutFrequencyFormat != null)
            traceHeader.setHighCutFrequency(ByteANumberConverter.byteAToShort(buffer, format.highCutFrequencyFormat.posStart));

        if (format.lowCutSlopeFormat != null)
            traceHeader.setLowCutSlope(ByteANumberConverter.byteAToShort(buffer, format.lowCutSlopeFormat.posStart));

        if (format.highCutSlopeFormat != null)
            traceHeader.setHighCutSlope(ByteANumberConverter.byteAToShort(buffer, format.highCutSlopeFormat.posStart));

        if (format.yearDataRecordedFormat != null)
            traceHeader.setYearDataRecorded(ByteANumberConverter.byteAToShort(buffer, format.yearDataRecordedFormat.posStart));

        if (format.dayOfYearFormat != null)
            traceHeader.setDayOfYear(ByteANumberConverter.byteAToShort(buffer, format.dayOfYearFormat.posStart));

        if (format.hourOfDayFormat != null)
            traceHeader.setHourOfDay(ByteANumberConverter.byteAToShort(buffer, format.hourOfDayFormat.posStart));

        if (format.minuteOfHourFormat != null)
            traceHeader.setMinuteOfHour(ByteANumberConverter.byteAToShort(buffer, format.minuteOfHourFormat.posStart));

        if (format.secondOfMinuteFormat != null)
            traceHeader.setSecondOfMinute(ByteANumberConverter.byteAToShort(buffer, format.secondOfMinuteFormat.posStart));

        if (format.timeBasisCodeFormat != null)
            traceHeader.setTimeBasisCode(ByteANumberConverter.byteAToShort(buffer, format.timeBasisCodeFormat.posStart));

        if (format.traceWeightingFactorFormat != null)
            traceHeader.setTraceWeightingFactor(ByteANumberConverter.byteAToShort(buffer, format.traceWeightingFactorFormat.posStart));

        if (format.geophoneGroupNumberOfRollSwitchPositionOneFormat != null)
            traceHeader.setGeophoneGroupNumberOfRollSwitchPositionOne(ByteANumberConverter.byteAToShort(buffer, format.geophoneGroupNumberOfRollSwitchPositionOneFormat.posStart));

        if (format.geophoneGroupNumberOfTraceNumberOneWOFRFormat != null)
            traceHeader.setGeophoneGroupNumberOfTraceNumberOneWOFR(ByteANumberConverter.byteAToShort(buffer, format.geophoneGroupNumberOfTraceNumberOneWOFRFormat.posStart));

        if (format.geophoneGroupNumberOfLastTraceWOFRFormat != null)
            traceHeader.setGeophoneGroupNumberOfLastTraceWOFR(ByteANumberConverter.byteAToShort(buffer, format.geophoneGroupNumberOfLastTraceWOFRFormat.posStart));

        if (format.gapSizeFormat != null)
            traceHeader.setGapSize(ByteANumberConverter.byteAToShort(buffer, format.gapSizeFormat.posStart));

        if (format.overTravelFormat != null)
            traceHeader.setOverTravel(ByteANumberConverter.byteAToShort(buffer, format.overTravelFormat.posStart));

        if (format.xOfCDPPositionFormat != null) {
            log.trace("Setting xOfCDPPositionFormat");
            traceHeader.setxOfCDPPosition(ByteANumberConverter.byteAToInt(buffer, format.xOfCDPPositionFormat.posStart));
        }

        if (format.yOfCDPPositionFormat != null) {
            log.trace("Setting yOfCDPPositionFormat");
            traceHeader.setyOfCDPPosition(ByteANumberConverter.byteAToInt(buffer, format.yOfCDPPositionFormat.posStart));
        }

        if (format.inLineNumberFormat != null)
            traceHeader.setInLineNumber(ByteANumberConverter.byteAToInt(buffer, format.inLineNumberFormat.posStart));

        if (format.crossLineNumberFormat != null)
            traceHeader.setCrossLineNumber(ByteANumberConverter.byteAToInt(buffer, format.crossLineNumberFormat.posStart));

        if (format.shotpointNumberFormat != null)
            traceHeader.setShotpointNumber(ByteANumberConverter.byteAToInt(buffer, format.shotpointNumberFormat.posStart));

        if (format.scalarForSPNumberFormat != null)
            traceHeader.setScalarForSPNumber(ByteANumberConverter.byteAToShort(buffer, format.scalarForSPNumberFormat.posStart));

        if (format.traceValuesMUFormat != null)
            traceHeader.setTraceValuesMU(ByteANumberConverter.byteAToShort(buffer, format.traceValuesMUFormat.posStart));

        if (format.transductionConstantFormat != null) {
            final byte[] transductionConstant = Arrays.copyOfRange(buffer, format.transductionConstantFormat.posStart, format.transductionConstantFormat.posEnd);
            traceHeader.setTransductionConstant(transductionConstant);
        }

        if (format.transductionUnitsFormat != null)
            traceHeader.setTransductionUnits(ByteANumberConverter.byteAToShort(buffer, format.transductionUnitsFormat.posStart));

        if (format.deviceTraceIdentifierFormat != null)
            traceHeader.setDeviceTraceIdentifier(ByteANumberConverter.byteAToShort(buffer, format.deviceTraceIdentifierFormat.posStart));

        if (format.scalarToBeAppliedToTimesFormat != null)
            traceHeader.setScalarForTimes(ByteANumberConverter.byteAToShort(buffer, format.scalarToBeAppliedToTimesFormat.posStart));

        if (format.sourceTypeOrientationFormat != null)
            traceHeader.setSourceTypeOrientation(ByteANumberConverter.byteAToShort(buffer, format.sourceTypeOrientationFormat.posStart));

        if (format.sourceEnergyDirectionFormat != null) {
            final byte[] sourceEnergyDirection = Arrays.copyOfRange(buffer, format.sourceEnergyDirectionFormat.posStart, format.sourceEnergyDirectionFormat.posEnd);
            traceHeader.setSourceEnergyDirection(sourceEnergyDirection);
        }

        if (format.sourceMeasurementFormat != null) {
            final byte[] sourceMeasurementFormat = Arrays.copyOfRange(buffer, format.sourceMeasurementFormat.posStart, format.sourceMeasurementFormat.posEnd);
            traceHeader.setSourceMeasurement(sourceMeasurementFormat);
        }

        if (format.sourceMeasurementUnitFormat != null)
            traceHeader.setSourceMeasurementUnit(ByteANumberConverter.byteAToShort(buffer, format.sourceMeasurementUnitFormat.posStart));

        return traceHeader;
    }
}
