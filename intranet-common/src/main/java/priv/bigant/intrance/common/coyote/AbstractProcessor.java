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

import priv.bigant.intrance.common.util.buf.ByteChunk;
import priv.bigant.intrance.common.util.log.UserDataHelper;
import priv.bigant.intrance.common.util.net.*;
import priv.bigant.intrance.common.util.res.StringManager;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides functionality and attributes common to all supported protocols (currently HTTP and AJP).
 */
public abstract class AbstractProcessor extends AbstractProcessorLight implements ActionHook {

    private static final StringManager sm = StringManager.getManager(AbstractProcessor.class);

    protected final AsyncStateMachine asyncStateMachine;
    protected final Request request;
    protected final Response response;
    protected volatile SocketWrapperBase<?> socketWrapper = null;


    /**
     * Error state for the request/response currently being processed.
     */
    private ErrorState errorState = ErrorState.NONE;

    protected final UserDataHelper userDataHelper;

    public AbstractProcessor() {
        this(new Request(), new Response());
    }


    protected AbstractProcessor(Request coyoteRequest, Response coyoteResponse) {
        asyncStateMachine = new AsyncStateMachine(this);
        request = coyoteRequest;
        response = coyoteResponse;
        response.setHook(this);
        request.setResponse(response);
        request.setHook(this);
        userDataHelper = new UserDataHelper(getLog());
    }

    /**
     * Update the current error state to the new error state if the new error state is more severe than the current
     * error state.
     *
     * @param errorState The error status details
     * @param t          The error which occurred
     */
    protected void setErrorState(ErrorState errorState, Throwable t) {
        //TODO response.setError();
        this.errorState = this.errorState.getMostSevere(errorState);
        // Don't change the status code for IOException since that is almost
        // certainly a client disconnect in which case it is preferable to keep
        // the original status code http://markmail.org/message/4cxpwmxhtgnrwh7n
        if (//TODO response.getStatus() < 400 &&
                !(t instanceof IOException)) {
            //TODO response.setStatus(500);
        }
        //TODO
        /*if (t != null) {
            request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, t);
        }*/
        /*if (blockIo) {
            // The error occurred on a non-container thread during async
            // processing which means not all of the necessary clean-up will
            // have been completed. Dispatch to a container thread to do the
            // clean-up. Need to do it this way to ensure that all the necessary
            // clean-up is performed.
            asyncStateMachine.asyncMustError();
            if (getLog().isDebugEnabled()) {
                getLog().debug(sm.getString("abstractProcessor.nonContainerThreadError"), t);
            }
            processSocketEvent(SocketEvent.ERROR, true);
        }*/
    }


    protected ErrorState getErrorState() {
        return errorState;
    }


    @Override
    public Request getRequest() {
        return request;
    }


    /**
     * Set the socket wrapper being used.
     *
     * @param socketWrapper The socket wrapper
     */
    protected final void setSocketWrapper(SocketWrapperBase<?> socketWrapper) {
        this.socketWrapper = socketWrapper;
    }


    /**
     * @return the socket wrapper being used.
     */
    protected final SocketWrapperBase<?> getSocketWrapper() {
        return socketWrapper;
    }


    /**
     * @return the Executor used by the underlying endpoint.
     */
    protected Executor getExecutor() {
        return null;//TODO endpoint.getExecutor();
    }


