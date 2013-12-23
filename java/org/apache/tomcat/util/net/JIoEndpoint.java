/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.net;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

/**
 * JIO 模式.
 * @see JIoEndpoint
 * NIO模式参考
 * @see NioEndpoint
 * APR 使用C编写的IO这块 .需要编译
 * @see AprEndpoint
 * 
 * Handle incoming TCP connections.
 *  处理进来的tcp链接
 * 
 * This class implement a simple server model: one listener thread accepts on a socket and
 * creates a new worker thread for each incoming connection.
 *
 *  这个类实现了简单的server模型. 一个socket监听服务.  然后创建新的worker thread(是从池里取,并非每次新建) 处理每个进来的链接.(socket)
 * 
 * More advanced Endpoints will reuse the threads, use queues, etc.
 *  更高级的是回收. 然后使用队列等..
 *
 * @author James Duncan Davidson
 * @author Jason Hunter
 * @author James Todd
 * @author Costin Manolache
 * @author Gal Shachor
 * @author Yoav Shapira
 * @author Remy Maucherat
 */
public class JIoEndpoint {


    // -------------------------------------------------------------- Constants


    protected static Log log = LogFactory.getLog(JIoEndpoint.class);

    protected StringManager sm = 
        StringManager.getManager("org.apache.tomcat.util.net.res");


    /**
     * The Request attribute key for the cipher suite.
     */
    public static final String CIPHER_SUITE_KEY = "javax.servlet.request.cipher_suite";

    /**
     * The Request attribute key for the key size.
     */
    public static final String KEY_SIZE_KEY = "javax.servlet.request.key_size";

    /**
     * The Request attribute key for the client certificate chain.
     */
    public static final String CERTIFICATE_KEY = "javax.servlet.request.X509Certificate";

    /**
     * The Request attribute key for the session id.
     * This one is a Tomcat extension to the Servlet spec.
     */
    public static final String SESSION_ID_KEY = "javax.servlet.request.ssl_session";


    // ----------------------------------------------------------------- Fields


    /**
     * Available workers.
     * 
     * worker 队列. WorkerStack 自己实现的内部类
     */
    protected WorkerStack workers = null;


    /**
     * Running state of the endpoint.
     * 该组件是否 运行
     */
    protected volatile boolean running = false;


    /**
     * Will be set to true whenever the endpoint is paused.
     * 当暂停时.将被设置为true
     */
    protected volatile boolean paused = false;


    /**
     * Track the initialization state of the endpoint.
     * 是否被初始化
     */
    protected boolean initialized = false;


    /**
     * Current worker threads busy count.
     * 当前使用的worker threads数
     */
    protected int curThreadsBusy = 0;


    /**
     * Current worker threads count.
     * 当前所有的worker threads数
     */
    protected int curThreads = 0;


    /**
     * Sequence number used to generate thread names.
     */
    protected int sequence = 0;


    /**
     * Associated server socket.
     * 
     * 当然是最重要的serverSocket拉, 请求都是他收到的.
     */
    protected ServerSocket serverSocket = null;


    // ------------------------------------------------------------- Properties


    /**
     * Acceptor thread count.
     * 
     * serverSocket的数量. 一般起来是1哦.
     */
    protected int acceptorThreadCount = 0;
    public void setAcceptorThreadCount(int acceptorThreadCount) { this.acceptorThreadCount = acceptorThreadCount; }
    public int getAcceptorThreadCount() { return acceptorThreadCount; }


    /**
     * External Executor based thread pool.
     * 基于线程池的执行器 里面有很多参数.  maxThread maxActive...
     * 
     */
    protected Executor executor = null;
    public void setExecutor(Executor executor) { this.executor = executor; }
    public Executor getExecutor() { return executor; }


