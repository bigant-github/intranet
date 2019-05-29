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
package priv.bigant.intrance.common.coyote.http11;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.util.net.NioChannel;
import priv.bigant.intrance.common.util.net.NioEndpoint;
import sun.rmi.runtime.Log;

/**
 * Abstract the protocol implementation, including threading, etc. Processor is single threaded and specific to
 * stream-based protocols, will not fit Jk protocols like JNI.
 *
 * @author Remy Maucherat
 * @author Costin Manolache
 */
public class Http11NioProtocol extends AbstractHttp11JsseProtocol<NioChannel> {

    private static final Logger log = LoggerFactory.getLogger(Http11NioProtocol.class);


    public Http11NioProtocol() {
        super(new NioEndpoint());
    }


    @Override
    protected Logger getLog() {
        return log;
    }


    // -------------------- Pool setup --------------------

    public void setPollerThreadCount(int count) {
        ((NioEndpoint) getEndpoint()).setPollerThreadCount(count);
    }

    public int getPollerThreadCount() {
        return ((NioEndpoint) getEndpoint()).getPollerThreadCount();
    }

    public void setSelectorTimeout(long timeout) {
        ((NioEndpoint) getEndpoint()).setSelectorTimeout(timeout);
    }

    public long getSelectorTimeout() {
        return ((NioEndpoint) getEndpoint()).getSelectorTimeout();
    }

    public void setPollerThreadPriority(int threadPriority) {
        ((NioEndpoint) getEndpoint()).setPollerThreadPriority(threadPriority);
    }

    public int getPollerThreadPriority() {
        return ((NioEndpoint) getEndpoint()).getPollerThreadPriority();
    }


    // ----------------------------------------------------- JMX related methods

    @Override
    protected String getNamePrefix() {
        if (isSSLEnabled()) {
            return ("https-" + getSslImplementationShortName() + "-nio");
        } else {
            return ("http-nio");
        }
    }
}
