package com.ugcs.gprvisualizer.gpr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.thecoldwine.sigrun.common.BinaryHeader;
import com.github.thecoldwine.sigrun.common.ParseProgressListener;
import com.github.thecoldwine.sigrun.common.SEGYStream;
import com.github.thecoldwine.sigrun.common.SEGYStreamFactory;
import com.github.thecoldwine.sigrun.common.SeismicTrace;
import com.github.thecoldwine.sigrun.common.TextHeader;
import com.github.thecoldwine.sigrun.serialization.BinaryHeaderFormat;
import com.github.thecoldwine.sigrun.serialization.BinaryHeaderFormatBuilder;
import com.github.thecoldwine.sigrun.serialization.FormatEntry;
import com.github.thecoldwine.sigrun.serialization.TraceHeaderFormat;
import com.github.thecoldwine.sigrun.serialization.TraceHeaderFormatBuilder;
import com.ugcs.gprvisualizer.math.CalmanFilter;

public class SgyLoader {
    private static final Logger logger = LoggerFactory.getLogger(SgyLoader.class.getName());

    private boolean printLog = false;
    
    public SgyLoader(boolean printLog) {
    	this.printLog = printLog;
    }
    
    private static ParseProgressListener makeListener() {
        return new ParseProgressListener() {
            @Override
            public void progress(long read) {
                //System.out.println("Progress changed to: " + read);
            }
        };
    }

    private static Set<ParseProgressListener> makeListenerSet() {
        Set<ParseProgressListener> result = new HashSet<ParseProgressListener>();
        result.add(makeListener());

        return result;
    }

    public static BinaryHeaderFormat makeBinHeaderFormat() {
        return BinaryHeaderFormatBuilder.aBinaryHeaderFormat()
                .withLineNumberFormat(FormatEntry.create(4, 8))
                .withSampleIntervalFormat(FormatEntry.create(16, 18))
                .withSamplesPerDataTraceFormat(FormatEntry.create(20, 22))                
                .withDataSampleCodeFormat(FormatEntry.create(24, 26))
                .withSegyFormatRevNumberFormat(FormatEntry.create(300, 302))
                .withFixedLengthTraceFlagFormat(FormatEntry.create(302, 304))
                .withNumberOf3200ByteFormat(FormatEntry.create(304, 306))
                .build();
    }

    public static TraceHeaderFormat makeTraceHeaderFormat() {
        return TraceHeaderFormatBuilder.aTraceHeaderFormat().
        		
        		withTraceSequenceNumberWLFormat(FormatEntry.create(0, 4)).
        		
                withEnsembleNumberFormat(FormatEntry.create(20, 24)).
                
                withCoordinateUnitsCodeFormat(FormatEntry.create(70, 72)).
                withSourceXFormat(FormatEntry.create(72, 76)).
                withSourceYFormat(FormatEntry.create(76, 80)).
                withGroupXFormat(FormatEntry.create(80, 84)).
                withGroupYFormat(FormatEntry.create(84, 88)).
                withXOfCDPPositionFormat(FormatEntry.create(180, 184)).
                withYOfCDPPositionFormat(FormatEntry.create(184, 188)).
                withNumberOfSamplesFormat(FormatEntry.create(114, 116)).
                
                //withLongtitude(FormatEntry.create(72, 76)).
                build();
    }

    static public SEGYStreamFactory streamFactory = SEGYStreamFactory.create(
			Charset.forName("UTF8"),
	        makeBinHeaderFormat(),
	        makeTraceHeaderFormat());
	
    
    static public FilenameFilter filter = new FilenameFilter() {
	    public boolean accept(File dir, String name) {
	        return name.toLowerCase().endsWith(".sgy");
	    }};
	    
    
	    
	    
	public ScanBuilder processFileList(List<File> fileList){
		
		ScanBuilder scanBuilder = new ScanBuilder();
		
    	for(File fl : fileList){
        	
    		String path = fl.getAbsolutePath();
    		logger.info(path);
    	
    		try {
    			
				this.execute(path, scanBuilder);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
    	}

    	
    	
    	return scanBuilder;
	
	}
	
    public ScanBuilder processFolder(File folder){
    	
    	return processFileList(Arrays.asList(folder.listFiles(filter)));

    }

	public void execute(String path, ScanBuilder scanBuilder) throws FileNotFoundException {

		FileChannel chan = new FileInputStream(path).getChannel();

		SEGYStream segyStream = streamFactory.makeStream(chan, makeListenerSet());

		if(printLog) {
			printTextHeader(segyStream.getTextHeader());
			printBinHeaderInfo(segyStream.getBinaryHeader());
		}

		for (SeismicTrace trace : segyStream) {
			
			//System.out.println("TraceSequenceNumberWL " + trace.getHeader().getTraceSequenceNumberWL());
			//System.out.println("getxOfCDPPosition: " + trace.getHeader().getxOfCDPPosition() + "  getyOfCDPPosition: " + trace.getHeader().getyOfCDPPosition());
			//System.out.println("lonitude: " + trace.getHeader().getLongitude() + "  latitude: " + trace.getHeader().getLatitude());
			
			scanBuilder.put(
				retrieveVal(trace.getHeader().getLongitude(), trace.getHeader().getSourceX()), 
				retrieveVal(trace.getHeader().getLatitude(), trace.getHeader().getSourceY()), 
				trace.getValues());
		}
	}

	private double retrieveVal(Double v1, Float v2) {
		if(v1 != null && Math.abs(v1) > 0.01) {
			
			return v1;
		}
		return v2;
	}
	
	private static void printTextHeader(TextHeader header) {
        System.out.println("Text Header info...");
        
                
        for (String s : header.getContents()) {
            System.out.println(s);
        }
    }

    private static void printBinHeaderInfo(BinaryHeader binaryHeader) {
        System.out.println("Binary Header info...");
        System.out.println("Data sample code:" + binaryHeader.getDataSampleCode());
        
        //binaryHeader.getTraceSequenceNumberWL();
    }

    private static void printTraceInfo(SeismicTrace trace) {
        System.out.println("Trace Header info.    " + trace.getHeader().getNumberOfSamples() + " samples. " + trace.getHeader().getPositionInFile() 
        		+ " . " + trace.getHeader().getEnsembleNumber());
        
        System.out.println("   gps " +  trace.getHeader().getSourceX() + " " + trace.getHeader().getSourceY() + "  "  +trace.getHeader().getCoordinateUnitsCode());
        System.out.println(" group " +  trace.getHeader().getGroupX() + " " + trace.getHeader().getGroupY() );
        
        
        
    }
    
    static void printF(float[] ar){
    	StringBuilder sb = new StringBuilder();
    	for(float f : ar){
    		sb.append(", ");
    		sb.append(f);
    	}
    	System.out.println(sb.toString());
    }

    static String printB(byte[] ar){
    	StringBuilder sb = new StringBuilder();
    	for(byte f : ar){
    		sb.append(", ");
    		sb.append(f);
    	}
    	return sb.toString();
    }

} 