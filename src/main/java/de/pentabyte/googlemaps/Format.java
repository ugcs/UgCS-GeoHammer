package de.pentabyte.googlemaps;

/**
 * @author michael hoereth
 *
 */
public enum Format {
	/**
	 * 8 bit PNG. Default.
	 */
	PNG("png"),
	/**
	 * 8 bit PNG
	 */
	PNG8("png8"),
	/**
	 * 32 bit PNG
	 */
	PNG32("png32"),
	/**
	 * GIF
	 */
	GIF("gif"),
	/**
	 * JPG
	 */
	JPG("jpg"),
	/**
	 * non-progressive JPEG compression
	 */
	JPG_BASELINE("jpg-baseline");

	/**
	 * Google's format name.
	 */
	private String value;

	private Format(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
