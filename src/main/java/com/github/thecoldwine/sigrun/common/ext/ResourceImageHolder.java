package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import javafx.scene.image.ImageView;

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
	
	public static javafx.scene.image.Image IMG_LOGO24;
	
	public static javafx.scene.image.Image FXIMG_DONE;

	public static ImageView getImageView(String name) {
		if(StringUtils.isBlank(name)) {
			return null;
		}
		
		javafx.scene.image.Image img = new javafx.scene.image.Image(ResourceImageHolder.class.getClassLoader().getResourceAsStream(name));
		ImageView iv = new ImageView();
		iv.setImage(img);
		
		return iv;
	}
	
	static {
		try {
			
			IMG_LEVEL = ImageIO.read(ResourceImageHolder.class.getClassLoader().getResourceAsStream("level.png"));
			
			IMG_LOGO24 = new javafx.scene.image.Image(ResourceImageHolder.class.getClassLoader().getResourceAsStream("logo24.png"));
			
			IMG_WIDTH = ImageIO.read(ResourceImageHolder.class.getClassLoader().getResourceAsStream("width16.png"));
			IMG_SHOVEL = ImageIO.read(ResourceImageHolder.class.getClassLoader().getResourceAsStream("shovel-48.png"));
			IMG_HOR_SLIDER = ImageIO.read(ResourceImageHolder.class.getClassLoader().getResourceAsStream("horSlid16.png"));
			IMG_VER_SLIDER = ImageIO.read(ResourceImageHolder.class.getClassLoader().getResourceAsStream("vertSlid16.png"));
			IMG_LOCK = ImageIO.read(ResourceImageHolder.class.getClassLoader().getResourceAsStream("lock16.png"));
			IMG_UNLOCK = ImageIO.read(ResourceImageHolder.class.getClassLoader().getResourceAsStream("unlock16.png"));
			IMG_CHOOSE = ImageIO.read(ResourceImageHolder.class.getClassLoader().getResourceAsStream("choose16.png"));
			FXIMG_DONE =  new javafx.scene.image.Image(ResourceImageHolder.class.getClassLoader().getResourceAsStream("done-16.png"));
			
			IMG_GPS = ImageIO.read(ResourceImageHolder.class.getClassLoader().getResourceAsStream("gps32.png"));
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
