/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package javax.servlet;

import java.util.EventListener;

	/** 
	 * Implementations of this interface receive notifications about
	 * changes to the servlet context of the web application they are
	 * part of.
	 * To receive notification events, the implementation class
	 * must be configured in the deployment descriptor for the web
	 * application.
	 * 
	 * 是对 servlet context 的监听. 能在初始化  和  结束的时候. 进行事件响应.
	 * 该文件必须 在 web.xml(部署文件)  配置.
	 * 
	 * @see ServletContextEvent
	 * @since	v 2.3
	 */

public interface ServletContextListener extends EventListener {
	/**
	 ** Notification that the web application initialization
	 ** process is starting.
	 ** All ServletContextListeners are notified of context
	 ** initialization before any filter or servlet in the web
	 ** application is initialized.
	 *
	 *  通知应用在 初始化处理中.
	 */

    public void contextInitialized ( ServletContextEvent sce );

	/**
	 ** Notification that the servlet context is about to be shut down.
	 ** All servlets and filters have been destroy()ed before any
	 ** ServletContextListeners are notified of context
	 ** destruction.
	 * 通知servet context 将要shutdown.
	 *
	 */
    public void contextDestroyed ( ServletContextEvent sce );
}

