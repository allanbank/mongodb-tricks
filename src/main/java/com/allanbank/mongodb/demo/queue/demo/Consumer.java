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

import java.util.concurrent.TimeUnit;

import com.allanbank.mongodb.MongoClient;
import com.allanbank.mongodb.MongoCollection;
import com.allanbank.mongodb.MongoFactory;
import com.allanbank.mongodb.MongoIterator;
import com.allanbank.mongodb.bson.Document;
import com.allanbank.mongodb.bson.NumericElement;
import com.allanbank.mongodb.bson.builder.BuilderFactory;
import com.allanbank.mongodb.bson.element.DocumentElement;

/**
 * A simple consumer of the queue.
 * 
 * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
 */
public class Consumer {

    /**
     * Runs a single consumer on the queue.
     * 
     * @param args
     *            Command line arguments. Expect the MongoDB URL, database and
     *            collection/queue name.
     * @throws InterruptedException
     *             If interrupted.
     */
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.out.println("Usage: java " + Consumer.class.getName()
                    + " <mongodb-url> <database> <queue/collection>");
            System.exit(1);
        }

        MongoClient client = MongoFactory.createClient(args[0]);
        MongoCollection index = client.getDatabase(args[1]).getCollection(
                "lookup");
        Document queueLookupDoc = index.findOne(BuilderFactory.start().add(
                "_id", args[2]));
        DocumentElement cursorElement = queueLookupDoc.get(
                DocumentElement.class, "cursor");

        MongoIterator<Document> iter = client.restart(cursorElement
                .getDocument());

        long lastCount = 0;
        while (iter.hasNext()) {
            Document doc = iter.next();
            NumericElement count = doc.get(NumericElement.class, "count");
            if (count == null) {
                System.out.println(iter.next());
            }
            else {
                if ((lastCount + 1) != count.getLongValue()) {
                    System.out.println(lastCount);
                    System.out.print(count.getLongValue());
                    System.out.print("...");
                }
                lastCount = count.getLongValue();
            }

            TimeUnit.MILLISECONDS.sleep(200);

        }
    }
}
