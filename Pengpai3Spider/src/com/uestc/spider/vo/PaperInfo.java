package com.uestc.spider.vo;

import java.util.Date;
import java.util.Queue;

public class PaperInfo {
	private String title;
	private String abstruct;
	private String content;
	private String newSource;
	private String downloadTime;
	private Queue<String> image;
	private String url;
	private Date date;

	public String getTitle() {
		return title;
	}

	public String getAbstruct() {
		return abstruct;
	}

	public String getContent() {
		return content;
	}

	public String getNewSource() {
		return newSource;
	}

	public String getDownloadTime() {
		return downloadTime;
	}

	public Queue<String> getImage() {
		return image;
	}

	public String getUrl() {
		return url;
	}

	public Date getDate() {
		return date;
	}

	public PaperInfo(String title, String abstruct, String content,
			String newSource, String downloadTime, Queue<String> image,
			String url, Date date) {
		super();
		this.title = title;
		this.abstruct = abstruct;
		this.content = content;
		this.newSource = newSource;
		this.downloadTime = downloadTime;
		this.image = image;
		this.url = url;
		this.date = date;
	}

}
