package com.ugcs.gprvisualizer.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.imaging.formats.tiff.write.TiffImageWriterLossless;

public class Tiff6 {

	BufferedImage img = createSomeImage();
	
	
	public static void main(String[] args) throws Exception {
		
		
		new Tiff6().test();
	}
	
	public void test() throws Exception {
		byte[] exifBytes = new byte[0];
		Map<String, Object> params = new HashMap<>();
		
		FileOutputStream os = new FileOutputStream(new File("d:/tmp/tiff/created3.tif"));
		
		TiffImageWriterLossless t = 
				new TiffImageWriterLossless(exifBytes);
		
		t.writeImage(img, os, params);
	}
	
	
	
	public static BufferedImage createSomeImage() {
		BufferedImage img = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) img.getGraphics();
		
		g2.setColor(Color.GRAY);
		
		int mrg = img.getHeight() / 4;
		
		g2.fillRect(mrg, mrg, img.getWidth() - 2 * mrg, img.getHeight() - 2 * mrg);
		
		g2.setColor(Color.RED);
		for (int x = 10; x < img.getWidth(); x += 20) {
			g2.drawLine(x, 30, x, img.getHeight() - 30);
		}
		return img;
	}
	
}
