package com.lordofthejars.nosqlunit.demo.mongodb;

import com.lordofthejars.nosqlunit.demo.model.Book;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoDbBookConverter {

    public static final String TITLE_FIELD = "title";
    public static final String NUM_PAGES_FIELD = "numberOfPages";

    public DBObject convert(Book book) {

        DBObject dbObject = new BasicDBObject();

        dbObject.put(TITLE_FIELD, book.getTitle());
        dbObject.put(NUM_PAGES_FIELD, book.getNumberOfPages());

        return dbObject;
    }


}
