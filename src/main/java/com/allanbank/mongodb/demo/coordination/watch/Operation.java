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

/**
 * Operation provides an enumeration of the possible changes to the documents.
 * 
 * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
 */
public enum Operation {
    /**
     * Represents an insert into the collection. This could be a upsert or
     * direct insert.
     */
    INSERT("i"),

    /** Represents an update to the document in the collection. */
    UPDATE("u"),

    /** Represents a delete of a document in the collection. */
    DELETE("d");

    /** The operation's token. */
    private final String myToken;

    /**
     * Creates a new WatchListener.Operation.
     * 
     * @param token
     *            The token for the operation.
     */
    private Operation(String token) {
        myToken = token;
    }

    /**
     * Returns the {@link Operation} for the provided token or throws a
     * {@link IllegalArgumentException} if the token does not match a
     * {@link Operation} token.
     * 
     * @param token
     *            The token to find the operation for.
     * @return The {@link Operation} for the token.
     * @throws IllegalArgumentException
     *             If the token is not a value {@link Operation} token.
     */
    public static Operation fromToken(String token)
            throws IllegalArgumentException {
        for (Operation op : values()) {
            if (op.getToken().equals(token)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown operation token '" + token
                + "'.");
    }

    /**
     * Returns the operation's token.
     * 
     * @return The operation's token.
     */
    public String getToken() {
        return myToken;
    }
}