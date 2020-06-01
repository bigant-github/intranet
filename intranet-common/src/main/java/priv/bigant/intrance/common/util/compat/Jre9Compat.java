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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.util.jar.JarFile;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;


class Jre9Compat extends Jre8Compat {

    private static final Class<?> inaccessibleObjectExceptionClazz;
    private static final Method setApplicationProtocolsMethod;

    static {
        Class<?> c1 = null;
        Method m2 = null;
        Method m3 = null;
        Method m4 = null;
        Method m5 = null;
        Method m6 = null;
        Method m7 = null;
        Method m8 = null;
        Method m9 = null;
        Method m10 = null;
        Method m11 = null;
        Constructor<JarFile> c12 = null;
        Method m13 = null;
        Object o14 = null;
        Object o15 = null;

        try {
            Class<?> moduleLayerClazz = Class.forName("java.lang.ModuleLayer");
            Class<?> configurationClazz = Class.forName("java.lang.module.Configuration");
            Class<?> resolvedModuleClazz = Class.forName("java.lang.module.ResolvedModule");
            Class<?> moduleReferenceClazz = Class.forName("java.lang.module.ModuleReference");
            Class<?> optionalClazz = Class.forName("java.util.Optional");
            Class<?> versionClazz = Class.forName("java.lang.Runtime$Version");
            Method runtimeVersionMethod = JarFile.class.getMethod("runtimeVersion");
            Method majorMethod = versionClazz.getMethod("major");

            c1 = Class.forName("java.lang.reflect.InaccessibleObjectException");
            m2 = SSLParameters.class.getMethod("setApplicationProtocols", String[].class);
            m3 = SSLEngine.class.getMethod("getApplicationProtocol");
            m4 = URLConnection.class.getMethod("setDefaultUseCaches", String.class, boolean.class);
            m5 = moduleLayerClazz.getMethod("boot");
            m6 = moduleLayerClazz.getMethod("configuration");
            m7 = configurationClazz.getMethod("modules");
            m8 = resolvedModuleClazz.getMethod("reference");
            m9 = moduleReferenceClazz.getMethod("location");
            m10 = optionalClazz.getMethod("isPresent");
            m11 = optionalClazz.getMethod("get");
            c12 = JarFile.class.getConstructor(File.class, boolean.class, int.class, versionClazz);
            m13 = JarFile.class.getMethod("isMultiRelease");
            o14 = runtimeVersionMethod.invoke(null);
            o15 = majorMethod.invoke(o14);

        } catch (ClassNotFoundException e) {
            // Must be Java 8
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            // Should never happen
        }

        inaccessibleObjectExceptionClazz = c1;
        setApplicationProtocolsMethod = m2;

    }


    static boolean isSupported() {
        return inaccessibleObjectExceptionClazz != null;
    }


}
