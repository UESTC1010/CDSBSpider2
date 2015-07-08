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
import org.openqa.selenium.firefox.FirefoxDriver;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.uestc.spider.dao.DBHelper;
import com.uestc.spider.utils.ImageDownloader;
import com.uestc.spider.utils.URLHandler;
import com.uestc.spider.vo.PaperInfo;

public class TenxunSpider implements SpiderInterface {
	final static String collName = "Tenxun";
	final static String encoding = "utf-8";
	final static String PWD = System.getProperty("user.dir");
	final static String[] startUrls = { "http://roll.ent.qq.com/" };

	@Override
	public Queue<String> getPaperUrlList(String[] startUrls) {
		int startUrlNum = startUrls.length;
		Queue<String> paperUrlList = new LinkedList<String>();
		try {
			for (int i = 0; i < startUrlNum; i++) {
				String startUrl = startUrls[i];
				if (startUrl == null) {
					continue;
				}
				WebDriver driver = new FirefoxDriver();
				driver.get(startUrl);
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				String htmlSourceCode = driver.getPageSource();
				driver.quit();
				// System.out.println(htmlSourceCode);
				String paperUrlRegex = "(?<=\\[明星\\]</span><a href=\")http://ent.qq.com/(.*?)\\.[a-z]+(?=\" target=\"_blank\">)";
				Pattern paperUrlPattern = Pattern.compile(paperUrlRegex);
				Matcher paperUrlMatcher = paperUrlPattern
						.matcher(htmlSourceCode);
				while (paperUrlMatcher.find()) {
					String paperUrl = paperUrlMatcher.group();
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

	@SuppressWarnings("unchecked")
	@Override
	public String getPaperProfile(HtmlPage htmlPage) {
		String profile = null;
		try {
			List<DomNode> domList = (List<DomNode>) htmlPage
					.getByXPath("//p[@class=\'titdd-Article\']");
			DomNode node = domList.get(0);
			profile = node.asText().trim();
			// System.out.println(profile);
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Failed to get profile.");
		}
		return profile;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getPaperContent(HtmlPage htmlPage) {
		String content = null;
		try {
			List<DomNode> domList = (List<DomNode>) htmlPage
					.getByXPath("//div[@id=\'Cnt-Main-Article-QQ\']");
			DomNode node = domList.get(0);
			content = node.asText().trim();
			content = content.substring(content.indexOf("\n") + 1);
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
					.getByXPath("//span[@bosszone=\'jgname\']");
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
					.getByXPath("//span[@class=\'a_time\']");
			DomNode node = nodeList.get(0);
			publishDate = node.asText().trim();
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
					.getByXPath("//div[@id=\'Cnt-Main-Article-QQ\']");
			node = nodeList.get(0);
		} catch (IndexOutOfBoundsException e) {
			List<DomNode> nodeList = (List<DomNode>) htmlPage
					.getByXPath("//div[@id=\'SmallWarp\']");
			node = nodeList.get(0);
		} catch (Exception e) {
			return null;
		}
		String effectiveBlock = node.asXml();
		// System.out.println(effectiveBlock);
		String imageRegex = "(?<=src=\")http://img1.gtimg.com/(.*?)\\.(jpg|jpeg|gif|png|bmp)";
		Pattern imagePattern = Pattern.compile(imageRegex);
		Matcher imageMatcher = imagePattern.matcher(effectiveBlock);
		String imageDir = PWD + File.separator + collName;
		int count = 1;
		while (imageMatcher.find()) {
			String imageUrl = imageMatcher.group();
			String imageNum = targetUrl.substring(
					targetUrl.lastIndexOf("/") + 1, targetUrl.lastIndexOf("."));
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
		TenxunSpider txSpider = new TenxunSpider();
		txSpider.handlePaperUrlList();
	}
}
