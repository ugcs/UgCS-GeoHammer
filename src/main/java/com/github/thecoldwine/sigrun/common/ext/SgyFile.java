package com.github.thecoldwine.sigrun.common.ext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.thecoldwine.sigrun.common.BinaryHeader;
import com.github.thecoldwine.sigrun.common.ConverterFactory;
import com.github.thecoldwine.sigrun.common.SeismicTrace;
import com.github.thecoldwine.sigrun.common.TextHeader;
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
import com.ugcs.gprvisualizer.app.commands.EdgeFinder;
import com.ugcs.gprvisualizer.gpr.SgyLoader;
import com.ugcs.gprvisualizer.math.CoordinatesMath;
import com.ugcs.gprvisualizer.math.HorizontalProfile;
import com.ugcs.gprvisualizer.math.ManuilovFilter;
import com.ugcs.gprvisualizer.math.ScanProfile;

public class SgyFile {
	

	private static final int MARK_BYTE_POS = 238;
	private static final Charset charset = Charset.forName("UTF8");
    private static final BinaryHeaderFormat binaryHeaderFormat = SgyLoader.makeBinHeaderFormat();
    private static final TraceHeaderFormat traceHeaderFormat = SgyLoader.makeTraceHeaderFormat();

    public static final TextHeaderReader textHeaderReader = new TextHeaderReader(charset);
    public static final BinaryHeaderReader binaryHeaderReader = new BinaryHeaderReader(binaryHeaderFormat);
    public static final TraceHeaderReader traceHeaderReader = new TraceHeaderReader(traceHeaderFormat);
	
    //unchanged blocks from original file
	private byte[] txtHdr;
	private byte[] binHdr;
	
    private BinaryHeader binaryHeader; 
    private List<Trace> traces; 
    
    private VerticalCutPart offset = new VerticalCutPart();
    
	private File file;
	private int currentTraceIndex = 0;
	
	private boolean unsaved = true;
	
	
    //horizontal cohesive lines of edges
    public List<HorizontalProfile> profiles;
    public HorizontalProfile groundProfile;
    
    // hyperbola probability calculated by AlgoritmicScan
    public ScanProfile algoScan;
    
    // amplitude
    public ScanProfile amplScan;
    
	
	private List<BaseObject> auxElements = new ArrayList<>();
	
	public void open(File file) throws Exception {
		this.file = file;		
		
		BinFile binFile = BinFile.load(file); 
		
		txtHdr = binFile.getTxtHdr();
		binHdr = binFile.getBinHdr();
		
		binaryHeader = binaryHeaderReader.read(binFile.getBinHdr());
		
		//System.out.println("dataTracesPerEnsemble " + binaryHeader.getDataTracesPerEnsemble());		
		
		System.out.println("binaryHeader.getSampleInterval() " + binaryHeader.getSampleInterval());
		//System.out.println("binaryHeader.getSampleIntervalOfOFR() " + binaryHeader.getSampleIntervalOfOFR());
		
		//System.out.println("ReelNumber            " + binaryHeader.getReelNumber());
		//System.out.println("DataTracesPerEnsemble " + binaryHeader.getDataTracesPerEnsemble());
		//System.out.println("AuxiliaryTracesPerEnsemble " + binaryHeader.getAuxiliaryTracesPerEnsemble());
		
		
		System.out.println("SamplesPerDataTrace " + binaryHeader.getSamplesPerDataTrace());
		//System.out.println("SamplesPerDataTraceOfOFR " + binaryHeader.getSamplesPerDataTraceOfOFR());
		
		//System.out.println("SweepLength " + binaryHeader.getSweepLength());
		
		setTraces(loadTraces(binFile));
		
		//////////
		
		//sampleIntervalInMcs
		Trace t = getTraces().get(getTraces().size()/2);
		System.out.println("SampleIntervalInMcs: " + t.getHeader().getSampleIntervalInMcs());
		
		System.out.println( " sgyFile.SamplesToCmAir: " + getSamplesToCmAir());
		System.out.println( " sgyFile.SamplesToCmGrn: " + getSamplesToCmGrn());
//		System.out.println("SampleIntervalInMcs: " + t.getHeader().getReeDelayRecordingTime());
//		System.out.println("SampleIntervalInMcs: " + t.getHeader().getDelayRecordingTime());
//		System.out.println("SampleIntervalInMcs: " + t.getHeader().getGapSize());
//		System.out.println("SampleIntervalInMcs: " + t.getHeader().getGroupStaticCorrectionInMs());
//		System.out.println("SampleIntervalInMcs: " + t.getHeader().getMuteTimeStart());
//		System.out.println("SampleIntervalInMcs: " + t.getHeader().getMuteTimeEnd());
		////////////
		
		updateInternalIndexes();
		
		markToAux();		
		
		new ManuilovFilter().filter(getTraces());
		
		updateInternalDist();
		
		setUnsaved(false);
		
		System.out.println("opened  '"+file.getName() + "'   load size: " + getTraces().size() + "  actual size: " + binFile.getTraces().size());
		
	}

	
	private static double SPEED_SM_NS_VACUUM = 30.0;
	private static double SPEED_SM_NS_SOIL = SPEED_SM_NS_VACUUM / 3.0;

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


	
	
