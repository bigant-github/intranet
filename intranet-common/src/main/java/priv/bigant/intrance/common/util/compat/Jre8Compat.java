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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.KeyStore.LoadStoreParameter;
import java.util.Collections;
import java.util.Map;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

class Jre8Compat extends JreCompat {

    private static final int RUNTIME_MAJOR_VERSION = 8;

    private static final Method setUseCipherSuitesOrderMethod;
    private static final Constructor<?> domainLoadStoreParameterConstructor;


    static {
        Method m1 = null;
        Constructor<?> c2 = null;
        try {
            // The class is Java6+...
            Class<?> clazz1 = Class.forName("javax.net.ssl.SSLParameters");
            // ...but this method is Java8+
            m1 = clazz1.getMethod("setUseCipherSuitesOrder", boolean.class);
            Class<?> clazz2 = Class.forName("java.security.DomainLoadStoreParameter");
            c2 = clazz2.getConstructor(URI.class, Map.class);
        } catch (SecurityException e) {
            // Should never happen
        } catch (NoSuchMethodException e) {
            // Expected on Java < 8
        } catch (ClassNotFoundException e) {
            // Should never happen
        }
        setUseCipherSuitesOrderMethod = m1;
        domainLoadStoreParameterConstructor = c2;
    }


    static boolean isSupported() {
        return setUseCipherSuitesOrderMethod != null;
    }


    @Override
    public void setUseServerCipherSuitesOrder(SSLEngine engine,
            boolean useCipherSuitesOrder) {
        SSLParameters sslParameters = engine.getSSLParameters();
        try {
            setUseCipherSuitesOrderMethod.invoke(sslParameters,
                    Boolean.valueOf(useCipherSuitesOrder));
            engine.setSSLParameters(sslParameters);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException(e);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvocationTargetException e) {
            throw new UnsupportedOperationException(e);
        }
    }


    @Override
    public LoadStoreParameter getDomainLoadStoreParameter(URI uri) {
        try {
            return (LoadStoreParameter) domainLoadStoreParameterConstructor.newInstance(
                    uri, Collections.EMPTY_MAP);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                InvocationTargetException e) {
            throw new UnsupportedOperationException(e);
        }
    }


    @Override
    public int jarFileRuntimeMajorVersion() {
        return RUNTIME_MAJOR_VERSION;
    }
}
