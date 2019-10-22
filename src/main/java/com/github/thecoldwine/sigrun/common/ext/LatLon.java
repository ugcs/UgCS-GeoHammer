package com.github.thecoldwine.sigrun.common.ext;

import java.util.Locale;

public class LatLon {

	private double lat_dgr;
	private double lon_dgr;
	
	public String toString() {
		
		return dgrToDMS(getLatDgr(), true ) +  " " + dgrToDMS(getLonDgr(), false); 
	}
	
	public LatLon(double lat_dgr, double lon_dgr) {
		this.lat_dgr = lat_dgr;
		this.lon_dgr = lon_dgr;
	}
	
	public double getLatDgr() {
		return lat_dgr;
	}

	public double getLonDgr() {
		return lon_dgr;
	}
	
	public void from(LatLon latLon) {
		this.lat_dgr = latLon.getLatDgr();
		this.lon_dgr = latLon.getLonDgr();
	}

	private String dgrToDMS(double dgr, boolean lat) {
		String postfix = "";
	
		if(lat ) {
			postfix = dgr > 0 ? "N" : "S";			
		}else {
			postfix = dgr > 0 ? "E" : "W";
		}
		
		dgr = Math.abs(dgr);
		
		int justdgr = (int)dgr;
		int justmin = (int)( (dgr-justdgr) *60 );
		double justsec = ( dgr - (double)justdgr - (double)justmin / 60.0  ) * 3600;
		
		// 40°02'04.0"S 65°11'00.2"E
		return String.format(Locale.ROOT, "%d°%d'%.3f\"%s", justdgr, justmin, justsec, postfix);
		//return justdgr + "°" + justmin + "'" + justsec + "\"" + postfix;
		
	}
	
}