    @Override
    public final AbstractEndpoint.Handler.SocketState dispatch(SocketEvent status) throws IOException {

        if (status == SocketEvent.OPEN_WRITE /* TODO && response.getWriteListener() != null*/) {
            asyncStateMachine.asyncOperation();
            try {
                if (flushBufferedWrite()) {
                    return AbstractEndpoint.Handler.SocketState.LONG;
                }
            } catch (IOException ioe) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Unable to write async data.", ioe);
                }
                status = SocketEvent.ERROR;
                //TODO request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, ioe);
            }
        } else if (status == SocketEvent.OPEN_READ /*TODO && request.getReadListener() != null*/) {
            dispatchNonBlockingRead();
        } else if (status == SocketEvent.ERROR) {
            // An I/O error occurred on a non-container thread. This includes:
            // - read/write timeouts fired by the Poller (NIO & APR)
            // - completion handler failures in NIO2

            /*TODO if (request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) == null) {
                // Because the error did not occur on a container thread the
                // request's error attribute has not been set. If an exception
                // is available from the socketWrapper, use it to set the
                // request's error attribute here so it is visible to the error
                // handling.
                request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, socketWrapper.getError());
            }*/

            /*TODO if (request.getReadListener() != null || response.getWriteListener() != null) {
                // The error occurred during non-blocking I/O. Set the correct
                // state else the error handling will trigger an ISE.
                asyncStateMachine.asyncOperation();
            }*/
        }




        if (getErrorState().isError()) {
            request.updateCounters();
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        } else {
            request.updateCounters();
            return dispatchEndRequest();
        }
    }


    @Override
    public final void action(ActionCode actionCode, Object param) {
        switch (actionCode) {
            // 'Normal' servlet support
            case COMMIT:
            case ACK: {

                /*//TODO if (!response.isCommitted()) {
                    try {
                        // Validate and write response headers
                        prepareResponse();
                    } catch (IOException e) {
                        setErrorState(ErrorState.CLOSE_CONNECTION_NOW, e);
                    }
                }*/
                break;
            }
            case CLOSE:
            case CLIENT_FLUSH: {
                action(ActionCode.COMMIT, null);
                break;
            }
            case AVAILABLE: {
                request.setAvailable(available(Boolean.TRUE.equals(param)));
                break;
            }
            case REQ_SET_BODY_REPLAY: {
                ByteChunk body = (ByteChunk) param;
                setRequestBody(body);
                break;
            }

            // Error handling
            case IS_ERROR: {
                ((AtomicBoolean) param).set(getErrorState().isError());
                break;
            }
            case IS_IO_ALLOWED: {
                ((AtomicBoolean) param).set(getErrorState().isIoAllowed());
                break;
            }
            case CLOSE_NOW: {
                // Prevent further writes to the response
                if (param instanceof Throwable) {
                    setErrorState(ErrorState.CLOSE_NOW, (Throwable) param);
                } else {
                    setErrorState(ErrorState.CLOSE_NOW, null);
                }
                break;
            }
            case DISABLE_SWALLOW_INPUT: {
                // Aborted upload or similar.
                // No point reading the remainder of the request.
                disableSwallowRequest();
                // This is an error state. Make sure it is marked as such.
                setErrorState(ErrorState.CLOSE_CLEAN, null);
                break;
            }

            // Request attribute support
            case REQ_HOST_ADDR_ATTRIBUTE: {
                if (getPopulateRequestAttributesFromSocket() && socketWrapper != null) {
                    request.remoteAddr().setString(socketWrapper.getRemoteAddr());
                }
                break;
            }
            case REQ_HOST_ATTRIBUTE: {
                populateRequestAttributeRemoteHost();
                break;
            }
            case REQ_LOCALPORT_ATTRIBUTE: {
                if (getPopulateRequestAttributesFromSocket() && socketWrapper != null) {
                    request.setLocalPort(socketWrapper.getLocalPort());
                }
                break;
            }
            case REQ_LOCAL_ADDR_ATTRIBUTE: {
                if (getPopulateRequestAttributesFromSocket() && socketWrapper != null) {
                    request.localAddr().setString(socketWrapper.getLocalAddr());
                }
                break;
            }
            case REQ_LOCAL_NAME_ATTRIBUTE: {
                if (getPopulateRequestAttributesFromSocket() && socketWrapper != null) {
                    request.localName().setString(socketWrapper.getLocalName());
                }
                break;
            }
            case REQ_REMOTEPORT_ATTRIBUTE: {
                if (getPopulateRequestAttributesFromSocket() && socketWrapper != null) {
                    request.setRemotePort(socketWrapper.getRemotePort());
                }
                break;
            }
            // Servlet 3.0 asynchronous support
            case ASYNC_START: {
                asyncStateMachine.asyncStart((AsyncContextCallback) param);
                break;
            }
            case ASYNC_COMPLETE: {
                clearDispatches();
                if (asyncStateMachine.asyncComplete()) {
                    processSocketEvent();
                }
                break;
            }
            case ASYNC_DISPATCH: {
                if (asyncStateMachine.asyncDispatch()) {
                    processSocketEvent();
                }
                break;
            }
            case ASYNC_DISPATCHED: {
                asyncStateMachine.asyncDispatched();
                break;
            }
            case ASYNC_ERROR: {
                asyncStateMachine.asyncError();
                break;
            }
            case ASYNC_IS_ASYNC: {
                ((AtomicBoolean) param).set(asyncStateMachine.isAsync());
                break;
            }
            case ASYNC_IS_COMPLETING: {
                ((AtomicBoolean) param).set(asyncStateMachine.isCompleting());
                break;
            }
            case ASYNC_IS_DISPATCHING: {
                ((AtomicBoolean) param).set(asyncStateMachine.isAsyncDispatching());
                break;
            }
            case ASYNC_IS_ERROR: {
                ((AtomicBoolean) param).set(asyncStateMachine.isAsyncError());
                break;
            }
            case ASYNC_IS_STARTED: {
                ((AtomicBoolean) param).set(asyncStateMachine.isAsyncStarted());
                break;
            }
            case ASYNC_IS_TIMINGOUT: {
                ((AtomicBoolean) param).set(asyncStateMachine.isAsyncTimingOut());
                break;
            }
            case ASYNC_RUN: {
                asyncStateMachine.asyncRun((Runnable) param);
                break;
            }
            case ASYNC_SETTIMEOUT: {
                if (param == null) {
                    return;
                }
                break;
            }
            case ASYNC_TIMEOUT: {
                AtomicBoolean result = (AtomicBoolean) param;
                result.set(asyncStateMachine.asyncTimeout());
                break;
            }
            case ASYNC_POST_PROCESS: {
                asyncStateMachine.asyncPostProcess();
                break;
            }

            // Servlet 3.1 non-blocking I/O
            case REQUEST_BODY_FULLY_READ: {
                AtomicBoolean result = (AtomicBoolean) param;
                result.set(isRequestBodyFullyRead());
                break;
            }
            case NB_READ_INTEREST: {
                AtomicBoolean isReady = (AtomicBoolean) param;
                isReady.set(isReadyForRead());
                break;
            }
            case NB_WRITE_INTEREST: {
                AtomicBoolean isReady = (AtomicBoolean) param;
                isReady.set(isReadyForWrite());
                break;
            }
            case DISPATCH_READ: {
                addDispatch(DispatchType.NON_BLOCKING_READ);
                break;
            }
            case DISPATCH_WRITE: {
                addDispatch(DispatchType.NON_BLOCKING_WRITE);
                break;
            }
            case DISPATCH_EXECUTE: {
                executeDispatches();
                break;
            }
            // Servlet 4.0 Push requests
            case IS_PUSH_SUPPORTED: {
                AtomicBoolean result = (AtomicBoolean) param;
                result.set(isPushSupported());
                break;
            }
            case PUSH_REQUEST: {
                doPush();
                break;
            }
        }
    }


    /**
     * Perform any necessary processing for a non-blocking read before dispatching to the adapter.
     */
    protected void dispatchNonBlockingRead() {
        asyncStateMachine.asyncOperation();
    }



    @Override
    public void recycle() {
        errorState = ErrorState.NONE;
        asyncStateMachine.recycle();
    }


    protected abstract int available(boolean doRead);


    protected abstract void setRequestBody(ByteChunk body);


    protected abstract void disableSwallowRequest();


    /**
     * Processors that populate request attributes directly (e.g. AJP) should over-ride this method and return {@code
     * false}.
     *
     * @return {@code true} if the SocketWrapper should be used to populate the request attributes, otherwise {@code
     * false}.
     */
    protected boolean getPopulateRequestAttributesFromSocket() {
        return true;
    }


    /**
     * Populate the remote host request attribute. Processors (e.g. AJP) that populate this from an alternative source
     * should override this method.
     */
    protected void populateRequestAttributeRemoteHost() {
        if (getPopulateRequestAttributesFromSocket() && socketWrapper != null) {
            request.remoteHost().setString(socketWrapper.getRemoteHost());
        }
    }


    protected void processSocketEvent() {
        SocketWrapperBase<?> socketWrapper = getSocketWrapper();
        if (socketWrapper != null) {
            //socketWrapper.processSocket(event, dispatch);
        }
    }


    protected boolean isReadyForRead() {
        if (available(true) > 0) {
            return true;
        }

        if (!isRequestBodyFullyRead()) {
            registerReadInterest();
        }

        return false;
    }


    protected abstract boolean isRequestBodyFullyRead();


    protected abstract void registerReadInterest();


    protected abstract boolean isReadyForWrite();


    protected void executeDispatches() {

    }


    /**
     * {@inheritDoc} Processors that implement HTTP upgrade must override this method.
     */
    @Override
    public boolean isUpgrade() {
        return false;
    }


    /**
     * Protocols that support push should override this method and return {@code true}.
     *
     * @return {@code true} if push is supported by this processor, otherwise {@code false}.
     */
    protected boolean isPushSupported() {
        return false;
    }


    /**
     * Process a push. Processors that support push should override this method and process the provided token.
     *
     * @throws UnsupportedOperationException if the protocol does not support push
     */
    protected void doPush() {
        throw new UnsupportedOperationException(
                sm.getString("abstractProcessor.pushrequest.notsupported"));
    }


    /**
     * Flush any pending writes. Used during non-blocking writes to flush any remaining data from a previous incomplete
     * write.
     *
     * @return <code>true</code> if data remains to be flushed at the end of
     * method
     * @throws IOException If an I/O error occurs while attempting to flush the data
     */
    protected abstract boolean flushBufferedWrite() throws IOException;

    /**
     * Perform any necessary clean-up processing if the dispatch resulted in the completion of processing for the
     * current request.
     *
     * @return The state to return for the socket once the clean-up for the current request has completed
     * @throws IOException If an I/O error occurs while attempting to end the request
     */
    protected abstract AbstractEndpoint.Handler.SocketState dispatchEndRequest() throws IOException;
}
