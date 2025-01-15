package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class ResourceImageHolder {

	public static Image IMG_LEVEL;
	public static Image IMG_SHOVEL;
	public static Image IMG_HOR_SLIDER;
	public static Image IMG_VER_SLIDER;
	public static Image IMG_WIDTH;
	
	public static Image IMG_LOCK;
	public static Image IMG_UNLOCK;
	
	public static Image IMG_CHOOSE;
	public static Image IMG_GPS;

	public static Image IMG_CLOSE_FILE;
	public static Image IMG_CLOSE;

	public static final String SAVE = """
		m 12,2.1 c -0.3,0 -0.5,0.2 -0.5,0.5 v 2.3 c 0,0.3 0.2,0.5 0.5,0.5 0.3,0 0.5,-0.2 0.5,-0.5 V 2.6 C 12.5,2.4 12.3,2.1 12,2.1 Z
		M 15.4,0.7 H 14.7 2.7 0.8 V 19.3 H 19.1 V 4.5 Z
		M 9.9,15.3 C 7.9,15.3 6.2,13.7 6.2,11.6 6.2,9.5 7.8,8 9.9,8 c 2.1,0 3.7,1.6 3.7,3.7 0,2.1 -1.7,3.6 -3.7,3.6 z
		M 13.7,5.5 H 3.7 V 1.7 h 10 z
		m -1.1,6.1 a 2.7,2.7 0 0 1 -2.7000004,2.7 2.7,2.7 0 0 1 -2.7,-2.7 2.7,2.7 0 0 1 2.7,-2.6999997 A 2.7,2.7 0 0 1 12.6,11.6 Z""";

	public static final String SAVE_TO = """
		m 9.3,17.9 c -0.1,0 -0.1,0 -0.2,0 C 9,17.9 8.8,17.8 8.7,17.7 8.6,17.6 8.5,17.3 8.6,17.1 l 1.5,-3.3 -6.3,-0.1 c -0.3,0 -0.5,-0.2 -0.5,-0.5 0,-0.1 0.1,-0.3 0.2,-0.4 0.1,-0.1 0.2,-0.2 0.4,-0.1 L 10.2,12.8 8.7,9.5 C 8.6,9.3 8.7,9.1 8.8,8.9 8.9,8.8 9.2,8.7 9.4,8.8 l 9.8,4.1 V 4.5 L 15.4,0.7 H 14.7 2.7 0.8 V 19.3 H 19.1 V 14 Z
		M 3.6,1.7 h 10 V 5.5 H 3.6 V 1.7 Z
		M 12,2.1 c -0.3,0 -0.5,0.2 -0.5,0.5 v 2.3 c 0,0.3 0.2,0.5 0.5,0.5 0.3,0 0.5,-0.2 0.5,-0.5 V 2.6 C 12.5,2.4 12.3,2.1 12,2.1 Z""";

	public static final String ZOOM_IN = """
		m 17.5,18.2 -4.1,-4.8 c 1.4,-1.2 2.3,-3.1 2.3,-5.1 0,-3.7 -3,-6.7 -6.7,-6.7 -3.7,0 -6.7,3 -6.7,6.7 0,3.7 3,6.7 6.7,6.7 1.3,0 2.5,-0.4 3.5,-1 l 4.2,4.9 c 0.1,0.1 0.2,0.2 0.4,0.2 0.1,0 0.2,0 0.3,-0.1 0.1,-0.1 0.2,-0.2 0.2,-0.4 0.1,-0.1 0,-0.3 -0.1,-0.4 z
		M 12.7,8.8 H 9.6 v 3.1 c 0,0.3 -0.2,0.5 -0.5,0.5 -0.3,0 -0.5,-0.2 -0.5,-0.5 V 8.8 H 5.4 C 5.1,8.8 4.9,8.6 4.9,8.3 4.9,8 5.1,7.8 5.4,7.8 H 8.5 V 4.7 C 8.5,4.4 8.7,4.2 9,4.2 c 0.3,0 0.5,0.2 0.5,0.5 v 3.1 h 3.1 c 0.3,0 0.5,0.2 0.5,0.5 0,0.3 -0.1,0.5 -0.4,0.5 z""";

	public static final String ZOOM_OUT = """
		m 17.5,18.2 -4.1,-4.8 c 1.4,-1.2 2.3,-3.1 2.3,-5.1 0,-3.7 -3,-6.7 -6.7,-6.7 -3.7,0 -6.7,3 -6.7,6.7 0,3.7 3,6.7 6.7,6.7 1.3,0 2.5,-0.4 3.5,-1 l 4.2,4.9 c 0.1,0.1 0.2,0.2 0.4,0.2 0.1,0 0.2,0 0.3,-0.1 0.1,-0.1 0.2,-0.2 0.2,-0.4 0.1,-0.1 0,-0.3 -0.1,-0.4 z
		M 12.7,8.8 H 5.4 C 5.1,8.8 4.9,8.6 4.9,8.3 4.9,8 5.1,7.8 5.4,7.8 h 7.2 c 0.3,0 0.5,0.2 0.5,0.5 0,0.3 -0.1,0.5 -0.4,0.5 z""";

	public static final String UNDO = "m 16.1,8.2 c 0,0 0,0 0,0 L 9.9,8.1 11.3,4.8 c 0,-0.1 0,-0.1 0,-0.2 0,-0.1 0,-0.3 -0.1,-0.3 C 11.1,4.2 10.8,4.1 10.7,4.2 L 0.7,8.1 C 0.5,8.1 0.3,8.3 0.3,8.5 0.3,8.7 0.4,8.9 0.6,9 l 9.9,4.2 c 0.1,0 0.1,0 0.2,0 0.1,0 0.3,-0.1 0.4,-0.1 0.1,-0.1 0.2,-0.4 0.1,-0.5 L 9.8,9.1 16,9.2 c 0,0 0,0 0,0 1.5,0.1 2.8,1.3 2.8,2.9 0,1.6 -1.3,2.9 -2.9,2.9 h -0.8 c -0.3,0 -0.5,0.2 -0.5,0.5 0,0.3 0.2,0.5 0.5,0.5 h 0.8 c 2.1,0 3.9,-1.7 3.9,-3.9 0,-2.1 -1.6,-3.8 -3.7,-3.9 z";

	public static final String CROP = """
		M 4.4644472,6.5765075 C 4.7644472,6.4765075 4.8,6.4 5.1,6.2 5.2,6 5.3,5.9 5.3,5.7 5.3,5.5 5.2,5.4 5.1,5.3 4.9,5.1 4.5530151,4.9825377 4.3530151,5.1825377 c -0.4,0.3 -0.8,0.3530151 -1.3,0.2530151 -0.4,-0.1 -0.4415829,-0.2060302 -0.6415829,-0.6060302 -0.2,-0.3 -0.2234925,-0.6060301 -0.1234925,-1.0060301 0.1,-0.4 0.5328168,-0.6877269 0.6060301,-0.8060302 0.3,-0.2 0.5120604,-0.2530151 0.9120604,-0.1530151 0.5,0.1 0.9295226,0.5765076 1.0295226,1.1765076 C 4.8355528,4.3409548 5.3,4.4 5.6,4.3 5.8,4.3 5.9,4.2 6,4.1 5.9,4 6,3.9 6,3.7 5.8,2.7 5.1,2 4.2,1.7 3.5,1.6 2.8,1.7 2.2,2 1.6,2.3 1.2,2.9 1,3.5 V 4.2 C 1,4.6 1.1,5.1 1.3,5.5 1.6,6 2.4523869,6.5879397 3.0523869,6.7879397 l 3.45,3.1649498 c -2.6,-1.2 2.5030151,2.9718595 -0.3969849,-0.6281408 C 5.905402,9.1247487 5.3,9.9 5,10.1 c -0.2,0.2 -0.2,0.5 0,0.7 4.8,5.8 11.8,8.5 13,8.5 h 0.1 l 1.1,-0.2 z
		M 5.7765075,12.311432 5.5650754,12.076508 C 5.3758636,11.866272 5.3,11.8 5,12 l -2.4,2 c -1.1,0.3 -1.8,1.3 -1.8,2.4 0,0.2 0,0.4 0.1,0.6 0.3,1.1 1.3,1.9 2.4,1.9 0.2,0 0.4,0 0.6,-0.1 0.7,-0.2 1.3174623,-0.576508 1.6174623,-1.176508 0.3,-0.6 0.4,-1.276507 0.3,-1.876507 -0.2,-0.8 -0.5180904,-1.31206 -1.3180904,-1.51206 l 1.5234924,-1.417463 c 0.4,0 -0.00516,-0.238031 -0.2463568,-0.50603 z
		m -3.053015,5.253644 c -0.3,-0.2 -0.5765076,-0.382538 -0.6765076,-0.782538 -0.2,-0.8 0.2060302,-1.318091 0.9060302,-1.518091 0.2820281,-0.0079 0.3740665,-0.205831 0.8463568,-0.02349 0.4867326,0.187917 0.6126884,0.282538 0.9536432,0.888568 L 4.8,16.6 c 0.042378,0.424347 -0.1885678,0.847613 -0.8885678,1.047613 -0.3,0.1 -0.8879397,0.11746 -1.1879397,-0.08254 z
		M 8.6831658,9.8120603 9.3938442,10.418844 C 9.2938442,10.718844 16.4,4.2 16.3,4.5 c -0.9,1.4 -2.5,3.6 -5.7,6 -0.2,0.2 -0.3,0.5 -0.1,0.7 v 0 c 0.1,0.1 0.2,0.2 0.4,0.2 H 11 c 0.1,0 0.2,0 0.3,-0.1 C 17.7,6.5 18.4,2.2 18.5,2 L 18.7,0.6 8.2415829,9.2234925 c -0.2,0.2 0.4282663,0.453015 0.6282663,0.753015 0.2,0.2000005 0.5704774,-0.8806532 -0.1866834,-0.1644472 z""";

	public static final String CROP2 = """
		M 4.3,6.6 C 4.6,6.5 4.8,6.4 5.1,6.2 5.2,6 5.3,5.9 5.3,5.7 5.3,5.5 5.2,5.4 5.1,5.3 4.9,5.1 4.6,5.1 4.4,5.3 4,5.6 3.6,5.7 3.1,5.6 2.7,5.5 2.4,5.3 2.2,4.9 2,4.6 2,4.2 2.1,3.8 2.2,3.4 2.4,3.1 2.8,2.9 3.1,2.7 3.5,2.6 3.9,2.7 4.4,2.8 4.9,3.3 5,3.9 5,4.2 5.3,4.4 5.6,4.3 5.8,4.3 5.9,4.2 6,4.1 5.9,4 6,3.9 6,3.7 5.8,2.7 5.1,2 4.2,1.7 3.5,1.6 2.8,1.7 2.2,2 1.6,2.3 1.2,2.9 1,3.5 1,3.8 1,4 1,4.2 1,4.6 1.1,5.1 1.3,5.5 1.6,6 2.1,6.4 2.7,6.6 L 15.5,17.4 C 12.9,16.2 8.7,13.7 5.8,10.1 5.6,9.9 5.3,9.9 5,10.1 c -0.2,0.2 -0.2,0.5 0,0.7 4.8,5.8 11.8,8.5 13,8.5 0,0 0.1,0 0.1,0 l 1.1,-0.2 z
		m 1.5,5.5 v 0 C 5.6,11.9 5.3,11.8 5,12 l -2.4,2 c -1.1,0.3 -1.8,1.3 -1.8,2.4 0,0.2 0,0.4 0.1,0.6 0.3,1.1 1.3,1.9 2.4,1.9 0.2,0 0.4,0 0.6,-0.1 C 4.6,18.6 5.1,18.2 5.4,17.6 5.7,17 5.8,16.3 5.7,15.7 5.5,14.9 4.9,14.2 4.1,14 L 5.6,12.7 C 6,12.7 6,12.4 5.8,12.1 Z
		M 2.7,17.8 C 2.4,17.6 2.1,17.3 2,16.9 1.8,16.1 2.3,15.3 3,15.1 c 0.1,0 0.2,0 0.4,0 0.6,0 1.2,0.4 1.4,1.1 0,0.1 0,0.2 0,0.4 0,0.7 -0.4,1.2 -1.1,1.4 -0.3,0.1 -0.7,0 -1,-0.2 z
		M 9.2,10 16.7,3.7 c -0.1,0.3 -0.3,0.5 -0.4,0.8 -0.9,1.4 -2.5,3.6 -5.7,6 -0.2,0.2 -0.3,0.5 -0.1,0.7 v 0 c 0.1,0.1 0.2,0.2 0.4,0.2 0,0 0.1,0 0.1,0 0.1,0 0.2,0 0.3,-0.1 C 17.7,6.5 18.4,2.2 18.5,2 L 18.7,0.6 8.5,9.2 C 8.3,9.4 8.2,9.7 8.4,10 c 0.2,0.2 0.5,0.2 0.8,0 z""";

	public static final String SELECT_RECT = """
		m 18.3,6.6 c 0.3,0 0.5,-0.2 0.5,-0.5 V 2 c 0,-0.3 -0.2,-0.5 -0.5,-0.5 h -4 C 14,1.5 13.8,1.7 13.8,2 v 1.4 h -7 V 2 C 6.8,1.7 6.6,1.5 6.3,1.5 h -4 C 1.8,1.4 1.6,1.7 1.6,2 v 4 c 0,0.3 0.2,0.5 0.5,0.5 h 1.4 v 6.9 H 2.1 c -0.3,0 -0.5,0.2 -0.5,0.5 v 4 c 0,0.3 0.2,0.5 0.5,0.5 h 4 c 0.3,0 0.5,-0.2 0.5,-0.5 v -1.4 h 7 v 1.4 c 0,0.3 0.2,0.5 0.5,0.5 h 4 c 0.3,0 0.5,-0.2 0.5,-0.5 v -4 c 0,-0.3 -0.2,-0.5 -0.5,-0.5 H 16.7 V 6.6 Z
		m -4.1,6.9 c -0.3,0 -0.5,0.2 -0.5,0.5 v 1.6 h -7 V 14 C 6.7,13.7 6.5,13.5 6.2,13.5 H 4.6 V 6.6 H 6.1 C 6.4,6.6 6.6,6.4 6.6,6.1 V 4.4 h 7 V 6 c 0,0.3 0.2,0.5 0.5,0.5 h 1.5 v 6.9 h -1.4 z""";

	public static final String MAP = """
		M 18.9,17 14.4,10.7 C 14.3,10.6 14.1,10.5 14,10.5 h -0.7 l 0.9,-2 C 14.8,7.2 14.7,5.6 14,4.3 13.2,3.1 12,2.3 10.5,2.2 10.3,2.2 10.2,2.2 10,2.2 8.7,2.3 7.4,3 6.7,4.3 5.9,5.5 5.8,7.1 6.4,8.5 l 0.9,2 H 6.5 c -0.2,0 -0.3,0.1 -0.4,0.2 L 1.4,17 c -0.1,0.2 -0.1,0.4 0,0.6 0.1,0.2 0.3,0.3 0.5,0.3 h 16.6 c 0.2,0 0.4,-0.1 0.5,-0.3 0.1,-0.2 0.1,-0.5 -0.1,-0.6 z		
		M 10.4,4 c 1.4,0 2.5,1.1 2.5,2.5 C 12.9,7.9 11.8,9 10.4,9 9,9 7.8,8 7.8,6.6 7.8,5.2 9,4 10.4,4 Z
		M 2.9,16.8 6.8,11.5 h 1 l 2,4.4 c 0.2,0.4 0.8,0.4 1,0 l 2,-4.4 h 0.9 l 3.7,5.3 z
		m 9,-10.2 a 1.5,1.5 0 0 1 -1.5,1.5 1.5,1.5 0 0 1 -1.5,-1.5 1.5,1.5 0 0 1 1.5,-1.5 1.5,1.5 0 0 1 1.5,1.5 z""";

	public static final String PATH = """
		M 17.5,7.9 l 1.7-3.7 c 0.4-0.8,0.3-1.7-0.1-2.4 v 0 c -0.4-0.7-1.2-1.1-2-1.2 c -0.1,0-0.2,0-0.3,0 c -0.8,0-1.5,0.5-2,1.2 c -0.4,0.7-0.5,1.6-0.1,2.4 l 1.7,3.7 c 0,0-0.2,1.1-3.8,1.6 c -7.5,0.9-7.5,1.9-7.5,2.3 c 0,0.7,0.7,1,2.8,1.4 c 4.3,0.3,8.2,1.2,8.4,1.8 c 0,0-0.1,0.2-0.3,0.3 c 0,0-2.5,1.1-12,2.6 l 1.3-2.8 c 0.4-0.8,0.3-1.7-0.1-2.4 v 0 c -0.4-0.7-1.2-1.1-2-1.2 c -0.1,0-0.2,0-0.3,0 c -0.8,0-1.5,0.5-2,1.2 c -0.4,0.7-0.5,1.6-0.1,2.4 l 1.9,4 c 0.1,0.1,0.2,0.2,0.4,0.2 c 0.2,0,0.3-0.1,0.4-0.2 l 0-0.1 c 0,0,0.1,0,0.1,0 l 0.1,0 c 10.4-1.6,12.7-2.7,12.8-2.8 c 0.9-0.6,0.9-1.2,0.8-1.5 c -0.5-1.8-6.7-2.4-9.3-2.6 c -0.8-0.1-1.3-0.2-1.5-0.3 c 0.9-0.4,3.4-0.9,6.3-1.3 C 17.3,10,17.5,8.2,17.5,7.9 z 
		M 17,3.8 c -0.4,0-0.7-0.3-0.7-0.7 s 0.3-0.7,0.7-0.7 s 0.7,0.3,0.7,0.7 S 17.3,3.8,17,3.8 z
		M 3.7,14.1 c 0,0.4-0.3,0.7-0.7,0.7 s -0.7-0.3-0.7-0.7 s 0.3-0.7,0.7-0.7 S 3.7,13.7,3.7,14.1 z""";

	public static final String LIGHT = """
		M 14.2,7.5 H 14 6.2 6 C 5.7,7.5 5.4,7.8 5.4,8.1 v 0.2 3.6 0 c 0,0.1 0,0.2 0.1,0.3 0.1,0.3 0.4,0.5 0.8,0.5 h 1.1 v 5.5 c 0,0.3 0.3,0.6 0.6,0.6 h 2 c 0.1,0 0.1,0 0.2,0 0.1,0 0.1,0 0.2,0 h 2 c 0.3,0 0.6,-0.3 0.6,-0.6 v -5.5 h 1 c 0.3,0 0.6,-0.2 0.8,-0.5 0,-0.1 0.1,-0.2 0.1,-0.3 v 0 V 8.3 8.1 C 14.8,7.8 14.5,7.5 14.2,7.5 Z		
		M 6.5,8.6 h 7.3 V 8.9 H 6.5 Z		
		M 16.4,3.7 C 16.2,3.5 15.8,3.5 15.7,3.7 L 14,5.4 c -0.1,0.1 -0.2,0.2 -0.2,0.4 0,0.2 0.1,0.3 0.2,0.4 0.1,0.1 0.2,0.2 0.4,0.2 0.1,0 0.3,-0.1 0.4,-0.2 L 16.5,4.5 C 16.6,4.2 16.6,3.9 16.4,3.7 Z		
		M 4.5,3.8 C 4.3,3.6 4,3.6 3.8,3.8 3.7,3.9 3.6,4 3.6,4.2 c 0,0.2 0.1,0.3 0.2,0.4 l 1.7,1.7 v 0 C 5.6,6.4 5.7,6.5 5.9,6.5 6,6.5 6.2,6.4 6.3,6.3 6.3,6.1 6.4,6 6.4,5.9 6.4,5.8 6.3,5.6 6.2,5.5 Z		
		M 10.1,1.5 C 9.8,1.5 9.6,1.7 9.6,2 V 5.5 C 9.6,5.8 9.8,6 10.1,6 10.4,6 10.6,5.8 10.6,5.5 V 2 c 0,-0.3 -0.2,-0.5 -0.5,-0.5 z""";	

	public static final String DELETE = """
		M 15.4,4.3 H 14.5 12.3 V 3.5 C 12.3,3 11.9,2.7 11.5,2.7 h -3 C 8,2.7 7.7,3.1 7.7,3.5 v 0.8 h -2.1 -1 c -0.3,0 -0.5,0.2 -0.5,0.5 0,0.3 0.2,0.5 0.5,0.5 h 0.5 l 1,11.9 c 0,0.2 0.2,0.4 0.5,0.4 h 6.9 c 0.2,0 0.4,-0.2 0.5,-0.4 L 15,5.3 h 0.4 c 0.3,0 0.5,-0.2 0.5,-0.5 0,-0.3 -0.2,-0.5 -0.5,-0.5 z		
		M 8.6,3.6 h 2.8 V 4.3 H 8.6 Z		
		m 0.1,12.7 c -0.1,0.1 -0.2,0.2 -0.3,0.2 v 0 C 8.2,16.5 8,16.3 7.9,16.1 L 7,6.3 C 7,6.2 7,6 7.1,5.9 7.2,5.8 7.3,5.7 7.4,5.7 7.7,5.7 7.9,5.9 7.9,6.1 L 8.8,16 c 0,0.1 0,0.2 -0.1,0.3 z	
		M 10.5,16 c 0,0.3 -0.2,0.5 -0.5,0.5 C 9.7,16.5 9.5,16.3 9.5,16 V 6.2 c 0,-0.3 0.2,-0.5 0.5,-0.5 0.3,0 0.5,0.2 0.5,0.5 z		
		m 1.8,0 c 0,0.2 -0.2,0.4 -0.5,0.4 h -0.1 c -0.1,0 -0.2,-0.1 -0.3,-0.2 -0.1,-0.1 -0.1,-0.2 -0.1,-0.3 l 0.9,-9.8 c 0,-0.3 0.2,-0.5 0.5,-0.4 0.1,0 0.2,0.1 0.3,0.2 0.1,0.1 0.1,0.2 0.1,0.3 z""";	

	public static final String DELETE_ALL = """
		m 17,9.8 h -4.9 c -0.3,0 -0.5,0.2 -0.5,0.5 0,0.3 0.2,0.5 0.5,0.5 H 17 c 0.3,0 0.5,-0.2 0.5,-0.5 C 17.4,10 17.2,9.8 17,9.8 Z
		M 12.1,6.4 h 3.8 c 0.3,0 0.5,-0.2 0.5,-0.5 0,-0.3 -0.2,-0.5 -0.5,-0.5 h -3.8 c -0.3,0 -0.5,0.2 -0.5,0.5 0,0.3 0.2,0.5 0.5,0.5 z
		m 7.2,1.2 h -7.2 c -0.3,0 -0.5,0.2 -0.5,0.5 0,0.3 0.2,0.5 0.5,0.5 h 7.2 c 0.3,0 0.5,-0.2 0.5,-0.5 0,-0.3 -0.2,-0.5 -0.5,-0.5 z
		M 11.4,4.4 H 10.5 8.4 V 3.5 C 8.4,3 8,2.7 7.6,2.7 h -3 C 4.1,2.7 3.8,3.1 3.8,3.5 v 0.8 h -2.1 -1 c -0.3,0 -0.5,0.2 -0.5,0.5 0,0.3 0.2,0.5 0.5,0.5 h 0.5 l 1,11.9 c 0,0.2 0.2,0.4 0.5,0.4 h 6.9 c 0.2,0 0.4,-0.2 0.5,-0.4 l 1,-11.9 h 0.4 C 11.8,5.3 12,5.1 12,4.8 11.9,4.6 11.7,4.4 11.4,4.4 Z		
		M 4.7,3.7 H 7.5 V 4.4 H 4.7 Z
		m 0,12.6 c -0.1,0.1 -0.2,0.2 -0.3,0.2 v 0 C 4.2,16.5 4,16.3 3.9,16.1 L 3.1,6.3 C 3.1,6.2 3.1,6.1 3.2,6 3.3,5.9 3.4,5.8 3.5,5.8 3.7,5.8 4,6 4,6.2 L 4.9,16 c 0,0.1 -0.1,0.2 -0.2,0.3 z
		M 6.6,16 c 0,0.3 -0.2,0.5 -0.5,0.5 C 5.8,16.5 5.6,16.3 5.6,16 V 6.3 C 5.6,6 5.8,5.8 6.1,5.8 6.4,5.8 6.6,6 6.6,6.3 Z
		m 1.7,0 c 0,0.2 -0.2,0.4 -0.5,0.4 H 7.7 C 7.6,16.4 7.5,16.3 7.4,16.2 7.3,16.1 7.3,16 7.3,15.9 L 8.2,6.2 C 8.2,5.9 8.4,5.7 8.7,5.8 8.9,5.8 9,5.9 9.1,6 9.2,6.1 9.2,6.2 9.2,6.3 Z""";	

	public static final String ADD_MARK	= """
		M 3.3,2.9 H 2.7 V 2.3 C 2.7,2 2.5,1.8 2.2,1.8 1.9,1.8 1.8,2 1.8,2.3 V 2.9 H 1.2 c -0.3,0 -0.5,0.2 -0.5,0.5 0,0.3 0.2,0.5 0.5,0.5 H 1.8 V 4.5 C 1.8,4.8 2,5 2.3,5 2.6,5 2.8,4.8 2.8,4.5 V 3.8 H 3.4 C 3.7,3.8 3.9,3.6 3.9,3.3 3.9,3 3.6,2.9 3.3,2.9 Z
		M 6,3.7 C 5.7,3.7 5.5,3.9 5.5,4.2 v 14.5 c 0,0.3 0.2,0.5 0.5,0.5 0.3,0 0.5,-0.2 0.5,-0.5 V 4.2 C 6.5,3.9 6.3,3.7 6,3.7 Z
		m 8.2,4 4.9,-3.2 C 19.3,4.4 19.4,4.2 19.3,4 19.2,3.8 19.1,3.7 18.9,3.7 c 0,0 -2.8,0 -5.5,0 -1.4,0 -2.8,0 -3.8,0 -2,0 -2,0 -2.2,0.3 0,0.1 0,0.1 0,0.2 v 7.3 c 0,0.3 0.2,0.5 0.5,0.5 h 11 c 0.2,0 0.4,-0.1 0.4,-0.3 0.1,-0.2 0,-0.4 -0.2,-0.5 z""";

	public static final String ARROW_LEFT = "M 19,9.5 11.7,9.4 13.4,5.5 C 13.4,5.4 13.5,5.3 13.5,5.3 13.5,5.1 13.4,5 13.3,4.9 13.1,4.7 12.9,4.7 12.7,4.8 L 0.8,9.3 C 0.6,9.4 0.4,9.6 0.4,9.9 c 0,0.2 0.1,0.5 0.4,0.5 l 11.7,4.9 c 0.1,0 0.2,0 0.2,0 0.2,0 0.3,-0.1 0.4,-0.2 0.2,-0.2 0.2,-0.4 0.1,-0.6 l -1.6,-4 7.3,0.1 c 0,0 0,0 0,0 0.3,0 0.6,-0.3 0.6,-0.6 C 19.6,9.8 19.3,9.5 19,9.5 Z";
	
	public static final String ARROW_RIGHT = "M 19.2,9.7 7.5,4.8 C 7.3,4.7 7,4.7 6.8,4.9 6.7,5 6.6,5.3 6.7,5.5 L 8.3,9.5 1,9.4 C 0.9,9.4 0.7,9.5 0.6,9.6 0.5,9.7 0.4,9.8 0.4,10 c 0,0.2 0.1,0.3 0.2,0.4 0.1,0.1 0.3,0.2 0.4,0.2 l 7.3,0.1 -1.7,3.9 c -0.1,0.2 -0.1,0.5 0.1,0.7 0.1,0.1 0.3,0.2 0.4,0.2 0.1,0 0.1,0 0.2,0 l 11.8,-4.6 c 0.2,-0.1 0.4,-0.3 0.4,-0.5 C 19.6,10 19.4,9.8 19.2,9.7 Z";
				
	public static final String FIT = """
		m 7.3,12.9 c 0,0 0,-0.1 0,0 v 0 L 7.2,12.8 h 5.3 V 7.4 c 0,0 0.1,0.1 0.1,0.1 0.1,0 0.1,0 0.2,0 h 3 c 0.3,0 0.5,-0.2 0.5,-0.5 0,-0.3 -0.2,-0.5 -0.5,-0.5 H 14 l 4.3,-4.3 c 0.2,-0.2 0.2,-0.5 0,-0.7 -0.2,-0.2 -0.5,-0.2 -0.7,0 l -4.3,4.3 v -2 c 0,-0.3 -0.2,-0.5 -0.5,-0.5 -0.3,0 -0.5,0.2 -0.5,0.5 V 7 7.1 c 0,0 0,0.1 0,0.1 0,0.1 0.1,0.1 0.1,0.1 v 0 H 7.1 c 0,0 0.1,-0.1 0.1,-0.1 0,-0.1 0,-0.1 0,-0.2 V 4 C 7.2,3.7 7,3.5 6.7,3.5 6.4,3.5 6.2,3.7 6.2,3.9 V 5.7 L 1.9,1.4 C 1.7,1.2 1.4,1.2 1.2,1.4 1.1,1.5 1,1.6 1,1.8 1,2 1.1,2.1 1.2,2.2 L 5.5,6.5 H 3.6 C 3.3,6.5 3.1,6.7 3.1,7 c 0,0.3 0.2,0.5 0.5,0.5 h 3.1 c 0.1,0 0.1,0 0.2,0 C 7,7.5 7,7.4 7,7.4 v 0 5.3 c 0,0 0,0 0,0 -0.1,0 -0.1,0 -0.2,0 h -3 c -0.3,0 -0.5,0.2 -0.5,0.5 0,0.3 0.2,0.5 0.5,0.5 H 5.6 L 1.3,18 c -0.2,0.2 -0.2,0.5 0,0.7 0.1,0.1 0.2,0.2 0.4,0.2 0.1,0 0.3,-0.1 0.4,-0.2 l 4.3,-4.3 v 1.9 c 0,0.3 0.2,0.5 0.5,0.5 0.3,0 0.5,-0.2 0.5,-0.5 V 13.2 13.1 C 7.4,13 7.3,13 7.3,12.9 Z		
		m 4.2,-4.5 v 3.4 H 8.1 V 8.4 Z
		m 7.1,9.5 -4.3,-4.3 h 1.9 c 0.3,0 0.5,-0.2 0.5,-0.5 0,-0.3 -0.2,-0.5 -0.5,-0.5 H 13 c -0.1,0 -0.1,0 -0.2,0 0,0 -0.1,0 -0.1,0.1 v 0 c 0,0 -0.1,0.1 -0.1,0.2 0,0.1 0,0.1 0,0.2 v 3 c 0,0.3 0.2,0.5 0.5,0.5 0.3,0 0.5,-0.2 0.5,-0.5 v -1.8 l 4.3,4.3 c 0.1,0.1 0.2,0.2 0.4,0.2 0.2,0 0.3,-0.1 0.4,-0.2 0.1,-0.1 0.2,-0.2 0.2,-0.4 -0.2,0 -0.2,-0.2 -0.3,-0.3 z""";			

	private static final double BUTTON_HEIGHT = 28;	
	
	public static javafx.scene.image.Image IMG_LOGO24;
	
	public static <B extends ButtonBase> B setButtonImage(String svgPathContent, B button) {
		SVGPath svgPath = new SVGPath();
        svgPath.setContent(svgPathContent);
        svgPath.setFill(Color.valueOf("#272525"));

		var scaleFactor = Math.min(1.2, (BUTTON_HEIGHT - 8) / svgPath.prefHeight(-1));	
		svgPath.setScaleX(scaleFactor);
		svgPath.setScaleY(scaleFactor);

		button.setGraphic(svgPath);
		button.setMinHeight(BUTTON_HEIGHT);
        button.setPrefHeight(BUTTON_HEIGHT);
        button.setMaxHeight(BUTTON_HEIGHT);

		return button;
	}

	public static ImageView getImageView(javafx.scene.image.Image image) {
		return new ImageView(image);
	}

	public static ImageView getImageView(String name) {
		if (StringUtils.isBlank(name)) {
			return null;
		}
		
		javafx.scene.image.Image img = 
				new javafx.scene.image.Image(
						getClassLoader()
						.getResourceAsStream(name));
		
		ImageView iv = new ImageView();
		iv.setImage(img);
		
		return iv;
	}
	
	static {
		try {
			
			IMG_LEVEL = ImageIO.read(getClassLoader()
					.getResourceAsStream("level.png"));			
			IMG_WIDTH = ImageIO.read(getClassLoader()
					.getResourceAsStream("width16.png"));
			IMG_SHOVEL = ImageIO.read(getClassLoader()
					.getResourceAsStream("shovel-48.png"));
			IMG_HOR_SLIDER = ImageIO.read(getClassLoader()
					.getResourceAsStream("horSlid16.png"));
			IMG_VER_SLIDER = ImageIO.read(getClassLoader()
					.getResourceAsStream("vertSlid16.png"));
			IMG_LOCK = ImageIO.read(getClassLoader()
					.getResourceAsStream("lock16.png"));
			IMG_UNLOCK = ImageIO.read(getClassLoader()
					.getResourceAsStream("unlock16.png"));
			IMG_CHOOSE = ImageIO.read(getClassLoader()
					.getResourceAsStream("choose16.png"));

			IMG_CLOSE_FILE = ImageIO.read(getClassLoader()
					.getResourceAsStream("closeFile.png"));

			IMG_CLOSE = ImageIO.read(getClassLoader()
					.getResourceAsStream("close.png"));
					
			IMG_GPS = ImageIO.read(getClassLoader()
					.getResourceAsStream("gps32.png"));

			IMG_LOGO24 = new javafx.scene.image.Image(getClassLoader()
					.getResourceAsStream("logo24.png"));
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static ClassLoader getClassLoader() {
		return ResourceImageHolder.class.getClassLoader();
	}
}
