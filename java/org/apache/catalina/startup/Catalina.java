/*
 * contributor license agreements.  See the NOTICE file distributed with
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.core.StandardServer;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.Rule;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;


/**
 * Catalina->Server->Service->容器/连接器/日志器
 * 
 * catalina 是tomcat的核心类.  tomcat启动 和  stop是通过该类实现.  strapup类是通过 通过操作该类.实现启动的配置.
 * 
 * Startup/Shutdown shell program for Catalina.  The following command line
 * options are recognized:
 * <ul>
 * <li><b>-config {pathname}</b> - Set the pathname of the configuration file
 *     to be processed.  If a relative path is specified, it will be
 *     interpreted as relative to the directory pathname specified by the
 *     "catalina.base" system property.   [conf/server.xml]
 * <li><b>-help</b> - Display usage information.
 * <li><b>-stop</b> - Stop the currently running instance of Catalina.
 * </u>
 *
 * Should do the same thing as Embedded, but using a server.xml file.
 *
 * Catalina 启动 /停止 壳程序,  下列命令是被承认的选项:
 * 
 *-config {pathname} .如果指定一个相对路径名 .设置该配置文件并且处理. 它将到catalina.base寻找. 并且解释.
 *-help               显示用法信息
 *-stop               停止当前运行的Catalina实例
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 737768 $ $Date: 2009-01-27 01:55:19 +0800 (Tue, 27 Jan 2009) $
 */

public class Catalina extends Embedded {


    // ----------------------------------------------------- Instance Variables


    /**
     * Pathname to the server configuration file.
     * server.xml 相对路径 
     */
    protected String configFile = "conf/server.xml";

    // XXX Should be moved to embedded
    /**
     * The shared extensions class loader for this server.
     * 
     * @see Bootstrap#init()
     */
    protected ClassLoader parentClassLoader =
        Catalina.class.getClassLoader();


    /**
     * The server component we are starting or stopping
     * server 组件 (通过digester setServer() 得到实例.  以及其子类实例 等 所有的一切都是通过digester 解析配置文件server.xml 得到)
     */
    protected Server server = null;


    /**
     * Are we starting a new server?
     * 是否在启动一个新的server
     */
    protected boolean starting = false;


    /**
     * Are we stopping an existing server?
     * 是否正在停止一个存在的server
     */
    protected boolean stopping = false;


    /**
     * Use shutdown hook flag.
     * 是否使用的 shutdownhook.. 
     * 普及:  钩子  (hook)  . 在jvm exit时候.可以通过注册一个thread进行部分资源的释放. 以及日志的记录. 达到代码的完善.
     * 称之为 钩子
     * 
     */
    protected boolean useShutdownHook = true;


    /**
     * Shutdown hook.
     * shutdown钩子 实例引用 .就是一个thread.
     */
    protected Thread shutdownHook = null;


    // ------------------------------------------------------------- Properties

    /*设置配置文件*/
    public void setConfig(String file) {
        configFile = file;
    }


    public void setConfigFile(String file) {
        configFile = file;
    }

    /*得到配置文件*/
    public String getConfigFile() {
        return configFile;
    }

  
    public void setUseShutdownHook(boolean useShutdownHook) {
        this.useShutdownHook = useShutdownHook;
    }


    public boolean getUseShutdownHook() {
        return useShutdownHook;
    }


    /**
     * Set the shared extensions class loader.
     *
     * @see Bootstrap#init() 调用
     * @param parentClassLoader The shared extensions class loader.
     */
    public void setParentClassLoader(ClassLoader parentClassLoader) {

        this.parentClassLoader = parentClassLoader;

    }


    /**
     * Set the server instance we are configuring.
     *
     *
     * @param server The new server
     */
    public void setServer(Server server) {
    	
        this.server = server;

    }

    // ----------------------------------------------------------- Main Program

    /**
     * The application main program.
     *
     * @param args Command line arguments
     */
    public static void main(String args[]) {
        (new Catalina()).process(args);
    }


