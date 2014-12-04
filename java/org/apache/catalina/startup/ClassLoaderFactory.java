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
import java.net.URL;
import java.util.ArrayList;

import org.apache.catalina.loader.StandardClassLoader;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;


/**
 * <p>Utility class for building class loaders for Catalina.  The factory
 * method requires the following parameters in order to build a new class
 * loader (with suitable defaults in all cases):</p>
 * 
 * 给catalina building  loader 的工具类.  是一个工厂类 . 需要一下参数:
 * loader (with suitable defaults in all cases):</p> 
 * 
 * 为Catalina加载class的工具类. 需要下列参数:(这里. 使用的是系统的类加载.  只是封装了一下而已.)
 * 
 * 1. 一个文件夹集合. 包含未解包的类文件资源.
 * 2. 一个文件夹集合 包含 类  jar文件.
 * 3. 父类加载器classloader实例
 * <ul>
 * <li>A set of directories containing unpacked classes (and resources)
 *     that should be included in the class loader's
 *     repositories.</li>
 * <li>A set of directories containing classes and resources in JAR files.
 *     Each readable JAR file discovered in these directories will be
 *     added to the class loader's repositories.</li>
 * <li><code>ClassLoader</code> instance that should become the parent of
 *     the new class loader.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 656800 $ $Date: 2008-05-16 03:26:47 +0800 (Fri, 16 May 2008) $
 */

public final class ClassLoaderFactory {


    private static Log log = LogFactory.getLog(ClassLoaderFactory.class);

    protected static final Integer IS_DIR = new Integer(0);
    protected static final Integer IS_JAR = new Integer(1);
    protected static final Integer IS_GLOB = new Integer(2);
    protected static final Integer IS_URL = new Integer(3);

    // --------------------------------------------------------- Public Methods


    /**
     * Create and return a new class loader, based on the configuration
     * defaults and the specified directory paths:
     * 使用 配置的路径 获得一个新的加载器.
     * @param unpacked Array of pathnames to unpacked directories that should
     *  be added to the repositories of the class loader, or <code>null</code> 
     * for no unpacked directories to be considered
     * @param packed Array of pathnames to directories containing JAR files
     *  that should be added to the repositories of the class loader, 
     * or <code>null</code> for no directories of JAR files to be considered
     * @param parent Parent class loader for the new class loader, or
     *  <code>null</code> for the system class loader.
     *
     * @exception Exception if an error occurs constructing the class loader
     */
    public static ClassLoader createClassLoader(File unpacked[],
                                                File packed[],
                                                ClassLoader parent)
        throws Exception {
        return createClassLoader(unpacked, packed, null, parent);
    }


    /**
     * Create and return a new class loader, based on the configuration
     * defaults and the specified directory paths:
     *
     * @param unpacked Array of pathnames to unpacked directories that should
     *  be added to the repositories of the class loader, or <code>null</code> 
     * for no unpacked directories to be considered
     * @param packed Array of pathnames to directories containing JAR files
     *  that should be added to the repositories of the class loader, 
     * or <code>null</code> for no directories of JAR files to be considered
     * @param urls Array of URLs to remote repositories, designing either JAR 
     *  resources or uncompressed directories that should be added to 
     *  the repositories of the class loader, or <code>null</code> for no 
     *  directories of JAR files to be considered
     * @param parent Parent class loader for the new class loader, or
     *  <code>null</code> for the system class loader.
     *
     * @exception Exception if an error occurs constructing the class loader
     */
    public static ClassLoader createClassLoader(File unpacked[],
                                                File packed[],
                                                URL urls[],
                                                ClassLoader parent)
        throws Exception {

        if (log.isDebugEnabled())
            log.debug("Creating new class loader");

        // Construct the "class path" for this class loader
        ArrayList list = new ArrayList();

        // Add unpacked directories
        // 把unpacked file 是文件夹 数组的file 构造成url .并且计入list
        if (unpacked != null) {
            for (int i = 0; i < unpacked.length; i++)  {
                File file = unpacked[i];
                if (!file.exists() || !file.canRead())
                    continue;
                file = new File(file.getCanonicalPath() + File.separator);
                URL url = file.toURI().toURL();
                if (log.isDebugEnabled())
                    log.debug("  Including directory " + url);
                list.add(url);
            }
        }

        // Add packed directory JAR files
        // *.jar 数组转成url. 存入list
        if (packed != null) {
            for (int i = 0; i < packed.length; i++) {
                File directory = packed[i];
                if (!directory.isDirectory() || !directory.exists() ||
                    !directory.canRead())
                    continue;
                String filenames[] = directory.list();
                for (int j = 0; j < filenames.length; j++) {
                    String filename = filenames[j].toLowerCase();
                    if (!filename.endsWith(".jar"))
                        continue;
                    File file = new File(directory, filenames[j]);
                    if (log.isDebugEnabled())
                        log.debug("  Including jar file " + file.getAbsolutePath());
                    URL url = file.toURI().toURL();
                    list.add(url);
                }
            }
        }

        // Construct the class loader itself
        // 使用构造一个StandardClassLoader (java.net.URLClassLoader) 子类
        URL[] array = (URL[]) list.toArray(new URL[list.size()]);
        StandardClassLoader classLoader = null;
        if (parent == null)
            classLoader = new StandardClassLoader(array);
        else
            classLoader = new StandardClassLoader(array, parent);
        return (classLoader);

    }


