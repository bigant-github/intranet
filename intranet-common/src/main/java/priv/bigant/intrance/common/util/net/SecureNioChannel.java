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

import priv.bigant.intrance.common.util.buf.ByteBufferUtils;
import priv.bigant.intrance.common.util.res.StringManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;


/**
 * Implementation of a secure socket channel
 */
public class SecureNioChannel extends NioChannel {

    private static final StringManager sm = StringManager.getManager(SecureNioChannel.class);

    // Value determined by observation of what the SSL Engine requested in
    // various scenarios
    private static final int DEFAULT_NET_BUFFER_SIZE = 16921;

    protected ByteBuffer netInBuffer;
    protected ByteBuffer netOutBuffer;

    protected SSLEngine sslEngine;

    protected boolean sniComplete = false;

    protected boolean handshakeComplete = false;
    protected HandshakeStatus handshakeStatus; //gets set by handshake

    protected boolean closed = false;
    protected boolean closing = false;

    protected NioSelectorPool pool;
    private final NioEndpoint endpoint;

    public SecureNioChannel(SocketChannel channel, SocketBufferHandler bufHandler, NioSelectorPool pool, NioEndpoint endpoint) {
        super(channel, bufHandler);
        // Create the network buffers (these hold the encrypted data).
        if (endpoint.getSocketProperties().getDirectSslBuffer()) {
            netInBuffer = ByteBuffer.allocateDirect(DEFAULT_NET_BUFFER_SIZE);
            netOutBuffer = ByteBuffer.allocateDirect(DEFAULT_NET_BUFFER_SIZE);
        } else {
            netInBuffer = ByteBuffer.allocate(DEFAULT_NET_BUFFER_SIZE);
            netOutBuffer = ByteBuffer.allocate(DEFAULT_NET_BUFFER_SIZE);
        }

        // selector pool for blocking operations
        this.pool = pool;
        this.endpoint = endpoint;
    }

    @Override
    public void free() {
        super.free();
        if (endpoint.getSocketProperties().getDirectSslBuffer()) {
            ByteBufferUtils.cleanDirectBuffer(netInBuffer);
            ByteBufferUtils.cleanDirectBuffer(netOutBuffer);
        }
    }

//===========================================================================================
//                  NIO SSL METHODS
//===========================================================================================

    /**
     * Flush the channel.
     *
     * @param block   Should a blocking write be used?
     * @param s       The selector to use for blocking, if null then a busy write will be initiated
     * @param timeout The timeout for this write operation in milliseconds, -1 means no timeout
     * @return <code>true</code> if the network buffer has been flushed out and
     * is empty else <code>false</code>
     * @throws IOException If an I/O error occurs during the operation
     */
    @Override
    public boolean flush(boolean block, Selector s, long timeout) throws IOException {
        if (!block) {
            flush(netOutBuffer);
        } else {
            pool.write(netOutBuffer, this, s, timeout, block);
        }
        return !netOutBuffer.hasRemaining();
    }

    /**
     * Flushes the buffer to the network, non blocking
     *
     * @param buf ByteBuffer
     * @return boolean true if the buffer has been emptied out, false otherwise
     * @throws IOException An IO error occurred writing data
     */
    protected boolean flush(ByteBuffer buf) throws IOException {
        int remaining = buf.remaining();
        if (remaining > 0) {
            int written = sc.write(buf);
            return written >= remaining;
        } else {
            return true;
        }
    }


    /**
     * Executes all the tasks needed on the same thread.
     *
     * @return the status
     */
    protected HandshakeStatus tasks() {
        Runnable r = null;
        while ((r = sslEngine.getDelegatedTask()) != null) {
            r.run();
        }
        return sslEngine.getHandshakeStatus();
    }

