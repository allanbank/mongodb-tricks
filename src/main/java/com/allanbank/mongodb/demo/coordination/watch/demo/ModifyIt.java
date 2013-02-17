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

import java.io.IOException;

import com.allanbank.mongodb.Durability;
import com.allanbank.mongodb.MongoClient;
import com.allanbank.mongodb.MongoCollection;
import com.allanbank.mongodb.MongoDatabase;
import com.allanbank.mongodb.MongoFactory;
import com.allanbank.mongodb.bson.Document;
import com.allanbank.mongodb.bson.builder.BuilderFactory;
import com.allanbank.mongodb.bson.builder.DocumentBuilder;
import com.allanbank.mongodb.bson.element.ObjectId;

/**
 * ModifyIt provides an sample program to create, modify and delete a single
 * document in a collection.
 * 
 * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
 */
public class ModifyIt {

    /**
     * Runs a create/modify/delete on a collection.
     * 
     * @param args
     *            Command line arguments. Expect the MongoDB URL, database and
     *            collection name as the first, second and third argument.
     * @throws InterruptedException
     *             If the thread is interrupted.
     * @throws IOException
     *             On a failure to close the MongoDB client.
     */
    public static void main(String[] args) throws InterruptedException,
            IOException {
        if (args.length < 3) {
            System.out.println("Usage: java " + ModifyIt.class.getName()
                    + " <mongodb-url> <database> <collection>");
            System.exit(1);
        }
        MongoClient client = MongoFactory.createClient(args[0]);
        MongoDatabase database = client.getDatabase(args[1]);
        MongoCollection collection = database.getCollection(args[2]);

        String id = new ObjectId().toHexString();

        Document query = BuilderFactory.start().add("_id", id).build();
        collection.insert(Durability.ACK, query);
        for (int i = 0; i < 10; ++i) {
            DocumentBuilder update = BuilderFactory.start();
            update.push("$inc").add("i", 1);
            collection.update(query, update, false, false, Durability.ACK);

            Thread.sleep(100);
        }

        collection.delete(query, Durability.ACK);

        client.close();
    }
}
