package de.pentabyte.googlemaps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Google Static Map Path.
 */
public class StaticPath implements Serializable {
	/**
	 * Earth radius in meters.
	 */
	private static int EARTH_RADIUS = 6371000;
	private static final long serialVersionUID = -1938620346181841310L;
	private String color;
	private String fillColor;
	private Integer weight;
	private final List<LatLon> coords;
	private final String polyline;

	/**
	 * @param coords
	 *            Path will eventually be converted to an encoded polyline.
	 */
	public StaticPath(List<LatLon> coords) {
		this.coords = coords;
		this.polyline = null;
	}

	/**
	 * @param polyline
	 */
	public StaticPath(String polyline) {
		this.coords = null;
		this.polyline = polyline;
	}

	public List<LatLon> getCoords() {
		return coords;
	}

	/**
	 * Lässt nur jeden zweiten Punkt über.
	 */
	private static List<LatLon> reduce(List<LatLon> list) {
		List<LatLon> less = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			if (i % 2 == 0)
				less.add(list.get(i));
		}
		if (((list.size() - 1) % 2) != 0) {
			less.add(list.get(list.size() - 1));
		}
		return less;
	}

	public String getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color.name();
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor.name();
	}

	public void setHexFillColor(String hexFillColor) {
		this.fillColor = "0x" + hexFillColor;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public void setHexColor(String hexColor) {
		this.color = "0x" + hexColor;
	}

	/**
	 * The coordinates will be compressed in such a way that coordinates which
	 * are too close together will be ignored. After this step, as many coords
	 * will be omitted until is short enough to comply with Google Map URL
	 * length.
	 */
	protected String formatFor(double minDistanceMeters, int maxPoints) {
		List<String> defs = new ArrayList<>();

		if (color != null)
			defs.add("color:" + color);

		if (fillColor != null)
			defs.add("fillcolor:" + fillColor);

		if (weight != null)
			defs.add("weight:" + weight);

		if (coords != null) {
			List<LatLon> reduced = new ArrayList<>();
			LatLon lastCoord = null;
			for (LatLon coord : coords) {
				if (lastCoord == null || distanceApproximate(lastCoord, coord) > minDistanceMeters) {
					reduced.add(coord);
					lastCoord = coord;
				}
			}

			while (reduced.size() > maxPoints) {
				reduced = reduce(reduced);
			}

			PolylineEncoder encoder = new PolylineEncoder();

			for (LatLon coord : reduced) {
				encoder.add(coord.getLatitude(), coord.getLongitude());
			}
			defs.add("enc:" + encoder.toString());
		}

		if (polyline != null) {
			defs.add("enc:" + polyline);
		}

		return StringUtils.join(defs, '|');
	}

	protected static double distanceAuto(LatLon c1, LatLon c2) {
		return distanceAuto(c1.getLatitude(), c1.getLongitude(), c2.getLatitude(), c2.getLongitude());
	}

	/**
	 * Depending on the distance, an exact or approximate algorithm will be
	 * chosen.
	 * 
	 * @return Abstand in Metern.
	 */
	protected static double distanceAuto(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.abs(lat1 - lat2);
		if (dLat > 4)
			return distance(lat1, lon1, lat2, lon2);
		double dLon = Math.abs(lon1 - lon2);
		if (dLon > 4)
			return distance(lat1, lon1, lat2, lon2);
		else
			return distanceApproximate(lat1, lon1, lat2, lon2);
	}

	/**
	 * @return Exact distance in meters / geodesic.
	 */
	protected static double distance(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lon2 - lon1);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return (EARTH_RADIUS * c);
	}

	/**
	 * An approximated solution (based on an equirectangular projection), much
	 * faster (it requires only 1 trig and 1 square root).
	 * 
	 * This approximation is relevant if your points are not too far apart. It
	 * will always over-estimate compared to the real haversine distance. For
	 * example it will add no more than 0.05382 % to the real distance if the
	 * delta latitude or longitude between your two points does not exceed 4
	 * decimal degrees.
	 */
	protected static double distanceApproximate(LatLon coord1, LatLon coord2) {
		return distanceApproximate(coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(),
				coord2.getLongitude());
	}

	/**
	 * @return Distance in meters according to planar projection. Accuracy will
	 *         decrease with the distance and can only be recommended for
	 *         distances of up to 4 degrees.
	 */
	protected static double distanceApproximate(double lat1, double lon1, double lat2, double lon2) {
		if (lon1 < 0)
			lon1 += 360;
		if (lon2 < 0)
			lon2 += 360;

		lat1 = Math.toRadians(lat1);
		lon1 = Math.toRadians(lon1);
		lat2 = Math.toRadians(lat2);
		lon2 = Math.toRadians(lon2);

		double x = (lon2 - lon1) * Math.cos((lat1 + lat2) / 2);
		double y = (lat2 - lat1);
		return Math.sqrt(x * x + y * y) * EARTH_RADIUS;
	}

}