    /**
     * Performs the WRAP function
     *
     * @param doWrite boolean
     * @return the result
     * @throws IOException An IO error occurred
     */
    protected SSLEngineResult handshakeWrap(boolean doWrite) throws IOException {
        //this should never be called with a network buffer that contains data
        //so we can clear it here.
        netOutBuffer.clear();
        //perform the wrap
        getBufHandler().configureWriteBufferForRead();
        SSLEngineResult result = sslEngine.wrap(getBufHandler().getWriteBuffer(), netOutBuffer);
        //prepare the results to be written
        netOutBuffer.flip();
        //set the status
        handshakeStatus = result.getHandshakeStatus();
        //optimization, if we do have a writable channel, write it now
        if (doWrite) flush(netOutBuffer);
        return result;
    }

    /**
     * Perform handshake unwrap
     *
     * @param doread boolean
     * @return the result
     * @throws IOException An IO error occurred
     */
    protected SSLEngineResult handshakeUnwrap(boolean doread) throws IOException {

        if (netInBuffer.position() == netInBuffer.limit()) {
            //clear the buffer if we have emptied it out on data
            netInBuffer.clear();
        }
        if (doread) {
            //if we have data to read, read it
            int read = sc.read(netInBuffer);
            if (read == -1) throw new IOException(sm.getString("channel.nio.ssl.eofDuringHandshake"));
        }
        SSLEngineResult result;
        boolean cont = false;
        //loop while we can perform pure SSLEngine data
        do {
            //prepare the buffer with the incoming data
            netInBuffer.flip();
            //call unwrap
            getBufHandler().configureReadBufferForWrite();
            result = sslEngine.unwrap(netInBuffer, getBufHandler().getReadBuffer());
            //compact the buffer, this is an optional method, wonder what would happen if we didn't
            netInBuffer.compact();
            //read in the status
            handshakeStatus = result.getHandshakeStatus();
            if (result.getStatus() == Status.OK &&
                    result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                //execute tasks if we need to
                handshakeStatus = tasks();
            }
            //perform another unwrap?
            cont = result.getStatus() == Status.OK &&
                    handshakeStatus == HandshakeStatus.NEED_UNWRAP;
        } while (cont);
        return result;
    }

    /**
     * Sends a SSL close message, will not physically close the connection here.
     * <br>To close the connection, you could do something like
     * <pre><code>
     *   close();
     *   while (isOpen() &amp;&amp; !myTimeoutFunction()) Thread.sleep(25);
     *   if ( isOpen() ) close(true); //forces a close if you timed out
     * </code></pre>
     *
     * @throws IOException if an I/O error occurs
     * @throws IOException if there is data on the outgoing network buffer and we are unable to flush it
     */
    @Override
    public void close() throws IOException {
        if (closing) return;
        closing = true;
        sslEngine.closeOutbound();

        if (!flush(netOutBuffer)) {
            throw new IOException(sm.getString("channel.nio.ssl.remainingDataDuringClose"));
        }
        //prep the buffer for the close message
        netOutBuffer.clear();
        //perform the close, since we called sslEngine.closeOutbound
        SSLEngineResult handshake = sslEngine.wrap(getEmptyBuf(), netOutBuffer);
        //we should be in a close state
        if (handshake.getStatus() != Status.CLOSED) {
            throw new IOException(sm.getString("channel.nio.ssl.invalidCloseState"));
        }
        //prepare the buffer for writing
        netOutBuffer.flip();
        //if there is data to be written
        flush(netOutBuffer);

        //is the channel closed?
        closed = (!netOutBuffer.hasRemaining() && (handshake.getHandshakeStatus() != HandshakeStatus.NEED_WRAP));
    }


    @Override
    public void close(boolean force) throws IOException {
        try {
            close();
        } finally {
            if (force || closed) {
                closed = true;
                sc.socket().close();
                sc.close();
            }
        }
    }


