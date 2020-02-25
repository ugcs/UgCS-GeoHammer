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
import com.ugcs.gprvisualizer.gpr.SgyLoader;
import com.ugcs.gprvisualizer.math.CoordinatesMath;
import com.ugcs.gprvisualizer.math.ManuilovFilter;

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
	
	private List<BaseObject> auxElements = new ArrayList<>();
	
	public void open(File file) throws Exception {
		this.file = file;		
		
		BinFile binFile = BinFile.load(file); 
		
		txtHdr = binFile.getTxtHdr();
		binHdr = binFile.getBinHdr();
		
		binaryHeader = binaryHeaderReader.read(binFile.getBinHdr());
		
		
		setTraces(loadTraces(binFile));
		
		updateInternalIndexes();
		
		markToAux();		
		
		//new ManuilovFilter().filter(getTraces());
		
		updateInternalDist();
		
		System.out.println("opened  '"+file.getName() + "'   load size: " + getTraces().size() + "  actual size: " + binFile.getTraces().size());
		
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
        
        SeismicValuesConverter converter = ConverterFactory.getConverter(binaryHeader.getDataSampleCode());
        
        final float[] values = converter.convert(binTrace.data);        
        
        LatLon latLon = getLatLon(header);
        if(latLon == null) {
        	return null;
        }
        
        Trace trace = new Trace(headerBin, header, values, latLon);
        if(headerBin[MARK_BYTE_POS] != 0 ) {
        	System.out.println("mark " +headerBin[MARK_BYTE_POS]);
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
		_logSaveMarks(file, marks);
		
		
		
		BinFile binFile = new BinFile();
		
		binFile.setTxtHdr(txtHdr);
		binFile.setBinHdr(binHdr);
		
		for(Trace trace : traces) {
			BinTrace binTrace = new BinTrace();
			
			binTrace.header = trace.getBinHeader();
			
			//set or clear marks
			if(marks.contains(trace.indexInFile)) {
				System.out.println("marks.contains(trace.indexInFile) " + trace.indexInFile);
			}
			binTrace.header[MARK_BYTE_POS] = (byte)(marks.contains(trace.indexInFile) ? -1 : 0);
			
			binTrace.data = ByteBufferHolder.valuesToByteBuffer(trace.getNormValues()).array();
			
			binFile.getTraces().add(binTrace);
		}		
		
		binFile.save(file);
		
		System.out.println("saved  "+file + "  size: " + getTraces().size());
	}

	public void _logSaveMarks(File file, Set<Integer> marks) {
		System.out.println("prepare marks   " + file.getName());
		for(Integer i : marks) {
			System.out.print(i + "  ");
		}
		System.out.println();
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
	
//	public void savePart(String fileName, List<ByteBufferProducer> blocks) throws IOException {
//		
//		BlockFile blockFile = BlockFile.open(file);
//		
//		FileOutputStream fos = new FileOutputStream(fileName);
//		FileChannel writechan = fos.getChannel();		
//		
//		writechan.write(ByteBuffer.wrap(txtHdrBlock.read(blockFile).array()));
//		writechan.write(ByteBuffer.wrap(binHdrBlock.read(blockFile).array()));		
//
//		for(ByteBufferProducer block : blocks) {
//			writechan.write(ByteBuffer.wrap(block.read(blockFile).array()));
//		}		
//		
//		writechan.close();
//		fos.close();		
//		
//		blockFile.close();
//	}

	public List<Trace> getTraces() {
		return traces;
	}

	public void setTraces(List<Trace> traces) {
		this.traces = traces;
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
}
