package com.uestc.spider.dao;

import java.util.Iterator;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.uestc.spider.vo.PaperInfo;

public class DBHelper {
	final static String dbIP = "localhost";
	final static int dbPort = 27017;
	final static String dbName = "MJTXPPData";
	private MongoClient mongoClient;

	public DBHelper() {
		ServerAddress mongoAddress = new ServerAddress(dbIP, dbPort);
		this.mongoClient = new MongoClient(mongoAddress);
	}

	public void quit() {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	public void insert2Collection(String collName, PaperInfo paperInfo) {
		MongoDatabase mongoDB = mongoClient.getDatabase(dbName);
		MongoCollection<Document> coll = mongoDB.getCollection(collName);
		Document doc = new Document();
		doc.append("Title", paperInfo.getTitle());
		doc.append("abstruct", paperInfo.getAbstruct());
		doc.append("Content", paperInfo.getContent());
		doc.append("NewSource", paperInfo.getNewSource());
		doc.append("downloadTime", paperInfo.getDownloadTime());
		doc.append("image", paperInfo.getImage());
		doc.append("Url", paperInfo.getUrl());
		doc.append("Date", paperInfo.getDate());
		Document query = new Document("Url", paperInfo.getUrl());
		Iterator<Document> iter = coll.find(query).iterator();
		if (!iter.hasNext()) {
			coll.insertOne(doc);
		}
	}

}
