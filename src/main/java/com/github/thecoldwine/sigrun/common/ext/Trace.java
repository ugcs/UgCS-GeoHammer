package com.github.thecoldwine.sigrun.common.ext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.thecoldwine.sigrun.common.TraceHeader;

public class Trace {
    
    private final Block headerBlock;
    private final Block dataBlock;
    
    private final TraceHeader header = null;
    private float[] originalvalues;
    private float[] normvalues;
    
    private LatLon latLon;
    private boolean active = true;
    private boolean end = false;
    private double prevDist = 100000;
    
    public int maxindex;
    public int maxindex2;
    
    public int indexInFile;
    public int indexInSet;
    
    
    public Set<Integer> max = new HashSet<>();
    
    public Trace(Block headerBlock, Block dataBlock, TraceHeader header, float[] originalvalues, LatLon latLon) {
        
    	
        //this.header = header;
        this.headerBlock = headerBlock; 
        this.dataBlock = dataBlock;
        this.originalvalues = originalvalues;
        this.latLon = latLon;
    }

    public TraceHeader getHeader() {
        return header;
    }
    
    public LatLon getLatLon() {
    	return latLon;
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

    public Block getHeaderBlock() {
		return headerBlock;
	}

	public Block getDataBlock() {
		return dataBlock;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}

	public double getPrevDist() {
		return prevDist;
	}

	public void setPrevDist(double prevDist) {
		this.prevDist = prevDist;
	}
    
	
	
}
