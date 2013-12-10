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


package org.apache.catalina.core;


import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.AnnotationProcessor;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.security.SecurityUtil;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.StringManager;
import org.apache.tomcat.util.log.SystemLogHandler;


/**
 * Implementation of a <code>javax.servlet.FilterConfig</code> useful in
 * managing the filter instances instantiated when a web application
 * is first started.
 *
 * 实现了javax.servlet.FilterConfig. 通常该filter实例在应用首次启动时初始化所有过滤器;
 *
 * @author Craig R. McClanahan
 * @version $Revision: 505593 $ $Date: 2007-02-10 08:54:56 +0800 (Sat, 10 Feb 2007) $
 */

final class ApplicationFilterConfig implements FilterConfig, Serializable {


    protected static StringManager sm =
        StringManager.getManager(Constants.Package);
    
    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new ApplicationFilterConfig for the specified filter
     * definition.
     * 
     * 根据一个指定filter构造一个ApplicationFilterConfig
     *
     * @param context The context with which we are associated
     * @param filterDef Filter definition for which a FilterConfig is to be
     *  constructed
     *
     * @exception ClassCastException if the specified class does not implement
     *  the <code>javax.servlet.Filter</code> interface
     * @exception ClassNotFoundException if the filter class cannot be found
     * @exception IllegalAccessException if the filter class cannot be
     *  publicly instantiated
     * @exception InstantiationException if an exception occurs while
     *  instantiating the filter object
     * @exception ServletException if thrown by the filter's init() method
     * @throws NamingException 
     * @throws InvocationTargetException 
     */
    public ApplicationFilterConfig(Context context, FilterDef filterDef)
        throws ClassCastException, ClassNotFoundException,
               IllegalAccessException, InstantiationException,
               ServletException, InvocationTargetException, NamingException {

        super();
        
        
        // 读取RestrictedFilters.properties文件.生成properties
        
        if (restrictedFilters == null) {
            restrictedFilters = new Properties();
            try {
                InputStream is = 
                    this.getClass().getClassLoader().getResourceAsStream
                        ("org/apache/catalina/core/RestrictedFilters.properties");
                if (is != null) {
                    restrictedFilters.load(is);
                } else {
                    context.getLogger().error(sm.getString("applicationFilterConfig.restrictedFiltersResources"));
                }
            } catch (IOException e) {
                context.getLogger().error(sm.getString("applicationFilterConfig.restrictedServletsResources"), e);
            }
        }
        
        this.context = context;
        setFilterDef(filterDef);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The Context with which we are associated.
     * 
     * 关联的Context)
     */
    private Context context = null;


    /**
     * The application Filter we are configured for.
     * 
     * 我们配置的应用过滤器
     */
    private transient Filter filter = null;


    /**
     * The <code>FilterDef</code> that defines our associated Filter.
     * 
     * 关联我们Filter的FilterDef
     */
    private FilterDef filterDef = null;


    /**
     * Restricted filters (which can only be loaded by a privileged webapp).
     * 
     *  受限制的过滤器.  仅能被私有webapp加载 (不明白)
     */
    protected static Properties restrictedFilters = null;

    
    // --------------------------------------------------- FilterConfig Methods


    /**
     * Return the name of the filter we are configuring. 
     * 
     * 返回过滤器名称
     */
    public String getFilterName() {

        return (filterDef.getFilterName());

    }


    /**
     * Return a <code>String</code> containing the value of the named
     * initialization parameter, or <code>null</code> if the parameter
     * does not exist.
     * 
     * 返回对应name的字符串. 如果不存在. 返回null
     * 
     * @param name Name of the requested initialization parameter
     */
    public String getInitParameter(String name) {

        Map map = filterDef.getParameterMap();
        if (map == null)
            return (null);
        else
            return ((String) map.get(name));

    }


    /**
     * Return an <code>Enumeration</code> of the names of the initialization
     * 
     * 返回ParameterMap的keys
     * parameters for this Filter.
     */
    public Enumeration getInitParameterNames() {

        Map map = filterDef.getParameterMap();
        if (map == null)
            return (new Enumerator(new ArrayList()));
        else
            return (new Enumerator(map.keySet()));

    }


    /**
     * Return the ServletContext of our associated web application.
     * 
     * 返回关联的ServletContext
     */
    public ServletContext getServletContext() {

        return (this.context.getServletContext());

    }