    /**
     * Create and return a new class loader, based on the configuration
     * defaults and the specified directory paths:
     *
     * @param locations Array of strings containing class directories, jar files,
     *  jar directories or URLS that should be added to the repositories of
     *  the class loader. The type is given by the member of param types.
     *  
     *  包含class  jar  jar目录 的文件夹.
     * @param types Array of types for the members of param locations.
     *  Possible values are IS_DIR (class directory), IS_JAR (single jar file),
     *  IS_GLOB (directory of jar files) and IS_URL (URL).
     *  
     *  与 locations 一一对应的类型
     *   IS_DIR(0); IS_JAR(1); IS_GLOB(2); IS_URL (3);
     *   
     * @param parent Parent class loader for the new class loader, or
     *  <code>null</code> for the system class loader.
     *  
     *  父类加载器
     *
     * @exception Exception if an error occurs constructing the class loader
     */
    public static ClassLoader createClassLoader(String locations[],
                                                Integer types[],
                                                ClassLoader parent)
        throws Exception {

        if (log.isDebugEnabled())
            log.debug("Creating new class loader");

        // Construct the "class path" for this class loader
        ArrayList list = new ArrayList();

        if (locations != null && types != null && locations.length == types.length) {
        	// 循环 localtions  typs 两个数组.得到url .并且添加到list
            for (int i = 0; i < locations.length; i++)  {
                String location = locations[i];
                if ( types[i] == IS_URL ) {
                    URL url = new URL(location);
                    if (log.isDebugEnabled())
                        log.debug("  Including URL " + url);
                    list.add(url);
                } else if ( types[i] == IS_DIR ) {
                    File directory = new File(location);
                    directory = new File(directory.getCanonicalPath());
                    if (!directory.exists() || !directory.isDirectory() ||
                        !directory.canRead())
                         continue;
                    URL url = directory.toURI().toURL();
                    if (log.isDebugEnabled())
                        log.debug("  Including directory " + url);
                    list.add(url);
                } else if ( types[i] == IS_JAR ) {
                    File file=new File(location);
                    file = new File(file.getCanonicalPath());
                    if (!file.exists() || !file.canRead())
                        continue;
                    URL url = file.toURI().toURL();
                    if (log.isDebugEnabled())
                        log.debug("  Including jar file " + url);
                    list.add(url);
                } else if ( types[i] == IS_GLOB ) {
                    File directory=new File(location);
                    if (!directory.exists() || !directory.isDirectory() ||
                        !directory.canRead())
                        continue;
                    if (log.isDebugEnabled())
                        log.debug("  Including directory glob "
                            + directory.getAbsolutePath());
                    String filenames[] = directory.list();
                    for (int j = 0; j < filenames.length; j++) {
                        String filename = filenames[j].toLowerCase();
                        if (!filename.endsWith(".jar"))
                            continue;
                        File file = new File(directory, filenames[j]);
                        file = new File(file.getCanonicalPath());
                        if (!file.exists() || !file.canRead())
                            continue;
                        if (log.isDebugEnabled())
                            log.debug("    Including glob jar file "
                                + file.getAbsolutePath());
                        URL url = file.toURI().toURL();
                        list.add(url);
                    }
                }
            }
        }

        // Construct the class loader itself
        URL[] array = (URL[]) list.toArray(new URL[list.size()]);
        if (log.isDebugEnabled())
            for (int i = 0; i < array.length; i++) {
                log.debug("  location " + i + " is " + array[i]);
            }
        
        // 使用这些 url 数组构建一个 classloader
        StandardClassLoader classLoader = null;
        if (parent == null)
            classLoader = new StandardClassLoader(array);
        else
            classLoader = new StandardClassLoader(array, parent);
        return (classLoader);

    }


}
