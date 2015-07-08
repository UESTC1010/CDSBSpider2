package com.uestc.spider.utils;

import java.util.Calendar;
import java.util.TimerTask;

import com.uestc.spider.model.MeijingSpider;
import com.uestc.spider.model.PengpaiSpider;
import com.uestc.spider.model.TenxunSpider;

public class ScheduledTimerTask extends TimerTask {

	@Override
	public void run() {
		MeijingSpider meijing = new MeijingSpider();
		PengpaiSpider pengpai = new PengpaiSpider();
		TenxunSpider tenxun = new TenxunSpider();
		System.out.println(Calendar.getInstance().getTime()
				+ "  running MeijingSpider...");
		meijing.handlePaperUrlList();
		System.out.println(Calendar.getInstance().getTime()
				+ "  Meijing finished");
		System.out.println(Calendar.getInstance().getTime()
				+ "  running PengpaiSpider...");
		pengpai.handlePaperUrlList();
		System.out.println(Calendar.getInstance().getTime()
				+ "  Pengpai finished");
		System.out.println(Calendar.getInstance().getTime()
				+ "  running TenxunSpider...");
		tenxun.handlePaperUrlList();
		System.out.println(Calendar.getInstance().getTime()
				+ "  Tenxun finished");
	}

}