    /**
     * Return a String representation of this object.
     * 
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ApplicationFilterConfig[");
        sb.append("name=");
        sb.append(filterDef.getFilterName());
        sb.append(", filterClass=");
        sb.append(filterDef.getFilterClass());
        sb.append("]");
        return (sb.toString());

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Return the application Filter we are configured for.
     *
     * @exception ClassCastException if the specified class does not implement
     *  the <code>javax.servlet.Filter</code> interface
     * @exception ClassNotFoundException if the filter class cannot be found
     * @exception IllegalAccessException if the filter class cannot be
     *  publicly instantiated
     * @exception InstantiationException if an exception occurs while
     *  instantiating the filter object
     * @exception ServletException if thrown by the filter's init() method
     * @throws NamingException 
     * @throws InvocationTargetException 
     */
    Filter getFilter() throws ClassCastException, ClassNotFoundException,
        IllegalAccessException, InstantiationException, ServletException, 
        InvocationTargetException, NamingException {

        // Return the existing filter instance, if any
        if (this.filter != null)
            return (this.filter);

        // Identify the class loader we will be using
        String filterClass = filterDef.getFilterClass();
        ClassLoader classLoader = null;
        if (filterClass.startsWith("org.apache.catalina."))
            classLoader = this.getClass().getClassLoader();
        else
            classLoader = context.getLoader().getClassLoader();

        ClassLoader oldCtxClassLoader =
            Thread.currentThread().getContextClassLoader();

        // Instantiate a new instance of this filter and return it
        Class clazz = classLoader.loadClass(filterClass);
        if (!isFilterAllowed(clazz)) {
            throw new SecurityException
                (sm.getString("applicationFilterConfig.privilegedFilter",
                        filterClass));
        }
        this.filter = (Filter) clazz.newInstance();
        if (!context.getIgnoreAnnotations()) {
            if (context instanceof StandardContext) {
               AnnotationProcessor processor = ((StandardContext)context).getAnnotationProcessor();
               processor.processAnnotations(this.filter);
               processor.postConstruct(this.filter);
            }
        }
        if (context instanceof StandardContext &&
            ((StandardContext) context).getSwallowOutput()) {
            try {
                SystemLogHandler.startCapture();
                filter.init(this);
            } finally {
                String log = SystemLogHandler.stopCapture();
                if (log != null && log.length() > 0) {
                    getServletContext().log(log);
                }
            }
        } else {
            filter.init(this);
        }
        return (this.filter);


    }


    /**
     * Return the filter definition we are configured for.
     * 
     * 返回定义的filter
     */
    FilterDef getFilterDef() {

        return (this.filterDef);

    }


    /**
     * Return <code>true</code> if loading this filter is allowed.
     * 返回filterClass是否允许被加载
     */
    protected boolean isFilterAllowed(Class filterClass) {

        // Privileged webapps may load all servlets without restriction
        if (context.getPrivileged()) {
            return true;
        }

        Class clazz = filterClass;
        while (clazz != null && !clazz.getName().equals("javax.servlet.Filter")) {
            if ("restricted".equals(restrictedFilters.getProperty(clazz.getName()))) {
                return (false);
            }
            clazz = clazz.getSuperclass();
        }
        
        return (true);

    }


    /**
     * Release the Filter instance associated with this FilterConfig,
     * if there is one.
     * 
     * 释放关联该实例的Filter实例
     */
    void release() {

        if (this.filter != null)
        {
            if (Globals.IS_SECURITY_ENABLED) {
                try {
                    SecurityUtil.doAsPrivilege("destroy", filter); 
                } catch(java.lang.Exception ex){                    
                    context.getLogger().error("ApplicationFilterConfig.doAsPrivilege", ex);
                }
                SecurityUtil.remove(filter);
            } else { 
                filter.destroy();
            }
            if (!context.getIgnoreAnnotations()) {
                try {
                    ((StandardContext)context).getAnnotationProcessor().preDestroy(this.filter);
                } catch (Exception e) {
                    context.getLogger().error("ApplicationFilterConfig.preDestroy", e);
                }
            }
        }
        this.filter = null;

     }


    /**
     * Set the filter definition we are configured for.  This has the side
     * effect of instantiating an instance of the corresponding filter class.
     *
     * @param filterDef The new filter definition
     *
     * @exception ClassCastException if the specified class does not implement
     *  the <code>javax.servlet.Filter</code> interface
     * @exception ClassNotFoundException if the filter class cannot be found
     * @exception IllegalAccessException if the filter class cannot be
     *  publicly instantiated
     * @exception InstantiationException if an exception occurs while
     *  instantiating the filter object
     * @exception ServletException if thrown by the filter's init() method
     * @throws NamingException 
     * @throws InvocationTargetException 
     */
    void setFilterDef(FilterDef filterDef)
        throws ClassCastException, ClassNotFoundException,
               IllegalAccessException, InstantiationException,
               ServletException, InvocationTargetException, NamingException {

        this.filterDef = filterDef;
        if (filterDef == null) {

            // Release any previously allocated filter instance
            if (this.filter != null){
                if( Globals.IS_SECURITY_ENABLED) {
                    try{
                        SecurityUtil.doAsPrivilege("destroy", filter);  
                    } catch(java.lang.Exception ex){    
                        context.getLogger().error("ApplicationFilterConfig.doAsPrivilege", ex);
                    }
                    SecurityUtil.remove(filter);
                } else { 
                    filter.destroy();
                }
                if (!context.getIgnoreAnnotations()) {
                    try {
                        ((StandardContext)context).getAnnotationProcessor().preDestroy(this.filter);
                    } catch (Exception e) {
                        context.getLogger().error("ApplicationFilterConfig.preDestroy", e);
                    }
                }
            }
            this.filter = null;

        } else {

            // Allocate a new filter instance
            Filter filter = getFilter();

        }

    }


    // -------------------------------------------------------- Private Methods


}
