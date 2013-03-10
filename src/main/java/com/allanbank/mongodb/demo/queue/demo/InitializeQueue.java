/*
 *         Copyright 2013 Allanbank Consulting, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.allanbank.mongodb.demo.queue.demo;

import java.io.IOException;

import com.allanbank.mongodb.MongoClient;
import com.allanbank.mongodb.MongoCollection;
import com.allanbank.mongodb.MongoDatabase;
import com.allanbank.mongodb.MongoFactory;
import com.allanbank.mongodb.MongoIterator;
import com.allanbank.mongodb.bson.Document;
import com.allanbank.mongodb.bson.builder.BuilderFactory;
import com.allanbank.mongodb.builder.Find;

/**
 * Initializes the queue.
 * 
 * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
 */
public class InitializeQueue {

    /**
     * Initializes the queue by creating a new capped collection and creating a
     * new cursor on it. Prints the cursor document to standard out.
     * 
     * @param args
     *            Command line arguments. Expect the MongoDB URL, database and
     *            collection/queue name.
     * @throws IOException
     *             On a failure to close the connection to MongoDB.
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java " + InitializeQueue.class.getName()
                    + " <mongodb-url> <database> <queue/collection>");
            System.exit(1);
        }

        MongoClient client = MongoFactory.createClient(args[0]);

        String dbName = args[1];
        String collectionName = args[2];

        MongoDatabase db = client.getDatabase(dbName);
        MongoCollection collection = db.getCollection(collectionName);

        collection.drop();
        db.createCappedCollection(collectionName, 100000000L);

        // For a tailable cursor to initialize we need at least a single
        // document in the collection.
        collection.insert(BuilderFactory.start().add("_id", "seed"));

        Find.Builder builder = new Find.Builder(BuilderFactory.start());
        builder.setTailable(true);
        // builder.setAwaitData(false); // Uncomment if not using patch for SERVER-8602
        MongoIterator<Document> cursor = collection.find(builder.build());

        // Graceful shutdown of the iterator locally but not on the server.
        cursor.stop();
        while (cursor.hasNext()) {
            System.out.println(cursor.next());
        }

        collection = db.getCollection("lookup");
        collection.delete(BuilderFactory.start().add("_id", collectionName));
        collection.insert(BuilderFactory.start().add("_id", collectionName)
                .add("cursor", cursor.asDocument()));

        // Printout the cursor document.
        System.out.println("Queue created: " + collectionName);
        System.out.println(collection
                .findOne(BuilderFactory.start().add("_id", collectionName))
                .get("cursor").getValueAsObject());

        client.close();
    }
}
