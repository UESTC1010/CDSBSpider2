package com.uestc.spider.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class URLHandler {

	public String getHTMLCodeByHTTP(String targetUrl, String encoding) {
		int tryCount = 5;
		while (tryCount-- > 0) {
			try {
				URL url = new URL(targetUrl);
				String html = "";
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setConnectTimeout(10 * 1000);
				connection.setReadTimeout(10 * 1000);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream(),
								encoding));
				String line = null;
				while ((line = reader.readLine()) != null) {
					html += line + "\n";
				}
				reader.close();
				if (html != null) {
					return html;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public boolean downloadInTimeout(ImageDownloader downloader, int timeout) {
		Thread t = new Thread(downloader);
		t.start();
		try {
			t.join(timeout*1000);
			return true;
		} catch (InterruptedException e) {
			System.err.println("Iamge download timeout in "
					+ downloader.imageUrl);
			return false;
		}
	}
}
