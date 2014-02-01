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

import org.apache.catalina.connector.Connector;

/**
 * A <strong>Service</strong> is a group of one or more
 * <strong>Connectors</strong> that share a single <strong>Container</strong>
 * to process their incoming requests.  This arrangement allows, for example,
 * a non-SSL and SSL connector to share the same population of web apps.
 * <p>
 * A given JVM can contain any number of Service instances; however, they are
 * completely independent of each other and share only the basic JVM facilities
 * and classes on the system class path.
 * 
 * 一个service可以管理一个或者 多个Connectors 和一个Container 处理他们接收到的请求.
 * 多个connector 可以分别处理 http https  ssl等的请求,  
 * 
 * 
 * 一个指定的jvm可以包含任意数量的service实例.  他们完整但不依赖其他实例.
 *   仅仅共享一个基本的jvm设备和classpath路径的classes.
 * 
 * @author Craig R. McClanahan
 * @version $Revision: 520968 $ $Date: 2007-03-22 02:00:39 +0800 (Thu, 22 Mar 2007) $
 */

public interface Service {

    // ------------------------------------------------------------- Properties


    /**
     * Return the <code>Container</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     * 
     * 返回处理请求的的Container.
     */
    public Container getContainer();

    /**
     * Set the <code>Container</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     *
     *设置关联的container.
     *
     * @param container The new Container
     */
    public void setContainer(Container container);

    /**
     * Return descriptive information about this Service implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     * 
     * 得到信息
     */
    public String getInfo();

    /**
     * Return the name of this Service.
     * 
     * 得到name属性
     */
    public String getName();

    /**
     * Set the name of this Service.
     * 设置该service的name.  
     *
     * @param name The new service name
     */
    public void setName(String name);

    /**
     * Return the <code>Server</code> with which we are associated (if any).
     * 
     * 返回关联的 上一级 server
     */
    public Server getServer();

    /**
     * Set the <code>Server</code> with which we are associated (if any).
     *
     *设置 上一级 server
     *
     * @param server The server that owns this Service
     */
    public void setServer(Server server);

    // --------------------------------------------------------- Public Methods


    /**
     * Add a new Connector to the set of defined Connectors, and associate it
     * with this Service's Container.
     * 
     * 添加一个Connector到Connectors集合 并且与service 的Container关联
     *
     * @param connector The Connector to be added
     */
    public void addConnector(Connector connector);

    /**
     * Find and return the set of Connectors associated with this Service.
     * 
     * 返回所有关联的connector
     */
    public Connector[] findConnectors();

    /**
     * Remove the specified Connector from the set associated from this
     * Service.  The removed Connector will also be disassociated from our
     * Container.
     *
     * 从集合中删除一个Connector 及去除关联. 
     * @param connector The Connector to be removed
     */
    public void removeConnector(Connector connector);

    /**
     * Invoke a pre-startup initialization. This is used to allow connectors
     * to bind to restricted ports under Unix operating environments.
     *
     * 启动前的初始化, 
     *
     * @exception LifecycleException If this server was already initialized.
     */
    public void initialize() throws LifecycleException;

    /**
     * Adds a named executor to the service
     * 
     * 添加executor
     * 
     * @param ex Executor
     */
    public void addExecutor(Executor ex);

    /**
     * Retrieves all executors
     * 返回所有executor
     * @return Executor[]
     */
    public Executor[] findExecutors();

    /**
     * Retrieves executor by name, null if not found
     * 
     * 通过名称找到executor.没有返回null
     * 
     * @param name String
     * @return Executor
     */
    public Executor getExecutor(String name);
    
    /**
     * Removes an executor from the service
     * 
     * 删除一个executor
     * 
     * @param ex Executor
     */
    public void removeExecutor(Executor ex);

}
