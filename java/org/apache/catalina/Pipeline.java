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
 * <p>Interface describing a collection of Valves that should be executed
 * in sequence when the <code>invoke()</code> method is invoked.  It is
 * required that a Valve somewhere in the pipeline (usually the last one)
 * must process the request and create the corresponding response, rather
 * than trying to pass the request on.</p>
 *
 * 接口描述了个一个有序的Valves(阀)集合将执行. 当被invoked反射调用的时候. 他需要一个在pipeline(通常最后一个)
 * 的Valve处理请求,并且创建对应的响应.  (最后一句不懂)
 * 
 * 
 * <p>There is generally a single Pipeline instance associated with each
 * Container.  The container's normal request processing functionality is
 * generally encapsulated in a container-specific Valve, which should always
 * be executed at the end of a pipeline.  To facilitate this, the
 * <code>setBasic()</code> method is provided to set the Valve instance that
 * will always be executed last.  Other Valves will be executed in the order
 * that they were added, before the basic Valve is executed.</p>
 * 
 * 通常.一个独立的Pipeline实例关联在每个Container. Container的正常请求处理功能通常封装成一个指定的Valve在管道最后执行.
 * setBasic() 方法提供设置一个最后被执行的valve. 其他valve将按照添加顺序在basic之前执行.
 * 
 * 
 * 
 * @author Craig R. McClanahan
 * @author Peter Donald
 * @version $Revision: 467222 $ $Date: 2006-10-24 11:17:11 +0800 (Tue, 24 Oct 2006) $
 */

public interface Pipeline {


    // ------------------------------------------------------------- Properties


    /**
     * <p>Return the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).
     */
    public Valve getBasic();


    /**
     * <p>Set the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).  Prioer to setting the basic Valve,
     * the Valve's <code>setContainer()</code> will be called, if it
     * implements <code>Contained</code>, with the owning Container as an
     * argument.  The method may throw an <code>IllegalArgumentException</code>
     * if this Valve chooses not to be associated with this Container, or
     * <code>IllegalStateException</code> if it is already associated with
     * a different Container.</p>
     *
     * @param valve Valve to be distinguished as the basic Valve
     */
    public void setBasic(Valve valve);


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Add a new Valve to the end of the pipeline associated with this
     * Container.  Prior to adding the Valve, the Valve's
     * <code>setContainer()</code> method will be called, if it implements
     * <code>Contained</code>, with the owning Container as an argument.
     * The method may throw an
     * <code>IllegalArgumentException</code> if this Valve chooses not to
     * be associated with this Container, or <code>IllegalStateException</code>
     * if it is already associated with a different Container.</p>
     *
     * 添加一个阀到pipeline, 会调用setContainer(). 
     * 如果已经被关联了不同Container. 将抛出异常:IllegalStateException(contextValve 关联的Container是Context)
     * 
     * @param valve Valve to be added
     *
     * @exception IllegalArgumentException if this Container refused to
     *  accept the specified Valve
     * @exception IllegalArgumentException if the specifie Valve refuses to be
     *  associated with this Container
     * @exception IllegalStateException if the specified Valve is already
     *  associated with a different Container
     */
    public void addValve(Valve valve);


    /**
     * Return the set of Valves in the pipeline associated with this
     * Container, including the basic Valve (if any).  If there are no
     * such Valves, a zero-length array is returned.
     * 
     * 返回所有阀,  没有返回空数组
     */
    public Valve[] getValves();


    /**
     * Remove the specified Valve from the pipeline associated with this
     * Container, if it is found; otherwise, do nothing.  If the Valve is
     * found and removed, the Valve's <code>setContainer(null)</code> method
     * will be called if it implements <code>Contained</code>.
     *
     * 删除指定的Valve. 如果删除成功.  将调用Valve的setContainer(null)
     *
     * @param valve Valve to be removed
     */
    public void removeValve(Valve valve);


    /**
     * <p>Return the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).
     * 
     * 得到第一个阀.没有返回basicvalve
     */
    public Valve getFirst();


}
