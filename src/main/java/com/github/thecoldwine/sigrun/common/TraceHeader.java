package com.github.thecoldwine.sigrun.common;

import java.util.Arrays;

@SuppressWarnings({"RedundantIfStatement", "UnusedDeclaration", "ConstantConditions"})
public class TraceHeader {
    @Reportable(value = "Position In File")
    private transient long positionInFile = -1;

    public long getPositionInFile() {
        return positionInFile;
    }

    public void setPositionInFile(long positionInFile) {
        this.positionInFile = positionInFile;
    }

    /**
     * Trace sequence number within the line - Numbers continue to increase if the same
     * line continues across multiple SEG Y files.
     * <p/>
     * <b>Highly recommended for all types of data</b>
     */
    @Reportable(value = "Trace Sequence Number Within The Line", startPosition = 0, endPosition = 4)
    private Integer traceSequenceNumberWL;
    /**
     * Trace sequence number within SEG Y file - each file starts with trace sequence one.
     */
    @Reportable(value = "Trace Sequence Number Within SEG Y File", startPosition = 4, endPosition = 8)
    private Integer traceSequenceNumberWS;
    /**
     * Original field record number.
     * <p/>
     * <b>Highly recommended for all types of data</b>
     */
    @Reportable(value = "Original File Record Number", startPosition = 8, endPosition = 12)
    private Integer originalFieldRecordNumber;
    /**
     * Trace number within the original field record.
     * <p/>
     * <b>Highly recommended for all types of data</b>
     */
    @Reportable(value = "Trace Number Within The Original Field Record", startPosition = 12, endPosition = 16)
    private Integer traceNumberWOFR;
    /**
     * Energy source point number - used when more than one record occurs at the same
     * effective surface location. It is recommended that the new entry defined in Trace Header
     * bytes 197-202 be used for shotpoint number;
     */
    @Reportable(value = "Energy Source Point Number", startPosition = 16, endPosition = 20)
    private Integer energySourcePointNumber;
    /**
     * Ensemble number
     */
    @Reportable(value = "Ensemble Number", startPosition = 20, endPosition = 24)
    private Integer ensembleNumber;
    /**
     * Trace number within the ensemble - each ensemble starts with trace number one.
     */
    @Reportable(value = "Trace Number Within The Ensemble", startPosition = 24, endPosition = 28)
    private Integer traceNumberWEnsemble;
    /**
     * Trace identification code
     */
    @Reportable(value = "Trace Identification Code", startPosition = 28, endPosition = 30)
    private TraceIdentificationCode traceIdentificationCode = TraceIdentificationCode.create((short) 0);
    /**
     * Number of vertically summed traces yielding this trace (1 is one trace, 2 is two traces, etc).
     */
    @Reportable(value = "Number Of Vertically Summed Traces", startPosition = 30, endPosition = 32)
    private Short numberOfVerticallySummedTraces;
    /**
     * Number of horizontally stacked traces yielding this trace (1 is on trace, 2 is two traces, etc ).
     */
    @Reportable(value = "Number Of Horizontally Stacked Traces", startPosition = 32, endPosition = 34)
    private Short numberOfHorizontallyStackedTraces;
    /**
     * Data use
     * 1 - Production
     * 2 - Test
     */
    @Reportable(value = "Data Use", startPosition = 34, endPosition = 36)
    private Short dataUse;
    /**
     * Distance from center of the source to the center of the receiver group (negative if opposite to direction
     * in which line is shot)
     */
    @Reportable(value = "Distance From Center Of The Source To Center Of Receiver Group", startPosition = 36,
            endPosition = 40)
    private Integer distanceFromTheCenterOfSP;

    /*
        The scalar in Trace Header bytes 69-70 applies to the following seven values
         1) Receiver group elevation.
         2) Surface elevation ar source.
         3) Source depth below thw surface (a positive number).
         4) Datum elevation at receiver group.
         5) Datum elevation at source.
         6) Water depth at source.
         7) Water depth at group.
        The units are feet or meters as specified in Binary File Header bytes (3255-3256). The Vertical Datum
        should be defined through a Location Data stanza (see section D-1 of SEG Y Standard rev 1)

     */

    /**
     * Receiver group elevation (all elevations above the Vertical datum are positive and below are negative).
     */
    @Reportable(value = "Receiver Group Elevation", startPosition = 40, endPosition = 44)
    private Integer receiverGroupElevation;
    /**
     * Surface elevation at source.
     */
    @Reportable(value = "Surface Elevation At Source", startPosition = 44, endPosition = 48)
    private Integer surfaceElevationAtSource;
    /**
     * Source depth below surface.
     */
    @Reportable(value = "Source Depth Below Surface", startPosition = 48, endPosition = 52)
    private Integer sourceDepthBelowSurface;
    /**
     * Datum elevation at receiver group.
     */
    @Reportable(value = "Datum Elevation At Receiver Group", startPosition = 52, endPosition = 56)
    private Integer datumElevationAtReceiverGroup;
    /**
     * Datum elevation at source.
     */
    @Reportable(value = "Datum Elevation At Source", startPosition = 56, endPosition = 60)
    private Integer datumElevationAtSource;
    /**
     * Water depth at source.
     */
    @Reportable(value = "Water Depth At Source", startPosition = 60, endPosition = 64)
    private Integer waterDepthAtSource;
    /**
     * Water depth at group.
     */
    @Reportable(value = "Water Depth At Group", startPosition = 64, endPosition = 68)
    private Integer waterDepthAtGroup;
    /**
     * Scalar to be applied to all elevations and depths in Trace Header bytes 41-68 to give the real value.
     * Scalar = 1, 10, 100, 1000 or 10000. If positive, scalar is used as multiplier, if negative as a divisor.
     */
    @Reportable(value = "Scalar For Elevations", startPosition = 68, endPosition = 70)
    private Short scalarForElevations;
    /**
     * Scalar to be applied to all coordinates specified in Trace Header bytes 73-88 and to bytes Trace Header 181-188
     * to give the real value. Scalar = 1, 10, 100, 1000, 10000. If positive, scalar is used as a multiplier; if
     * negative, scalar is used as divisor.
     */
    @Reportable(value = "Scalar For Coordinates", startPosition = 70, endPosition = 72)
    private Short scalarForCoordinates;
    /*
        The coordinate reference system should be identified through an extended Location Data stanza (section D-1).

        If the coordinate units are in seconds of arc, decimal degrees or DMS, the X values represent longitude and
        the Y values latitude. A positive values designates ease of Greenwich Meridian or north of equator and
        a negative value designates south or west.
    */

