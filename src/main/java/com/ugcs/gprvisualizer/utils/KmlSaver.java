package com.ugcs.gprvisualizer.utils;

import java.io.File;

import com.ugcs.gprvisualizer.app.auxcontrol.BaseObject;
import com.ugcs.gprvisualizer.app.auxcontrol.FoundPlace;
import com.ugcs.gprvisualizer.gpr.Model;


public class KmlSaver {

	Model model;
	
	public KmlSaver(Model model) {
		this.model = model;
	}

	public void save(File klmFile) throws Exception {
		
		KmlBuilder builder = new KmlBuilder(klmFile);
		
		int i = 1;
		for (BaseObject el : model.getAuxElements()) {
			
			if (el instanceof FoundPlace) {
				FoundPlace fp = (FoundPlace) el;
				builder.addPoint(fp.getTrace().getLatLon(), "" + i);
				
				i++;
			}
		}
		
		builder.save();
	}

}