    /**
     * The instance main program.
     *
     *实例主程序代码
     *
     * @param args Command line arguments
     */
    public void process(String args[]) {

        setAwait(true);
        setCatalinaHome();
        setCatalinaBase();
        try {
            if (arguments(args)) {
                if (starting) {
                    load(args);
                    start();
                } else if (stopping) {
                    stopServer();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Process the specified command line arguments, and return
     * <code>true</code> if we should continue processing; otherwise
     * return <code>false</code>.
     * 处理 参数 .并且返回 true (可以继续下一步) false
     *
     * @param args Command line arguments to process
     */
    protected boolean arguments(String args[]) {

        boolean isConfig = false;

        if (args.length < 1) {
            usage();
            return (false);
        }

        for (int i = 0; i < args.length; i++) {
            if (isConfig) {
                configFile = args[i];
                isConfig = false;
            } else if (args[i].equals("-config")) {
                isConfig = true;
            } else if (args[i].equals("-nonaming")) {
                setUseNaming( false );
            } else if (args[i].equals("-help")) {
                usage();
                return (false);
            } else if (args[i].equals("start")) {
                starting = true;
                stopping = false;
            } else if (args[i].equals("stop")) {
                starting = false;
                stopping = true;
            } else {
                usage();
                return (false);
            }
        }

        return (true);

    }


    /**
     * Return a File object representing our configuration file.
     * 把conf/server.xml 作为一个File类返回给调用的地方
     */
    protected File configFile() {

        File file = new File(configFile);
        if (!file.isAbsolute())
            file = new File(System.getProperty("catalina.base"), configFile);
        return (file);

    }


    /**
     * Create and configure the Digester we will be using for startup.
     * 
     * 新建一个我们用来startup的配置Digester(通过解析xml .配置类的依赖关系)
     * 
     * 这个digester是对应一个conf/server.xml文件.  
     * 
     */
    protected Digester createStartDigester() {
        long t1=System.currentTimeMillis();
        // Initialize the digester
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setRulesValidation(true);
        HashMap<Class, List<String>> fakeAttributes = new HashMap<Class, List<String>>();
        ArrayList<String> attrs = new ArrayList<String>();
        attrs.add("className");
        fakeAttributes.put(Object.class, attrs);
        digester.setFakeAttributes(fakeAttributes);
        digester.setClassLoader(StandardServer.class.getClassLoader());

        // Configure the actions we will be using
        // 初始化org.apache.catalina.core.StandardServer
        //试着读取Server 的className属性.  使用创建对象. 没有默认使用org.apache.catalina.core.StandardServer
        digester.addObjectCreate("Server",
                                 "org.apache.catalina.core.StandardServer",
                                 "className");
        // 为server 设置属性 由xml决定. 如果属性名和 class 的名一样的话
        digester.addSetProperties("Server");
        
        
        // 通过setServer添加到上一级标签  这个地方就是后面的push 的this, 即root.
        //
        digester.addSetNext("Server",
                            "setServer",
                            "org.apache.catalina.Server"); //最后一个标签表示参数类型

        digester.addObjectCreate("Server/GlobalNamingResources",
                                 "org.apache.catalina.deploy.NamingResources");
        digester.addSetProperties("Server/GlobalNamingResources");
        digester.addSetNext("Server/GlobalNamingResources",
                            "setGlobalNamingResources",
                            "org.apache.catalina.deploy.NamingResources");

        digester.addObjectCreate("Server/Listener",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties("Server/Listener");
        digester.addSetNext("Server/Listener",
                            "addLifecycleListener",
                            "org.apache.catalina.LifecycleListener");

        digester.addObjectCreate("Server/Service",
                                 "org.apache.catalina.core.StandardService",
                                 "className");
        digester.addSetProperties("Server/Service");
        digester.addSetNext("Server/Service",
                            "addService",
                            "org.apache.catalina.Service");

        digester.addObjectCreate("Server/Service/Listener",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties("Server/Service/Listener");
        digester.addSetNext("Server/Service/Listener",
                            "addLifecycleListener",
                            "org.apache.catalina.LifecycleListener");

        //Executor
        digester.addObjectCreate("Server/Service/Executor",
                         "org.apache.catalina.core.StandardThreadExecutor",
                         "className");
        digester.addSetProperties("Server/Service/Executor");

        digester.addSetNext("Server/Service/Executor",
                            "addExecutor",
                            "org.apache.catalina.Executor");

        
        digester.addRule("Server/Service/Connector",
                         new ConnectorCreateRule());
        digester.addRule("Server/Service/Connector", 
                         new SetAllPropertiesRule(new String[]{"executor"}));
        digester.addSetNext("Server/Service/Connector",
                            "addConnector",
                            "org.apache.catalina.connector.Connector");
        
        


        digester.addObjectCreate("Server/Service/Connector/Listener",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties("Server/Service/Connector/Listener");
        digester.addSetNext("Server/Service/Connector/Listener",
                            "addLifecycleListener",
                            "org.apache.catalina.LifecycleListener");

        // Add RuleSets for nested elements
        digester.addRuleSet(new NamingRuleSet("Server/GlobalNamingResources/"));
        digester.addRuleSet(new EngineRuleSet("Server/Service/"));
        digester.addRuleSet(new HostRuleSet("Server/Service/Engine/"));
        digester.addRuleSet(new ContextRuleSet("Server/Service/Engine/Host/"));
        digester.addRuleSet(ClusterRuleSetFactory.getClusterRuleSet("Server/Service/Engine/Host/Cluster/"));
        digester.addRuleSet(new NamingRuleSet("Server/Service/Engine/Host/Context/"));

        // When the 'engine' is found, set the parentClassLoader.
        digester.addRule("Server/Service/Engine",
                         new SetParentClassLoaderRule(parentClassLoader));
        digester.addRuleSet(ClusterRuleSetFactory.getClusterRuleSet("Server/Service/Engine/Cluster/"));

        long t2=System.currentTimeMillis();
        if (log.isDebugEnabled())
            log.debug("Digester for server.xml created " + ( t2-t1 ));
        return (digester);

    }


    /**
     * Create and configure the Digester we will be using for shutdown.
     * 为shutdown 配置一个Digester  .读取conf/server.xml
     */
    protected Digester createStopDigester() {

        // Initialize the digester
        Digester digester = new Digester();

        // Configure the rules we need for shutting down
        digester.addObjectCreate("Server",
                                 "org.apache.catalina.core.StandardServer",
                                 "className");
        digester.addSetProperties("Server");
        digester.addSetNext("Server",
                            "setServer",
                            "org.apache.catalina.Server");

        return (digester);

    }


    public void stopServer() {
        stopServer(null);
    }

    public void stopServer(String[] arguments) {

        if (arguments != null) {
            arguments(arguments);
        }

        if( server == null ) {
            // Create and execute our Digester
        	// 建一个Digester 并且执行她
            Digester digester = createStopDigester();
            digester.setClassLoader(Thread.currentThread().getContextClassLoader());
            File file = configFile();
            try {
                InputSource is =
                    new InputSource("file://" + file.getAbsolutePath());
                FileInputStream fis = new FileInputStream(file);
                is.setByteStream(fis);
                digester.push(this);
                digester.parse(is);
                fis.close();
            } catch (Exception e) {
                log.error("Catalina.stop: ", e);
                System.exit(1);
            }
        }

        // Stop the existing server
        try {
            if (server.getPort()>0) {
            	String hostAddress = InetAddress.getByName("localhost").getHostAddress();
            	Socket socket = new Socket(hostAddress, server.getPort());
            	OutputStream stream = socket.getOutputStream();
            	String shutdown = server.getShutdown();
            	for (int i = 0; i < shutdown.length(); i++)
            		stream.write(shutdown.charAt(i));
            	stream.flush();
            	stream.close();
            	socket.close();
            } else {
                log.error(sm.getString("catalina.stopServer"));
                System.exit(1);
            }
        } catch (IOException e) {
            log.error("Catalina.stop: ", e);
            System.exit(1);
        }

    }


    /**
     * Set the <code>catalina.base</code> System property to the current
     * working directory if it has not been set.
     * @deprecated Use initDirs()
     */
    public void setCatalinaBase() {
        initDirs();
    }

    /**
     * Set the <code>catalina.home</code> System property to the current
     * working directory if it has not been set.
     * @deprecated Use initDirs()
     */
    public void setCatalinaHome() {
        initDirs();
    }

    /**
     * Start a new server instance.
     * 启动一个新的server实例
     */
    public void load() {

        long t1 = System.nanoTime();

        initDirs();

        // Before digester - it may be needed
        // 解析之前.   执行一些必要的东西
        initNaming();

        // Create and execute our Digester
        // 创建 并且 执行我们的xml解析(开始的)
        Digester digester = createStartDigester();

        InputSource inputSource = null;
        InputStream inputStream = null;
        File file = null;
        try {
            file = configFile();
            inputStream = new FileInputStream(file);
            inputSource = new InputSource("file://" + file.getAbsolutePath());
        } catch (Exception e) {
            ;
        }
        if (inputStream == null) {
            try {
                inputStream = getClass().getClassLoader()
                    .getResourceAsStream(getConfigFile());
                inputSource = new InputSource
                    (getClass().getClassLoader()
                     .getResource(getConfigFile()).toString());
            } catch (Exception e) {
                ;
            }
        }

        // This should be included in catalina.jar
        // Alternative: don't bother with xml, just create it manually.
        
        // 包含在Catalina.jar的一个文件
        // ?? 如果server.xml 没有.  就加载该文件
        if( inputStream==null ) {
            try {
                inputStream = getClass().getClassLoader()
                .getResourceAsStream("server-embed.xml");
                inputSource = new InputSource
                (getClass().getClassLoader()
                        .getResource("server-embed.xml").toString());
            } catch (Exception e) {
                ;
            }
        }
        

        if ((inputStream == null) && (file != null)) {
            log.warn("Can't load server.xml from " + file.getAbsolutePath());
            return;
        }

        try {
            inputSource.setByteStream(inputStream);
            /**
             * 这个地方是最重要的.  将Catalina 设置为digester的最上端.
             * 
             * 在createStartDigeter()时候.  调用了setServer().  所以各个地方都做好了关联. 
             * 
             * 
             */
            digester.push(this);   
            digester.parse(inputSource);
            inputStream.close();
        } catch (Exception e) {
            log.warn("Catalina.start using "
                               + getConfigFile() + ": " , e);
            return;
        }

        // Stream redirection
        initStreams();

        // Start the new server
        if (server instanceof Lifecycle) {
            try {
            	/*初始化各个组件  会调用子类组件的initialize方法 */
                server.initialize();
            } catch (LifecycleException e) {
                log.error("Catalina.start", e);
            }
        }

        long t2 = System.nanoTime();
        if(log.isInfoEnabled())
            log.info("Initialization processed in " + ((t2 - t1) / 1000000) + " ms");

    }


    /** 
     * 
     * Load using arguments
     * 
     * 使用参数加载
     * @see Bootstrap#load()
     */
    public void load(String args[]) {

        try {
        	// 判断参数的合法性
            if (arguments(args))
            	// load . 准备digester.
                load();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void create() {

    }

    public void destroy() {

    }

    /**
     * Start a new server instance.
     * 启动 server实例
     */
    public void start() {
    	// 判断.  如果没有server 就load . 
        if (server == null) {
            load();
        }

        long t1 = System.nanoTime();
        
        // Start the new server
        if (server instanceof Lifecycle) {
        	// 启动最上层的container.  之后, 由父类启动子类
            try {
                ((Lifecycle) server).start();
            } catch (LifecycleException e) {
                log.error("Catalina.start: ", e);
            }
        }

        long t2 = System.nanoTime();
        if(log.isInfoEnabled())
            log.info("Server startup in " + ((t2 - t1) / 1000000) + " ms");

        try {
            // Register shutdown hook
        	// 注册钩子
            if (useShutdownHook) {
                if (shutdownHook == null) {
                    shutdownHook = new CatalinaShutdownHook();
                }
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            }
        } catch (Throwable t) {
            // This will fail on JDK 1.2. Ignoring, as Tomcat can run
            // fine without the shutdown hook.
        }

        if (await) {
            await();
            stop();
        }

    }


    /**
     * Stop an existing server instance.
     * 停止 存在的server实例
     */
    public void stop() {

        try {
            // Remove the ShutdownHook first so that server.stop() 
            // doesn't get invoked twice
            if (useShutdownHook) {
            	
            	// 如果有钩子使用, 调用stop方法后,  删除钩子,  不重复调用
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
        } catch (Throwable t) {
            // This will fail on JDK 1.2. Ignoring, as Tomcat can run
            // fine without the shutdown hook.
        	// 这个在jdk1.2的时候会失败.  忽略. tomcat可以继续运行
        	//??
        }

        // Shut down the server
        if (server instanceof Lifecycle) {
            try {
                ((Lifecycle) server).stop();
            } catch (LifecycleException e) {
                log.error("Catalina.stop", e);
            }
        }

    }


    /**
     * Await and shutdown.
     * 
     * await
     */
    public void await() {

        server.await();

    }


    /**
     * Print usage information for this application.
     * 
     * 打印应用用法信息
     */
    protected void usage() {

        System.out.println
            ("usage: java org.apache.catalina.startup.Catalina"
             + " [ -config {pathname} ]"
             + " [ -nonaming ] { start | stop }");

    }


    // --------------------------------------- CatalinaShutdownHook Inner Class

    // XXX Should be moved to embedded !
    /**
     * Shutdown hook which will perform a clean shutdown of Catalina if needed.
     * 如果是意外的推出, 会调用 stop 方法
     * 
     * catalina 的关闭钩子
     */
    protected class CatalinaShutdownHook extends Thread {

        public void run() {
        	// 没有被关闭的时候   .   调用stop
            if (server != null) {
                Catalina.this.stop();
            }
            
        }

    }
    
    
    private static org.apache.juli.logging.Log log=
        org.apache.juli.logging.LogFactory.getLog( Catalina.class );

}


// ------------------------------------------------------------ Private Classes


/**
 * Rule that sets the parent class loader for the top object on the stack,
 * which must be a <code>Container</code>.
 * 
 * 
 * 为stack顶部的Container对象 设置parentClassLoader加载器
 */

final class SetParentClassLoaderRule extends Rule {

    public SetParentClassLoaderRule(ClassLoader parentClassLoader) {

        this.parentClassLoader = parentClassLoader;

    }

    ClassLoader parentClassLoader = null;

    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {

        if (digester.getLogger().isDebugEnabled())
            digester.getLogger().debug("Setting parent class loader");

        Container top = (Container) digester.peek();
        top.setParentClassLoader(parentClassLoader);

    }


}
