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


package org.apache.catalina.startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;


/**
 * Utility class to read the bootstrap Catalina configuration.
 *
 *	读取程序引导需要的catalina 配置 的一个工具类
 *
 *在bootStrap 的createClassLoader中. 用来创建加载器 的一些信息.
 *
 * @author Remy Maucherat
 * @version $Revision: 467222 $ $Date: 2006-10-24 11:17:11 +0800 (Tue, 24 Oct 2006) $
 */

public class CatalinaProperties {


    // ------------------------------------------------------- Static Variables

    private static org.apache.juli.logging.Log log=
        org.apache.juli.logging.LogFactory.getLog( CatalinaProperties.class );

    private static Properties properties = null;


    static {
    	/**
    	 * 类被加载时候. 就调用方法.  填充 properties
    	 */
        loadProperties();

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return specified property value.
     * 返回指定的属性值.
     */
    public static String getProperty(String name) {
	
        return properties.getProperty(name);

    }


    /**
     * Return specified property value.
     */
    public static String getProperty(String name, String defaultValue) {

        return properties.getProperty(name, defaultValue);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Load properties.
     * 
     * 从配置文件加载catalina.properties
     * 
     * 
     */
    private static void loadProperties() {

        InputStream is = null;
        Throwable error = null;
        //读取  systemprops.xml 得到链接.然后加载
        try {
            String configUrl = getConfigUrl();
            if (configUrl != null) {
                is = (new URL(configUrl)).openStream();
            }
        } catch (Throwable t) {
            // Ignore
        }

        if (is == null) {
        	//然后从 ${catalina.base}/conf/catalina.properties 
        	//读取文件
            try {
                File home = new File(getCatalinaBase());
                File conf = new File(home, "conf");
                File properties = new File(conf, "catalina.properties");
                is = new FileInputStream(properties);
            } catch (Throwable t) {
                // Ignore 忽略
            }
        }

        if (is == null) {
        	//从 指定目录下获取
            try {
                is = CatalinaProperties.class.getResourceAsStream
                    ("/org/apache/catalina/startup/catalina.properties");
            } catch (Throwable t) {
                // Ignore
            }
        }

        if (is != null) {
            try {
                properties = new Properties();
                properties.load(is);
                is.close();
            } catch (Throwable t) {
                error = t;
            }
        }

        if ((is == null) || (error != null)) {
            // Do something
            log.warn("Failed to load catalina.properties", error);
            // That's fine - we have reasonable defaults.
            //默认一个properties  
            properties=new Properties();
            
        }

        // Register the properties as system properties
        // 将properties key value 注册到 系统property
        Enumeration enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            String value = properties.getProperty(name);
            if (value != null) {
                System.setProperty(name, value);
            }
        }

    }


    /**
     * Get the value of the catalina.home environment variable.
     * 得到${catalina.home} 环境变量
     */
    private static String getCatalinaHome() {
        return System.getProperty("catalina.home",
                                  System.getProperty("user.dir"));
    }
    
    
    /**
     * Get the value of the catalina.base environment variable.
     * 得到${catalina.base} 环境变量
     */
    private static String getCatalinaBase() {
        return System.getProperty("catalina.base", getCatalinaHome());
    }


    /**
     * Get the value of the configuration URL
     * 
     *  
     */
    private static String getConfigUrl() {
        return System.getProperty("catalina.config");
    }


}
