package com.github.thecoldwine.sigrun.common.ext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.thecoldwine.sigrun.common.BinaryHeader;
import com.github.thecoldwine.sigrun.common.ConverterFactory;
import com.github.thecoldwine.sigrun.common.TraceHeader;
import com.github.thecoldwine.sigrun.common.ext.BinFile.BinTrace;
import com.github.thecoldwine.sigrun.converters.SeismicValuesConverter;
import com.github.thecoldwine.sigrun.serialization.BinaryHeaderFormat;
import com.github.thecoldwine.sigrun.serialization.BinaryHeaderReader;
import com.github.thecoldwine.sigrun.serialization.TextHeaderReader;
import com.github.thecoldwine.sigrun.serialization.TraceHeaderFormat;
import com.github.thecoldwine.sigrun.serialization.TraceHeaderReader;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.gpr.SgyLoader;
import com.ugcs.gprvisualizer.math.ManuilovFilter;

public class GprFile extends SgyFile {
	
	private static final int MARK_BYTE_POS = 238;
	private static final Charset charset = Charset.forName("UTF8");
    private static final BinaryHeaderFormat binaryHeaderFormat 
    	= SgyLoader.makeBinHeaderFormat();
    private static final TraceHeaderFormat traceHeaderFormat 
    	= SgyLoader.makeTraceHeaderFormat();

    public static final TextHeaderReader textHeaderReader 
    	= new TextHeaderReader(charset);
    public static final BinaryHeaderReader binaryHeaderReader 
    	= new BinaryHeaderReader(binaryHeaderFormat);
    public static final TraceHeaderReader traceHeaderReader 
    	= new TraceHeaderReader(traceHeaderFormat);
	
    //unchanged blocks from original file
    private byte[] txtHdr;
    private byte[] binHdr;
    
    private BinaryHeader binaryHeader; 


	public void open(File file) throws Exception {
		this.file = file;		
		
		BinFile binFile = BinFile.load(file); 
		
		txtHdr = binFile.getTxtHdr();
		binHdr = binFile.getBinHdr();
		
		binaryHeader = binaryHeaderReader.read(binFile.getBinHdr());
		
		System.out.println("binaryHeader.getSampleInterval() " 
				+ binaryHeader.getSampleInterval());
		System.out.println("SamplesPerDataTrace " 
				+ binaryHeader.getSamplesPerDataTrace());
		setTraces(loadTraces(binFile));
		
		//sampleIntervalInMcs
		Trace t = getTraces().get(getTraces().size() / 2);
		System.out.println("SampleIntervalInMcs: " 
				+ t.getHeader().getSampleIntervalInMcs());
		
		System.out.println(" sgyFile.SamplesToCmAir: " + getSamplesToCmAir());
		System.out.println(" sgyFile.SamplesToCmGrn: " + getSamplesToCmGrn());
		
		updateInternalIndexes();
		
		markToAux();		
		
		new ManuilovFilter().filter(getTraces());
		
		updateInternalDist();
		
		setUnsaved(false);
		
		System.out.println("opened  '" + file.getName() 
			+ "'   load size: " + getTraces().size() 
			+ "  actual size: " + binFile.getTraces().size());
	}
    

	public Trace next(BinTrace binTrace) throws IOException {
		
		byte[] headerBin = binTrace.header;		
        TraceHeader header = traceHeaderReader.read(headerBin);
        
        SeismicValuesConverter converter = 
        		ConverterFactory.getConverter(binaryHeader.getDataSampleCode());
        
        final float[] values = converter.convert(binTrace.data);        
        
        LatLon latLon = getLatLon(header);
        if (latLon == null) {
        	return null;
        }
        
        Trace trace = new Trace(headerBin, header, values, latLon);
        if (headerBin[MARK_BYTE_POS] != 0) {
        	trace.setMarked(true);
        }
        
        return trace;
	}

