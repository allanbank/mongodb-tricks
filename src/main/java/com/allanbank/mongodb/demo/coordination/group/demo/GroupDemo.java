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
package com.allanbank.mongodb.demo.coordination.group.demo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.allanbank.mongodb.MongoClient;
import com.allanbank.mongodb.MongoCollection;
import com.allanbank.mongodb.MongoDatabase;
import com.allanbank.mongodb.MongoFactory;
import com.allanbank.mongodb.demo.coordination.group.GroupListener;
import com.allanbank.mongodb.demo.coordination.group.GroupManager;
import com.allanbank.mongodb.demo.coordination.group.GroupMember;

/**
 * Demo for the members of a group.
 * 
 * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
 */
public class GroupDemo {
    /**
     * Runs a single watcher on a collection.
     * 
     * @param args
     *            Command line arguments. Expect the MongoDB URL, database and
     *            collection name and optional root context.
     * @throws InterruptedException
     *             If the thread is interrupted.
     */
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.out.println("Usage: java " + GroupDemo.class.getName()
                    + " <mongodb-url> <database> <collection> <root-path>");
        }

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        MongoClient client = MongoFactory.createClient(args[0]);
        MongoDatabase database = client.getDatabase(args[1]);
        MongoCollection collection = database.getCollection(args[2]);

        String rootContext = "/path/to/important/group/";
        if (args.length > 3) {
            rootContext = args[3];
            System.exit(1);
        }

        GroupManager manager = new GroupManager(executor, client, collection,
                rootContext);
        manager.addListener(new GroupListener() {
            @Override
            public void memberRemoved(String context) {
                System.out.println(context + " - Removed");
            }

            @Override
            public void memberAdded(String context) {
                System.out.println(context + " - Added");
            }
        });

        final GroupMember member = manager.addMember();

        // Faster cleanup, if we can.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                member.remove();
            }
        });

        while (true) {
            Thread.sleep(10000000L);
        }
    }
}
