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

import org.apache.catalina.core.StandardServer;


/**
 * <p><strong>ServerFactory</strong> allows the registration of the
 * (singleton) <code>Server</code> instance for this JVM, so that it
 * can be accessed independently of any existing reference to the
 * component hierarchy.  This is important for administration tools
 * that are built around the internal component implementation classes.
 *
 * Server工厂(其实就是单例拉).  默认是StandardServer.
 * 
 * @author Craig R. McClanahan
 * @version $Revision: 467222 $ $Date: 2006-10-24 11:17:11 +0800 (Tue, 24 Oct 2006) $
 */

public class ServerFactory {


    // ------------------------------------------------------- Static Variables


    /**
     * The singleton <code>Server</code> instance for this JVM.
     * 
     * 单例server实例
     */
    private static Server server = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Return the singleton <code>Server</code> instance for this JVM.
     */
    public static Server getServer() {
        if( server==null )
            server=new StandardServer();
        return (server);

    }


    /**
     * Set the singleton <code>Server</code> instance for this JVM.  This
     * method must <strong>only</strong> be called from a constructor of
     * the (singleton) <code>Server</code> instance that is created for
     * this execution of Catalina.
     * 
     * 设置一个单例的Server实例.  这个方法仅能被server构造调用.
     *
     * @param theServer The new singleton instance
     */
    public static void setServer(Server theServer) {

        if (server == null)
            server = theServer;

    }


}