    /**
     * Source coordinate X
     */
    @Reportable(value = "Source Coordinate X", startPosition = 72, endPosition = 76)
    private Float sourceX;
    /**
     * Source coordinate Y
     */
    @Reportable(value = "Source Coordinate Y", startPosition = 76, endPosition = 80)
    private Float sourceY;
    /**
     * Group coordinate X
     */
    @Reportable(value = "Group Coordinate X", startPosition = 80, endPosition = 84)
    private Integer groupX;
    /**
     * Group coordinate Y
     */
    @Reportable(value = "Group Coordinate Y", startPosition = 84, endPosition = 88)
    private Integer groupY;
    /**
     * Coordinate units:
     * 1 = Length (meters of feet)
     * 2 = Seconds of arc
     * 3 = Decimal degrees
     * 4 = DMS
     */
    @Reportable(value = "Coordinate Units Code", startPosition = 88, endPosition = 90)
    private CoordinateUnitsCode coordinateUnitsCode = CoordinateUnitsCode.create((short) 0);
    /**
     * Weathering velocity (ft/s or m/s as specified in Binary File Header bytes 3255-3256)
     */
    @Reportable(value = "Weathering Velocity", startPosition = 90, endPosition = 92)
    private Short weatheringVelocity;
    /**
     * Subweathering velocity (ft/s or m/s as specified in Binary File Header bytes 3255-3256)
     */
    @Reportable(value = "Subweathering Velocity", startPosition = 92, endPosition = 94)
    private Short subweatheringVelocity;
    /**
     * Uphole time at source in milliseconds
     */
    @Reportable(value = "Uphole Time At Source In Ms", startPosition = 94, endPosition = 96)
    private Short upholeTimeAtSourceInMs;
    /**
     * Uphole time at group in milliseconds
     */
    @Reportable(value = "Uphole Time At Group In Ms", startPosition = 96, endPosition = 98)
    private Short upholeTimeAtGroupInMs;
    /**
     * Source static correction in milliseconds
     */
    @Reportable(value = "Source Static Correction In Ms", startPosition = 98, endPosition = 100)
    private Short sourceStaticCorrectionInMs;
    /**
     * Group static correction in milliseconds
     */
    @Reportable(value = "Group Static Correction In Ms", startPosition = 100, endPosition = 102)
    private Short groupStaticCorrectionInMs;
    /**
     * Total static applied in milliseconds. (Zero if no static has been applied)
     */
    @Reportable(value = "Total Static Applied In Ms", startPosition = 102, endPosition = 104)
    private Short totalStaticAppliedInMs;
    /**
     * Lag time A - Time in milliseconds between end of 240-byte trace identification header and time break. The value
     * is positive if time break occurs after the end of header; negative if time break occurs before the end of header.
     * Time break is defined as the initiation pulse that may be recorded on an auxiliary trace or as otherwise by the
     * recording system.
     */
    @Reportable(value = "Lag Time A", startPosition = 104, endPosition = 106)
    private Short lagTimeA;
    /**
     * Lag time B - Time in milliseconds between time break and the initiation time if the energy source.
     * May be positive or negative.
     */
    @Reportable(value = "Lag Time B", startPosition = 106, endPosition = 108)
    private Short lagTimeB;
    /**
     * Delay recording time - Time in milliseconds between initiation time of energy source and the time when recording
     * of data samples begins. In SEG Y rev 0 this entry was intended for deep-water work if data recording does not
     * start at zero time. The entry can be negative to accommodate negative start times (i.e. data recorded before time
     * zero, presumably as a result of static application to the data trace). If a non-zero value (negative or positive) is
     * recorded in this entry, a comment to that effect should appear in Text File Header.
     */
    @Reportable(value = "Delay Recording Time", startPosition = 108, endPosition = 110)
    private Short delayRecordingTime;
    /**
     * Mute time - Start time in milliseconds
     */
    @Reportable(value = "Mute Time Start", startPosition = 110, endPosition = 112)
    private Short muteTimeStart;
    /**
     * Mute time - End time in milliseconds
     */
    @Reportable(value = "Mute Time End", startPosition = 112, endPosition = 114)
    private Short muteTimeEnd;
    /**
     * Number of samples in this trace
     * <p/>
     * <b>Highly recommended for all types of data</b>
     */
    @Reportable(value = "Number Of Samples", startPosition = 114, endPosition = 116)
    private Short numberOfSamples;
    /**
     * Sample interval in microseconds for this trace.
     * The number of bytes in a trace record must ne consistent with the number of samples written in the trace header.
     * This is important for all recording medial; but it is particularly crucial for the correct processing of SEG Y
     * data in disk files.
     * <p/>
     * If the fixes length trace flag in bytes 3503-3504 of the Binary File Header is set, the sample interval and
     * number of samples in every trace in the SEG Y file must be the same as the values recorded in the Binary File
     * Header. If the fixed length trace flag is not set, the sample interval and number of samples may vary from
     * trace to trace.
     * <p/>
     * <b>Highly recommended for all types of data</b>
     */
    @Reportable(value = "Sample Interval In Microseconds", startPosition = 116, endPosition = 118)
    private Short sampleIntervalInMcs;
    /**
     * Gain type of field instruments:
     * 1 = fixed
     * 2 = binary
     * 3 = floating point
     * 4..N = optional use
     */
    @Reportable(value = "Gain Type For Instruments", startPosition = 118, endPosition = 120)
    private GainTypeForInstruments gainTypeForInstruments = GainTypeForInstruments.create((short) 0);
    /**
     * Instrument gain constant
     */
    @Reportable(value = "Instrument Gain Constant", startPosition = 120, endPosition = 122)
    private Short instrumentGainConstant;
    /**
     * Instrument early or initial gain
     */
    @Reportable(value = "Instrument Early Or Initial Gain", startPosition = 122, endPosition = 124)
    private Short instrumentEarlyOrInitialGain;
    /**
     * Correlated
     * 1 - yes
     * 2 - no
     */
    @Reportable(value = "Correlated", startPosition = 124, endPosition = 126)
    private Short correlated;
    /**
     * Sweep frequency at start (Hz)
     */
    @Reportable(value = "Sweep Frequency At Start (Hz)", startPosition = 126, endPosition = 128)
    private Short sweepFrequencyAtStart;
    /**
     * Sweep frequency at end (Hz)
     */
    @Reportable(value = "Sweep Frequency At End (Hz)", startPosition = 128, endPosition = 130)
    private Short sweepFrequencyAtEnd;
    /**
     * Sweep length in milliseconds
     */
    @Reportable(value = "Sweep Length In Milliseconds", startPosition = 130, endPosition = 132)
    private Short sweepLengthInMilliseconds;
    /**
     * Sweep type:
     * 1 - linear
     * 2 - parabolic
     * 3 - exponential
     * 4 - other
     */
    @Reportable(value = "Sweep Type", startPosition = 132, endPosition = 134)
    private Short sweepType;
    /**
     * Sweep trace taper length at start in milliseconds
     */
    @Reportable(value = "Sweep Trace Taper Length At Start In Milliseconds", startPosition = 134,
            endPosition = 136)
    private Short sweepTraceTaperLengthAtStartInMilliseconds;
    /**
     * Sweep trace taper length at end in milliseconds
     */
    @Reportable(value = "Sweep Trace Typer Length At End In Milliseconds", startPosition = 136,
            endPosition = 138)
    private Short sweepTraceTaperLengthAtEndInMilliseconds;
    /**
     * Taper type:
     * 1 - linear
     * 2 - cos^2
     * 3 - other
     */
    @Reportable(value = "Taper Type", startPosition = 138, endPosition = 140)
    private Short taperType;
    /**
     * Alias filter frequency (Hz), if used
     */
    @Reportable(value = "Alias Filter Frequency", startPosition = 140, endPosition = 142)
    private Short aliasFilterFrequency;
    /**
     * Alias filter scope (dB/octave)
     */
    @Reportable(value = "Alias Filer Slope", startPosition = 142, endPosition = 144)
    private Short aliasFilterSlope;
    /**
     * Notch filter frequency (Hz), if used
     */
    @Reportable(value = "Notch Filter Frequency", startPosition = 144, endPosition = 146)
    private Short notchFilterFrequency;
    /**
     * Notch filter slope
     */
    @Reportable(value = "Notch Filter Slope", startPosition = 146, endPosition = 148)
    private Short notchFilterSlope;
    /**
     * Low-cut frequency (Hz), if used
     */
    @Reportable(value = "Low Cut Frequency", startPosition = 148, endPosition = 150)
    private Short lowCutFrequency;
    /**
     * High-cut frequency (Hz), if used
     */
    @Reportable(value = "High Cut Frequency", startPosition = 150, endPosition = 152)
    private Short highCutFrequency;
    /**
     * Low-cut slope (dB/octave)
     */
    @Reportable(value = "Low Cut Slope", startPosition = 152, endPosition = 154)
    private Short lowCutSlope;
    /**
     * High-cut slope (dB/octave)
     */
    @Reportable(value = "High Cut Slope", startPosition = 154, endPosition = 156)
    private Short highCutSlope;
    /**
     * Year data recorded - The 1975 standard is unclear as to whether this should be recorded as a 2-digit or a 4-digit
     * year and both have been used. For SEG Y revisions beyond rev 0, the year should be recorded as the complete 4-digit
     * Gregorian calendar year (i.e. the year 2001 should be recorded as 2001(10) (7D1(16))).
     */
    @Reportable(value = "Year Data Recorded", startPosition = 156, endPosition = 158)
    private Short yearDataRecorded;
    /**
     * Day of year (Julian day for GMT and UTC time basis).
     */
    @Reportable(value = "Day Of Year", startPosition = 158, endPosition = 160)
    private Short dayOfYear;
    /**
     * Hour of day (24 hour clock)
     */
    @Reportable(value = "Hour Of Day", startPosition = 160, endPosition = 162)
    private Short hourOfDay;
    /**
     * Minute of hour
     */
    @Reportable(value = "Minute Of Hour", startPosition = 162, endPosition = 164)
    private Short minuteOfHour;
    /**
     * Second of minute
     */
    @Reportable(value = "Second Of Minute", startPosition = 164, endPosition = 166)
    private Short secondOfMinute;
    /**
     * Time basis code:
     * 1 - Local
     * 2 - GMT (Greenwich Mean Time)
     * 3 - Other, should be explained in a user defined stanza in the Extended Textual File Header
     * 4 - UTC (Coordinated Universal Time)
     */
    @Reportable(value = "Time Basis Code", startPosition = 166, endPosition = 168)
    private Short timeBasisCode;
    /**
     * Trace weighting factor - Defined as 2^-N volts for the least significant bit (N = 0, 1 .. 32767)
     */
    @Reportable(value = "Trace Weighting Factor", startPosition = 168, endPosition = 170)
    private Short traceWeightingFactor;
    /**
     * Geophone group number of roll switch position one.
     */
    @Reportable(value = "Geophone Group Number Of Roll Switch Position One", startPosition = 170,
            endPosition = 172)
    private Short geophoneGroupNumberOfRollSwitchPositionOne;
    /**
     * Geophone group number of trace number one within original field record.
     */
    @Reportable(value = "Geophone Group Number Of Trace Number One Within Original Field Record", startPosition = 172,
            endPosition = 174)
    private Short geophoneGroupNumberOfTraceNumberOneWOFR;
    /**
     * Geophone group number of last trace within original field record.
     */
    @Reportable(value = "Geophone Group Number Of Last Trace Within Original Field Record", startPosition = 174,
            endPosition = 176)
    private Short geophoneGroupNumberOfLastTraceWOFR;
    /**
     * Gap size (total number of groups dropped)
     */
    @Reportable(value = "Gap Size", startPosition = 176, endPosition = 178)
    private Short gapSize;
    /**
     * Over travel associated with taper at beginning or end of line:
     * 1 - down (or behind)
     * 2 - up (or ahead)
     */
    @Reportable(value = "Over Travel", startPosition = 178, endPosition = 180)
    private Short overTravel;
    /**
     * X coordinate of ensemble (CDP) position of this trace (scalar in Trace Header bytes 71-72 applies). The coordinate
     * reference system
     */
    @Reportable(value = "X Of CDP Position", startPosition = 180, endPosition = 184)
    private Integer xOfCDPPosition;
    /**
     * Y coordinate of ensemble (CDP) position of this trace (scalar in bytes Trace Header 71-72 applies). The coordinate
     * reference system should be identified through an extended header Location Data stanza
     */
    @Reportable(value = "Y Of CDP Position", startPosition = 184, endPosition = 188)
    private Integer yOfCDPPosition;
    /**
     * For 3-D poststack data, this field should be used for the in-line number. If one in-line per SEG Y file is being
     * recorded, this value should be the same for all traces in the file and the same value will be recorded in bytes
     * 3205-3208 of the Binary File Header.
     */
    @Reportable(value = "In-Line Number", startPosition = 188, endPosition = 192)
    private Integer inLineNumber;
    /**
     * For 3-D poststack data, this field should be used for the cross-line number. This will typically be the same
     * value as the ensemble (CDP) number in Trace Header bytes 21-24, but this does not have to be the case.
     */
    @Reportable(value = "X-Line Number", startPosition = 192, endPosition = 196)
    private Integer crossLineNumber;
    /**
     * Shotpoint number - This is probably only applicable to 2-D poststack data. Note the it is assumed that the shotpoint
     * number refers to the source location nearest to the ensemble (CDP) location for a particular trace. If this is not
     * the case =, there should be a comment in the Textual File Header explaining what the shotpoint number actually
     * refers to.
     */
    @Reportable(value = "Shot Point Number", startPosition = 196, endPosition = 200)
    private Integer shotpointNumber;
    /**
     * Scalar to be applied to the shotpoint number in Trace Header bytes 197-200 to give the real value. If positive,
     * scalar is used as a multiplier; if negative as a divisor; if zero the shotpoint number is not scaled (i.e. it is
     * an integer. A typical value will be -10, allowing shotpoint numbers with one decimal digit to the right of the
     * decimal point).
     */
    @Reportable(value = "Scalar To Be Applied To The Shot Point Number", startPosition = 200, endPosition = 202)
    private Short scalarForSPNumber;
    /**
     * Trace values measurement unit:
     * -1 - Other (should be described in Data Sample Measurement Stanza)
     * 0 - Unknown
     * 1 - Pascal (Pa)
     * 2 - Volts (v)
     * 3 - Millivolts (mV)
     * 4 - Amperes (A)
     * 5 - Meters (m)
     * 6 - Meters per second (m/s)
     * 7 - Meters per second squared (m/s^2)
     * 8 - Newton (N)
     * 9 - Watt (W)
     */
    @Reportable(value = "Trace Values Measurement Unit", startPosition = 202, endPosition = 204)
    private Short traceValuesMU;
    /**
     * Transduction constant - The multiplicative constant used to convert the Data Trace samples to the Transduction
     * Units (specified in Trace Header bytes 211-212). The constant is encoded as a four-byte, two's complement integer
     * (bytes 205-208) which is the mantissa and a two-byte, two's complement integer (bytes 209-210) which is the power
     * of ten exponent (i.e. Bytes 205-208 * 10 ** Bytes 209-210)
     */
    @Reportable(value = "Transduction Constant", startPosition = 204, endPosition = 210)
    private byte[] transductionConstant = new byte[6];
    /**
     * Transduction units - The unit of measurement of the Data Trace samples after they have been multiplies by the
     * Transduction Constant specified in Trace Header bytes 205-210.
     * -1 - Other (should be described in Data Sample Measurement Unit stanza, page 36)
     * 0 - Unknown
     * 1 - Pascal (Pa)
     * 2 - Volts (v)
     * 3 - Millivolts (mV)
     * 4 - Amperes (A)
     * 5 - Meters (m)
     * 6 - Meters per second (m/s)
     * 7 - Meters per second squared (m/s^2)
     * 8 - Newton (N)
     * 9 - Watt (N)
     */
    @Reportable(value = "Transduction Units", startPosition = 210, endPosition = 212)
    private Short transductionUnits;
    /**
     * Device/Trace Identifier - The unit number or id number of the device associated with the Data Trace (i.e. 4386
     * for vibrator serial name 4368 or 20316 for gun 16 on string 3 in vessel 2). This field allows traces to be
     * associated across trace ensembles independently of the trace number (Trace Header bytes 25-28)
     */
    @Reportable(value = "Device Trace Identifier", startPosition = 212, endPosition = 214)
    private Short deviceTraceIdentifier;
    /**
     * Scalar to be applied to times specified in Trace Header bytes 95-114 to give the true time value in milliseconds.
     * Scalar = 1, +10, +100, +1000, or +10000. If positive, scalar is used as a multiplier; if negative, scalar used as
     * divisor. A value of zero is assumed to be a scalar value of 1.
     */
    @Reportable(value = "Scalar To Be Applies To Times", startPosition = 214, endPosition = 216)
    private Short scalarToBeAppliedToTimes;
    /**
     * Source Type/Orientation - Defines the type and the orientation of the energy source. The terms vertical, cross-line
     * and in-line refer to the three axes of an orthogonal coordinate system. The absolute azimuthal orientation of the
     * coordinate system asex can be defined in the Bin Grid Definition Stanza.
     * -1 to -n - Other (should be described in Source Type/Orientation Stanza)
     * 0 - Unknown
     * 1 - Vibratory - Vertical orientation
     * 2 - Vibratory - Cross-line orientation
     * 3 - Vibratory - In-line orientation
     * 4 - Impulsive - Vertical orientation
     * 5 - Impulsive - Cross-line orientation
     * 6 - Impulsive - In-line orientation
     * 7 - Distributed Impulsive - Vertical orientation
     * 8 - Distributed Impulsive - Cross-line orientation
     * 9 - Distributed Impulsive - In-line orientation
     */
    @Reportable(value = "Source Type/Orientation", startPosition = 216, endPosition = 218)
    private Short sourceTypeOrientation;
    /**
     * Source Energy Direction with respect to the source orientation - The positive orientation direction is defined is
     * defined in bytes 217-218 of the Trace Header. The energy direction is encoded in tenths of degrees (i.e. 347.8 is
     * encoded as 3478)
     * <p/>
     * <b>6 bytes!</b>
     */
    @Reportable(value = "Source Energy Direction", startPosition = 218, endPosition = 224)
    private byte[] sourceEnergyDirection = new byte[6];
    /**
     * Source Measurement - Describes the source effort used to generate the trace. The measurement can be simple,
     * qualitative measurements such as the total weight of explosive used or the peak air gun pressure or the number of
     * vibrators times the sweep duration. Although these simple measurements are acceptable, it is preferable to use
     * true measurement units of energy or work.
     * <p/>
     * The constant is encoded as a four-byte, two's complement integer (bytes 225-228) which is mantissa and a two-byte,
     * two's complement integer (bytes 229-230) which is the power of ten exponent (i.e. Bytes 225-228 * 10**Bytes 229-230)
     * <p/>
     * <b>6 bytes!</b>
     */
    @Reportable(value = "Source Measurement", startPosition = 224, endPosition = 230)
    private byte[] sourceMeasurement = new byte[6];
    /**
     * Source measurement Unit - The unit used for the Source Measurement, Trace header bytes 225-230.
     * -1 - Other (should be described in Source Measurement Unit stanza)
     * 0 - Unknown
     * 1 - Joule (J)
     * 2 - Kilowatt (kW)
     * 3 - Pascal (Pa)
     * 4 - Bar (Bar)
     * 4 - Bar-meter (Bar-m)
     * 5 - Newton (N)
     * 6 - Kilograms (kg)
     */
    @Reportable(value = "Source Measurement Unit", startPosition = 230, endPosition = 232)
    private Short sourceMeasurementUnit;
    
    
    
