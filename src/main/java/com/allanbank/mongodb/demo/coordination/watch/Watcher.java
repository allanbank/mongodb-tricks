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
package com.allanbank.mongodb.demo.coordination.watch;

import static com.allanbank.mongodb.builder.QueryBuilder.and;
import static com.allanbank.mongodb.builder.QueryBuilder.or;
import static com.allanbank.mongodb.builder.QueryBuilder.where;
import static com.allanbank.mongodb.builder.expression.Expressions.constant;

import java.util.regex.Pattern;

import com.allanbank.mongodb.Callback;
import com.allanbank.mongodb.MongoClient;
import com.allanbank.mongodb.MongoCollection;
import com.allanbank.mongodb.MongoCursorControl;
import com.allanbank.mongodb.StreamCallback;
import com.allanbank.mongodb.bson.Document;
import com.allanbank.mongodb.bson.DocumentAssignable;
import com.allanbank.mongodb.bson.Element;
import com.allanbank.mongodb.bson.builder.BuilderFactory;
import com.allanbank.mongodb.bson.builder.DocumentBuilder;
import com.allanbank.mongodb.builder.ComparisonOperator;
import com.allanbank.mongodb.builder.Find;

/**
 * Watcher provides a convenient mechanism for setting up the structures for
 * watching for changes to documents in a myCollection. This class assumes that
 * the {@code _id} for the documents are strings and allows the user to select
 * subsets of the documents based on a regular expression.
 * 
 * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
 */
public class Watcher {

    /** The controls for the active stream of updates. */
    protected MongoCursorControl myControls;

    /** The listener to notify that something has changed. */
    protected final WatchListener myListener;

    /** The collection being watched. */
    private final MongoCollection myCollection;

    /** The context for the {@code _id} of items to watch. */
    private final Pattern myContext;

    /** The last timestamp seen. For restarts. */
    private Element myLastTs;

    /** The client for the watcher. */
    private final MongoClient myMongoClient;

    /**
     * Creates a new Watcher.
     * 
     * @param mongoClient
     *            The client for the watcher.
     * @param collection
     *            The collection being watched.
     * @param context
     *            The context for the {@code _id} of items to watch.
     * @param listener
     *            The listener to notify that something has changed.
     */
    public Watcher(final MongoClient mongoClient,
            final MongoCollection collection, final Pattern context,
            final WatchListener listener) {
        myMongoClient = mongoClient;
        myCollection = collection;
        myContext = context;
        myListener = listener;
        myControls = null;
        myLastTs = null;
    }

    /**
     * Restarts the watcher.
     */
    public synchronized void restart() {
        stop();
        start();
    }

    /**
     * Starts the watcher.
     */
    public synchronized void start() {
        if (myControls == null) {
            final String ns = myCollection.getDatabaseName() + "."
                    + myCollection.getName();

            final DocumentAssignable wantQuery = or(
                    where("ns").equals(ns).and("op")
                            .in(constant("i"), constant("d")).and("o._id")
                            .matches(myContext),
                    where("ns").equals(ns).and("op").equals("u").and("o2._id")
                            .matches(myContext));

            final Find.Builder builder = new Find.Builder();
            if (myLastTs != null) {
                final DocumentBuilder tsQuery = BuilderFactory.start();
                tsQuery.push(myLastTs.getName()).add(
                        myLastTs.withName(ComparisonOperator.GT.getToken()));

                builder.setQuery(and(tsQuery, wantQuery));
            }
            else {
                builder.setQuery(wantQuery);
            }

            builder.tailable();

            myControls = myMongoClient.getDatabase("local")
                    .getCollection("oplog.rs")
                    .streamingFind(new OpLogNotification(), builder.build());
        }
    }

    /**
     * Stops the watcher.
     */
    public synchronized void stop() {
        if (myControls != null) {
            myControls.close();
        }
    }

    /**
     * Handle {@code oplog.rs} updates.
     * 
     * @param opLogDoc
     *            The document from the {@code oplog.rs} myCollection.
     */
    protected synchronized void notifyChange(final Document opLogDoc) {
        Element id = opLogDoc.findFirst("o2", "_id");
        if (id == null) {
            id = opLogDoc.findFirst("o", "_id");
        }

        Operation op = Operation.fromToken(opLogDoc.get("op")
                .getValueAsString());

        if (op == Operation.DELETE) {
            myListener.changed(op, id.getValueAsString(), null);
        }
        else {
            myCollection.findOneAsync(
                    new WatchedDocumentCallback(op, id.getValueAsString()),
                    BuilderFactory.start().add(id));
        }

        // Remember we saw this event.
        // TODO - move this into the WatchDocumentCallback.
        myLastTs = opLogDoc.findFirst("ts");
    }

    /**
     * OpLogNotification provides handling for the changed logged in the
     * {@code oplog.rs} myCollection.
     * 
     * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
     */
    protected final class OpLogNotification implements StreamCallback<Document> {
        @Override
        public void callback(final Document result) {
            notifyChange(result);
        }

        @Override
        public void done() {
            restart();
        }

        @Override
        public void exception(final Throwable thrown) {
            restart();
        }
    }

    /**
     * WatchedDocumentCallback provides the callback used to report the document
     * that has changed.
     * 
     * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
     */
    protected final class WatchedDocumentCallback implements Callback<Document> {

        /** The context of the update. */
        private final String myChangedContext;

        /** The operation of the update. */
        private final Operation myOperation;

        /**
         * Creates a new WatchedDocumentCallback.
         * 
         * @param op
         *            The operation seen.
         * @param changedContext
         *            The myContext of the update.
         */
        public WatchedDocumentCallback(final Operation op,
                final String changedContext) {
            myOperation = op;
            myChangedContext = changedContext;
        }

        @Override
        public void callback(final Document result) {
            myListener.changed(myOperation, myChangedContext, result);
        }

        @Override
        public void exception(final Throwable thrown) {
            restart();
        }
    }
}
