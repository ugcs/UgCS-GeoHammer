package de.pentabyte.googlemaps;

/**
 * A geographic location as used by {@link StaticMap} or {@link StaticMarker}.
 * 
 * @author michael hoereth
 */
public class Location {
	private final String query;
	private boolean geocodingRequired;

	/**
	 * @param query
	 *            anything which can be geocoded to a coordinate by Google
	 */
	public Location(String query) {
		this.query = query;
		this.geocodingRequired = true;
	}

	/**
	 * Will create a location which will not require geocoding.
	 */
	public Location(double latitude, double longitude) {
		this((float) latitude + "," + (float) longitude);
		this.geocodingRequired = false;
	}

	protected boolean isGeocodingRequired() {
		return geocodingRequired;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return query;
	}
}
