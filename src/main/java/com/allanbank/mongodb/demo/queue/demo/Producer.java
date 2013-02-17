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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.allanbank.mongodb.MongoClient;
import com.allanbank.mongodb.MongoCollection;
import com.allanbank.mongodb.MongoDatabase;
import com.allanbank.mongodb.MongoFactory;
import com.allanbank.mongodb.bson.builder.BuilderFactory;
import com.allanbank.mongodb.bson.builder.DocumentBuilder;
import com.allanbank.mongodb.bson.element.ObjectId;

/**
 * Produce documents to insert into the queue.
 * 
 * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
 */
public class Producer {

    /**
     * Runs a single producer on the queue.
     * 
     * @param args
     *            The for the producer. Expect the MongoDBURL, database and
     *            collection/queue name
     * @throws InterruptedException
     *             If the producer is interrupted.
     */
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.out.println("Usage: java " + Producer.class.getName()
                    + " <mongodb-url> <database> <queue/collection>");
            System.exit(1);
        }

        MongoClient client = MongoFactory.createClient(args[0]);

        String dbName = args[1];
        String collectionName = args[2];

        MongoDatabase db = client.getDatabase(dbName);
        MongoCollection collection = db.getCollection(collectionName);

        int count = 0;
        UUID producerIdentifier = UUID.randomUUID();
        DocumentBuilder builder = BuilderFactory.start();
        while (true) {
            builder.reset();
            builder.add("_id", new ObjectId());
            builder.add("producer", producerIdentifier);
            builder.add("count", count++);

            collection.insert(builder);

            TimeUnit.MILLISECONDS.sleep(100);

            if ((count % 1000) == 0) {
                System.out.println(count);
            }
        }
    }
}
