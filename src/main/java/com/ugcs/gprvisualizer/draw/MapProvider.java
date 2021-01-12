package com.ugcs.gprvisualizer.draw;

import java.awt.image.BufferedImage;

import com.github.thecoldwine.sigrun.common.ext.MapField;

public interface MapProvider {
	BufferedImage loadimg(MapField field);
	
	int getMaxZoom();
	
	int getMapScale();
}
