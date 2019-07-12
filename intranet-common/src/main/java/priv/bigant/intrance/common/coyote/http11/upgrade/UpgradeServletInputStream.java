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
package priv.bigant.intrance.common.coyote.http11.upgrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.coyote.http11.servlet.ReadListener;
import priv.bigant.intrance.common.coyote.http11.servlet.ServletInputStream;
import priv.bigant.intrance.common.util.ExceptionUtils;
import priv.bigant.intrance.common.util.net.SocketWrapperBase;
import priv.bigant.intrance.common.util.res.StringManager;

import java.io.IOException;

public class UpgradeServletInputStream extends ServletInputStream {

    private static final Logger log = LoggerFactory.getLogger(UpgradeServletInputStream.class);
    private static final StringManager sm = StringManager.getManager(UpgradeServletInputStream.class);

    private final UpgradeProcessorBase processor;
    private final SocketWrapperBase<?> socketWrapper;

    private volatile boolean closed = false;
    private volatile boolean eof = false;
    // Start in blocking-mode
    private volatile Boolean ready = Boolean.TRUE;
    private volatile ReadListener listener = null;


    public UpgradeServletInputStream(UpgradeProcessorBase processor,
                                     SocketWrapperBase<?> socketWrapper) {
        this.processor = processor;
        this.socketWrapper = socketWrapper;
    }


    @Override
    public final boolean isFinished() {
        if (listener == null) {
            throw new IllegalStateException(
                    sm.getString("upgrade.sis.isFinished.ise"));
        }
        return eof;
    }


    @Override
    public final boolean isReady() {
        if (listener == null) {
            throw new IllegalStateException(
                    sm.getString("upgrade.sis.isReady.ise"));
        }

        if (eof || closed) {
            return false;
        }

        // If we already know the current state, return it.
        if (ready != null) {
            return ready.booleanValue();
        }

        try {
            ready = Boolean.valueOf(socketWrapper.isReadyForRead());
        } catch (IOException e) {
            onError(e);
        }
        return ready.booleanValue();
    }


    @Override
    public final int read() throws IOException {
        preReadChecks();

        return readInternal();
    }


    @Override
    public final int read(byte[] b, int off, int len) throws IOException {
        preReadChecks();

        try {
            int result = socketWrapper.read(listener == null, b, off, len);
            if (result == -1) {
                eof = true;
            }
            return result;
        } catch (IOException ioe) {
            close();
            throw ioe;
        }
    }


    @Override
    public void close() throws IOException {
        eof = true;
        closed = true;
    }


    private void preReadChecks() {
        if (listener != null && (ready == null || !ready.booleanValue())) {
            throw new IllegalStateException(sm.getString("upgrade.sis.read.ise"));
        }
        if (closed) {
            throw new IllegalStateException(sm.getString("upgrade.sis.read.closed"));
        }
        // No longer know if data is available
        ready = null;
    }


    private int readInternal() throws IOException {
        // Single byte reads for non-blocking need special handling so all
        // single byte reads run through this method.
        byte[] b = new byte[1];
        int result;
        try {
            result = socketWrapper.read(listener == null, b, 0, 1);
        } catch (IOException ioe) {
            close();
            throw ioe;
        }
        if (result == 0) {
            return -1;
        } else if (result == -1) {
            eof = true;
            return -1;
        } else {
            return b[0] & 0xFF;
        }
    }


    final void onDataAvailable() {
        try {
            if (listener == null || !socketWrapper.isReadyForRead()) {
                return;
            }
        } catch (IOException e) {
            onError(e);
        }
        ready = Boolean.TRUE;
        ClassLoader oldCL = processor.getUpgradeToken().getContextBind().bind(false, null);
        try {
            if (!eof) {
                listener.onDataAvailable();
            }
            if (eof) {
                listener.onAllDataRead();
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            onError(t);
        } finally {
            processor.getUpgradeToken().getContextBind().unbind(false, oldCL);
        }
    }


    private final void onError(Throwable t) {
        if (listener == null) {
            return;
        }
        ClassLoader oldCL = processor.getUpgradeToken().getContextBind().bind(false, null);
        try {
            listener.onError(t);
        } catch (Throwable t2) {
            ExceptionUtils.handleThrowable(t2);
            log.warn(sm.getString("upgrade.sis.onErrorFail"), t2);
        } finally {
            processor.getUpgradeToken().getContextBind().unbind(false, oldCL);
        }
        try {
            close();
        } catch (IOException ioe) {
            if (log.isDebugEnabled()) {
                log.debug(sm.getString("upgrade.sis.errorCloseFail"), ioe);
            }
        }
        ready = Boolean.FALSE;
    }


    final boolean isClosed() {
        return closed;
    }
}