	public void markToAux() {
		
		for(int i=0; i<traces.size(); i++) {
			Trace trace = traces.get(i);

			if(trace.isMarked()) {
				this.getAuxElements().add(new FoundPlace(trace.indexInFile, offset));
			}
		}
	}
	
	private List<Trace> loadTraces(BinFile binFile) throws Exception {
		List<Trace> traces = new ArrayList<>();
		

		Trace tracePrev = null;
		ctrace= 0;
		//while(blockFile.hasNext()) {
		for(BinTrace binTrace : binFile.getTraces()) {
			
			Trace trace = next(binTrace);
			ctrace++;
			if(trace == null) {
				continue;
			}
			
			traces.add(trace);			
		}
		
		//end mark
		//if(!traces.isEmpty()) {
		//	traces.get(traces.size()-1).setEnd(true);
		//}
		
		return traces;
	}
	
	public void updateInternalIndexes() {
		for(int i=0; i<traces.size(); i++) {
			traces.get(i).indexInFile = i;
			traces.get(i).setEnd(false);			
		}		
		traces.get(traces.size()-1).setEnd(true);
	}

	public void updateInternalDist() {
		traces.get(0).setPrevDist(0);		
		for(int i=1; i<traces.size(); i++) {
			Trace tracePrev = traces.get(i-1);
			Trace trace 	= traces.get(i);
			
			double dist = CoordinatesMath.measure(
				tracePrev.getLatLon().getLatDgr(), tracePrev.getLatLon().getLonDgr(), 
				trace.getLatLon().getLatDgr(), trace.getLatLon().getLonDgr());
			
			trace.setPrevDist(dist);
		}		
		
	}
	
	int ctrace= 0;
	public Trace next(BinTrace binTrace) throws IOException {
		
		byte[] headerBin = binTrace.header;		
        TraceHeader header = traceHeaderReader.read(headerBin);
        
        //System.out.println("getSecondOfMinute "+header.getSecondOfMinute());
        
        SeismicValuesConverter converter = ConverterFactory.getConverter(binaryHeader.getDataSampleCode());
        
        final float[] values = converter.convert(binTrace.data);        
        
        LatLon latLon = getLatLon(header);
        if(latLon == null) {
        	return null;
        }
        
        Trace trace = new Trace(headerBin, header, values, latLon);
        if(headerBin[MARK_BYTE_POS] != 0 ) {
        	//System.out.println("mark " +headerBin[MARK_BYTE_POS]);
        	trace.setMarked(true);
        }
        
        return trace;
		
	}