    /**
     * Maximum amount of worker threads.
     * 最大worker threads.数
     */
    protected int maxThreads = 200;
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        if (running) {
            synchronized(workers) {
                workers.resize(maxThreads);
            }
        }
    }
    public int getMaxThreads() { return maxThreads; }


    /**
     * Priority of the acceptor and poller threads.
     */
    protected int threadPriority = Thread.NORM_PRIORITY;
    public void setThreadPriority(int threadPriority) { this.threadPriority = threadPriority; }
    public int getThreadPriority() { return threadPriority; }

    
    /**
     * Server socket port.
     * 端口
     */
    protected int port;
    public int getPort() { return port; }
    public void setPort(int port ) { this.port=port; }


    /**
     * Address for the server socket.
     */
    protected InetAddress address;
    public InetAddress getAddress() { return address; }
    public void setAddress(InetAddress address) { this.address = address; }


    /**
     * Handling of accepted sockets.
     * 处理接受的socket
     */
    protected Handler handler = null;
    public void setHandler(Handler handler ) { this.handler = handler; }
    public Handler getHandler() { return handler; }


    /**
     * Allows the server developer to specify the backlog that
     * should be used for server sockets. By default, this value
     * is 100.
     * 最大阻塞socket接口量 .默认100
     */
    protected int backlog = 100;
    public void setBacklog(int backlog) { if (backlog > 0) this.backlog = backlog; }
    public int getBacklog() { return backlog; }


    /**
     * Socket TCP no delay.
     */
    protected boolean tcpNoDelay = false;
    public boolean getTcpNoDelay() { return tcpNoDelay; }
    public void setTcpNoDelay(boolean tcpNoDelay) { this.tcpNoDelay = tcpNoDelay; }


    /**
     * Socket linger.
     */
    protected int soLinger = 100;
    public int getSoLinger() { return soLinger; }
    public void setSoLinger(int soLinger) { this.soLinger = soLinger; }


    /**
     * Socket timeout.
     */
    protected int soTimeout = -1;
    public int getSoTimeout() { return soTimeout; }
    public void setSoTimeout(int soTimeout) { this.soTimeout = soTimeout; }


    /**
     * The default is true - the created threads will be
     *  in daemon mode. If set to false, the control thread
     *  will not be daemon - and will keep the process alive.
     *  
     *  新建线程是否是守护线程.  默认是true 。
     */
    protected boolean daemon = true;
    public void setDaemon(boolean b) { daemon = b; }
    public boolean getDaemon() { return daemon; }


    /**
     * Name of the thread pool, which will be used for naming child threads.
     * worker thread 命名. name+threadCount
     */
    protected String name = "TP";
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }


    /**
     * Server socket factory.
     * Server socket 工厂
     */
    protected ServerSocketFactory serverSocketFactory = null;
    public void setServerSocketFactory(ServerSocketFactory factory) { this.serverSocketFactory = factory; }
    public ServerSocketFactory getServerSocketFactory() { return serverSocketFactory; }


    public boolean isRunning() {
        return running;
    }
    
    public boolean isPaused() {
        return paused;
    }
    
    public int getCurrentThreadCount() {
        return curThreads;
    }
    
    public int getCurrentThreadsBusy() {
        return workers!=null?curThreads - workers.size():0;
    }
    

    // ------------------------------------------------ Handler Inner Interface


    /**
     * Bare bones interface used for socket processing. Per thread data is to be
     * stored in the ThreadWithAttributes extra folders, or alternately in
     * thread local fields.
     * 
     * 处理socket的一个接口定义
     */
    public interface Handler {
        public boolean process(Socket socket);
    }


    // --------------------------------------------------- Acceptor Inner Class


    /**
     * Server socket acceptor thread.
     * 
     * Server socket接收器线程.
     * 
     * 一直run. 将接受到的socket异步处理. 
     */
    protected class Acceptor implements Runnable {


        /**
         * The background thread that listens for incoming TCP/IP connections and
         * hands them off to an appropriate processor.
         * 
         * 后台线程监听进来的TCP/IP链接. 并且选择一个processor异步处理
         */
        public void run() {

            // Loop until we receive a shutdown command
        	// 循环.知道得到shutdown命令
            while (running) {

                // Loop if endpoint is paused
            	// 如果暂停.就sleep
                while (paused) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }

                // Accept the next incoming connection from the server socket
                //从server socket接受下一个进来的链接
                try {
                    Socket socket = serverSocketFactory.acceptSocket(serverSocket);
                    serverSocketFactory.initSocket(socket);
                    // Hand this socket off to an appropriate processor
                    //处理socket
                    if (!processSocket(socket)) {
                        // Close socket right away
                    	//最后关闭该socket
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }catch ( IOException x ) {
                    if ( running ) log.error(sm.getString("endpoint.accept.fail"), x);
                } catch (Throwable t) {
                    log.error(sm.getString("endpoint.accept.fail"), t);
                }

                // The processor will recycle itself when it finishes
                // 这个processor 将被回收.在最后.
            }

        }

    }


    // ------------------------------------------- SocketProcessor Inner Class


    /**
     * This class is the equivalent of the Worker, but will simply use in an
     * external Executor thread pool.
     * 
     * 相当worker. 
     */
    protected class SocketProcessor implements Runnable {
        
        protected Socket socket = null;
        
        public SocketProcessor(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            // Process the request from this socket
        	// 处理请求.
            if (!setSocketOptions(socket) || !handler.process(socket)) {
                // Close socket
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }

            // Finish up this request
            socket = null;

        }
        
    }
    
    
    // ----------------------------------------------------- Worker Inner Class

    /**
     * Server processor class.
     * 
     * 处说中的worker.  处理socket的类
     */
    protected class Worker implements Runnable {

        protected Thread thread = null;
        protected boolean available = false;
        protected Socket socket = null;

        
        /**
         * Process an incoming TCP/IP connection on the specified socket.  Any
         * exception that occurs during processing must be logged and swallowed.
         * <b>NOTE</b>:  This method is called from our Connector's thread.  We
         * must assign it to our own thread so that multiple simultaneous
         * requests can be handled.
         * 
         * 处理从Connector 线程进来的Socket. 
         * 
         * 默认情况.available 是false,  所以.分配过来的socket设置到局部变量.  available变成false.
         * 然后唤醒等待的线程. await.await获得有socket进入. 立即将局部变量的socket交给run()
         * 然后交handler.process(socket)处理! 所有的执行都是当即返回. 以免主线程阻塞!
         * 
         *
         * @param socket TCP socket to process
         */
        synchronized void assign(Socket socket) {

            // Wait for the Processor to get the previous Socket
        	// 等待Processor得到先前的socket.
            while (available) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            // Store the newly available Socket and notify our thread
            this.socket = socket;
            available = true;
            notifyAll();

        }

        
        /**
         * Await a newly assigned Socket from our Connector, or <code>null</code>
         * if we are supposed to shut down
         * 
         * 等待一个新的分配的socket.
         */
        private synchronized Socket await() {

            // Wait for the Connector to provide a new Socket
            while (!available) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            // Notify the Connector that we have received this Socket
            Socket socket = this.socket;
            available = false;
            notifyAll();

            return (socket);

        }



        /**
         * The background thread that listens for incoming TCP/IP connections and
         * hands them off to an appropriate processor.
         * 
         * 后台线程监听进来的TCP/IP. 并且处理他.
         */
        public void run() {

            // Process requests until we receive a shutdown signal
            while (running) {

                // Wait for the next socket to be assigned
                Socket socket = await();
                if (socket == null)
                    continue;

                // Process the request from this socket
                if (!setSocketOptions(socket) || !handler.process(socket)) {
                    // Close socket
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                }

                // Finish up this request
                socket = null;
               
                //使用完了. 回收.
                recycleWorkerThread(this);

            }

        }


        /**
         * Start the background processing thread.
         * 
         * 启动一个Worker
         */
        public void start() {
            thread = new Thread(this);
            thread.setName(getName() + "-" + (++curThreads));
            thread.setDaemon(true);
            thread.start();
        }


    }


    // -------------------- Public methods --------------------
    /**
     * 初始化serverSocket.
     * 
     * 
     * @throws Exception
     */
    public void init()
        throws Exception {

        if (initialized)
            return;
        
        // Initialize thread count defaults for acceptor
        
        if (acceptorThreadCount == 0) {
            acceptorThreadCount = 1;
        }
        if (serverSocketFactory == null) {
            serverSocketFactory = ServerSocketFactory.getDefault();
        }
        if (serverSocket == null) {
            try {
                if (address == null) {
                    serverSocket = serverSocketFactory.createSocket(port, backlog);
                } else {
                    serverSocket = serverSocketFactory.createSocket(port, backlog, address);
                }
            } catch (BindException be) {
                if (address == null)
                    throw new BindException(be.getMessage() + "<null>:" + port);
                else
                    throw new BindException(be.getMessage() + " " +
                            address.toString() + ":" + port);
            }
        }
        //if( serverTimeout >= 0 )
        //    serverSocket.setSoTimeout( serverTimeout );
        
        initialized = true;
        
    }
    
    public void start()
        throws Exception {
        // Initialize socket if not done before
        if (!initialized) {
            init();
        }
        if (!running) {
            running = true;
            paused = false;

            // Create worker collection
            // 创建worker
            if (executor == null) {
            	// executor 在server.xml中的Connector标签. 如果指定了executor则会被set. 就不会是null
            	
                workers = new WorkerStack(maxThreads);
            }

            // Start acceptor threads
            for (int i = 0; i < acceptorThreadCount; i++) {
                Thread acceptorThread = new Thread(new Acceptor(), getName() + "-Acceptor-" + i);
                acceptorThread.setPriority(threadPriority);
                acceptorThread.setDaemon(daemon);
                acceptorThread.start();
            }
        }
    }

    public void pause() {
        if (running && !paused) {
            paused = true;
            unlockAccept();
        }
    }

    public void resume() {
        if (running) {
            paused = false;
        }
    }

    public void stop() {
        if (running) {
            running = false;
            unlockAccept();
        }
    }

    /**
     * Deallocate APR memory pools, and close server socket.
     */
    public void destroy() throws Exception {
        if (running) {
            stop();
        }
        if (serverSocket != null) {
            try {
                if (serverSocket != null)
                    serverSocket.close();
            } catch (Exception e) {
                log.error(sm.getString("endpoint.err.close"), e);
            }
            serverSocket = null;
        }
        initialized = false ;
    }

    
    /**
     * Unlock the accept by using a local connection.
     */
    protected void unlockAccept() {
        Socket s = null;
        try {
            // Need to create a connection to unlock the accept();
            if (address == null) {
                s = new Socket(InetAddress.getByName("localhost").getHostAddress(), port);
            } else {
                s = new Socket(address, port);
                    // setting soLinger to a small value will help shutdown the
                    // connection quicker
                s.setSoLinger(true, 0);
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(sm.getString("endpoint.debug.unlock", "" + port), e);
            }
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }


    /**
     * Set the options for the current socket.
     */
    protected boolean setSocketOptions(Socket socket) {
        // Process the connection
        int step = 1;
        try {

            // 1: Set socket options: timeout, linger, etc
            if (soLinger >= 0) { 
                socket.setSoLinger(true, soLinger);
            }
            if (tcpNoDelay) {
                socket.setTcpNoDelay(tcpNoDelay);
            }
            if (soTimeout > 0) {
                socket.setSoTimeout(soTimeout);
            }

            // 2: SSL handshake
            step = 2;
            serverSocketFactory.handshake(socket);

        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                if (step == 2) {
                    log.debug(sm.getString("endpoint.err.handshake"), t);
                } else {
                    log.debug(sm.getString("endpoint.err.unexpected"), t);
                }
            }
            // Tell to close the socket
            return false;
        }
        return true;
    }

    
    /**
     * Create (or allocate) and return an available processor for use in
     * processing a specific HTTP request, if possible.  If the maximum
     * allowed processors have already been created and are in use, return
     * <code>null</code> instead.
     * 
     * 创建一个新的.或者从队列分配一个Worker. 如果最大processors了. 并且没有空闲的.  那么返回null.
     * 
     * maxThreads < 0 表示不限制worker创建数 .
     * 
     * 这些都是根据配置来进行的.默认是200
     */
    protected Worker createWorkerThread() {

        synchronized (workers) {
            if (workers.size() > 0) {
            	//如果有现成的. 直接返回
            	
                curThreadsBusy++;
                return workers.pop();
            }
            if ((maxThreads > 0) && (curThreads < maxThreads)) {
            	//如果maxThreads大于0 . 并且当前已有小于它. 创建一个新的.
                curThreadsBusy++;
                if (curThreadsBusy == maxThreads) {
                    log.info(sm.getString("endpoint.info.maxThreads",
                            Integer.toString(maxThreads), address,
                            Integer.toString(port)));
                }
                return (newWorkerThread());
            } else {
            	
                if (maxThreads < 0) {
                    // 如果maxThreads小于0   worker管够.  除非机器受不了(毕竟是在跑的线程)
                	
                	curThreadsBusy++;
                    return (newWorkerThread());
                } else {
                    return (null);
                }
            }
        }

    }


    /**
     * Create and return a new processor suitable for processing HTTP
     * requests and returning the corresponding responses.
     * 
     * 创建一个新的worker. 并启动他的线程
     */
    protected Worker newWorkerThread() {

        Worker workerThread = new Worker();
        workerThread.start();
        return (workerThread);

    }


    /**
     * Return a new worker thread, and block while to worker is available.
     * 从自己的队列取一个worker.并且返回
     */
    protected Worker getWorkerThread() {
        // Allocate a new worker thread
        Worker workerThread = createWorkerThread();
        while (workerThread == null) {
            try {
                synchronized (workers) {
                    workers.wait();
                }
            } catch (InterruptedException e) {
                // Ignore
            }
            workerThread = createWorkerThread();
        }
        return workerThread;
    }


    /**
     * Recycle the specified Processor so that it can be used again.
     *  回收指定的Processor. 他们可以再次被使用.
     * @param workerThread The processor to be recycled
     */
    protected void recycleWorkerThread(Worker workerThread) {
        synchronized (workers) {
            workers.push(workerThread);
            curThreadsBusy--;
            workers.notify();
        }
    }


    /**
     * Process given socket.
     * 处理指定的socket
     */
    protected boolean processSocket(Socket socket) {
        try {
        	// server.xml 指定. 如果executor 不为空.就将socket交给executor
            if (executor == null) {
                getWorkerThread().assign(socket);
            } else {
                executor.execute(new SocketProcessor(socket));
            }
        } catch (Throwable t) {
            // This means we got an OOM or similar creating a thread, or that
            // the pool and its queue are full
            log.error(sm.getString("endpoint.process.fail"), t);
            return false;
        }
        return true;
    }
    

    // ------------------------------------------------- WorkerStack Inner Class

    /**
     * 自己实现的一个队列
     * @author xiangyang
     *
     */
    public class WorkerStack {
        
        protected Worker[] workers = null;
        protected int end = 0; // 不为null的长度
        
        public WorkerStack(int size) {
            workers = new Worker[size];
        }
        
        /** 
         * Put the object into the queue. If the queue is full (for example if
         * the queue has been reduced in size) the object will be dropped.
         * 
         * 把object放入队列.如果队列满的.  object将被丢弃.
         * 
         * @param   object  the object to be appended to the queue (first
         *                  element).
         */
        public void push(Worker worker) {
            if (end < workers.length) {
                workers[end++] = worker;
            } else {
            	// 没有引用 .交给GC回收了.  所以curThreads--
                curThreads--;
            }
        }
        
        /**
         * Get the first object out of the queue. Return null if the queue
         * is empty. 
         * 返回第一个对象.没有返回null
         */
        public Worker pop() {
            if (end > 0) {
                return workers[--end];
            }
            return null;
        }
        
        /**
         * Get the first object out of the queue, Return null if the queue
         * is empty.
         */
        public Worker peek() {
            return workers[end];
        }
        
        /**
         * Is the queue empty?
         */
        public boolean isEmpty() {
            return (end == 0);
        }
        
        /**
         * How many elements are there in this queue?
         */
        public int size() {
            return (end);
        }
        
        /**
         * Resize the queue. If there are too many objects in the queue for the
         * new size, drop the excess.
         *  
         *  使用新的长度格式数组. 多的将丢弃
         * 
         * @param newSize
         */
        public void resize(int newSize) {
            Worker[] newWorkers = new Worker[newSize];
            int len = workers.length;
            if (newSize < len) {
                len = newSize;
            }
            System.arraycopy(workers, 0, newWorkers, 0, len);
            workers = newWorkers;
        }
    }

}
