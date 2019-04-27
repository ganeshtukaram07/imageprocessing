package com.tss.filters;

import java.io.File;
import java.io.FileFilter;

public class FileUtil {

	public File[] findAllFile() {
		try {
			File folder = new File("output_images");
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
}
