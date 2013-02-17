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
package com.allanbank.mongodb.demo.coordination.watch.demo;

import java.util.regex.Pattern;

import com.allanbank.mongodb.MongoClient;
import com.allanbank.mongodb.MongoCollection;
import com.allanbank.mongodb.MongoDatabase;
import com.allanbank.mongodb.MongoFactory;
import com.allanbank.mongodb.bson.Document;
import com.allanbank.mongodb.demo.coordination.watch.Operation;
import com.allanbank.mongodb.demo.coordination.watch.WatchListener;
import com.allanbank.mongodb.demo.coordination.watch.Watcher;

/**
 * WatchIt provides an sample program to watch a collection.
 * 
 * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
 */
public class WatchIt {

    /**
     * Runs a single watcher on a collection.
     * 
     * @param args
     *            Command line arguments. Expect the MongoDB URL, database and
     *            collection name.
     * @throws InterruptedException
     *             If the thread is interrupted.
     */
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.out.println("Usage: java " + WatchIt.class.getName()
                    + " <mongodb-url> <database> <collection>");
            System.exit(1);
        }

        MongoClient client = MongoFactory.createClient(args[0]);
        MongoDatabase database = client.getDatabase(args[1]);
        MongoCollection collection = database.getCollection(args[2]);

        Watcher watcher = new Watcher(client, collection,
                Pattern.compile(".*"), new WatchListener() {
                    @Override
                    public void changed(Operation op, String context,
                            Document document) {
                        if (op == Operation.DELETE) {
                            System.out.println(context + ": " + op);
                        }
                        else {
                            System.out.println(context + ": " + op + ": "
                                    + document);
                        }
                    }
                });
        watcher.start();

        while (true) {
            Thread.sleep(10000000L);
        }
    }
}
