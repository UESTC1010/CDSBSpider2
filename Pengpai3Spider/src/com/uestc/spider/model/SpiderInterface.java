package com.uestc.spider.model;

import java.util.Queue;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.uestc.spider.dao.DBHelper;
import com.uestc.spider.vo.PaperInfo;

public interface SpiderInterface {

	public Queue<String> getPaperUrlList(String[] startUrls);

	public String getPaperTitle(HtmlPage htmlPage);

	public String getPaperProfile(HtmlPage htmlPage);

	public String getPaperContent(HtmlPage htmlPage);

	public String getPaperPublisher(HtmlPage htmlPage);

	public String getPaperPublishDate(HtmlPage htmlPage);

	public Queue<String> getPaperImages(HtmlPage htmlPage, String targetUrl);

	public void storeData2Db(PaperInfo paperInfo, DBHelper dbHelper);

	public void handlePaperUrl(String url, DBHelper dbHelper);

	public void handlePaperUrlList();
}
