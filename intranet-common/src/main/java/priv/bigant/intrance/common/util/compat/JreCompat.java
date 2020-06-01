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
package priv.bigant.intrance.common.util.compat;

import priv.bigant.intrance.common.util.res.StringManager;

import java.net.URI;
import java.security.KeyStore.LoadStoreParameter;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;


/**
 * This is the base implementation class for JRE compatibility and provides an
 * implementation based on Java 7. Sub-classes may extend this class and provide
 * alternative implementations for later JRE versions
 */
public class JreCompat {

    private static final JreCompat instance;
    private static StringManager sm = StringManager.getManager(JreCompat.class.getPackage().getName());
    private static final boolean jre9Available;
    private static final boolean jre8Available;


    static {
        // This is Tomcat 8 with a minimum Java version of Java 7. The latest
        // Java version the optional features require is Java 9.
        // Look for the highest supported JVM first
        if (Jre9Compat.isSupported()) {
            instance = new Jre9Compat();
            jre9Available = true;
            jre8Available = true;
        }
        else if (Jre8Compat.isSupported()) {
            instance = new Jre8Compat();
            jre9Available = false;
            jre8Available = true;
        } else {
            instance = new JreCompat();
            jre9Available = false;
            jre8Available = false;
        }
    }


    public static JreCompat getInstance() {
        return instance;
    }


    // Java 7 implementation of Java 8 methods

    public static boolean isJre8Available() {
        return jre8Available;
    }


    @SuppressWarnings("unused")
    public void setUseServerCipherSuitesOrder(SSLEngine engine, boolean useCipherSuitesOrder) {
        throw new UnsupportedOperationException(sm.getString("jreCompat.noServerCipherSuiteOrder"));
    }


    @SuppressWarnings("unused")
    public LoadStoreParameter getDomainLoadStoreParameter(URI uri) {
        throw new UnsupportedOperationException(sm.getString("jreCompat.noDomainLoadStoreParameter"));
    }


    // Java 7 implementation of Java 9 methods

    public static boolean isJre9Available() {
        return jre9Available;
    }


}
