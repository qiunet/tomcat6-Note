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


/**
 * Common interface for component life cycle methods.  Catalina components
 * may, but are not required to, implement this interface (as well as the
 * appropriate interface(s) for the functionality they support) in order to
 * provide a consistent mechanism to start and stop the component.
 *
 * 生命周期方法的通用接口. 
 * 
 * 实现这个接口, 可以提供一致性的机制 来 启动  停止组件
 * 
 * @author Craig R. McClanahan
 * @version $Revision: 467222 $ $Date: 2006-10-24 11:17:11 +0800 (Tue, 24 Oct 2006) $
 */

public interface Lifecycle {


    // ----------------------------------------------------- Manifest Constants


    /**
     * The LifecycleEvent type for the "component init" event.
     * 初始化事件
     *
     */
    public static final String INIT_EVENT = "init";


    /**
     * The LifecycleEvent type for the "component start" event.
     * 启动事件
     */
    public static final String START_EVENT = "start";


    /**
     * The LifecycleEvent type for the "component before start" event.
     * 启动之前事件
     */
    public static final String BEFORE_START_EVENT = "before_start";


    /**
     * The LifecycleEvent type for the "component after start" event.
     * 启动之后事件
     */
    public static final String AFTER_START_EVENT = "after_start";


    /**
     * The LifecycleEvent type for the "component stop" event.
     * 停止事件
     */
    public static final String STOP_EVENT = "stop";


    /**
     * The LifecycleEvent type for the "component before stop" event.
     * 停止前
     */
    public static final String BEFORE_STOP_EVENT = "before_stop";


    /**
     * The LifecycleEvent type for the "component after stop" event.
     * 停止后
     */
    public static final String AFTER_STOP_EVENT = "after_stop";


    /**
     * The LifecycleEvent type for the "component destroy" event.
     * 销毁事件
     */
    public static final String DESTROY_EVENT = "destroy";


    /**
     * The LifecycleEvent type for the "periodic" event.
     * ?? 
     */
    public static final String PERIODIC_EVENT = "periodic";


    // --------------------------------------------------------- Public Methods


    /**
     * Add a LifecycleEvent listener to this component.
     * 
     * 添加一个事件监听
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener);


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     * 
     * 得到lifecycle 关联的监听,  如果没有监听注册.  返回一个 0长度的数组
     */
    public LifecycleListener[] findLifecycleListeners();


    /**
     * Remove a LifecycleEvent listener from this component.
     *
     * 去除一个监听
     * 
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener);


    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called before any of the public
     * methods of this component are utilized.  It should also send a
     * LifecycleEvent of type START_EVENT to any registered listeners.
     *
     * 该组件 启动中  调用这个方法.  将发送一个  START_EVENT 事件到注册的监听者
     * .
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException;


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.  It should also send a LifecycleEvent
     * of type STOP_EVENT to any registered listeners.
     * 
     * 优雅结束该组件的时候调用该方法.  将发送一个STOP_EVENT 时间到监听
     * 
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException;


}
