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

package priv.bigant.intrance.common.util.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.util.net.jsse.JSSEImplementation;
import priv.bigant.intrance.common.util.res.StringManager;
import sun.rmi.runtime.Log;

import javax.net.ssl.SSLSession;


/**
 * Provides a factory and base implementation for the Tomcat specific mechanism that allows alternative SSL/TLS
 * implementations to be used without requiring the implementation of a full JSSE provider.
 */
public abstract class SSLImplementation {

    private static final Logger logger = LoggerFactory.getLogger(SSLImplementation.class);
    private static final StringManager sm = StringManager.getManager(SSLImplementation.class);

    /**
     * Obtain an instance (not a singleton) of the implementation with the given class name.
     *
     * @param className The class name of the required implementation or null to use the default (currently {@link
     *                  JSSEImplementation}.
     * @return An instance of the required implementation
     * @throws ClassNotFoundException If an instance of the requested class cannot be created
     */
    public static SSLImplementation getInstance(String className)
            throws ClassNotFoundException {
        if (className == null)
            return new JSSEImplementation();

        try {
            Class<?> clazz = Class.forName(className);
            return (SSLImplementation) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            String msg = sm.getString("sslImplementation.cnfe", className);
            if (logger.isDebugEnabled()) {
                logger.debug(msg, e);
            }
            throw new ClassNotFoundException(msg, e);
        }
    }


    public abstract SSLSupport getSSLSupport(SSLSession session);

    public abstract SSLUtil getSSLUtil(SSLHostConfigCertificate certificate);

    public abstract boolean isAlpnSupported();
}
