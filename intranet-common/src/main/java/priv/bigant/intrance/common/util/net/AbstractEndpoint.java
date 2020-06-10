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
package priv.bigant.intrance.common.util.net;


/**
 * @param <S> The type for the sockets managed by this endpoint.
 * @author Mladen Turk
 * @author Remy Maucherat
 */
public abstract class AbstractEndpoint<S> {

    // -------------------------------------------------------------- Constants

    protected AbstractEndpoint() {
    }

    public interface Handler<S> {

        /**
         * Different types of socket states to react upon.
         */
        public enum SocketState {
            // TODO Add a new state to the AsyncStateMachine and remove
            //      ASYNC_END (if possible)
            OPEN, CLOSED, LONG, ASYNC_END, SENDFILE, UPGRADING, UPGRADED, SUSPENDED
        }


    }

    protected enum BindState {
        UNBOUND, BOUND_ON_INIT, BOUND_ON_START, SOCKET_CLOSED_ON_STOP
    }


    // ----------------------------------------------------------------- Fields


    /**
     * Socket properties
     */
    private SocketProperties socketProperties = new SocketProperties();


    /**
     * Allows the server developer to specify the acceptCount (backlog) that should be used for server sockets. By
     * default, this value is 100.
     */
    private int acceptCount = 100;

    public void setAcceptCount(int acceptCount) {
        if (acceptCount > 0) this.acceptCount = acceptCount;
    }

    public int getAcceptCount() {
        return acceptCount;
    }

    @Deprecated
    public void setBacklog(int backlog) {
        setAcceptCount(backlog);
    }

    @Deprecated
    public int getBacklog() {
        return getAcceptCount();
    }

    /**
     * Socket linger.
     *
     * @return The current socket linger time for sockets created by this endpoint
     */
    public int getConnectionLinger() {
        return socketProperties.getSoLingerTime();
    }

    public void setConnectionLinger(int connectionLinger) {
        socketProperties.setSoLingerTime(connectionLinger);
        socketProperties.setSoLingerOn(connectionLinger >= 0);
    }

    @Deprecated
    public int getSoLinger() {
        return getConnectionLinger();
    }

    @Deprecated
    public void setSoLinger(int soLinger) {
        setConnectionLinger(soLinger);
    }


    public void setConnectionTimeout(int soTimeout) {
        socketProperties.setSoTimeout(soTimeout);
    }

    @Deprecated
    public void setSoTimeout(int soTimeout) {
        setConnectionTimeout(soTimeout);
    }


    /**
     * Handling of accepted sockets.
     */
    private Handler<S> handler = null;

    public Handler<S> getHandler() {
        return handler;
    }

}