    /**
     * Reads a sequence of bytes from this channel into the given buffer.
     *
     * @param dst The buffer into which bytes are to be transferred
     * @return The number of bytes read, possibly zero, or <tt>-1</tt> if the channel has reached end-of-stream
     * @throws IOException              If some other I/O error occurs
     * @throws IllegalArgumentException if the destination buffer is different than getBufHandler().getReadBuffer()
     */
    @Override
    public int read(ByteBuffer dst) throws IOException {
        //are we in the middle of closing or closed?
        if (closing || closed) return -1;
        //did we finish our handshake?
        if (!handshakeComplete) throw new IllegalStateException(sm.getString("channel.nio.ssl.incompleteHandshake"));

        //read from the network
        int netread = sc.read(netInBuffer);
        //did we reach EOF? if so send EOF up one layer.
        if (netread == -1) return -1;

        //the data read
        int read = 0;
        //the SSL engine result
        SSLEngineResult unwrap;
        do {
            //prepare the buffer
            netInBuffer.flip();
            //unwrap the data
            unwrap = sslEngine.unwrap(netInBuffer, dst);
            //compact the buffer
            netInBuffer.compact();

            if (unwrap.getStatus() == Status.OK || unwrap.getStatus() == Status.BUFFER_UNDERFLOW) {
                //we did receive some data, add it to our total
                read += unwrap.bytesProduced();
                //perform any tasks if needed
                if (unwrap.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                    tasks();
                }
                //if we need more network data, then bail out for now.
                if (unwrap.getStatus() == Status.BUFFER_UNDERFLOW) {
                    break;
                }
            } else if (unwrap.getStatus() == Status.BUFFER_OVERFLOW) {
                if (read > 0) {
                    // Buffer overflow can happen if we have read data. Return
                    // so the destination buffer can be emptied before another
                    // read is attempted
                    break;
                } else {
                    // The SSL session has increased the required buffer size
                    // since the buffer was created.
                    if (dst == getBufHandler().getReadBuffer()) {
                        // This is the normal case for this code
                        getBufHandler().expand(sslEngine.getSession().getApplicationBufferSize());
                        dst = getBufHandler().getReadBuffer();
                    } else if (dst == getAppReadBufHandler().getByteBuffer()) {
                        getAppReadBufHandler()
                                .expand(sslEngine.getSession().getApplicationBufferSize());
                        dst = getAppReadBufHandler().getByteBuffer();
                    } else {
                        // Can't expand the buffer as there is no way to signal
                        // to the caller that the buffer has been replaced.
                        throw new IOException(
                                sm.getString("channel.nio.ssl.unwrapFailResize", unwrap.getStatus()));
                    }
                }
            } else {
                // Something else went wrong
                throw new IOException(sm.getString("channel.nio.ssl.unwrapFail", unwrap.getStatus()));
            }
        } while (netInBuffer.position() != 0); //continue to unwrapping as long as the input buffer has stuff
        return read;
    }

    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * @param src The buffer from which bytes are to be retrieved
     * @return The number of bytes written, possibly zero
     * @throws IOException If some other I/O error occurs
     */
    @Override
    public int write(ByteBuffer src) throws IOException {
        checkInterruptStatus();
        if (src == this.netOutBuffer) {
            //we can get here through a recursive call
            //by using the NioBlockingSelector
            int written = sc.write(src);
            return written;
        } else {
            // Are we closing or closed?
            if (closing || closed) {
                throw new IOException(sm.getString("channel.nio.ssl.closing"));
            }

            if (!flush(netOutBuffer)) {
                // We haven't emptied out the buffer yet
                return 0;
            }

            // The data buffer is empty, we can reuse the entire buffer.
            netOutBuffer.clear();

            SSLEngineResult result = sslEngine.wrap(src, netOutBuffer);
            // The number of bytes written
            int written = result.bytesConsumed();
            netOutBuffer.flip();

            if (result.getStatus() == Status.OK) {
                if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) tasks();
            } else {
                throw new IOException(sm.getString("channel.nio.ssl.wrapFail", result.getStatus()));
            }

            // Force a flush
            flush(netOutBuffer);

            return written;
        }
    }

    @Override
    public int getOutboundRemaining() {
        return netOutBuffer.remaining();
    }

    @Override
    public boolean flushOutbound() throws IOException {
        int remaining = netOutBuffer.remaining();
        flush(netOutBuffer);
        int remaining2 = netOutBuffer.remaining();
        return remaining2 < remaining;
    }

    public SSLEngine getSslEngine() {
        return sslEngine;
    }

    public ByteBuffer getEmptyBuf() {
        return emptyBuf;
    }
}
