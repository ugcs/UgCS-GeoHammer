package com.ugcs.gprvisualizer.draw;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import com.github.thecoldwine.sigrun.common.ext.LatLon;
import com.github.thecoldwine.sigrun.common.ext.MapField;

import de.pentabyte.googlemaps.Location;
import de.pentabyte.googlemaps.StaticMap;
import de.pentabyte.googlemaps.StaticMap.Maptype;

public class GoogleMapProvider implements MapProvider {

	private static String GOOGLE_API_KEY;
	
	static {
		InputStream inputStream = null;
		try {
			inputStream = SatelliteMap.class.getClassLoader()
					.getResourceAsStream("googleapikey");
			java.util.Scanner s = new java.util.Scanner(inputStream)
					.useDelimiter("\\A");
			GOOGLE_API_KEY = s.hasNext() ? s.next() : "";
		
			s.close();
		} catch (Exception e) {
			System.out.println("no google api key -> no googlemaps");
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
	
	
	public BufferedImage loadimg(MapField field) {
		
		BufferedImage img = null;
		
		StaticMap map = new StaticMap(640, 640, GOOGLE_API_KEY);
		
		map.setScale(getMapScale());
		map.setMaptype(Maptype.hybrid);
		
		LatLon midlPoint = field.getSceneCenter();
		int imgZoom = field.getZoom();
		map.setLocation(new Location(
				midlPoint.getLatDgr(), midlPoint.getLonDgr()), imgZoom); 
		map.setMaptype(Maptype.hybrid);
		
		try {
			
			String url = map.toString();
			
			//https://maps.googleapis.com/maps/api/staticmap?size=640x640&center=40.714%2C-73.998&zoom=16&maptype=hybrid&key=AIzaSyAoXv4VEhXEB_YSkPngzoqCFykT03yir7M
			//https://maps.googleapis.com/maps/api/staticmap?maptype=satellite&center=40.714%2c%20-73.998&zoom=20&size=1800x1800&key=AIzaSyAoXv4VEhXEB_YSkPngzoqCFykT03yir7M
			//url = "https://maps.googleapis.com/maps/api/staticmap?maptype=satellite&center=40.714%2c%20-73.998&zoom=20&size=1800x1800&key=AIzaSyAoXv4VEhXEB_YSkPngzoqCFykT03yir7M";
			System.out.println(url);
			
			System.setProperty("java.net.useSystemProxies", "true");
			img = ImageIO.read(new URL(url));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return img;
	}


	@Override
	public int getMaxZoom() {
		
		return 30;
	}


	@Override
	public int getMapScale() {
		
		return 2;
	}
	
}
