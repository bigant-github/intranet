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
package priv.bigant.intrance.common.coyote;


import org.slf4j.Logger;
import priv.bigant.intrance.common.util.net.AbstractEndpoint.Handler.SocketState;
import priv.bigant.intrance.common.util.net.DispatchType;
import priv.bigant.intrance.common.util.net.SocketEvent;
import priv.bigant.intrance.common.util.net.SocketWrapperBase;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This is a light-weight abstract processor implementation that is intended as a basis for all Processor
 * implementations from the light-weight upgrade processors to the HTTP/AJP processors.
 */
public abstract class AbstractProcessorLight implements Processor {

    private final Set<DispatchType> dispatches = new CopyOnWriteArraySet<>();

    @Override
    public SocketState process(SocketWrapperBase<?> socketWrapper, SocketEvent status) throws IOException {
        return service(socketWrapper);
    }


    public void addDispatch(DispatchType dispatchType) {
        synchronized (dispatches) {
            dispatches.add(dispatchType);
        }
    }


    protected void clearDispatches() {
        synchronized (dispatches) {
            dispatches.clear();
        }
    }


    /**
     * Service a 'standard' HTTP request. This method is called for both new requests and for requests that have
     * partially read the HTTP request line or HTTP headers. Once the headers have been fully read this method is not
     * called again until there is a new HTTP request to process. Note that the request type may change during
     *
     * @param socketWrapper The connection to process
     * @return The state the caller should put the socket in when this method returns
     * @throws IOException If an I/O error occurs during the processing of the request
     */
    public abstract SocketState service(SocketWrapperBase<?> socketWrapper) throws IOException;

    protected abstract Logger getLog();
}
