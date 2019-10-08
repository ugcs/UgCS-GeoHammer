package de.pentabyte.googlemaps;

/**
 * For encoding Google-Polylines.
 */
public class PolylineEncoder {
	private StringBuffer buffer = new StringBuffer();
	private int prevLat = 0;
	private int prevLon = 0;

	private void encodeSignedNumber(int num) {
		int sgn_num = num << 1;
		if (num < 0) {
			sgn_num = ~(sgn_num);
		}
		encodeNumber(sgn_num);
	}

	private void encodeNumber(int num) {
		while (num >= 0x20) {
			int nextValue = (0x20 | (num & 0x1f)) + 63;
			buffer.append((char) (nextValue));
			num >>= 5;
		}
		num += 63;
		buffer.append((char) (num));
	}

	/**
	 * Add coordinate to polyline.
	 */
	public void add(double latitude, double longitude) {
		int lat = (int) (latitude * 1e+5);
		int lon = (int) (longitude * 1e+5);
		encodeSignedNumber(lat - prevLat);
		encodeSignedNumber(lon - prevLon);
		prevLat = lat;
		prevLon = lon;
	}

	@Override
	public String toString() {
		return buffer.toString();
	}
}