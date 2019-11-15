package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ResourceImageHolder {

	public static Image IMG_SHOVEL;
	public static javafx.scene.image.Image FXIMG_DONE;

	
	static {
		try {
			IMG_SHOVEL = ImageIO.read(ResourceImageHolder.class.getClassLoader().getResourceAsStream("shovel-48.png"));
			
			FXIMG_DONE =  new javafx.scene.image.Image(ResourceImageHolder.class.getClassLoader().getResourceAsStream("done-16.png"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
