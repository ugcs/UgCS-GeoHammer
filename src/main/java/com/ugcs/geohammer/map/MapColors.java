package com.ugcs.geohammer.map;

import com.ugcs.geohammer.view.Colors;

import java.awt.Color;

public final class MapColors {

	public static final Color SELECTION = new Color(0xE821F8);

	// track

	public static final Color TRACK = new Color(0x39E5DA);

	public static final Color OTHER_TRACK = Colors.opaque(TRACK, 0.75f);

	public static final Color SELECTED_LINE = SELECTION;

	// quality issues

	public static final Color ALTITUDE_ISSUE = new Color(0xF65532);

	public static final Color LINE_DISTANCE_ISSUE = new Color(0xFCEF26);

	public static final Color DATA_ISSUE = ALTITUDE_ISSUE;

	public static final Color DATA_ISSUE_RING = LINE_DISTANCE_ISSUE;

	// trace cutter

	public static final Color TRACE_CUTTER_LINE = Color.WHITE;

	public static final Color TRACE_CUTTER_POINT = Color.WHITE;

	public static final Color TRACE_CUTTER_MIDDLE_POINT = Color.GRAY;

	public static final Color TRACE_CUTTER_SELECTED_POINT = SELECTION;

	// ruler

	public static final Color RULER_LINE = Color.GREEN;

	public static final Color RULER_POINT = Color.WHITE;

	public static final Color RULER_SELECTED_POINT = SELECTION;

	private MapColors() {
	}
}
