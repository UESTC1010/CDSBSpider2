package com.uestc.spider.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageDownloader implements Runnable {
	public String imageUrl;
	private String storeDir; // D:\\imageDir
	private String imageRelativePath; // testPhoto_001.jpg

	public ImageDownloader(String imageUrl, String storeDir,
			String imageRelativePath) {
		this.imageUrl = imageUrl;
		this.storeDir = storeDir;
		this.imageRelativePath = imageRelativePath;
	}

	public void run() {
		File dir = new File(storeDir);
		if (!dir.isDirectory()) {
			dir.mkdirs();
		}
		OutputStream os = null;
		InputStream is = null;
		try {
			URL url = new URL(imageUrl);
			File outFile = new File(storeDir + File.separator
					+ imageRelativePath);
			if (outFile.isFile()) {
				return;
			}
			os = new FileOutputStream(outFile);
			is = url.openStream();
			byte[] buffer = new byte[1024];
			int length = -1;
			while ((length = is.read(buffer)) != -1) {
				os.write(buffer, 0, length);
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
