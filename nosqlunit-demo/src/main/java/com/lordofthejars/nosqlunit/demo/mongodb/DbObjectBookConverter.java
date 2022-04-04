package com.lordofthejars.nosqlunit.demo.mongodb;

import static com.lordofthejars.nosqlunit.demo.mongodb.MongoDbBookConverter.NUM_PAGES_FIELD;
import static com.lordofthejars.nosqlunit.demo.mongodb.MongoDbBookConverter.TITLE_FIELD;

import com.lordofthejars.nosqlunit.demo.model.Book;
import com.mongodb.DBObject;

public class DbObjectBookConverter {

	public Book convert(DBObject dbObject) {
		
		String title = (String) dbObject.get(TITLE_FIELD);
		int numberOfPages = (Integer) dbObject.get(NUM_PAGES_FIELD);
		
		return new Book(title, numberOfPages);
		
	}

}
