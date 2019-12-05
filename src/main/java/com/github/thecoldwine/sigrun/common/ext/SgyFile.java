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
import java.util.List;

import com.github.thecoldwine.sigrun.common.BinaryHeader;
import com.github.thecoldwine.sigrun.common.ConverterFactory;
import com.github.thecoldwine.sigrun.common.SeismicTrace;
import com.github.thecoldwine.sigrun.common.TextHeader;
import com.github.thecoldwine.sigrun.common.TraceHeader;
import com.github.thecoldwine.sigrun.converters.SeismicValuesConverter;
import com.github.thecoldwine.sigrun.serialization.BinaryHeaderFormat;
import com.github.thecoldwine.sigrun.serialization.BinaryHeaderReader;
import com.github.thecoldwine.sigrun.serialization.TextHeaderReader;
import com.github.thecoldwine.sigrun.serialization.TraceHeaderFormat;
import com.github.thecoldwine.sigrun.serialization.TraceHeaderReader;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.gpr.SgyLoader;
import com.ugcs.gprvisualizer.math.CoordinatesMath;

public class SgyFile {

	private static final Charset charset = Charset.forName("UTF8");
    private static final BinaryHeaderFormat binaryHeaderFormat = SgyLoader.makeBinHeaderFormat();
    private static final TraceHeaderFormat traceHeaderFormat = SgyLoader.makeTraceHeaderFormat();

    private static final TextHeaderReader textHeaderReader = new TextHeaderReader(charset);
    private static final BinaryHeaderReader binaryHeaderReader = new BinaryHeaderReader(binaryHeaderFormat);
    private static final TraceHeaderReader traceHeaderReader = new TraceHeaderReader(traceHeaderFormat);
	
	
    private BinaryHeader binaryHeader; 
    private List<Trace> traces; 
    
    private VerticalCutPart offset = new VerticalCutPart();
    
	private Block txtHdrBlock;
	private Block binHdrBlock;
	private File file;
	private int currentTraceIndex = 0;
	
	private List<BaseObject> auxElements = new ArrayList<>();
	
	public void open(File file) throws IOException {
		this.file = file;		
		
		BlockFile blockFile = BlockFile.open(file);
		
		txtHdrBlock = blockFile.next(TextHeader.TEXT_HEADER_SIZE);
		binHdrBlock = blockFile.next(BinaryHeader.BIN_HEADER_LENGTH);
		
		binaryHeader = binaryHeaderReader.read(binHdrBlock.read(blockFile).array());
		
		
		setTraces(loadTraces(blockFile));
		
		blockFile.close();
	}
	
	private List<Trace> loadTraces(BlockFile blockFile){
		List<Trace> traces = new ArrayList<>();
		
		try {
			Trace tracePrev = null;
			while(blockFile.hasNext()) {
				
				Trace trace = next(blockFile);
				
				if(tracePrev != null) {
					trace.setPrevDist(CoordinatesMath.measure(
						tracePrev.getLatLon().getLatDgr(), tracePrev.getLatLon().getLonDgr(), 
						trace.getLatLon().getLatDgr(), trace.getLatLon().getLonDgr()));
				}
				tracePrev = trace;
				
				if(trace != null) {
					traces.add(trace);
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			
			System.out.println("traces.size(): " + traces.size() );
		}
		
		if(!traces.isEmpty()) {
			traces.get(traces.size()-1).setEnd(true);
		}
		
		return traces;
	}
	
	public Trace next(BlockFile blockFile) throws IOException {
		
		Block traceHdrBlock = blockFile.next(TraceHeader.TRACE_HEADER_LENGTH);
        TraceHeader header = traceHeaderReader.read(traceHdrBlock.read(blockFile).array());
        int dataLength = binaryHeader.getDataSampleCode().getSize() * header.getNumberOfSamples();
		
        Block traceDataBlock = blockFile.next(dataLength);
        
        
        SeismicValuesConverter converter = ConverterFactory.getConverter(binaryHeader.getDataSampleCode());
        final float[] values = converter.convert(traceDataBlock.read(blockFile).array());
        
        
        LatLon latLon = getLatLon(header);
        if(latLon == null) {
        	return null;
        }
        Trace trace = new Trace(traceHdrBlock, traceDataBlock, header, values, latLon);
        trace.indexInFile = currentTraceIndex;
        currentTraceIndex++;
        
        return trace;
		
	}

	LatLon getLatLon(TraceHeader header) {
		double lon = retrieveVal(header.getLongitude(), header.getSourceX()); 
		double lat = retrieveVal(header.getLatitude(), header.getSourceY()); 

		if (Math.abs(lon) < 0.1 && Math.abs(lat) < 0.1) {
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

	public void saveTraces(File file, List<Trace> traces) throws IOException {
		
		
		
		FileOutputStream fos = new FileOutputStream(file);
		FileChannel writechan = fos.getChannel();		
		
		BlockFile blockFile = BlockFile.open(file);
		writechan.write(ByteBuffer.wrap(txtHdrBlock.read(blockFile).array()));
		writechan.write(ByteBuffer.wrap(binHdrBlock.read(blockFile).array()));		
		
		for(Trace trace : traces) {
			write(blockFile, writechan, trace.getHeaderBlock());
			//write(writechan, trace.getDataBlock());
			writechan.write(ByteBufferHolder.valuesToByteBuffer(trace.getNormValues()));
		}		
		
		writechan.close();
		fos.close();		
		
	}
	
	protected void write(BlockFile blockFile, FileChannel writechan, Block block) throws IOException {
		writechan.write(ByteBuffer.wrap(block.read(blockFile).array()));
	}
	
	public void savePart(String fileName, List<ByteBufferProducer> blocks) throws IOException {
		
		BlockFile blockFile = BlockFile.open(file);
		
		FileOutputStream fos = new FileOutputStream(fileName);
		FileChannel writechan = fos.getChannel();		
		
		writechan.write(ByteBuffer.wrap(txtHdrBlock.read(blockFile).array()));
		writechan.write(ByteBuffer.wrap(binHdrBlock.read(blockFile).array()));		

		for(ByteBufferProducer block : blocks) {
			writechan.write(ByteBuffer.wrap(block.read(blockFile).array()));
		}		
		
		writechan.close();
		fos.close();		
		
		blockFile.close();
	}

	public List<Trace> getTraces() {
		return traces;
	}

	private void setTraces(List<Trace> traces) {
		this.traces = traces;
	}

	public File getFile() {
		return file;
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

	public void setOffset(VerticalCutPart offset) {
		this.offset = offset;
	}
	
}
