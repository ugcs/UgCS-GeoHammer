package com.github.thecoldwine.sigrun.common.ext;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ResourceImageHolder {

	public static Image IMG_SHOVEL;
	static {
		try {
			IMG_SHOVEL = ImageIO.read(ResourceImageHolder.class.getClassLoader().getResourceAsStream("shovel-48.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
