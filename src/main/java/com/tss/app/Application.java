package com.tss.app;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.tss.filters.FileUtil;
import com.tss.filters.HighpassFilters;
import com.tss.filters.ImFilter;
import com.tss.filters.LowpassFilters;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.AutoThresholder.Method;

public class Application {

	public static void main(String[] args) {
		/**
		 * 
		 * FIRST START IM-FILTER ALGORITHM 
		 * INPUT- ./input_images/
		 * OUTPUT-./output_images/
		 * 
		 */
		try {
			ImFilter imFilter = new ImFilter();
			File[] files = imFilter.findAllFile();
			imFilter.deleteAllFiles();
			if (files.length == 0) {
				System.out.println("NOT FOUND FILES...");
			} else {
				for (File file : files) {
					Image image = ImageIO.read(file);
					ImagePlus imagePlus = new ImagePlus("INPUT IMAGE", image);
					imagePlus.show();
					BufferedImage sourceImage = imFilter.getImage(file);
					BufferedImage outputImage = imFilter.texture(5.495, sourceImage);
					imFilter.writeImage(outputImage, file.getName());
					System.out.println("convolution finished");
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		/**
		 * SECOND HIGH PASS FILTER ALGORITHM APPLY INPUT- ./output_images/ OUTPUT- SHOW
		 * IN FRAME
		 * 
		 */
		try {
			FileUtil fileUtil = new FileUtil();
			File[] files = fileUtil.findAllFile();
			
			System.out.println("CALL ALL FILE DELETE METHOD" );
			/**
			 * DELTE ALL METHODS OUTPUT FILE
			 */
			HighpassFilters.deleteAllFiles();
			System.out.println("DELTE ALL FILES..");
			for (File file : files) {
				// new File("D://2.png")
				Image image = ImageIO.read(file);
				ImagePlus imagePlus = new ImagePlus("INPUT IMAGE", image);
				imagePlus.show();
				HighpassFilters highpassFilters = new HighpassFilters(file.getName());
				highpassFilters.setup("Highpass filter", imagePlus);
				ImageProcessor ip = imagePlus.getChannelProcessor();
				highpassFilters.run(ip);
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		/**
		 * THIRED LOW PASS FILTER ALGORITHM 
		 * INPUT- ./output_images/ 
		 * OUTPUT- SHOW IN FRAME
		 * 
		 */
//		try {
//			FileUtil fileUtil = new FileUtil();
//			File[] files = fileUtil.findAllFile();
//			for (File file : files) {
//				// new File("D://2.png")
//				Image image = ImageIO.read(file);
//				ImagePlus imagePlus = new ImagePlus("Input Image", image);
//				imagePlus.show();
//				LowpassFilters lowpassFilters = new LowpassFilters();
//				lowpassFilters.setup("LOW PASS FILTER", imagePlus);
//				ImageProcessor ip = imagePlus.getChannelProcessor();
//				lowpassFilters.run(ip);
//				// lowpassFilters.showDialog(ip);
//				// lowpassFilters.filtering(ip, imagePlus);
//			}
//		} catch (Exception e) {
//			e.printStackTrace(System.err);
//		}
	}
}