	public void save(File file) throws Exception {
		
		Set<Integer> marks = prepareMarksIndexSet();
		BinFile binFile = new BinFile();
		
		binFile.setTxtHdr(txtHdr);
		binFile.setBinHdr(binHdr);
		
		SeismicValuesConverter converter = 
				ConverterFactory.getConverter(binaryHeader.getDataSampleCode());
		
		for (Trace trace : traces) {
			BinTrace binTrace = new BinTrace();
			
			binTrace.header = trace.getBinHeader();
			
			//set or clear marks
			binTrace.header[MARK_BYTE_POS] = 
					(byte) (marks.contains(trace.indexInFile) ? -1 : 0);
			
			binTrace.data = converter.valuesToByteBuffer(trace.getNormValues()).array();
			
			binFile.getTraces().add(binTrace);
		}		
		
		binFile.save(file);
	}

	public SgyFile copy() {

		
		SgyFile file2 = new GprFile();
		
		file2.setFile(this.getFile());
		
		List<Trace> traces = new ArrayList<>();
		for (Trace org : this.getTraces()) {
			
			float[] values = Arrays.copyOf(
					org.getNormValues(), org.getNormValues().length);
			
			Trace tr = new Trace(org.getBinHeader(), 
					org.getHeader(), values, org.getLatLon());
			traces.add(tr);
		}		
		
		file2.setTraces(traces);
		
		return file2;
	}

	public byte[] getTxtHdr() {
		return txtHdr;
	}

	public void setTxtHdr(byte[] d) {
		txtHdr = d;
	}

	public byte[] getBinHdr() {
		return binHdr;		
	}

	public void setBinHdr(byte[] d) {
		binHdr = d;		
	}
	
	public BinaryHeader getBinaryHeader() {
		return binaryHeader;
	}

	public void setBinaryHeader(BinaryHeader bh) {
		binaryHeader = bh;
	}
	
	public double getSamplesToCmGrn() {
		// dist between 2 samples
		double sampleIntervalNS = getBinaryHeader().getSampleInterval() / 1000.0;
		double sampleDist = SPEED_SM_NS_SOIL * sampleIntervalNS / 2;
		return sampleDist;
	}

	public double getSamplesToCmAir() {
		// dist between 2 samples
		double sampleIntervalNS = getBinaryHeader().getSampleInterval() / 1000.0;
		double sampleDist = SPEED_SM_NS_VACUUM * sampleIntervalNS / 2;
		return sampleDist;
	}
	
	private Set<Integer> prepareMarksIndexSet() {
		Set<Integer> result = new HashSet<>();
		
		for (BaseObject bo : getAuxElements()) {
			if (bo instanceof FoundPlace) {
				FoundPlace fp = (FoundPlace) bo;
				result.add(fp.getTraceInFile());
			}
		}
		
		return result;
	}
	
	private LatLon getLatLon(TraceHeader header) {
		double lon = retrieveVal(header.getLongitude(), header.getSourceX()); 
		double lat = retrieveVal(header.getLatitude(), header.getSourceY()); 
		
		if (Double.isNaN(lon) || Double.isNaN(lat) 
				|| Math.abs(lon) < 0.1 
				|| Math.abs(lat) < 0.1 
				|| Math.abs(lon) > 18000 
				|| Math.abs(lat) > 18000) {
		
			return null;
		}

		
		// prism: 65.3063232422°N 40.0569335938°W
		// 65.3063232421875 -40.05693359375
		double rlon = convertDegreeFraction(lon);
		double rlat = convertDegreeFraction(lat);
		
		return new LatLon(rlat, rlon);
	}

	private double retrieveVal(Double v1, Float v2) {
		if (v1 != null && Math.abs(v1) > 0.01) {
			return v1;
		}
		return v2;
	}

	private List<Trace> loadTraces(BinFile binFile) throws Exception {
		List<Trace> traces = new ArrayList<>();

		for (BinTrace binTrace : binFile.getTraces()) {
			
			Trace trace = next(binTrace);
			
			if (trace == null) {
				continue;
			}
			
			traces.add(trace);
		}
		
		return traces;
	}
	
	public int getSampleInterval() {
		return getBinaryHeader().getSampleInterval();
	}


	@Override
	public SgyFile copyHeader() {
		
		GprFile gprFile = new GprFile();
		gprFile.setBinHdr(this.getBinHdr());
		gprFile.setTxtHdr(this.getTxtHdr());		
		gprFile.setBinaryHeader(this.getBinaryHeader());
		
		return gprFile;
	}


	@Override
	public void saveAux(File file) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
