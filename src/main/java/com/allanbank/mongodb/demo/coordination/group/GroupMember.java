/*
 * Copyright 2013, Allanbank Consulting, Inc. 
 *           All Rights Reserved
 */

package com.allanbank.mongodb.demo.coordination.group;

import com.allanbank.mongodb.Durability;
import com.allanbank.mongodb.MongoCollection;
import com.allanbank.mongodb.bson.builder.BuilderFactory;

/**
 * GroupMember provides a representation of a group member.
 * 
 * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
 */
public class GroupMember {

    /** The context/{@code _id} for the group member. */
    private final String myId;

    /** The collection containing the group members. */
    private final MongoCollection myCollection;

    /** Set to true when the member is removed. */
    private boolean myRemoved;

    /**
     * Creates a new GroupMember.
     * 
     * @param id
     *            The context/{@code _id} for the group member.
     * @param collection
     *            The collection containing the group members.
     */
    public GroupMember(String id, MongoCollection collection) {
        myId = id;
        myCollection = collection;

        myRemoved = false;
    }

    /**
     * Returns the id of the member.
     * 
     * @return The id of the member.
     */
    public String getId() {
        return myId;
    }

    /**
     * Returns the id of the member.
     * 
     * @return The id of the member.
     */
    public synchronized boolean remove() {
        if (!myRemoved) {
            myCollection.delete(BuilderFactory.start().add("_id", myId),
                    Durability.ACK);
            myRemoved = true;

            return true;
        }

        return false;
    }

    /**
     * Returns true if the member has been manually removed.
     * 
     * @return True if the member has been manually removed.
     */
    public boolean isRemoved() {
        return myRemoved;
    }
}
