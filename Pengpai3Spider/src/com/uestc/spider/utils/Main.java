package com.uestc.spider.utils;

import java.util.Timer;

public class Main {
	public static void main(String[] args) {
		ScheduledTimerTask stt = new ScheduledTimerTask();
		Timer timer = new Timer();
		timer.schedule(stt, 0, 180 * 1000);
	}
}
