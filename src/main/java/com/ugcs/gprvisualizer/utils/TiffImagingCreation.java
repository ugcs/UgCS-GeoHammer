package com.ugcs.gprvisualizer.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;

import org.apache.commons.imaging.formats.tiff.write.TiffImageWriterLossless;
import org.apache.commons.imaging.ImagingConstants;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoDoubles;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoShorts;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import com.github.thecoldwine.sigrun.common.ext.LatLon;

public class TiffImagingCreation {

	public static void main(String[] args) throws Exception {
		File f1 = new File("d:/tmp/tiff/created1.tif");
		BufferedImage img = createSomeImage();
		LatLon lt =  new LatLon(54, 			24.00); 
		LatLon rb = new LatLon(54 - 0.02, 	24.02);
		
//		ImageIO.write(img, "TIFF", f1);		
//		
//		TiffOutputSet tiffExif = new TiffImagingCreation().prepareGeoExif(img, lt, rb);
//		
//		OutputStream os = new FileOutputStream(new File("d:/tmp/tiff/created2.tif"));
//		
//		new ExifRewriter().updateExifMetadataLossless(f1, os,
//				tiffExif);
		
		
		
		new TiffImagingCreation().save(f1, img, 
				lt, rb);
	}
	
	public void save(File f1,
		BufferedImage img,
		LatLon lt,
		LatLon rb) throws Exception {
		
		
		final Map<String, Object> params = new HashMap<>();
        params.put(ImagingConstants.PARAM_KEY_COMPRESSION, TiffConstants.TIFF_COMPRESSION_UNCOMPRESSED);
        
        //params.put("BITS_PER_SAMPLE", 4);        
        
        
		
		TiffOutputSet tiffExif = prepareGeoExif(img, lt, rb);
		
        params.put("EXIF", tiffExif);		
		
		Imaging.writeImage(img, f1, ImageFormats.TIFF, params);
		///////////////////
	}

	public TiffOutputSet prepareGeoExif(BufferedImage img, LatLon lt, LatLon rb)
			throws ImageWriteException {
		TiffOutputSet tiffExif = new TiffOutputSet();
		TiffOutputDirectory dir = tiffExif.addRootDirectory();


		dir.add(new TagInfoDoubles("ModelPixelScaleTag", 33550, 3, 
				TiffDirectoryType.TIFF_DIRECTORY_ROOT),
				new double[] {
						(rb.getLonDgr() - lt.getLonDgr()) / (double) img.getWidth(), 
						(rb.getLatDgr() - lt.getLatDgr()) / (double) img.getHeight(), 
						0.0}
			);
		
 
		dir.add(new TagInfoDoubles("ModelTiepointTag", 33922, 6,
				TiffDirectoryType.TIFF_DIRECTORY_ROOT),
				new double[] {
						0.0, 	0.0, 	0.0, 	lt.getLonDgr(), 	lt.getLatDgr(), 	0.0
				}
				
						//440720.0, 100000.0, 0.0}
			);

		dir.add(new TagInfoShorts("GeoKeyDirectoryTag", 34735, 16, 
				TiffDirectoryType.TIFF_DIRECTORY_ROOT),
				new short[] {
					1,    1, 0, 3,
					1024, 0, 1, 2,
					1025, 0, 1, 1,
					2048, 0, 1, 4326
				}
			);
		return tiffExif;
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

	/*
//		 256 (0x100: ImageWidth): 512 (1 Short)
//		 257 (0x101: ImageLength): 512 (1 Short)
//		 258 (0x102: BitsPerSample): 8 (1 Short)
//		 259 (0x103: Compression): 1 (1 Short)
		 
		dir.add(new TagInfoShort("ImageWidth", 256, 
				TiffDirectoryType.TIFF_DIRECTORY_ROOT), 
				(short) 1000 );
		 
		dir.add(new TagInfoShort("ImageLength", 257, 
				TiffDirectoryType.TIFF_DIRECTORY_ROOT), 
				(short) 800 );

		dir.add(new TagInfoShort("BitsPerSample", 258, 
				TiffDirectoryType.TIFF_DIRECTORY_ROOT), 
				(short) 8 );

		dir.add(new TagInfoShort("Compression", 259, 
				TiffDirectoryType.TIFF_DIRECTORY_ROOT), 
				(short) TiffConstants.TIFF_COMPRESSION_UNCOMPRESSED );

//		 262 (0x106: PhotometricInterpretation): 1 (1 Short)
//		 273 (0x111: PreviewImageStart): 8, 8200, 16392, 24584, 32776, 40968, 49160, 57352, 65544, 73736, 81928, 90120, 98312, 106504, 114696, 122888, 131080, 139272, 147464, 155656, 163848, 172040, 180232, 188424, 196616, 204808, 213000, 221192, 229384, 237576, 245768, 253960 (32 Long)
//		 277 (0x115: SamplesPerPixel): 1 (1 Short)
//		 278 (0x116: RowsPerStrip): 16 (1 Short)
//		 279 (0x117: PreviewImageLength): 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192, 8192 (32 Long)
//		 284 (0x11c: PlanarConfiguration): 1 (1 Short)		 

	 */
}