    private Double longitude;
    
    private Double latitude;
    
    /**
     * Unassigned - For optional information
     * <p/>
     * 233-240
     */
    @Reportable(value = "Optional Bytes", startPosition = 232, endPosition = 240)
    private byte[] optional = new byte[8];
    public static final int TRACE_HEADER_LENGTH = 240;

    public Integer getTraceSequenceNumberWL() {
        return traceSequenceNumberWL;
    }

    public void setTraceSequenceNumberWL(Integer traceSequenceNumberWL) {
        this.traceSequenceNumberWL = traceSequenceNumberWL;
    }

    public Integer getTraceSequenceNumberWS() {
        return traceSequenceNumberWS;
    }

    public void setTraceSequenceNumberWS(Integer traceSequenceNumberWS) {
        this.traceSequenceNumberWS = traceSequenceNumberWS;
    }

    public Integer getOriginalFieldRecordNumber() {
        return originalFieldRecordNumber;
    }

    public void setOriginalFieldRecordNumber(Integer originalFieldRecordNumber) {
        this.originalFieldRecordNumber = originalFieldRecordNumber;
    }

    public Integer getTraceNumberWOFR() {
        return traceNumberWOFR;
    }

    public void setTraceNumberWOFR(Integer traceNumberWOFR) {
        this.traceNumberWOFR = traceNumberWOFR;
    }

