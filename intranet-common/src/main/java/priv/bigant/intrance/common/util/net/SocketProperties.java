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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

import javax.management.ObjectName;

/**
 * Properties that can be set in the &lt;ServerConnector&gt; element in server.xml. All properties are prefixed with
 * &quot;socket.&quot; and are currently only working for the Nio connector
 */
public class SocketProperties {

    /**
     * Enable/disable socket processor cache, this bounded cache stores SocketProcessor objects to reduce GC Default is
     * 500 -1 is unlimited 0 is disabled
     */
    protected int processorCache = 500;

    /**
     * Enable/disable poller event cache, this bounded cache stores PollerEvent objects to reduce GC for the poller
     * Default is 500 -1 is unlimited 0 is disabled &gt;0 the max number of objects to keep in cache.
     */
    protected int eventCache = 500;

    /**
     * Enable/disable direct buffers for the network buffers Default value is disabled
     */
    protected boolean directBuffer = false;

    /**
     * Enable/disable direct buffers for the network buffers for SSL Default value is disabled
     */
    protected boolean directSslBuffer = false;

    /**
     * Socket receive buffer size in bytes (SO_RCVBUF). JVM default used if not set.
     */
    protected Integer rxBufSize = null;

    /**
     * Socket send buffer size in bytes (SO_SNDBUF). JVM default used if not set.
     */
    protected Integer txBufSize = null;

    /**
     * The application read buffer size in bytes. Default value is rxBufSize
     */
    protected int appReadBufSize = 8192;

    /**
     * The application write buffer size in bytes Default value is txBufSize
     */
    protected int appWriteBufSize = 8192;

    /**
     * NioChannel pool size for the endpoint, this value is how many channels -1 means unlimited cached, 0 means no
     * cache Default value is 500
     */
    protected int bufferPool = 500;

    /**
     * Buffer pool size in bytes to be cached -1 means unlimited, 0 means no cache Default value is 100MB (1024*1024*100
     * bytes)
     */
    protected int bufferPoolSize = 1024 * 1024 * 100;

    /**
     * TCP_NO_DELAY option. JVM default used if not set.
     */
    protected Boolean tcpNoDelay = Boolean.TRUE;

    /**
     * SO_KEEPALIVE option. JVM default used if not set.
     */
    protected Boolean soKeepAlive = null;

    /**
     * OOBINLINE option. JVM default used if not set.
     */
    protected Boolean ooBInline = null;

    /**
     * SO_REUSEADDR option. JVM default used if not set.
     */
    protected Boolean soReuseAddress = null;

    /**
     * SO_LINGER option, paired with the <code>soLingerTime</code> value. JVM defaults used unless both attributes are
     * set.
     */
    protected Boolean soLingerOn = null;

    /**
     * SO_LINGER option, paired with the <code>soLingerOn</code> value. JVM defaults used unless both attributes are
     * set.
     */
    protected Integer soLingerTime = null;

    /**
     * SO_TIMEOUT option. default is 20000.
     */
    protected Integer soTimeout = Integer.valueOf(20000);

    /**
     * Performance preferences according to http://docs.oracle.com/javase/1.5.0/docs/api/java/net/Socket.html#setPerformancePreferences(int,%20int,%20int)
     * All three performance attributes must be set or the JVM defaults will be used.
     */
    protected Integer performanceConnectionTime = null;

    /**
     * Performance preferences according to http://docs.oracle.com/javase/1.5.0/docs/api/java/net/Socket.html#setPerformancePreferences(int,%20int,%20int)
     * All three performance attributes must be set or the JVM defaults will be used.
     */
    protected Integer performanceLatency = null;

    /**
     * Performance preferences according to http://docs.oracle.com/javase/1.5.0/docs/api/java/net/Socket.html#setPerformancePreferences(int,%20int,%20int)
     * All three performance attributes must be set or the JVM defaults will be used.
     */
    protected Integer performanceBandwidth = null;

    /**
     * The minimum frequency of the timeout interval to avoid excess load from the poller during high traffic
     */
    protected long timeoutInterval = 1000;

    /**
     * Timeout in milliseconds for an unlock to take place.
     */
    protected int unlockTimeout = 250;

    private ObjectName oname = null;


    public void setProperties(Socket socket) throws SocketException {
        if (rxBufSize != null)
            socket.setReceiveBufferSize(rxBufSize);
        if (txBufSize != null)
            socket.setSendBufferSize(txBufSize);
        if (ooBInline != null)
            socket.setOOBInline(ooBInline);
        if (soKeepAlive != null)
            socket.setKeepAlive(soKeepAlive);
        if (performanceConnectionTime != null && performanceLatency != null && performanceBandwidth != null)
            socket.setPerformancePreferences(performanceConnectionTime, performanceLatency, performanceBandwidth);
        if (soReuseAddress != null)
            socket.setReuseAddress(soReuseAddress);
        if (soLingerOn != null && soLingerTime != null)
            socket.setSoLinger(soLingerOn, soLingerTime);
        if (soTimeout != null && soTimeout >= 0)
            socket.setSoTimeout(soTimeout);
        if (tcpNoDelay != null)
            socket.setTcpNoDelay(tcpNoDelay);
    }

    public void setProperties(ServerSocket socket) throws SocketException {
        if (rxBufSize != null)
            socket.setReceiveBufferSize(rxBufSize);
        if (performanceConnectionTime != null && performanceLatency != null && performanceBandwidth != null)
            socket.setPerformancePreferences(performanceConnectionTime, performanceLatency, performanceBandwidth);
        if (soReuseAddress != null)
            socket.setReuseAddress(soReuseAddress);
        if (soTimeout != null && soTimeout >= 0)
            socket.setSoTimeout(soTimeout);
    }

    public boolean getDirectBuffer() {
        return directBuffer;
    }

    public boolean getDirectSslBuffer() {
        return directSslBuffer;
    }

    public boolean getSoLingerOn() {
        return soLingerOn;
    }

    public int getSoLingerTime() {
        return soLingerTime;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public int getBufferPool() {
        return bufferPool;
    }

    public int getEventCache() {
        return eventCache;
    }

    public int getAppReadBufSize() {
        return appReadBufSize;
    }

    public int getAppWriteBufSize() {
        return appWriteBufSize;
    }

    public int getProcessorCache() {
        return processorCache;
    }

    public long getTimeoutInterval() {
        return timeoutInterval;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public void setSoLingerTime(int soLingerTime) {
        this.soLingerTime = soLingerTime;
    }

    public void setSoLingerOn(boolean soLingerOn) {
        this.soLingerOn = soLingerOn;
    }

    public int getUnlockTimeout() {
        return unlockTimeout;
    }

    void setObjectName(ObjectName oname) {
        this.oname = oname;
    }

    ObjectName getObjectName() {
        return oname;
    }
}
