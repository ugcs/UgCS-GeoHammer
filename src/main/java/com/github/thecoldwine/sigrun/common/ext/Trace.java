package com.github.thecoldwine.sigrun.common.ext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.thecoldwine.sigrun.common.TraceHeader;
import com.ugcs.gprvisualizer.math.HorizontalProfile;

public class Trace {
    
	private final byte[] binHeader;
    private final TraceHeader header;
    
    private float[] originalvalues;
    private float[] normvalues;
    
    private LatLon latLon;
    private LatLon latLonOrigin;
    //private boolean active = true;
    private boolean end = false;
    
    //tmp for loading
    private boolean marked = false;
    
    
    //meters
    private double prevDist = 100000;
    
    public int maxindex;
    //public int maxindex2;
    public int verticalOffset;
    
    public int indexInFile;
    public int indexInSet;
    
    public int[] good;
    
    
    /*
     * 0
     * 1 - 0 ('+' -> '-')
     * 2 - 0 ('-' -> '+')
     * 3 - min
     * 4 - max
     *  
     */
    public int[] edge;
    
    public Set<Integer> max = new HashSet<>();
    
    public Trace(byte[] binHeader, TraceHeader header, float[] originalvalues, LatLon latLon) {
        
    	
        this.header = header;
        this.binHeader = binHeader; 
        this.originalvalues = originalvalues;
        this.latLon = latLon;
        this.latLonOrigin = latLon;
        
        this.good = new int[originalvalues.length];
        this.edge = new int[originalvalues.length];
    }
    
    public TraceHeader getHeader() {
        return header;
    }
    
    public LatLon getLatLon() {
    	return latLon;
    }

    public LatLon getLatLonOrigin() {
    	return latLonOrigin;
    }

    public void setLatLon(LatLon latLon2) {
		this.latLon = latLon2;
		
	}
    

    public float[] getOriginalValues() {
    	return originalvalues;
    }

    public void setOriginalValues(float[] vals) {
    	originalvalues = vals;
    }
    
    public float[] getNormValues() {
    	return normvalues != null ? normvalues : originalvalues;
    }

    public void setNormValues(float[] vals) {
    	normvalues = vals;
    }

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}

	// in cm
	public double getPrevDist() {
		return prevDist;
	}

	// in cm
	public void setPrevDist(double prevDist) {
		this.prevDist = prevDist;
	}
    
	public byte[] getBinHeader() {
		return binHeader;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}
	
}
