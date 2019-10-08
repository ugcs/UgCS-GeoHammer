package de.pentabyte.googlemaps;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.utils.URIBuilder;

/**
 * Link-Creator for static Google Maps v2. Call {@link #toString()} to create
 * static map URL.
 * 
 * @see <a href="https://github.com/hoereth/google-static-map-creator">github
 *      readme file</a> for examples.
 * 
 * @author michael hoereth
 *
 */
public class StaticMap implements Serializable {
	private static final long serialVersionUID = 155958884165520846L;
	private static String API_URL = "https://maps.googleapis.com/maps/api/staticmap";

	private final int width, height;
	private String apiKey;
	private Maptype maptype;
	private Format format = Format.PNG;
	private int scale = 1;
	private Location center;
	private Integer zoom;
	private List<StaticMarker> markers;
	private List<StaticPath> paths;
	private Locale locale;

	public enum Maptype {
		roadmap, satellite, hybrid, terrain
	}

	/**
	 * The dimensions (points) will be multiplied with the scale factor.
	 * 
	 * @param width
	 *            points
	 * @param height
	 *            points
	 * @param apiKey
	 */
	public StaticMap(int width, int height, String apiKey) {
		if (width > 2048)
			throw new IllegalArgumentException("width must not exceed 640");
		this.width = width;
		this.height = height;
		this.apiKey = apiKey;
	}

	public int getScale() {
		return scale;
	}

	/**
	 * Default: 1.
	 * 
	 * @param scale
	 */
	public void setScale(int scale) {
		if (scale < 1 || scale > 4 || scale == 3)
			throw new IllegalArgumentException("scale must be 1,2 or 4");
		this.scale = scale;
	}

	/**
	 * @param zoom
	 *            1: World 5: Landmass/continent 10: City 15: Streets 20:
	 *            Buildings
	 */
	public void setLocation(Location center, int zoom) {
		this.center = center;
		this.zoom = zoom;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getWidthPoints() {
		return width / scale;
	}

	public int getHeightPoints() {
		return height / scale;
	}

	public List<StaticMarker> getMarkers() {
		return markers;
	}

	public void setMarkers(List<StaticMarker> markers) {
		this.markers = markers;
	}

	public void addMarker(StaticMarker marker) {
		if (markers == null) {
			markers = new ArrayList<>();
		}
		markers.add(marker);
	}

	public List<StaticPath> getPaths() {
		return paths;
	}

	public void setPaths(List<StaticPath> paths) {
		this.paths = paths;
	}

	public void addPath(StaticPath path) {
		if (paths == null) {
			paths = new ArrayList<>();
		}
		paths.add(path);
	}

	/**
	 * Google-Map-Link
	 */
	public String toString() {
		try {
			URIBuilder builder;

			builder = new URIBuilder(API_URL);

			// Dimensionen
			builder.addParameter("size", width + "x" + height);
			if (center != null && zoom != null) {
				builder.addParameter("center", center.toString());
				builder.addParameter("zoom", zoom.toString());
			}
			if (scale != 1)
				builder.addParameter("scale", String.valueOf(scale));
			if (maptype != null)
				builder.addParameter("maptype", maptype.name());
			if (format != null && Format.PNG != format)
				builder.addParameter("format", format.getValue());
			if (getZoom() != null)
				builder.addParameter("zoom", String.valueOf(getZoom()));

			if (markers != null) {
				for (StaticMarker marker : markers) {
					builder.addParameter("markers", marker.toString());
				}
			}

			if (paths != null) {
				for (StaticPath path : paths) {
					if (path.getCoords() != null) {
						InternalBoundingBox box = new InternalBoundingBox(path.getCoords());
						double minDistance = box.getHeightMeters() / Math.max(getHeight(), getWidth());
						builder.addParameter("path", path.formatFor(minDistance, 40));
					} else {
						builder.addParameter("path", path.formatFor(0, 0));
					}
				}
			}

			if (apiKey != null)
				builder.addParameter("key", apiKey);

			if (locale != null && locale.getLanguage() != null)
				builder.addParameter("language", locale.getLanguage());

			String url = builder.build().toString();

			// No need to encode those:
			return url.replace("%3A", ":").replace("%40", "@");
		} catch (URISyntaxException e) {
			throw new RuntimeException("kann nicht sein", e);
		}
	}

	/**
	 * @return If only one annotation present: the annotation's zoom. null,
	 *         otherwise.
	 */
	private Integer getZoom() {
		if ((markers != null && markers.size() == 1) && (paths == null || paths.size() == 0)) {
			StaticMarker marker = markers.get(0);
			if (marker.getZoom() != null) {
				return marker.getZoom();
			}
		}
		return null;
	}

	public Maptype getMaptype() {
		return maptype;
	}

	public void setMaptype(Maptype maptype) {
		this.maptype = maptype;
	}

	public Format getFormat() {
		return format;
	}

	/**
	 * {@link Format#PNG}, if none specified.
	 * 
	 * @param format
	 */
	public void setFormat(Format format) {
		this.format = format;
	}

	/**
	 * Any markers or paths present?
	 */
	public boolean hasContent() {
		return markers != null && markers.size() > 0 || paths != null && paths.size() > 0;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * A geographic rectangle
	 */
	private static class InternalBoundingBox {
		private double latMin = Double.POSITIVE_INFINITY;
		private double latMax = Double.NEGATIVE_INFINITY;
		private double lonMin = Double.POSITIVE_INFINITY;
		private double lonMax = Double.NEGATIVE_INFINITY;

		protected InternalBoundingBox(Collection<? extends LatLon> coords) {
			for (LatLon coord : coords) {
				if (coord.getLatitude() > latMax)
					latMax = coord.getLatitude();
				if (coord.getLatitude() < latMin)
					latMin = coord.getLatitude();
				if (coord.getLongitude() > lonMax)
					lonMax = coord.getLongitude();
				if (coord.getLongitude() < lonMin)
					lonMin = coord.getLongitude();
			}
		}

		protected double getHeightMeters() {
			LatLon upperLeft = new LatLonImpl(latMax, lonMin);
			LatLon bottomLeft = new LatLonImpl(latMin, lonMin);
			return StaticPath.distanceAuto(upperLeft, bottomLeft);
		}
	}
}
