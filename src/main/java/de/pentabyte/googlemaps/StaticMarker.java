package de.pentabyte.googlemaps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a Google Static Map Marker.
 */
public class StaticMarker implements Serializable {
	private static final long serialVersionUID = -2566197476723898328L;
	private String color;
	private Character label;
	private final Location location;
	private String customIconUrl;
	private String anchor;
	private boolean shadow = true;
	private Integer zoom;
	private Integer zIndex;

	public StaticMarker(double lat, double lon) {
		this.location = new Location(lat, lon);
	}

	public StaticMarker(String query) {
		this.location = new Location(query);
	}

	/**
	 * @param hexColor
	 *            rrggbb value, e.g.: 00FF00 for green.
	 * 
	 * @see StaticMarker#setColor(String)
	 */
	public void setHexColor(String hexColor) {
		this.color = "0x" + hexColor;
	}

	public String getColor() {
		return color;
	}

	/**
	 * @param color
	 * 
	 * @see #setHexColor(String)
	 */
	public void setColor(Color color) {
		this.color = color.name();
	}

	public Character getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            specifies a single uppercase alphanumeric character from the
	 *            set {A-Z, 0-9}.
	 */
	public void setLabel(Character label) {
		this.label = label;
	}

	public String getCustomIconUrl() {
		return customIconUrl;
	}

	public void setCustomIconUrl(String customIconUrl) {
		this.customIconUrl = customIconUrl;
	}

	public String getAnchor() {
		return anchor;
	}

	/**
	 * Set the anchor as an x,y point of the icon (such as 10,5), or as a
	 * predefined alignment using one of the following values: top, bottom,
	 * left, right, center, topleft, topright, bottomleft, or bottomright.
	 * 
	 * @param anchor
	 */
	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}

	public Integer getZoom() {
		return zoom;
	}

	/**
	 * Only relevant, if this marker will be the only annotation on the map.
	 * 
	 * @param zoom
	 *            1: World 5: Landmass/continent 10: City 15: Streets 20:
	 *            Buildings
	 */
	public void setZoom(Integer zoom) {
		this.zoom = zoom;
	}

	public boolean isShadow() {
		return shadow;
	}

	public void setShadow(boolean shadow) {
		this.shadow = shadow;
	}

	/**
	 * Static Google Map Marker Definition
	 */
	@Override
	public String toString() {
		List<String> defs = new ArrayList<>();
		List<String> styles = new ArrayList<>();

		if (color != null)
			styles.add("color:" + color);

		if (label != null)
			styles.add("label:" + String.valueOf(label));

		if (anchor != null)
			styles.add("anchor:" + anchor);

		if (customIconUrl != null) {
			styles.add("icon:" + customIconUrl);
			styles.add("scale:2");
			if (!shadow) {
				styles.add("shadow:false");
			}
		}

		if (styles.size() > 0)
			defs.add(StringUtils.join(styles, "|"));

		defs.add(this.location.toString());

		return StringUtils.join(defs, '|');
	}

	public Integer getzIndex() {
		return zIndex;
	}

	public void setzIndex(Integer zIndex) {
		this.zIndex = zIndex;
	}

}