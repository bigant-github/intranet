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

import org.slf4j.Logger;
import priv.bigant.intrance.common.util.ExceptionUtils;
import priv.bigant.intrance.common.util.collections.SynchronizedStack;
import priv.bigant.intrance.common.util.res.StringManager;
import priv.bigant.intrance.common.util.threads.LimitLatch;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * @param <S> The type for the sockets managed by this endpoint.
 * @author Mladen Turk
 * @author Remy Maucherat
 */
public abstract class AbstractEndpoint<S> {

    // -------------------------------------------------------------- Constants

    protected static final StringManager sm = StringManager.getManager(AbstractEndpoint.class);

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


        /**
         * Release any resources associated with the given SocketWrapper.
         *
         * @param socketWrapper The socketWrapper to release resources for
         */
        public void release(SocketWrapperBase<S> socketWrapper);


    }

    protected enum BindState {
        UNBOUND, BOUND_ON_INIT, BOUND_ON_START, SOCKET_CLOSED_ON_STOP
    }


    // ----------------------------------------------------------------- Fields

    /**
     * Running state of the endpoint.
     */
    protected volatile boolean running = false;


    /**
     * Will be set to true whenever the endpoint is paused.
     */
    protected volatile boolean paused = false;


    /**
     * counter for nr of connections handled by an endpoint
     */
    private volatile LimitLatch connectionLimitLatch = null;

    /**
     * Socket properties
     */
    SocketProperties socketProperties = new SocketProperties();

    public SocketProperties getSocketProperties() {
        return socketProperties;
    }

    /**
     * External Executor based thread pool.
     */
    private Executor executor = null;

    public Executor getExecutor() {
        return executor;
    }


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

    private volatile BindState bindState = BindState.UNBOUND;


    public void setTcpNoDelay(boolean tcpNoDelay) {
        socketProperties.setTcpNoDelay(tcpNoDelay);
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


    /**
     * Socket timeout.
     *
     * @return The current socket timeout for sockets created by this endpoint
     */
    public int getConnectionTimeout() {
        return socketProperties.getSoTimeout();
    }

    public void setConnectionTimeout(int soTimeout) {
        socketProperties.setSoTimeout(soTimeout);
    }

    @Deprecated
    public void setSoTimeout(int soTimeout) {
        setConnectionTimeout(soTimeout);
    }


    /**
     * Name of the thread pool, which will be used for naming child threads.
     */
    private String name = "TP";


    /**
     * Handling of accepted sockets.
     */
    private Handler<S> handler = null;

    public Handler<S> getHandler() {
        return handler;
    }



    /**
     * Process the given SocketWrapper with the given status. Used to trigger processing as if the Poller (for those
     * endpoints that have one) selected the socket.
     *
     * @param socketWrapper The socket wrapper to process
     * @param event         The socket event to be processed
     * @param dispatch      Should the processing be performed on a new container thread
     * @return if processing was triggered successfully
     */
    public boolean processSocket(SocketWrapperBase<S> socketWrapper, SocketEvent event, boolean dispatch) {
        try {
            if (socketWrapper == null) {
                return false;
            }
            SocketProcessorBase sc = createSocketProcessor(socketWrapper, event);
            Executor executor = getExecutor();
            if (dispatch && executor != null) {
                executor.execute(sc);
            } else {
                sc.run();
            }
        } catch (RejectedExecutionException ree) {
            getLog().warn(sm.getString("endpoint.executor.fail", socketWrapper), ree);
            return false;
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            // This means we got an OOM or similar creating a thread, or that
            // the pool and its queue are full
            getLog().error(sm.getString("endpoint.process.fail"), t);
            return false;
        }
        return true;
    }


    protected abstract SocketProcessorBase<S> createSocketProcessor(SocketWrapperBase<S> socketWrapper, SocketEvent event);


    protected abstract Logger getLog();

    protected long countDownConnection() {
        LimitLatch latch = connectionLimitLatch;
        if (latch != null) {
            long result = latch.countDown();
            if (result < 0) {
                getLog().warn(sm.getString("endpoint.warn.incorrectConnectionCount"));
            }
            return result;
        } else return -1;
    }


// --Commented out by Inspection START (2020/6/1 上午10:14):
//    /**
//     * Close the server socket (to prevent further connections) if the server socket was originally bound on {@link
//     * #start()} (rather than on {@link #init()}).
//     *
//     * @see #getBindOnInit()
//     */
//    public final void closeServerSocketGraceful() {
//        if (bindState == BindState.BOUND_ON_START) {
//            bindState = BindState.SOCKET_CLOSED_ON_STOP;
//            try {
//                doCloseServerSocket();
//            } catch (IOException ioe) {
//                getLog().warn(sm.getString("endpoint.serverSocket.closeFailed", getName()), ioe);
//            }
//        }
//    }
// --Commented out by Inspection STOP (2020/6/1 上午10:14)


}

