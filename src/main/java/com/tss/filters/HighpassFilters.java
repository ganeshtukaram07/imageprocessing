package com.tss.filters;

import ij.*;
import ij.process.*;
import ij.plugin.filter.*;
import ij.plugin.frame.RoiManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Box.Filler;

import ij.gui.*;
import ij.measure.ResultsTable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class HighpassFilters implements PlugInFilter {
	private int threshold, order;
	private int M, N, size, w, h;
	private ImagePlus imp;
	private FHT fht;
	private ImageProcessor mask, ipFilter;
	private String filter;
	private boolean displayFilter;
	private String fileName;

	/**
	 * START APPLICATION HERE....
	 * 
	 * @param args
	 * @throws IOException
	 */
	// public static void main(String[] args) throws IOException {
	// FileUtil fileUtil = new FileUtil();
	// File[] files = fileUtil.findAllFile();
	// for (File file : files) {
	// // new File("D://2.png")
	// Image image = ImageIO.read(file);
	// ImagePlus imagePlus = new ImagePlus("INPUT IMAGE", image);
	// imagePlus.show();
	// HighpassFilters highpassFilters = new HighpassFilters();
	// highpassFilters.setup("Highpass filter", imagePlus);
	// ImageProcessor ip = imagePlus.getChannelProcessor();
	// highpassFilters.run(ip);
	// }
	// }
	public HighpassFilters(String fileName) {
		this.fileName = fileName;
	}

	// method from PlugInFilter Interface
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		
		return DOES_ALL;
	}

	// method from PlugInFilter Interface
	public void run(ImageProcessor ip) {

		ip = imp.getProcessor();
		if (showDialog(ip))
			filtering(ip, imp);
		IJ.showProgress(1.0);
	}

	/**
	 * the following method opens a window for users
	 * 
	 * @param ip
	 * @return
	 */
	boolean showDialog(ImageProcessor ip) {
		int dim = 0;
		M = ip.getWidth();
		N = ip.getHeight();
		if (M != N)
			dim = (int) (Math.min(M, N) / 2);
		else
			dim = M / 2;
		threshold = 20;
		order = 1;
		String[] choices = { "Ideal", "Butterworth", "Gaussian" };

		GenericDialog gd = new GenericDialog("Filters");
		gd.addChoice("Highpass Frequency Filters: ", choices, "Ideal");
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		int choiceIndex = gd.getNextChoiceIndex();
		filter = choices[choiceIndex];

		GenericDialog gd2 = new GenericDialog("Filter Parameters");
		gd2.addNumericField("Threshold Factor:", threshold, 0);
		if (filter.equals("Butterworth"))
			gd2.addNumericField("Order:", order, 0);
		gd2.addCheckbox("Display Filter", displayFilter);
		gd2.showDialog();
		if (gd2.wasCanceled())
			return false;
		if (gd2.invalidNumber()) {
			IJ.error("Error", "Invalid input number");
			return false;
		}
		threshold = (int) gd2.getNextNumber();
		if (filter.equals("Butterworth"))
			order = (int) gd2.getNextNumber();
		displayFilter = gd2.getNextBoolean();
		if (threshold >= 0 && threshold <= dim)
			return true;
		else {
			GenericDialog gd3;
			boolean flag = true;
			while (flag) {
				threshold = 20;
				JOptionPane.showMessageDialog(null, "error, threshold must belong to [" + 0 + "," + dim + "]");
				gd3 = new GenericDialog(" Threshold ");
				gd3.addNumericField("Threshold Factor:", threshold, 0);
				gd3.showDialog();
				if (gd3.wasCanceled() || gd3.invalidNumber())
					return false;
				else {
					threshold = (int) gd3.getNextNumber();
					if (threshold >= 0 && threshold <= dim)
						flag = false;
				}
			}
		}
		return true;
	}

	/**
	 * shows the power spectrum and filters the image
	 * 
	 * @param ip
	 * @param imp
	 */

	public void filtering(ImageProcessor ip, ImagePlus imp) {
		int maxN = Math.max(M, N);
		size = 2;
		while (size < maxN)
			size *= 2;
		IJ.runPlugIn("ij.plugin.FFT", "forward");
		h = Math.round((size - N) / 2);
		w = Math.round((size - M) / 2);
		ImageProcessor ip2 = ip.createProcessor(size, size); // processor of the padded image
		ip2.fill();
		ip2.insert(ip, w, h);
		if (ip instanceof ColorProcessor) {
			ImageProcessor bright = ((ColorProcessor) ip2).getBrightness();
			fht = new FHT(bright);
			fht.rgb = (ColorProcessor) ip.duplicate(); // get a duplication of brightness in order to add it after
														// filtering
		} else
			fht = new FHT(ip2);

		fht.originalColorModel = ip.getColorModel();
		fht.originalBitDepth = imp.getBitDepth();
		fht.transform(); // calculates the Fourier transformation

		if (filter.equals("Ideal"))
			ipFilter = Ideal();
		if (filter.equals("Butterworth"))
			ipFilter = Butterworth(order);
		if (filter.equals("Gaussian"))
			ipFilter = Gaussian();

		fht.swapQuadrants(ipFilter);
		byte[] pixels_id = (byte[]) ipFilter.getPixels();
		float[] pixels_fht = (float[]) fht.getPixels();
		// System.out.println("FFT MATRIX PIXEL: SHOW IN TEXT FILE ");
		// File filePixelOutput = new File("pixel_output");
		// if (!filePixelOutput.exists()) {
		// filePixelOutput.mkdirs();
		// }

		// File file = new File(filePixelOutput.getAbsolutePath() + File.separator +
		// "fft_pixel_output.txt");
		// StringBuilder builder = new StringBuilder();
		for (int i = 0; i < size * size; i++) {
			pixels_fht[i] = (float) (pixels_fht[i] * (pixels_id[i] & 255) / 255.0);
			// builder.append(pixels_fht[i]).append(",");

		}

		// try {
		// BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		// writer.write(builder.toString());
		//
		// writer.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		mask = fht.getPowerSpectrum();
		// System.out.println("FFT FINAL OUTPUT :: ");
		// float[][] ss =fht.getFloatArray();
		// for(int i=0; i < ss.length;i++ ) {
		// for(int j = 0 ; j < ss[i].length;j++) {
		// System.out.print(ss[i][j]+"\t");
		// }
		// System.out.println();
		// }
		ImagePlus imp2 = new ImagePlus("inverse FFT of " + imp.getTitle(), mask);
		imp2.setProperty("FHT", fht);
		imp2.setCalibration(imp.getCalibration());
		// BufferedImage bImage = mask.getBufferedImage(); //to combine inverse and
		// filterimage
		BufferedImage bImage = imp2.getBufferedImage();

		ResultsTable.createTableFromImage(ip).show("RESULT TABLE");

		System.out.println("CREATE TABLE REULST");
		writeImage(bImage, fileName+"_" + imp.getTitle() + ".jpg");
		doInverseTransform(fht);
	}

	/**
	 * creates an ideal highpass filter
	 * 
	 * @return
	 */
	public ByteProcessor Ideal() {
		ByteProcessor ip = new ByteProcessor(M, N);
		ip.setColor(Color.white);
		ip.fill();
		int xcenter = M / 2;
		int ycenter = N / 2;

		for (int radius = 0; radius < threshold; radius++) {
			for (double counter = 0; counter < 10; counter = counter + 0.001) {
				double x = Math.sin(counter) * radius + xcenter;
				double y = Math.cos(counter) * radius + ycenter;
				ip.putPixel((int) x, (int) y, 0);
			}

		}

		ByteProcessor ip2 = new ByteProcessor(size, size);
		byte[] p = (byte[]) ip2.getPixels();
		for (int i = 0; i < size * size; i++)
			p[i] = (byte) 255;
		ip2.insert(ip, w, h);
		if (displayFilter)
			new ImagePlus("Ideal filter", ip2).show();
		BufferedImage fImage = ip2.getBufferedImage();
		writeImage(fImage, fileName+"_ideal.jpg");

		return ip2;
	}

	/**
	 * creates a Butterworth highpass filter
	 * 
	 * @param n
	 * @return
	 */
	public ByteProcessor Butterworth(int n) {
		ByteProcessor ip = new ByteProcessor(M, N);
		double value = 0;
		double distance = 0;
		int xcenter = (M / 2) + 1;
		int ycenter = (N / 2) + 1;

		for (int y = 0; y < N; y++) {
			for (int x = 0; x < M; x++) {
				distance = Math.abs(x - xcenter) * Math.abs(x - xcenter)
						+ Math.abs(y - ycenter) * Math.abs(y - ycenter);
				distance = Math.sqrt(distance);
				double parz = Math.pow(threshold / distance, 2 * n);
				value = 255 * (1 / (1 + parz));
				ip.putPixelValue(x, y, value);
			}
		}

		ByteProcessor ip2 = new ByteProcessor(size, size);
		byte[] p = (byte[]) ip2.getPixels();
		for (int i = 0; i < size * size; i++)
			p[i] = (byte) 255;
		ip2.insert(ip, w, h);

		BufferedImage fImage = ip2.getBufferedImage();
		writeImage(fImage, fileName+"_butterworth.jpg");
		if (displayFilter)
			new ImagePlus("Butterworth filter", ip2).show();
		return ip2;
	}

	public void writeImage(BufferedImage textureImage, String fileName) {
		try {
			File folder = new File("output_methods");
			if (!folder.exists()) {
				folder.mkdirs();
			}

			ImageIO.write(textureImage, "jpg", new File(folder + File.separator + fileName));

		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * creates a gaussian highpass filter
	 * 
	 * @return
	 */

	public ByteProcessor Gaussian() {
		ByteProcessor ip = new ByteProcessor(M, N);
		double value = 0;
		double distance = 0;
		int xcenter = (M / 2) + 1;
		int ycenter = (N / 2) + 1;
		// System.out.println("GAUSSIAN METHOD RESULT :: ");
		for (int y = 0; y < N; y++) {
			for (int x = 0; x < M; x++) {
				distance = Math.abs(x - xcenter) * Math.abs(x - xcenter)
						+ Math.abs(y - ycenter) * Math.abs(y - ycenter);
				distance = Math.sqrt(distance);
				value = 255 - (255 * Math.exp((-1 * distance * distance) / (2 * threshold * threshold)));
				// System.out.print(value + "\t");
				ip.putPixelValue(x, y, value);
			}
			// System.out.println();
		}

		ByteProcessor ip2 = new ByteProcessor(size, size);
		byte[] p = (byte[]) ip2.getPixels();
		for (int i = 0; i < size * size; i++)
			p[i] = (byte) 255;
		ip2.insert(ip, w, h);
		if (displayFilter)
			new ImagePlus("Gaussian filter", ip2).show();

		BufferedImage fImage = ip2.getBufferedImage();
		writeImage(fImage, fileName+"gaussian.jpg");

		return ip2;
	}

	/**
	 * applies the inverse Fourier transform to the filtered image
	 * 
	 * @param fht
	 */
	void doInverseTransform(FHT fht) {
		fht = fht.getCopy();
		fht.inverseTransform();
		fht.resetMinAndMax();
		ImageProcessor ip2 = fht;
		fht.setRoi(w, h, M, N);
		ip2 = fht.crop();

		int bitDepth = fht.originalBitDepth > 0 ? fht.originalBitDepth : imp.getBitDepth();
		switch (bitDepth) {
		case 8:
			ip2 = ip2.convertToByte(true);
			break;
		case 16:
			ip2 = ip2.convertToShort(true);
			break;
		case 24:
			if (fht.rgb == null || ip2 == null) {
				IJ.error("FFT", "Unable to set brightness");
				return;
			}
			ColorProcessor rgb = (ColorProcessor) fht.rgb.duplicate();
			rgb.setBrightness((FloatProcessor) ip2);
			ip2 = rgb;
			fht.rgb = null;
			break;
		case 32:
			break;
		}
		if (bitDepth != 24 && fht.originalColorModel != null)
			ip2.setColorModel(fht.originalColorModel);
		String title = imp.getTitle();
		if (title.startsWith("FFT of "))
			title = title.substring(7, title.length());
		ImagePlus imp2 = new ImagePlus("Inverse FFT of " + title, ip2);
		if (imp2.getWidth() == imp.getWidth())
			imp2.setCalibration(imp.getCalibration());
		imp2.show();
		BufferedImage fImage = imp2.getBufferedImage();
		writeImage(fImage, fileName+"__inverse_fft_of_" + title + ".jpg");

		// imp2.draw();
	}

	public static void deleteAllFiles() {
		try {
			File folder = new File("output_methods");
			File[] files = folder.listFiles(new FileFilter() {

				public boolean accept(File pathname) {

					return pathname.getName().endsWith("jpg") || pathname.getName().endsWith("JPG")
							|| pathname.getName().endsWith("png");
				}
			});
			System.out.println("NUMBER OF FILES :: " + files.length);
			if (folder.exists()) {
				for (File file : files) {
					file.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

	}
}