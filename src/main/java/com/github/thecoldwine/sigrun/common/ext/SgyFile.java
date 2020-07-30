package com.github.thecoldwine.sigrun.common.ext;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
import com.ugcs.gprvisualizer.app.Sout;
import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.app.commands.DistCalculator;
import com.ugcs.gprvisualizer.app.commands.DistancesSmoother;
import com.ugcs.gprvisualizer.app.commands.EdgeFinder;
import com.ugcs.gprvisualizer.app.commands.SpreadCoordinates;
import com.ugcs.gprvisualizer.gpr.SgyLoader;
import com.ugcs.gprvisualizer.math.CoordinatesMath;
import com.ugcs.gprvisualizer.math.HorizontalProfile;
import com.ugcs.gprvisualizer.math.ManuilovFilter;
import com.ugcs.gprvisualizer.math.ScanProfile;

public abstract class SgyFile {
	

    protected List<Trace> traces; 
    
    private VerticalCutPart offset = new VerticalCutPart();
    
	protected File file;
	
	private boolean unsaved = true;
	
	
    //horizontal cohesive lines of edges
    public List<HorizontalProfile> profiles;
    public HorizontalProfile groundProfile;
    
    // hyperbola probability calculated by AlgoritmicScan
    public ScanProfile algoScan;
    
    // amplitude
    public ScanProfile amplScan;
	private List<BaseObject> auxElements = new ArrayList<>();
	
	private boolean spreadCoordinatesNecessary = false;
	
	protected static double SPEED_SM_NS_VACUUM = 30.0;
	protected static double SPEED_SM_NS_SOIL = SPEED_SM_NS_VACUUM / 3.0;

	public abstract void open(File file) throws Exception;
	
	public abstract void save(File file) throws Exception;
	
	public abstract void saveAux(File file) throws Exception;
	
	public abstract double getSamplesToCmGrn();

	public abstract double getSamplesToCmAir();

	public abstract SgyFile copy();
	
	public abstract SgyFile copyHeader();
	
	public abstract int getSampleInterval();
	
	
	public void markToAux() {
		for (int i = 0; i < traces.size(); i++) {
			Trace trace = traces.get(i);

			if (trace.isMarked()) {
				this.getAuxElements().add(
						new FoundPlace(trace.indexInFile, offset));
			}
		}
	}
	
	public void updateInternalIndexes() {
		for (int i = 0; i < traces.size(); i++) {
			traces.get(i).indexInFile = i;
			traces.get(i).setEnd(false);			
		}		
		traces.get(traces.size() - 1).setEnd(true);
	}

	public void updateInternalDist() {
	//	calcDistances();
		
		
	//	prolongDistances();
		
		
		new DistCalculator().execute(this, null);
		
		setSpreadCoordinatesNecessary(SpreadCoordinates.isSpreadingNecessary(this));
		
		//smoothDistances();
		new DistancesSmoother().execute(this, null);
		
	}

	


	protected void write(BlockFile blockFile, FileChannel writechan, Block block) 
			throws IOException {
		writechan.write(ByteBuffer.wrap(block.read(blockFile).array()));
	}
	
	public List<Trace> getTraces() {
		return traces;
	}

	public void setTraces(List<Trace> traces) {
		this.traces = traces;
		new EdgeFinder().execute(this, null);
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



	public boolean isUnsaved() {
		return unsaved;
	}

	public void setUnsaved(boolean unsaved) {
		this.unsaved = unsaved;
	}
	
	public int getMaxSamples() {
		return getTraces().get(0).getNormValues().length;
	}

	public int size() {
		return getTraces().size();
	}
	
	public int getLeftDistTraceIndex(int traceIndex, double distCm) {
		
		return 
			Math.max(0,
			traceIndex - (int) (distCm / getTraces().get(traceIndex).getPrevDist()));
//		double sumDist = 0;
//		
//		while (traceIndex > 0 && sumDist < distCm) {
//			
//			sumDist += getTraces().get(traceIndex).getPrevDist();
//			traceIndex--;
//		}
//		
//		return traceIndex;
	}

	public int getRightDistTraceIndex(int traceIndex, double distCm) {
		
		return 
			Math.min(size()-1, 	
			traceIndex + (int) (distCm / getTraces().get(traceIndex).getPrevDist()));
		
//		double sumDist = 0;
//		
//		while (traceIndex < size() - 1 && sumDist < distCm) {
//			traceIndex++;
//			sumDist += getTraces().get(traceIndex).getPrevDist();
//			
//		}
//		
//		return traceIndex;
	}

	public static double convertDegreeFraction(double org) {
		org = org / 100.0;
		int dgr = (int) org;
		double fract = org - dgr;
		double rx = dgr + fract / 60.0 * 100.0;
		return rx;
	}

	public static double convertBackDegreeFraction(double org) {
		
		int dgr = (int) org;
		double fr = org - dgr;
		double fr2 = fr * 60.0 / 100.0;
		double r = 100.0 * (dgr + fr2);
		
		return r;
	}

	public int getGood(int tr, int s) {
		
		return getTraces().get(tr).good[s];
	}	

	public int getEdge(int tr, int s) {
		
		return getTraces().get(tr).edge[s];
	}	

	public float getVal(int tr, int s) {
		
		return getTraces().get(tr).getNormValues()[s];
	}

	public boolean isSpreadCoordinatesNecessary() {
		return spreadCoordinatesNecessary;
	}

	public void setSpreadCoordinatesNecessary(boolean spreadCoordinatesNecessary) {
		this.spreadCoordinatesNecessary = spreadCoordinatesNecessary;
	}	

}
