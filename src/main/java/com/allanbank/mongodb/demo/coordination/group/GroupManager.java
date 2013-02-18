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
package com.allanbank.mongodb.demo.coordination.group;

import static com.allanbank.mongodb.Durability.ACK;
import static com.allanbank.mongodb.builder.MiscellaneousOperator.IN;
import static com.allanbank.mongodb.builder.QueryBuilder.where;
import static com.allanbank.mongodb.demo.coordination.watch.Operation.DELETE;
import static com.allanbank.mongodb.demo.coordination.watch.Operation.UPDATE;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.allanbank.mongodb.Durability;
import com.allanbank.mongodb.MongoClient;
import com.allanbank.mongodb.MongoCollection;
import com.allanbank.mongodb.bson.Document;
import com.allanbank.mongodb.bson.builder.ArrayBuilder;
import com.allanbank.mongodb.bson.builder.BuilderFactory;
import com.allanbank.mongodb.bson.builder.DocumentBuilder;
import com.allanbank.mongodb.bson.element.ObjectId;
import com.allanbank.mongodb.demo.coordination.watch.Operation;
import com.allanbank.mongodb.demo.coordination.watch.WatchListener;
import com.allanbank.mongodb.demo.coordination.watch.Watcher;

/**
 * GroupManager provides the ability to monitor and join a group.
 * 
 * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
 */
public class GroupManager {

    /** The listener to notify that something has changed. */
    protected final WatchListener myListener;

    /** The collection being watched. */
    private final MongoCollection myCollection;

    /** used for updating the registration in MongoDB. */
    private final ScheduledExecutorService myExecutor;

    /** The list of listeners. */
    private final List<GroupListener> myListeners;

    /** The list of listeners. */
    private final ConcurrentMap<String, WeakReference<GroupMember>> myMembers;

    /** The client for the watcher. */
    private final MongoClient myMongoClient;

    /** The root context for the {@code _id} of items in the group. */
    private final String myRootContext;

    /** The scheduled task for updating the timestamp for each group member. */
    private ScheduledFuture<?> myScheduledTask;

    /** The watcher for updates to the group. */
    private final Watcher myWatcher;

    /**
     * Creates a new GroupManager.
     * 
     * @param executor
     *            Used for updating a registration in MongoDB.
     * @param mongoClient
     *            The client for the watcher.
     * @param collection
     *            The collection being watched.
     * @param rootContext
     *            The context for the {@code _id} of items to watch.
     */
    public GroupManager(final ScheduledExecutorService executor,
            final MongoClient mongoClient, final MongoCollection collection,
            final String rootContext) {
        myExecutor = executor;
        myMongoClient = mongoClient;
        myCollection = collection;
        myRootContext = rootContext;

        myMembers = new ConcurrentHashMap<String, WeakReference<GroupMember>>();
        myListeners = new CopyOnWriteArrayList<GroupListener>();
        myListener = new GroupWatchListener();

        myWatcher = new Watcher(myMongoClient, myCollection,
                Pattern.compile(myRootContext + ".*"), myListener);
    }

    /**
     * Adds a listener to the group.
     * 
     * @param listener
     *            The listener to add.
     */
    public void addListener(final GroupListener listener) {
        myListeners.add(listener);
    }

    /**
     * Adds a member to the group. Users must hold onto the GroupMember
     * returned. If it is garbage collected the member will be removed from the
     * group.
     * 
     * @return The representation of the group member.
     */
    public GroupMember addMember() {
        // Use ObjectId to get a cluster unique id.
        final ObjectId id = new ObjectId();
        final String context = myRootContext + id.toHexString();

        final GroupMember member = new GroupMember(context, myCollection);

        myCollection.insert(Durability.ACK,
                BuilderFactory.start().add("_id", context)
                        .add("ts", new Date()));
        myMembers.put(context, new WeakReference<GroupMember>(member));

        return member;
    }

    /**
     * Removes a listener from the group.
     * 
     * @param listener
     *            The listener to be removed.
     */
    public void removeListener(final GroupListener listener) {
        myListeners.remove(listener);
    }

    /**
     * Restarts the GroupManager.
     */
    public synchronized void restart() {
        stop();
        start();
    }

    /**
     * Starts the GroupManager.
     */
    public synchronized void start() {
        if (myScheduledTask == null) {
            myScheduledTask = myExecutor.scheduleAtFixedRate(
                    new PeriodicUpdateRunnable(), 1, 5, TimeUnit.SECONDS);
        }
        myWatcher.start();
    }

    /**
     * Stops the {@link GroupManager}.
     */
    public synchronized void stop() {
        if (myScheduledTask != null) {
            myScheduledTask.cancel(false);
            myScheduledTask = null;
        }
        myWatcher.stop();
    }

    /**
     * Notifies all of the listeners of the added or removed member.
     * 
     * @param op
     *            The operation.
     * @param context
     *            The context of the member.
     */
    protected void notifyListeners(final Operation op, final String context) {
        for (final GroupListener listener : myListeners) {
            if (op == DELETE) {
                listener.memberRemoved(context);
            }
            else {
                listener.memberAdded(context);
            }
        }
    }

    /**
     * Updates the active members as still being here and deletes inactive
     * members.
     */
    protected void updateMembers() {
        boolean haveUpdate = false;
        boolean haveRemove = false;

        final DocumentBuilder removeQuery = BuilderFactory.start();
        final ArrayBuilder removeIds = removeQuery.push("_id").pushArray(
                IN.getToken());

        final DocumentBuilder updateQuery = BuilderFactory.start();
        final ArrayBuilder updateIds = updateQuery.push("_id").pushArray(
                IN.getToken());

        for (final Map.Entry<String, WeakReference<GroupMember>> entry : myMembers
                .entrySet()) {
            final GroupMember member = entry.getValue().get();
            if ((member == null) || member.isRemoved()) {
                removeIds.add(entry.getKey());
                haveRemove = true;
                myMembers.remove(entry.getKey());
            }
            else {
                updateIds.add(member.getId());
                haveUpdate = true;
            }
        }

        if (haveRemove) {
            myCollection.delete(removeQuery, false, ACK);
        }

        if (haveUpdate) {
            DocumentBuilder update = BuilderFactory.start();
            update.push("$set").add("ts", new Date());
            myCollection.update(updateQuery, update, true, false, ACK);
        }

        // Now delete the stale members.
        myCollection.delete(where("ts").lessThanTimestamp(
                System.currentTimeMillis() - SECONDS.toMillis(30)));
    }

    /**
     * GroupWatchListener provides the adapter from a {@link WatchListener} to
     * the GroupListeners.
     * 
     * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
     */
    protected final class GroupWatchListener implements WatchListener {
        @Override
        public void changed(final Operation op, final String context,
                final Document document) {
            if (op != UPDATE) {
                notifyListeners(op, context);
            }
        }
    }

    /**
     * PeriodicUpdateRunnable provides a {@link Runnable} to periodically update
     * that members still exist.
     * 
     * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
     */
    protected final class PeriodicUpdateRunnable implements Runnable {
        @Override
        public void run() {
            updateMembers();
        }
    }

}
