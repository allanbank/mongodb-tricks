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

/**
 * GroupListener provides notification of changes to the members of a group.
 * 
 * @copyright 2013, Allanbank Consulting, Inc., All Rights Reserved
 */
public interface GroupListener {
    /**
     * Notification that a member has been added. The {@code context} includes
     * the root context string.
     * 
     * @param context
     *            The context of the member added.
     */
    public void memberAdded(String context);

    /**
     * Notification that a member has been removed. The {@code context} includes
     * the root context string.
     * 
     * @param context
     *            The context of the member removed.
     */
    public void memberRemoved(String context);
}