    public Integer getEnergySourcePointNumber() {
        return energySourcePointNumber;
    }

    public void setEnergySourcePointNumber(Integer energySourcePointNumber) {
        this.energySourcePointNumber = energySourcePointNumber;
    }

    public Integer getEnsembleNumber() {
        return ensembleNumber;
    }

    public void setEnsembleNumber(Integer ensembleNumber) {
        this.ensembleNumber = ensembleNumber;
    }

    public Integer getTraceNumberWEnsemble() {
        return traceNumberWEnsemble;
    }

    public void setTraceNumberWEnsemble(Integer traceNumberWEnsemble) {
        this.traceNumberWEnsemble = traceNumberWEnsemble;
    }

    public TraceIdentificationCode getTraceIdentificationCode() {
        return traceIdentificationCode;
    }

    public void setTraceIdentificationCode(TraceIdentificationCode traceIdentificationCode) {
        this.traceIdentificationCode = traceIdentificationCode;
    }

    public Short getNumberOfVerticallySummedTraces() {
        return numberOfVerticallySummedTraces;
    }

    public void setNumberOfVerticallySummedTraces(Short numberOfVerticallySummedTraces) {
        this.numberOfVerticallySummedTraces = numberOfVerticallySummedTraces;
    }

    public Short getNumberOfHorizontallyStackedTraces() {
        return numberOfHorizontallyStackedTraces;
    }

    public void setNumberOfHorizontallyStackedTraces(Short numberOfHorizontallyStackedTraces) {
        this.numberOfHorizontallyStackedTraces = numberOfHorizontallyStackedTraces;
    }

    public Short getDataUse() {
        return dataUse;
    }

    public void setDataUse(Short dataUse) {
        this.dataUse = dataUse;
    }

    public Integer getDistanceFromTheCenterOfSP() {
        return distanceFromTheCenterOfSP;
    }

    public void setDistanceFromTheCenterOfSP(Integer distanceFromTheCenterOfSP) {
        this.distanceFromTheCenterOfSP = distanceFromTheCenterOfSP;
    }

    public Integer getReceiverGroupElevation() {
        return receiverGroupElevation;
    }

    public void setReceiverGroupElevation(Integer receiverGroupElevation) {
        this.receiverGroupElevation = receiverGroupElevation;
    }

    public Integer getSurfaceElevationAtSource() {
        return surfaceElevationAtSource;
    }

    public void setSurfaceElevationAtSource(Integer surfaceElevationAtSource) {
        this.surfaceElevationAtSource = surfaceElevationAtSource;
    }

    public Integer getSourceDepthBelowSurface() {
        return sourceDepthBelowSurface;
    }

    public void setSourceDepthBelowSurface(Integer surfaceDepthBelowSurface) {
        this.sourceDepthBelowSurface = surfaceDepthBelowSurface;
    }

    public Integer getDatumElevationAtReceiverGroup() {
        return datumElevationAtReceiverGroup;
    }

    public void setDatumElevationAtReceiverGroup(Integer datumElevationAtReceiverGroup) {
        this.datumElevationAtReceiverGroup = datumElevationAtReceiverGroup;
    }

    public Integer getDatumElevationAtSource() {
        return datumElevationAtSource;
    }

    public void setDatumElevationAtSource(Integer datumElevationAtSource) {
        this.datumElevationAtSource = datumElevationAtSource;
    }

    public Integer getWaterDepthAtSource() {
        return waterDepthAtSource;
    }

    public void setWaterDepthAtSource(Integer waterDepthAtSource) {
        this.waterDepthAtSource = waterDepthAtSource;
    }

    public Integer getWaterDepthAtGroup() {
        return waterDepthAtGroup;
    }

    public void setWaterDepthAtGroup(Integer waterDepthAtGroup) {
        this.waterDepthAtGroup = waterDepthAtGroup;
    }

    public Short getScalarForElevations() {
        return scalarForElevations;
    }

    public void setScalarForElevations(Short scalarForElevations) {
        this.scalarForElevations = scalarForElevations;
    }

    public Short getScalarForCoordinates() {
        return scalarForCoordinates;
    }

    public void setScalarForCoordinates(Short scalarForCoordinates) {
        this.scalarForCoordinates = scalarForCoordinates;
    }

    public Float getSourceX() {
        return sourceX;
    }

    public void setSourceX(Float sourceX) {
        this.sourceX = sourceX;
    }

    public Float getSourceY() {
        return sourceY;
    }

    public void setSourceY(Float sourceY) {
        this.sourceY = sourceY;
    }

    public Integer getGroupX() {
        return groupX;
    }

    public void setGroupX(Integer groupX) {
        this.groupX = groupX;
    }

    public Integer getGroupY() {
        return groupY;
    }

    public void setGroupY(Integer groupY) {
        this.groupY = groupY;
    }

    public CoordinateUnitsCode getCoordinateUnitsCode() {
        return coordinateUnitsCode;
    }

    public void setCoordinateUnitsCode(CoordinateUnitsCode coordinateUnitsCode) {
        this.coordinateUnitsCode = coordinateUnitsCode;
    }

    public Short getWeatheringVelocity() {
        return weatheringVelocity;
    }

    public void setWeatheringVelocity(Short weatheringVelocity) {
        this.weatheringVelocity = weatheringVelocity;
    }

    public Short getSubweatheringVelocity() {
        return subweatheringVelocity;
    }

    public void setSubweatheringVelocity(Short subweatheringVelocity) {
        this.subweatheringVelocity = subweatheringVelocity;
    }

    public Short getUpholeTimeAtSourceInMs() {
        return upholeTimeAtSourceInMs;
    }

    public void setUpholeTimeAtSourceInMs(Short upholeTimeAtSourceInMs) {
        this.upholeTimeAtSourceInMs = upholeTimeAtSourceInMs;
    }

    public Short getUpholeTimeAtGroupInMs() {
        return upholeTimeAtGroupInMs;
    }

    public void setUpholeTimeAtGroupInMs(Short upholeTimeAtGroupInMs) {
        this.upholeTimeAtGroupInMs = upholeTimeAtGroupInMs;
    }

    public Short getSourceStaticCorrectionInMs() {
        return sourceStaticCorrectionInMs;
    }

    public void setSourceStaticCorrectionInMs(Short sourceStaticCorrectionInMs) {
        this.sourceStaticCorrectionInMs = sourceStaticCorrectionInMs;
    }

    public Short getGroupStaticCorrectionInMs() {
        return groupStaticCorrectionInMs;
    }

    public void setGroupStaticCorrectionInMs(Short groupStaticCorrectionInMs) {
        this.groupStaticCorrectionInMs = groupStaticCorrectionInMs;
    }

    public Short getLagTimeA() {
        return lagTimeA;
    }

    public void setLagTimeA(Short lagTimeA) {
        this.lagTimeA = lagTimeA;
    }

    public Short getLagTimeB() {
        return lagTimeB;
    }

    public void setLagTimeB(Short lagTimeB) {
        this.lagTimeB = lagTimeB;
    }

    public Short getDelayRecordingTime() {
        return delayRecordingTime;
    }

