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

import com.allanbank.mongodb.bson.Document;

/**
 * WatchListener provides a callback that a document has changed.
 * 
 * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
 */
public interface WatchListener {

    /**
     * Notification that a document changed.
     * 
     * @param op
     *            The operation that was performed. If a delete then the
     *            document will be <code>null</code>.
     * @param context
     *            The context (_id) for the changed document.
     * @param document
     *            The document that was changed.
     */
    public void changed(Operation op, String context, Document document);
}
