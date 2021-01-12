package com.ugcs.gprvisualizer.draw;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.imageio.ImageIO;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MapField;

import de.pentabyte.googlemaps.Location;
import de.pentabyte.googlemaps.StaticMap;
import de.pentabyte.googlemaps.StaticMap.Maptype;

public class HereMapProvider implements MapProvider {

	public int getMaxZoom() {
		return 20;
	}
	
	@Override
	public BufferedImage loadimg(MapField field) {
		
		BufferedImage img = null;
		
		
		LatLon midlPoint = field.getSceneCenter();
		int imgZoom = field.getZoom();
		//map.setLocation(new Location(midlPoint.getLatDgr(), midlPoint.getLonDgr()), imgZoom); 
		
		 //DecimalFormat df = new DecimalFormat("#.000000");
		DecimalFormat df = new DecimalFormat("#.0000000", DecimalFormatSymbols.getInstance(Locale.US));
		
		try {
			String HERE_API_KEY = "mX93tKDhNQW4jB9qWR7U8njVda4OWZu9S8t7Q1blkCs";
			String url = String.format("https://image.maps.ls.hereapi.com/mia/1.6/mapview?"
					+ "apiKey=%s"
					+ "&c=%s,%s"
					+ "&t=3"
					+ "&z=%d"
					+ "&nodot&w=1200&h=1200", 
					HERE_API_KEY, 
					df.format(midlPoint.getLatDgr()), 
					df.format(midlPoint.getLonDgr()), 
					imgZoom);
			
			System.out.println(url);
			
			System.setProperty("java.net.useSystemProxies", "true");
			img = ImageIO.read(new URL(url));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return img;
	}

	@Override
	public int getMapScale() {
		
		return 1;
	}

}