	LatLon getLatLon(TraceHeader header) {
		double lon = retrieveVal(header.getLongitude(), header.getSourceX()); 
		double lat = retrieveVal(header.getLatitude(), header.getSourceY()); 

		
		
		if (Double.isNaN(lon) || Double.isNaN(lat) ||
			Math.abs(lon) < 0.1 || Math.abs(lat) < 0.1 || 
			Math.abs(lon) > 18000 || Math.abs(lat) > 18000) {
		
			System.out.println( "bad lat lon  " + ctrace + " -> "+ lat + " " + lon  );
			return null;
		}

		
		// prism: 65.3063232422°N 40.0569335938°W
		// 65.3063232421875 -40.05693359375
		double rlon = convertDegreeFraction(lon);
		double rlat = convertDegreeFraction(lat);
		
		return new LatLon(rlat, rlon);
	}

	private double convertDegreeFraction(double org) {
		org = org / 100.0;
		int dgr = (int) org;
		double fract = org - dgr;
		double rx = dgr + fract / 60.0 * 100.0;
		return rx;
	}
	
	private double retrieveVal(Double v1, Float v2) {
		if(v1 != null && Math.abs(v1) > 0.01) {
			return v1;
		}
		return v2;
	}

	public void save(File file) throws Exception {
		
		Set<Integer> marks = prepareMarksIndexSet();
		BinFile binFile = new BinFile();
		
		binFile.setTxtHdr(txtHdr);
		binFile.setBinHdr(binHdr);
		
		SeismicValuesConverter converter = ConverterFactory.getConverter(binaryHeader.getDataSampleCode());
		
		for(Trace trace : traces) {
			BinTrace binTrace = new BinTrace();
			
			binTrace.header = trace.getBinHeader();
			
			//set or clear marks
			binTrace.header[MARK_BYTE_POS] = (byte)(marks.contains(trace.indexInFile) ? -1 : 0);
			
			binTrace.data = converter.valuesToByteBuffer(trace.getNormValues()).array();
			
			binFile.getTraces().add(binTrace);
		}		
		
		binFile.save(file);
		
		System.out.println("saved  "+file + "  size: " + getTraces().size());
	}

	private Set<Integer> prepareMarksIndexSet() {
		Set<Integer> result = new HashSet<>();
		
		for(BaseObject bo : getAuxElements()) {
			if(bo instanceof FoundPlace) {
				FoundPlace fp = (FoundPlace)bo;
				result.add(((FoundPlace) bo).getTraceInFile());
			}
		}
		
		return result;
	}
	
	protected void write(BlockFile blockFile, FileChannel writechan, Block block) throws IOException {
		writechan.write(ByteBuffer.wrap(block.read(blockFile).array()));
	}
	
	public List<Trace> getTraces() {
		return traces;
	}

	public void setTraces(List<Trace> traces) {
		this.traces = traces;
		
		
		new EdgeFinder().execute(this);
		
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public List<BaseObject> getAuxElements() {
		return auxElements;
	}

	public void setAuxElements(List<BaseObject> auxElements) {
		this.auxElements = auxElements;
	}

	public VerticalCutPart getOffset() {
		return offset;
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

	public boolean isUnsaved() {
		return unsaved;
	}

	public void setUnsaved(boolean unsaved) {
		this.unsaved = unsaved;
	}
	
	public int getMaxSamples() {
		return getTraces().get(0).getNormValues().length;
	}

	public SgyFile copy() {

		
		SgyFile file2 = new SgyFile();
		
		file2.setFile(this.getFile());
		
		List<Trace> traces = new ArrayList<>();
		for(Trace org : this.getTraces()){
			
			float[] values = Arrays.copyOf(org.getNormValues(), org.getNormValues().length);
			
			Trace tr = new Trace(org.getBinHeader(), org.getHeader(), values, org.getLatLon());
			traces.add(tr);
		}
		
		
		file2.setTraces(traces);
		
		return file2;
	}

	public int size() {
		return getTraces().size();
	}
}
