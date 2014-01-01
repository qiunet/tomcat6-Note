/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina;


import java.util.EventObject;


/**
 * General event for notifying listeners of significant changes on a Session.
 * 当session有重要更改. 通知监听的事件类.
 * @author Craig R. McClanahan
 * @version $Revision: 467222 $ $Date: 2006-10-24 11:17:11 +0800 (Tue, 24 Oct 2006) $
 */

public final class SessionEvent
    extends EventObject {


    /**
     * The event data associated with this event.
     * 
     * 关联事件数据
     */
    private Object data = null;


    /**
     * The Session on which this event occurred.
     * 
     * 当前发生事件的session
     */
    private Session session = null;


    /**
     * The event type this instance represents.
     * 
     * 事件实例类型
     */
    private String type = null;


    /**
     * Construct a new SessionEvent with the specified parameters.
     * 构造一个新的session事件.
     *
     * @param session Session on which this event occurred
     * @param type Event type
     * @param data Event data
     */
    public SessionEvent(Session session, String type, Object data) {

        super(session);
        this.session = session;
        this.type = type;
        this.data = data;

    }


    /**
     * Return the event data of this event.
     */
    public Object getData() {

        return (this.data);

    }


    /**
     * Return the Session on which this event occurred.
     */
    public Session getSession() {

        return (this.session);

    }


    /**
     * Return the event type of this event.
     */
    public String getType() {

        return (this.type);

    }


    /**
     * Return a string representation of this event.
     */
    public String toString() {

        return ("SessionEvent['" + getSession() + "','" +
                getType() + "']");

    }


}
