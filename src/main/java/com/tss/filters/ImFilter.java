package com.tss.filters;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImFilter {

	int counter = 1;

	public File[] findAllFile() {
		try {
			File folder = new File("input_images");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			return folder.listFiles(new FileFilter() {

				public boolean accept(File pathname) {

					return pathname.getName().endsWith("jpg") || pathname.getName().endsWith("png");
				}
			});
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}

	public BufferedImage getImage(File file) {
		BufferedImage image = null;
		try {
			// System.out.println("enter image name");
			//
			// BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
			// String imageName;
			// imageName = bf.readLine();
			// File input = new File(imageName);
			image = ImageIO.read(file);
			System.out.println("image loaded successfully");
		} catch (Exception e) {
			System.out.println(e);
		}
		return image;
	}

	public BufferedImage texture(double teta, BufferedImage sourceImage) throws IOException {
		// BufferedImage sourceImage = null;
		BufferedImage destImage = null;

		ColorModel srcCM = null;
		// sourceImage = GetImage();
		srcCM = sourceImage.getColorModel();
		// dstCM=srcCM;
		double t;
		t = teta;
		destImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		System.out.println(srcCM.getColorSpace().getType());
		// srcCM.createCompatibleWritableRaster(sourceImage.getWidth(),sourceImage.getHeight());
		double a = 0.7071;
		int i = 0;
		float[] sharpen = new float[24];
		double w = 2;
		double inter1 = 0, inter2 = 0, inter3 = 0;
		double inter4 = 0, inter5 = 0, inter6 = 0;
		double finalAns = 0;
		inter2 = Math.pow(((-1) * a), (2 * w));
		// System.out.println(inter2);
		inter5 = w * 3.14 * Math.pow(a, w);
		for (int x = 1; x < 5; x++) {
			for (int y = 1; y < 7; y++) {
				inter1 = (Math.pow(x, 2) + Math.pow(y, 2)) / 2;
				// System.out.println(inter1);
				inter3 = Math.exp(inter1 * inter2);
				// System.out.println(inter3);
				inter4 = (x * Math.cos(t)) + (y * Math.sin(t));
				inter6 = Math.exp(inter4 * inter5);

				finalAns = inter6 * inter3;
				System.out.println(finalAns);
				sharpen[i] = (float) finalAns;
				i++;
			}
		}
		// System.out.print("kernel is");

		Kernel kernel = new Kernel(4, 6, sharpen);
		BufferedImageOp sharpenFilter = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

		return destImage = sharpenFilter.filter(sourceImage, destImage);

	}

	public ImFilter() throws IOException {
		// texture(5.495);
	}

	// @Override
	// public void paint(Graphics g) {
	// g.drawImage(destImage, 0, 0, null);
	// }

	public void writeImage(BufferedImage textureImage, String fileName) {
		try {
			File folder = new File("output_images");
			if (!folder.exists()) {
				folder.mkdirs();
			}

			ImageIO.write(textureImage, "jpg", new File(folder + File.separator + fileName));
			counter++;
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public void deleteAllFiles() {
		try {
			File folder = new File("output_images");
			File[] files = folder.listFiles(new FileFilter() {

				public boolean accept(File pathname) {

					return pathname.getName().endsWith("jpg") || pathname.getName().endsWith("png");
				}
			});
			if (folder.exists()) {
                          for(File file : files) {
                        	  file.delete();
                          }
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

	}

	// public static void main(String[] args) throws IOException {
	// // // TODO code application logic here
	// // // GaborFilter gf =new GaborFilter();
	// // JFrame frame = new JFrame("display image");
	// // Panel panel = new ImFilter();
	// // frame.getContentPane().add(panel);
	// // frame.setSize(500, 500);
	// // frame.setVisible(true);
	//
	// ImFilter imFilter = new ImFilter();
	// File[] files = imFilter.findAllFile();
	// if (files.length == 0) {
	// System.out.println("NOT FOUND FILES...");
	// } else {
	// for (File file : files) {
	// BufferedImage sourceImage = imFilter.getImage(file);
	// BufferedImage outputImage = imFilter.texture(5.495, sourceImage);
	// imFilter.writeImage(outputImage, file.getName());
	// System.out.println("convolution finished");
	// }
	// }
	// }
}