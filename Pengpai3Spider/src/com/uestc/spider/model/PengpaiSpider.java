package com.uestc.spider.model;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.uestc.spider.dao.DBHelper;
import com.uestc.spider.utils.ImageDownloader;
import com.uestc.spider.utils.URLHandler;
import com.uestc.spider.vo.PaperInfo;

public class PengpaiSpider implements SpiderInterface {
	final static String collName = "Pengpai";
	final static String encoding = "utf-8";
	final static String PWD = System.getProperty("user.dir");
	final static String[] startUrls = { "http://www.thepaper.cn/" };
	final static String paperUrlHeader = "http://www.thepaper.cn/";

	@Override
	public Queue<String> getPaperUrlList(String[] startUrls) {
		int startUrlNum = startUrls.length;
		Queue<String> paperUrlList = new LinkedList<String>();
		try {
			for (int i = 0; i < startUrlNum; i++) {
				String startUrl = startUrls[i];
				WebDriver driver = new HtmlUnitDriver(true);
				driver.get(startUrl);
				try {
					Thread.sleep(20 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				String htmlSourceCode = driver.getPageSource();
				driver.quit();
				// System.out.println(htmlSourceCode);
				// int front = htmlSourceCode.indexOf("<div class=\"main_lt\"");
				// int tail = htmlSourceCode.indexOf("<div class=\"main_rt\"");
				// if (front > tail) {
				// System.err
				// .println("The value of front flag should not greater than tail,please check their regex");
				// return null;
				// }
				// String effectiveBlock = htmlSourceCode.substring(front,
				// tail);
				String paperUrlRegex = "(?<=<a href=\")newsDetail_forward_\\d+";
				Pattern paperUrlPattern = Pattern.compile(paperUrlRegex);
				Matcher paperUrlMatcher = paperUrlPattern
						.matcher(htmlSourceCode);
				while (paperUrlMatcher.find()) {
					String paperUrl = paperUrlHeader + paperUrlMatcher.group();
					if (!paperUrlList.contains(paperUrl)) {
						paperUrlList.offer(paperUrl);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return paperUrlList;
	}

	@Override
	public String getPaperTitle(HtmlPage htmlPage) {
		String title = htmlPage.getTitleText();
		title = title.substring(0, title.indexOf("_")).trim();
		// System.out.println(title);
		return title;
	}

	@Override
	public String getPaperProfile(HtmlPage htmlPage) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getPaperContent(HtmlPage htmlPage) {
		String content = null;
		try {
			List<DomNode> domList = (List<DomNode>) htmlPage
					.getByXPath("//div[@class=\'news_txt\']");
			DomNode node = domList.get(0);
			content = node.asText().trim();
			// System.out.println(content);
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Failed to get content.");
		}
		return content;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getPaperPublisher(HtmlPage htmlPage) {
		String publisher = null;
		try {
			List<DomNode> nodeList = (List<DomNode>) htmlPage
					.getByXPath("//div[@class=\'news_about\']");
			DomNode node = nodeList.get(0);
			publisher = node.asText();
			publisher = publisher.split("\n")[0].trim();
			// System.out.println(publisher);
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Failed to get publisher.");
		}
		return publisher;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getPaperPublishDate(HtmlPage htmlPage) {
		String publishDate = null;
		try {
			List<DomNode> nodeList = (List<DomNode>) htmlPage
					.getByXPath("//div[@class=\'news_about\']");
			DomNode node = nodeList.get(0);
			publishDate = node.asText();
			publishDate = publishDate.split("\n")[1].split("来自")[0].trim();
			publishDate = publishDate.split(" ")[0].replace("-", "");
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Failed to get publish date.");
			return null;
		}
		return publishDate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Queue<String> getPaperImages(HtmlPage htmlPage, String targetUrl) {
		Queue<String> imageList = new LinkedList<String>();
		URLHandler urlHandler = new URLHandler();

		DomNode node = null;
		try {
			List<DomNode> nodeList = (List<DomNode>) htmlPage
					.getByXPath("//div[@class=\'news_txt\']");
			node = nodeList.get(0);
		} catch (IndexOutOfBoundsException e) {
			List<DomNode> nodeList = (List<DomNode>) htmlPage
					.getByXPath("//ul[@id=\'thumb\']");
			node = nodeList.get(0);
		}
		String effectiveBlock = node.asXml();
		// System.out.println(effectiveBlock);
		String imageRegex = "(?<=src=\")http://image.thepaper.cn/(www/)?image/(.*?)\\.(jpg|jpeg|gif|png|bmp)";
		Pattern imagePattern = Pattern.compile(imageRegex);
		Matcher imageMatcher = imagePattern.matcher(effectiveBlock);
		String imageDir = PWD + File.separator + collName;
		int count = 1;
		while (imageMatcher.find()) {
			String imageUrl = imageMatcher.group();
			String imageNum = targetUrl.substring(
					targetUrl.lastIndexOf("_") + 1, targetUrl.length());
			String suffix = imageUrl.substring(imageUrl.lastIndexOf(".") + 1)
					.trim();
			String imageRelativePath = String.format("%s_%04d.%s", imageNum,
					count++, suffix);
			String imageAbsolutePath = imageDir + File.separator
					+ imageRelativePath;
			if (!imageList.contains(imageAbsolutePath)) {
				@SuppressWarnings("static-access")
				String tempImageRelativePath = String.format("%s%s%s", this.collName,File.separator,imageRelativePath); 
				imageList.add(tempImageRelativePath);
				ImageDownloader downloader = new ImageDownloader(imageUrl,
						imageDir, imageRelativePath);
				urlHandler.downloadInTimeout(downloader, 30);
			}
		}
		return imageList;
	}

	private Date getCrawlDate() {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		// System.out.println(date);
		return date;
	}

	@Override
	public void storeData2Db(PaperInfo paperInfo, DBHelper dbHelper) {
		dbHelper.insert2Collection(collName, paperInfo);
	}

	@Override
	public void handlePaperUrl(String url, DBHelper dbHelper) {
		try {
			WebClient client = new WebClient();
			client.getOptions().setJavaScriptEnabled(false);
			client.getOptions().setCssEnabled(false);
			HtmlPage htmlPage = null;
			try {
				htmlPage = client.getPage(url);
			} catch (FailingHttpStatusCodeException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (htmlPage == null) {
				client.close();
				return;
			}
			String title = null, profile = null, content = null, publisher = null, publishDate = null;
			Date crawlDate = null;
			Queue<String> imageList = null;
			title = getPaperTitle(htmlPage);
			profile = getPaperProfile(htmlPage);
			content = getPaperContent(htmlPage);
			publisher = getPaperPublisher(htmlPage);
			publishDate = getPaperPublishDate(htmlPage);
			imageList = getPaperImages(htmlPage, url);
			crawlDate = getCrawlDate();
			PaperInfo paperInfo = new PaperInfo(title, profile, content,
					publisher, publishDate, imageList, url, crawlDate);
			storeData2Db(paperInfo, dbHelper);
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handlePaperUrlList() {
		Queue<String> urlList = getPaperUrlList(startUrls);
		if (urlList == null) {
			System.out.println("Failed to get url list.");
			return;
		}
		DBHelper dbHelper = new DBHelper();
		while (!urlList.isEmpty()) {
			String paperUrl = urlList.poll();
			handlePaperUrl(paperUrl, dbHelper);
		}
		dbHelper.quit();
	}

	public static void main(String[] args) {
		PengpaiSpider pengpaiSpider = new PengpaiSpider();
		// pengpaiSpider.handlePaperUrlList();
		String[] startUrls = { "http://www.thepaper.cn/" };
		System.out.println(pengpaiSpider.getPaperUrlList(startUrls).size());

	}
}
