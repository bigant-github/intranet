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
package priv.bigant.intrance.common;

/**
 * Global constants that are applicable to multiple packages within Catalina.
 *
 * @author Craig R. McClanahan
 */
public final class Globals {


    /**
     * The servlet context attribute under which we store a flag used to mark this request as having been processed by
     * the SSIServlet. We do this because of the pathInfo mangling happening when using the CGIServlet in conjunction
     * with the SSI servlet. (value stored as an object of type String)
     *
     * @deprecated Unused. This is no longer used as the CGIO servlet now has generic handling for when it is used as an
     * include. This will be removed in Tomcat 10
     */
    @Deprecated
    public static final String SSI_FLAG_ATTR = "org.apache.catalina.ssi.SSIServlet";


    /**
     * Default domain for MBeans if none can be determined
     */
    public static final String DEFAULT_MBEAN_DOMAIN = "Catalina";


}
