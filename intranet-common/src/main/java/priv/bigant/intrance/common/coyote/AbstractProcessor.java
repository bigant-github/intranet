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

import priv.bigant.intrance.common.util.net.*;

import java.io.IOException;


/**
 * Provides functionality and attributes common to all supported protocols (currently HTTP and AJP).
 */
public abstract class AbstractProcessor {

    protected final Request request;
    protected final Response response;
    protected volatile SocketWrapperBase<?> socketWrapper = null;
    public abstract void service(SocketWrapperBase<?> socketWrapper) throws IOException;

    public AbstractProcessor() {
        this(new Request(), new Response());
    }


    protected AbstractProcessor(Request coyoteRequest, Response coyoteResponse) {
        request = coyoteRequest;
        response = coyoteResponse;
        request.setResponse(response);
    }

    public abstract void recycle();
    /**
     * Set the socket wrapper being used.
     *
     * @param socketWrapper The socket wrapper
     */
    protected final void setSocketWrapper(SocketWrapperBase<?> socketWrapper) {
        this.socketWrapper = socketWrapper;
    }


}