    public void setDelayRecordingTime(Short delayRecordingTime) {
        this.delayRecordingTime = delayRecordingTime;
    }

    public Short getMuteTimeStart() {
        return muteTimeStart;
    }

    public void setMuteTimeStart(Short muteTimeStart) {
        this.muteTimeStart = muteTimeStart;
    }

    public Short getMuteTimeEnd() {
        return muteTimeEnd;
    }

    public void setMuteTimeEnd(Short muteTimeEnd) {
        this.muteTimeEnd = muteTimeEnd;
    }

    public Short getNumberOfSamples() {
        return numberOfSamples;
    }

    public void setNumberOfSamples(Short numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public Short getSampleIntervalInMcs() {
        return sampleIntervalInMcs;
    }

    public void setSampleIntervalInMcs(Short sampleIntervalInMcs) {
        this.sampleIntervalInMcs = sampleIntervalInMcs;
    }

    public GainTypeForInstruments getGainTypeForInstruments() {
        return gainTypeForInstruments;
    }

    public void setGainTypeForInstruments(GainTypeForInstruments gainTypeForInstruments) {
        this.gainTypeForInstruments = gainTypeForInstruments;
    }

    public Short getInstrumentGainConstant() {
        return instrumentGainConstant;
    }

    public void setInstrumentGainConstant(Short instrumentGainConstant) {
        this.instrumentGainConstant = instrumentGainConstant;
    }

    public Short getInstrumentEarlyOrInitialGain() {
        return instrumentEarlyOrInitialGain;
    }

    public void setInstrumentEarlyOrInitialGain(Short instrumentEarlyOrInitialGain) {
        this.instrumentEarlyOrInitialGain = instrumentEarlyOrInitialGain;
    }

    public Short getCorrelated() {
        return correlated;
    }

    public void setCorrelated(Short correlated) {
        this.correlated = correlated;
    }

    public Short getSweepFrequencyAtStart() {
        return sweepFrequencyAtStart;
    }

    public void setSweepFrequencyAtStart(Short sweepFrequencyAtStart) {
        this.sweepFrequencyAtStart = sweepFrequencyAtStart;
    }

    public Short getSweepFrequencyAtEnd() {
        return sweepFrequencyAtEnd;
    }

    public void setSweepFrequencyAtEnd(Short sweepFrequencyAtEnd) {
        this.sweepFrequencyAtEnd = sweepFrequencyAtEnd;
    }

    public Short getSweepLengthInMilliseconds() {
        return sweepLengthInMilliseconds;
    }

    public void setSweepLengthInMilliseconds(Short sweepLengthInMilliseconds) {
        this.sweepLengthInMilliseconds = sweepLengthInMilliseconds;
    }

    public Short getSweepType() {
        return sweepType;
    }

    public void setSweepType(Short sweepType) {
        this.sweepType = sweepType;
    }

    public Short getSweepTraceTaperLengthAtStartInMilliseconds() {
        return sweepTraceTaperLengthAtStartInMilliseconds;
    }

    public void setSweepTraceTaperLengthAtStartInMilliseconds(Short sweepTraceTaperLengthAtStartInMilliseconds) {
        this.sweepTraceTaperLengthAtStartInMilliseconds = sweepTraceTaperLengthAtStartInMilliseconds;
    }

    public Short getSweepTraceTaperLengthAtEndInMilliseconds() {
        return sweepTraceTaperLengthAtEndInMilliseconds;
    }

    public void setSweepTraceTaperLengthAtEndInMilliseconds(Short sweepTraceTaperLengthAtEndInMilliseconds) {
        this.sweepTraceTaperLengthAtEndInMilliseconds = sweepTraceTaperLengthAtEndInMilliseconds;
    }

    public Short getTaperType() {
        return taperType;
    }

    public void setTaperType(Short taperType) {
        this.taperType = taperType;
    }

    public Short getAliasFilterFrequency() {
        return aliasFilterFrequency;
    }

    public void setAliasFilterFrequency(Short aliasFilterFrequency) {
        this.aliasFilterFrequency = aliasFilterFrequency;
    }

    public Short getAliasFilterSlope() {
        return aliasFilterSlope;
    }

    public void setAliasFilterSlope(Short aliasFilterSlope) {
        this.aliasFilterSlope = aliasFilterSlope;
    }

    public Short getNotchFilterFrequency() {
        return notchFilterFrequency;
    }

    public void setNotchFilterFrequency(Short notchFilterFrequency) {
        this.notchFilterFrequency = notchFilterFrequency;
    }

    public Short getNotchFilterSlope() {
        return notchFilterSlope;
    }

    public void setNotchFilterSlope(Short notchFilterSlope) {
        this.notchFilterSlope = notchFilterSlope;
    }

    public Short getLowCutFrequency() {
        return lowCutFrequency;
    }

    public void setLowCutFrequency(Short lowCutFrequency) {
        this.lowCutFrequency = lowCutFrequency;
    }

    public Short getHighCutFrequency() {
        return highCutFrequency;
    }

    public void setHighCutFrequency(Short highCutFrequency) {
        this.highCutFrequency = highCutFrequency;
    }

    public Short getLowCutSlope() {
        return lowCutSlope;
    }

    public void setLowCutSlope(Short lowCutSlope) {
        this.lowCutSlope = lowCutSlope;
    }

    public Short getHighCutSlope() {
        return highCutSlope;
    }

    public void setHighCutSlope(Short highCutSlope) {
        this.highCutSlope = highCutSlope;
    }

    public Short getYearDataRecorded() {
        return yearDataRecorded;
    }

    public void setYearDataRecorded(Short yearDataRecorded) {
        this.yearDataRecorded = yearDataRecorded;
    }

    public Short getDayOfYear() {
        return dayOfYear;
    }

    public void setDayOfYear(Short dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    public Short getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(Short hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public Short getSecondOfMinute() {
        return secondOfMinute;
    }

    public void setSecondOfMinute(Short secondOfMinute) {
        this.secondOfMinute = secondOfMinute;
    }

    public Short getTimeBasisCode() {
        return timeBasisCode;
    }

    public void setTimeBasisCode(Short timeBasisCode) {
        this.timeBasisCode = timeBasisCode;
    }

    public Short getTraceWeightingFactor() {
        return traceWeightingFactor;
    }

    public void setTraceWeightingFactor(Short traceWeightingFactor) {
        this.traceWeightingFactor = traceWeightingFactor;
    }

    public Short getGeophoneGroupNumberOfRollSwitchPositionOne() {
        return geophoneGroupNumberOfRollSwitchPositionOne;
    }

    public void setGeophoneGroupNumberOfRollSwitchPositionOne(Short geophoneGroupNumberOfRollSwitchPositionOne) {
        this.geophoneGroupNumberOfRollSwitchPositionOne = geophoneGroupNumberOfRollSwitchPositionOne;
    }

    public Short getGeophoneGroupNumberOfTraceNumberOneWOFR() {
        return geophoneGroupNumberOfTraceNumberOneWOFR;
    }

    public void setGeophoneGroupNumberOfTraceNumberOneWOFR(Short geophoneGroupNumberOfTraceNumberOneWOFR) {
        this.geophoneGroupNumberOfTraceNumberOneWOFR = geophoneGroupNumberOfTraceNumberOneWOFR;
    }

    public Short getGapSize() {
        return gapSize;
    }

    public void setGapSize(Short gapSize) {
        this.gapSize = gapSize;
    }

    public Short getOverTravel() {
        return overTravel;
    }

    public void setOverTravel(Short overTravel) {
        this.overTravel = overTravel;
    }

    public Integer getxOfCDPPosition() {
        return xOfCDPPosition;
    }

    public void setxOfCDPPosition(Integer xOfCDPPosition) {
        this.xOfCDPPosition = xOfCDPPosition;
    }

    public Integer getyOfCDPPosition() {
        return yOfCDPPosition;
    }

    public void setyOfCDPPosition(Integer yOfCDPPosition) {
        this.yOfCDPPosition = yOfCDPPosition;
    }

    public Integer getInLineNumber() {
        return inLineNumber;
    }

    public void setInLineNumber(Integer inLineNumber) {
        this.inLineNumber = inLineNumber;
    }

    public Integer getCrossLineNumber() {
        return crossLineNumber;
    }

    public void setCrossLineNumber(Integer crossLineNumber) {
        this.crossLineNumber = crossLineNumber;
    }

    public Integer getShotpointNumber() {
        return shotpointNumber;
    }

    public void setShotpointNumber(Integer shotpointNumber) {
        this.shotpointNumber = shotpointNumber;
    }

    public Short getScalarForSPNumber() {
        return scalarForSPNumber;
    }

    public void setScalarForSPNumber(Short scalarForSPNumber) {
        this.scalarForSPNumber = scalarForSPNumber;
    }

    public Short getTraceValuesMU() {
        return traceValuesMU;
    }

    public void setTraceValuesMU(Short traceValuesMU) {
        this.traceValuesMU = traceValuesMU;
    }

    public byte[] getTransductionConstant() {
        return transductionConstant;
    }

    public void setTransductionConstant(byte[] transductionConstant) {
        this.transductionConstant = transductionConstant;
    }

    public Short getTransductionUnits() {
        return transductionUnits;
    }

    public void setTransductionUnits(Short transductionUnits) {
        this.transductionUnits = transductionUnits;
    }

    public Short getDeviceTraceIdentifier() {
        return deviceTraceIdentifier;
    }

    public void setDeviceTraceIdentifier(Short deviceTraceIdentifier) {
        this.deviceTraceIdentifier = deviceTraceIdentifier;
    }

    public Short getScalarToBeAppliedToTimes() {
        return scalarToBeAppliedToTimes;
    }

    public void setScalarForTimes(Short scalarToBeAppliedToTimes) {
        this.scalarToBeAppliedToTimes = scalarToBeAppliedToTimes;
    }

    public Short getSourceTypeOrientation() {
        return sourceTypeOrientation;
    }

    public void setSourceTypeOrientation(Short sourceTypeOrientation) {
        this.sourceTypeOrientation = sourceTypeOrientation;
    }

    public byte[] getSourceEnergyDirection() {
        return sourceEnergyDirection;
    }

    public void setSourceEnergyDirection(byte[] sourceEnergyDirection) {
        this.sourceEnergyDirection = sourceEnergyDirection;
    }

    public byte[] getSourceMeasurement() {
        return sourceMeasurement;
    }

    public void setSourceMeasurement(byte[] sourceMeasurement) {
        this.sourceMeasurement = sourceMeasurement;
    }

    public Short getSourceMeasurementUnit() {
        return sourceMeasurementUnit;
    }

    public void setSourceMeasurementUnit(Short sourceMeasurementUnit) {
        this.sourceMeasurementUnit = sourceMeasurementUnit;
    }

    public byte[] getOptional() {
        return optional;
    }

    public void setOptional(byte[] optional) {
        this.optional = optional;
    }

    public Short getTotalStaticAppliedInMs() {
        return totalStaticAppliedInMs;
    }

    public void setTotalStaticAppliedInMs(Short totalStaticAppliedInMs) {
        this.totalStaticAppliedInMs = totalStaticAppliedInMs;
    }

    public Short getMinuteOfHour() {
        return minuteOfHour;
    }

    public void setMinuteOfHour(Short minuteOfHour) {
        this.minuteOfHour = minuteOfHour;
    }

    public Short getGeophoneGroupNumberOfLastTraceWOFR() {
        return geophoneGroupNumberOfLastTraceWOFR;
    }

    public void setGeophoneGroupNumberOfLastTraceWOFR(Short geophoneGroupNumberOfLastTraceWOFR) {
        this.geophoneGroupNumberOfLastTraceWOFR = geophoneGroupNumberOfLastTraceWOFR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TraceHeader that = (TraceHeader) o;

        if (aliasFilterFrequency != null ? !aliasFilterFrequency.equals(that.aliasFilterFrequency) : that.aliasFilterFrequency != null)
            return false;
        if (aliasFilterSlope != null ? !aliasFilterSlope.equals(that.aliasFilterSlope) : that.aliasFilterSlope != null)
            return false;
        if (coordinateUnitsCode != that.coordinateUnitsCode) return false;
        if (correlated != null ? !correlated.equals(that.correlated) : that.correlated != null) return false;
        if (crossLineNumber != null ? !crossLineNumber.equals(that.crossLineNumber) : that.crossLineNumber != null)
            return false;
        if (dataUse != null ? !dataUse.equals(that.dataUse) : that.dataUse != null) return false;
        if (datumElevationAtReceiverGroup != null ? !datumElevationAtReceiverGroup.equals(that.datumElevationAtReceiverGroup) : that.datumElevationAtReceiverGroup != null)
            return false;
        if (datumElevationAtSource != null ? !datumElevationAtSource.equals(that.datumElevationAtSource) : that.datumElevationAtSource != null)
            return false;
        if (dayOfYear != null ? !dayOfYear.equals(that.dayOfYear) : that.dayOfYear != null) return false;
        if (delayRecordingTime != null ? !delayRecordingTime.equals(that.delayRecordingTime) : that.delayRecordingTime != null)
            return false;
        if (deviceTraceIdentifier != null ? !deviceTraceIdentifier.equals(that.deviceTraceIdentifier) : that.deviceTraceIdentifier != null)
            return false;
        if (distanceFromTheCenterOfSP != null ? !distanceFromTheCenterOfSP.equals(that.distanceFromTheCenterOfSP) : that.distanceFromTheCenterOfSP != null)
            return false;
        if (energySourcePointNumber != null ? !energySourcePointNumber.equals(that.energySourcePointNumber) : that.energySourcePointNumber != null)
            return false;
        if (ensembleNumber != null ? !ensembleNumber.equals(that.ensembleNumber) : that.ensembleNumber != null)
            return false;
        if (gainTypeForInstruments != that.gainTypeForInstruments) return false;
        if (gapSize != null ? !gapSize.equals(that.gapSize) : that.gapSize != null) return false;
        if (geophoneGroupNumberOfLastTraceWOFR != null ? !geophoneGroupNumberOfLastTraceWOFR.equals(that.geophoneGroupNumberOfLastTraceWOFR) : that.geophoneGroupNumberOfLastTraceWOFR != null)
            return false;
        if (geophoneGroupNumberOfRollSwitchPositionOne != null ? !geophoneGroupNumberOfRollSwitchPositionOne.equals(that.geophoneGroupNumberOfRollSwitchPositionOne) : that.geophoneGroupNumberOfRollSwitchPositionOne != null)
            return false;
        if (geophoneGroupNumberOfTraceNumberOneWOFR != null ? !geophoneGroupNumberOfTraceNumberOneWOFR.equals(that.geophoneGroupNumberOfTraceNumberOneWOFR) : that.geophoneGroupNumberOfTraceNumberOneWOFR != null)
            return false;
        if (groupStaticCorrectionInMs != null ? !groupStaticCorrectionInMs.equals(that.groupStaticCorrectionInMs) : that.groupStaticCorrectionInMs != null)
            return false;
        if (groupX != null ? !groupX.equals(that.groupX) : that.groupX != null) return false;
        if (groupY != null ? !groupY.equals(that.groupY) : that.groupY != null) return false;
        if (highCutFrequency != null ? !highCutFrequency.equals(that.highCutFrequency) : that.highCutFrequency != null)
            return false;
        if (highCutSlope != null ? !highCutSlope.equals(that.highCutSlope) : that.highCutSlope != null) return false;
        if (hourOfDay != null ? !hourOfDay.equals(that.hourOfDay) : that.hourOfDay != null) return false;
        if (inLineNumber != null ? !inLineNumber.equals(that.inLineNumber) : that.inLineNumber != null) return false;
        if (instrumentEarlyOrInitialGain != null ? !instrumentEarlyOrInitialGain.equals(that.instrumentEarlyOrInitialGain) : that.instrumentEarlyOrInitialGain != null)
            return false;
        if (instrumentGainConstant != null ? !instrumentGainConstant.equals(that.instrumentGainConstant) : that.instrumentGainConstant != null)
            return false;
        if (lagTimeA != null ? !lagTimeA.equals(that.lagTimeA) : that.lagTimeA != null) return false;
        if (lagTimeB != null ? !lagTimeB.equals(that.lagTimeB) : that.lagTimeB != null) return false;
        if (lowCutFrequency != null ? !lowCutFrequency.equals(that.lowCutFrequency) : that.lowCutFrequency != null)
            return false;
        if (lowCutSlope != null ? !lowCutSlope.equals(that.lowCutSlope) : that.lowCutSlope != null) return false;
        if (minuteOfHour != null ? !minuteOfHour.equals(that.minuteOfHour) : that.minuteOfHour != null) return false;
        if (muteTimeEnd != null ? !muteTimeEnd.equals(that.muteTimeEnd) : that.muteTimeEnd != null) return false;
        if (muteTimeStart != null ? !muteTimeStart.equals(that.muteTimeStart) : that.muteTimeStart != null)
            return false;
        if (notchFilterFrequency != null ? !notchFilterFrequency.equals(that.notchFilterFrequency) : that.notchFilterFrequency != null)
            return false;
        if (notchFilterSlope != null ? !notchFilterSlope.equals(that.notchFilterSlope) : that.notchFilterSlope != null)
            return false;
        if (numberOfHorizontallyStackedTraces != null ? !numberOfHorizontallyStackedTraces.equals(that.numberOfHorizontallyStackedTraces) : that.numberOfHorizontallyStackedTraces != null)
            return false;
        if (numberOfSamples != null ? !numberOfSamples.equals(that.numberOfSamples) : that.numberOfSamples != null)
            return false;
        if (numberOfVerticallySummedTraces != null ? !numberOfVerticallySummedTraces.equals(that.numberOfVerticallySummedTraces) : that.numberOfVerticallySummedTraces != null)
            return false;
        if (!Arrays.equals(optional, that.optional)) return false;
        if (originalFieldRecordNumber != null ? !originalFieldRecordNumber.equals(that.originalFieldRecordNumber) : that.originalFieldRecordNumber != null)
            return false;
        if (overTravel != null ? !overTravel.equals(that.overTravel) : that.overTravel != null) return false;
        if (receiverGroupElevation != null ? !receiverGroupElevation.equals(that.receiverGroupElevation) : that.receiverGroupElevation != null)
            return false;
        if (sampleIntervalInMcs != null ? !sampleIntervalInMcs.equals(that.sampleIntervalInMcs) : that.sampleIntervalInMcs != null)
            return false;
        if (scalarForCoordinates != null ? !scalarForCoordinates.equals(that.scalarForCoordinates) : that.scalarForCoordinates != null)
            return false;
        if (scalarForElevations != null ? !scalarForElevations.equals(that.scalarForElevations) : that.scalarForElevations != null)
            return false;
        if (scalarForSPNumber != null ? !scalarForSPNumber.equals(that.scalarForSPNumber) : that.scalarForSPNumber != null)
            return false;
        if (scalarToBeAppliedToTimes != null ? !scalarToBeAppliedToTimes.equals(that.scalarToBeAppliedToTimes) : that.scalarToBeAppliedToTimes != null)
            return false;
        if (secondOfMinute != null ? !secondOfMinute.equals(that.secondOfMinute) : that.secondOfMinute != null)
            return false;
        if (shotpointNumber != null ? !shotpointNumber.equals(that.shotpointNumber) : that.shotpointNumber != null)
            return false;
        if (sourceDepthBelowSurface != null ? !sourceDepthBelowSurface.equals(that.sourceDepthBelowSurface) : that.sourceDepthBelowSurface != null)
            return false;
        if (!Arrays.equals(sourceEnergyDirection, that.sourceEnergyDirection)) return false;
        if (!Arrays.equals(sourceMeasurement, that.sourceMeasurement)) return false;
        if (sourceMeasurementUnit != null ? !sourceMeasurementUnit.equals(that.sourceMeasurementUnit) : that.sourceMeasurementUnit != null)
            return false;
        if (sourceStaticCorrectionInMs != null ? !sourceStaticCorrectionInMs.equals(that.sourceStaticCorrectionInMs) : that.sourceStaticCorrectionInMs != null)
            return false;
        if (sourceTypeOrientation != null ? !sourceTypeOrientation.equals(that.sourceTypeOrientation) : that.sourceTypeOrientation != null)
            return false;
        if (sourceX != null ? !sourceX.equals(that.sourceX) : that.sourceX != null) return false;
        if (sourceY != null ? !sourceY.equals(that.sourceY) : that.sourceY != null) return false;
        if (subweatheringVelocity != null ? !subweatheringVelocity.equals(that.subweatheringVelocity) : that.subweatheringVelocity != null)
            return false;
        if (surfaceElevationAtSource != null ? !surfaceElevationAtSource.equals(that.surfaceElevationAtSource) : that.surfaceElevationAtSource != null)
            return false;
        if (sweepFrequencyAtEnd != null ? !sweepFrequencyAtEnd.equals(that.sweepFrequencyAtEnd) : that.sweepFrequencyAtEnd != null)
            return false;
        if (sweepFrequencyAtStart != null ? !sweepFrequencyAtStart.equals(that.sweepFrequencyAtStart) : that.sweepFrequencyAtStart != null)
            return false;
        if (sweepLengthInMilliseconds != null ? !sweepLengthInMilliseconds.equals(that.sweepLengthInMilliseconds) : that.sweepLengthInMilliseconds != null)
            return false;
        if (sweepTraceTaperLengthAtEndInMilliseconds != null ? !sweepTraceTaperLengthAtEndInMilliseconds.equals(that.sweepTraceTaperLengthAtEndInMilliseconds) : that.sweepTraceTaperLengthAtEndInMilliseconds != null)
            return false;
        if (sweepTraceTaperLengthAtStartInMilliseconds != null ? !sweepTraceTaperLengthAtStartInMilliseconds.equals(that.sweepTraceTaperLengthAtStartInMilliseconds) : that.sweepTraceTaperLengthAtStartInMilliseconds != null)
            return false;
        if (sweepType != null ? !sweepType.equals(that.sweepType) : that.sweepType != null) return false;
        if (taperType != null ? !taperType.equals(that.taperType) : that.taperType != null) return false;
        if (timeBasisCode != null ? !timeBasisCode.equals(that.timeBasisCode) : that.timeBasisCode != null)
            return false;
        if (totalStaticAppliedInMs != null ? !totalStaticAppliedInMs.equals(that.totalStaticAppliedInMs) : that.totalStaticAppliedInMs != null)
            return false;
        if (traceIdentificationCode != that.traceIdentificationCode) return false;
        if (traceNumberWEnsemble != null ? !traceNumberWEnsemble.equals(that.traceNumberWEnsemble) : that.traceNumberWEnsemble != null)
            return false;
        if (traceNumberWOFR != null ? !traceNumberWOFR.equals(that.traceNumberWOFR) : that.traceNumberWOFR != null)
            return false;
        if (traceSequenceNumberWL != null ? !traceSequenceNumberWL.equals(that.traceSequenceNumberWL) : that.traceSequenceNumberWL != null)
            return false;
        if (traceSequenceNumberWS != null ? !traceSequenceNumberWS.equals(that.traceSequenceNumberWS) : that.traceSequenceNumberWS != null)
            return false;
        if (traceValuesMU != null ? !traceValuesMU.equals(that.traceValuesMU) : that.traceValuesMU != null)
            return false;
        if (traceWeightingFactor != null ? !traceWeightingFactor.equals(that.traceWeightingFactor) : that.traceWeightingFactor != null)
            return false;
        if (!Arrays.equals(transductionConstant, that.transductionConstant)) return false;
        if (transductionUnits != null ? !transductionUnits.equals(that.transductionUnits) : that.transductionUnits != null)
            return false;
        if (upholeTimeAtGroupInMs != null ? !upholeTimeAtGroupInMs.equals(that.upholeTimeAtGroupInMs) : that.upholeTimeAtGroupInMs != null)
            return false;
        if (upholeTimeAtSourceInMs != null ? !upholeTimeAtSourceInMs.equals(that.upholeTimeAtSourceInMs) : that.upholeTimeAtSourceInMs != null)
            return false;
        if (waterDepthAtGroup != null ? !waterDepthAtGroup.equals(that.waterDepthAtGroup) : that.waterDepthAtGroup != null)
            return false;
        if (waterDepthAtSource != null ? !waterDepthAtSource.equals(that.waterDepthAtSource) : that.waterDepthAtSource != null)
            return false;
        if (weatheringVelocity != null ? !weatheringVelocity.equals(that.weatheringVelocity) : that.weatheringVelocity != null)
            return false;
        if (xOfCDPPosition != null ? !xOfCDPPosition.equals(that.xOfCDPPosition) : that.xOfCDPPosition != null)
            return false;
        if (yOfCDPPosition != null ? !yOfCDPPosition.equals(that.yOfCDPPosition) : that.yOfCDPPosition != null)
            return false;
        if (yearDataRecorded != null ? !yearDataRecorded.equals(that.yearDataRecorded) : that.yearDataRecorded != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = traceSequenceNumberWL != null ? traceSequenceNumberWL.hashCode() : 0;
        result = 31 * result + (traceSequenceNumberWS != null ? traceSequenceNumberWS.hashCode() : 0);
        result = 31 * result + (originalFieldRecordNumber != null ? originalFieldRecordNumber.hashCode() : 0);
        result = 31 * result + (traceNumberWOFR != null ? traceNumberWOFR.hashCode() : 0);
        result = 31 * result + (energySourcePointNumber != null ? energySourcePointNumber.hashCode() : 0);
        result = 31 * result + (ensembleNumber != null ? ensembleNumber.hashCode() : 0);
        result = 31 * result + (traceNumberWEnsemble != null ? traceNumberWEnsemble.hashCode() : 0);
        result = 31 * result + (traceIdentificationCode != null ? traceIdentificationCode.hashCode() : 0);
        result = 31 * result + (numberOfVerticallySummedTraces != null ? numberOfVerticallySummedTraces.hashCode() : 0);
        result = 31 * result + (numberOfHorizontallyStackedTraces != null ? numberOfHorizontallyStackedTraces.hashCode() : 0);
        result = 31 * result + (dataUse != null ? dataUse.hashCode() : 0);
        result = 31 * result + (distanceFromTheCenterOfSP != null ? distanceFromTheCenterOfSP.hashCode() : 0);
        result = 31 * result + (receiverGroupElevation != null ? receiverGroupElevation.hashCode() : 0);
        result = 31 * result + (surfaceElevationAtSource != null ? surfaceElevationAtSource.hashCode() : 0);
        result = 31 * result + (sourceDepthBelowSurface != null ? sourceDepthBelowSurface.hashCode() : 0);
        result = 31 * result + (datumElevationAtReceiverGroup != null ? datumElevationAtReceiverGroup.hashCode() : 0);
        result = 31 * result + (datumElevationAtSource != null ? datumElevationAtSource.hashCode() : 0);
        result = 31 * result + (waterDepthAtSource != null ? waterDepthAtSource.hashCode() : 0);
        result = 31 * result + (waterDepthAtGroup != null ? waterDepthAtGroup.hashCode() : 0);
        result = 31 * result + (scalarForElevations != null ? scalarForElevations.hashCode() : 0);
        result = 31 * result + (scalarForCoordinates != null ? scalarForCoordinates.hashCode() : 0);
        result = 31 * result + (sourceX != null ? sourceX.hashCode() : 0);
        result = 31 * result + (sourceY != null ? sourceY.hashCode() : 0);
        result = 31 * result + (groupX != null ? groupX.hashCode() : 0);
        result = 31 * result + (groupY != null ? groupY.hashCode() : 0);
        result = 31 * result + (coordinateUnitsCode != null ? coordinateUnitsCode.hashCode() : 0);
        result = 31 * result + (weatheringVelocity != null ? weatheringVelocity.hashCode() : 0);
        result = 31 * result + (subweatheringVelocity != null ? subweatheringVelocity.hashCode() : 0);
        result = 31 * result + (upholeTimeAtSourceInMs != null ? upholeTimeAtSourceInMs.hashCode() : 0);
        result = 31 * result + (upholeTimeAtGroupInMs != null ? upholeTimeAtGroupInMs.hashCode() : 0);
        result = 31 * result + (sourceStaticCorrectionInMs != null ? sourceStaticCorrectionInMs.hashCode() : 0);
        result = 31 * result + (groupStaticCorrectionInMs != null ? groupStaticCorrectionInMs.hashCode() : 0);
        result = 31 * result + (totalStaticAppliedInMs != null ? totalStaticAppliedInMs.hashCode() : 0);
        result = 31 * result + (lagTimeA != null ? lagTimeA.hashCode() : 0);
        result = 31 * result + (lagTimeB != null ? lagTimeB.hashCode() : 0);
        result = 31 * result + (delayRecordingTime != null ? delayRecordingTime.hashCode() : 0);
        result = 31 * result + (muteTimeStart != null ? muteTimeStart.hashCode() : 0);
        result = 31 * result + (muteTimeEnd != null ? muteTimeEnd.hashCode() : 0);
        result = 31 * result + (numberOfSamples != null ? numberOfSamples.hashCode() : 0);
        result = 31 * result + (sampleIntervalInMcs != null ? sampleIntervalInMcs.hashCode() : 0);
        result = 31 * result + (gainTypeForInstruments != null ? gainTypeForInstruments.hashCode() : 0);
        result = 31 * result + (instrumentGainConstant != null ? instrumentGainConstant.hashCode() : 0);
        result = 31 * result + (instrumentEarlyOrInitialGain != null ? instrumentEarlyOrInitialGain.hashCode() : 0);
        result = 31 * result + (correlated != null ? correlated.hashCode() : 0);
        result = 31 * result + (sweepFrequencyAtStart != null ? sweepFrequencyAtStart.hashCode() : 0);
        result = 31 * result + (sweepFrequencyAtEnd != null ? sweepFrequencyAtEnd.hashCode() : 0);
        result = 31 * result + (sweepLengthInMilliseconds != null ? sweepLengthInMilliseconds.hashCode() : 0);
        result = 31 * result + (sweepType != null ? sweepType.hashCode() : 0);
        result = 31 * result + (sweepTraceTaperLengthAtStartInMilliseconds != null ? sweepTraceTaperLengthAtStartInMilliseconds.hashCode() : 0);
        result = 31 * result + (sweepTraceTaperLengthAtEndInMilliseconds != null ? sweepTraceTaperLengthAtEndInMilliseconds.hashCode() : 0);
        result = 31 * result + (taperType != null ? taperType.hashCode() : 0);
        result = 31 * result + (aliasFilterFrequency != null ? aliasFilterFrequency.hashCode() : 0);
        result = 31 * result + (aliasFilterSlope != null ? aliasFilterSlope.hashCode() : 0);
        result = 31 * result + (notchFilterFrequency != null ? notchFilterFrequency.hashCode() : 0);
        result = 31 * result + (notchFilterSlope != null ? notchFilterSlope.hashCode() : 0);
        result = 31 * result + (lowCutFrequency != null ? lowCutFrequency.hashCode() : 0);
        result = 31 * result + (highCutFrequency != null ? highCutFrequency.hashCode() : 0);
        result = 31 * result + (lowCutSlope != null ? lowCutSlope.hashCode() : 0);
        result = 31 * result + (highCutSlope != null ? highCutSlope.hashCode() : 0);
        result = 31 * result + (yearDataRecorded != null ? yearDataRecorded.hashCode() : 0);
        result = 31 * result + (dayOfYear != null ? dayOfYear.hashCode() : 0);
        result = 31 * result + (hourOfDay != null ? hourOfDay.hashCode() : 0);
        result = 31 * result + (minuteOfHour != null ? minuteOfHour.hashCode() : 0);
        result = 31 * result + (secondOfMinute != null ? secondOfMinute.hashCode() : 0);
        result = 31 * result + (timeBasisCode != null ? timeBasisCode.hashCode() : 0);
        result = 31 * result + (traceWeightingFactor != null ? traceWeightingFactor.hashCode() : 0);
        result = 31 * result + (geophoneGroupNumberOfRollSwitchPositionOne != null ? geophoneGroupNumberOfRollSwitchPositionOne.hashCode() : 0);
        result = 31 * result + (geophoneGroupNumberOfTraceNumberOneWOFR != null ? geophoneGroupNumberOfTraceNumberOneWOFR.hashCode() : 0);
        result = 31 * result + (geophoneGroupNumberOfLastTraceWOFR != null ? geophoneGroupNumberOfLastTraceWOFR.hashCode() : 0);
        result = 31 * result + (gapSize != null ? gapSize.hashCode() : 0);
        result = 31 * result + (overTravel != null ? overTravel.hashCode() : 0);
        result = 31 * result + (xOfCDPPosition != null ? xOfCDPPosition.hashCode() : 0);
        result = 31 * result + (yOfCDPPosition != null ? yOfCDPPosition.hashCode() : 0);
        result = 31 * result + (inLineNumber != null ? inLineNumber.hashCode() : 0);
        result = 31 * result + (crossLineNumber != null ? crossLineNumber.hashCode() : 0);
        result = 31 * result + (shotpointNumber != null ? shotpointNumber.hashCode() : 0);
        result = 31 * result + (scalarForSPNumber != null ? scalarForSPNumber.hashCode() : 0);
        result = 31 * result + (traceValuesMU != null ? traceValuesMU.hashCode() : 0);
        result = 31 * result + (transductionConstant != null ? Arrays.hashCode(transductionConstant) : 0);
        result = 31 * result + (transductionUnits != null ? transductionUnits.hashCode() : 0);
        result = 31 * result + (deviceTraceIdentifier != null ? deviceTraceIdentifier.hashCode() : 0);
        result = 31 * result + (scalarToBeAppliedToTimes != null ? scalarToBeAppliedToTimes.hashCode() : 0);
        result = 31 * result + (sourceTypeOrientation != null ? sourceTypeOrientation.hashCode() : 0);
        result = 31 * result + (sourceEnergyDirection != null ? Arrays.hashCode(sourceEnergyDirection) : 0);
        result = 31 * result + (sourceMeasurement != null ? Arrays.hashCode(sourceMeasurement) : 0);
        result = 31 * result + (sourceMeasurementUnit != null ? sourceMeasurementUnit.hashCode() : 0);
        result = 31 * result + (optional != null ? Arrays.hashCode(optional) : 0);
        return result;
    }

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
}