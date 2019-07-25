package priv.bigant.intrance.common;

import javax.management.ObjectName;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

public abstract class Config {
    protected static Config config;
    /**
     * 服务器端口
     */
    private int httpAcceptPort = 45679;
    private int bufferSize = 16;
    /**
     * 线程池等待时间
     */
    private int threadKeepAliveTime = 5000;

    protected Config() {

    }

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
    protected static Integer soTimeout = 2000;

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

    public void setProperties(AsynchronousSocketChannel socket) throws IOException {
        if (rxBufSize != null)
            socket.setOption(StandardSocketOptions.SO_RCVBUF, rxBufSize);
        if (txBufSize != null)
            socket.setOption(StandardSocketOptions.SO_SNDBUF, txBufSize);
        if (soKeepAlive != null)
            socket.setOption(StandardSocketOptions.SO_KEEPALIVE, soKeepAlive);
        if (soReuseAddress != null)
            socket.setOption(StandardSocketOptions.SO_REUSEADDR, soReuseAddress);
        if (soLingerOn != null && soLingerOn && soLingerTime != null)
            socket.setOption(StandardSocketOptions.SO_LINGER, soLingerTime);
        if (tcpNoDelay != null)
            socket.setOption(StandardSocketOptions.TCP_NODELAY, tcpNoDelay);
    }

    public void setProperties(AsynchronousServerSocketChannel socket) throws IOException {
        if (rxBufSize != null)
            socket.setOption(StandardSocketOptions.SO_RCVBUF, rxBufSize);
        if (soReuseAddress != null)
            socket.setOption(StandardSocketOptions.SO_REUSEADDR, soReuseAddress);
    }

    public boolean getDirectBuffer() {
        return directBuffer;
    }

    public boolean getDirectSslBuffer() {
        return directSslBuffer;
    }

    public boolean getOoBInline() {
        return ooBInline;
    }

    public int getPerformanceBandwidth() {
        return performanceBandwidth;
    }

    public int getPerformanceConnectionTime() {
        return performanceConnectionTime;
    }

    public int getPerformanceLatency() {
        return performanceLatency;
    }

    public int getRxBufSize() {
        return rxBufSize;
    }

    public boolean getSoKeepAlive() {
        return soKeepAlive;
    }

    public boolean getSoLingerOn() {
        return soLingerOn;
    }

    public int getSoLingerTime() {
        return soLingerTime;
    }

    public boolean getSoReuseAddress() {
        return soReuseAddress;
    }

    public static int getSoTimeout() {
        return soTimeout;
    }

    public boolean getTcpNoDelay() {
        return tcpNoDelay;
    }

    public int getTxBufSize() {
        return txBufSize;
    }

    public int getBufferPool() {
        return bufferPool;
    }

    public int getBufferPoolSize() {
        return bufferPoolSize;
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

    public int getDirectBufferPool() {
        return bufferPool;
    }

    public void setPerformanceConnectionTime(int performanceConnectionTime) {
        this.performanceConnectionTime = performanceConnectionTime;
    }

    public void setTxBufSize(int txBufSize) {
        this.txBufSize = txBufSize;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public void setSoReuseAddress(boolean soReuseAddress) {
        this.soReuseAddress = soReuseAddress;
    }

    public void setSoLingerTime(int soLingerTime) {
        this.soLingerTime = soLingerTime;
    }

    public void setSoKeepAlive(boolean soKeepAlive) {
        this.soKeepAlive = soKeepAlive;
    }

    public void setRxBufSize(int rxBufSize) {
        this.rxBufSize = rxBufSize;
    }

    public void setPerformanceLatency(int performanceLatency) {
        this.performanceLatency = performanceLatency;
    }

    public void setPerformanceBandwidth(int performanceBandwidth) {
        this.performanceBandwidth = performanceBandwidth;
    }

    public void setOoBInline(boolean ooBInline) {
        this.ooBInline = ooBInline;
    }

    public void setDirectBuffer(boolean directBuffer) {
        this.directBuffer = directBuffer;
    }

    public void setDirectSslBuffer(boolean directSslBuffer) {
        this.directSslBuffer = directSslBuffer;
    }

    public void setSoLingerOn(boolean soLingerOn) {
        this.soLingerOn = soLingerOn;
    }

    public void setBufferPool(int bufferPool) {
        this.bufferPool = bufferPool;
    }

    public void setBufferPoolSize(int bufferPoolSize) {
        this.bufferPoolSize = bufferPoolSize;
    }

    public void setEventCache(int eventCache) {
        this.eventCache = eventCache;
    }

    public void setAppReadBufSize(int appReadBufSize) {
        this.appReadBufSize = appReadBufSize;
    }

    public void setAppWriteBufSize(int appWriteBufSize) {
        this.appWriteBufSize = appWriteBufSize;
    }

    public void setProcessorCache(int processorCache) {
        this.processorCache = processorCache;
    }

    public void setTimeoutInterval(long timeoutInterval) {
        this.timeoutInterval = timeoutInterval;
    }

    public void setDirectBufferPool(int directBufferPool) {
        this.bufferPool = directBufferPool;
    }

    public int getUnlockTimeout() {
        return unlockTimeout;
    }

    public void setUnlockTimeout(int unlockTimeout) {
        this.unlockTimeout = unlockTimeout;
    }

    void setObjectName(ObjectName oname) {
        this.oname = oname;
    }

    ObjectName getObjectName() {
        return oname;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public static Config getConfig() {
        return config;
    }

    public int getHttpAcceptPort() {
        return httpAcceptPort;
    }

    public void setHttpAcceptPort(int httpAcceptPort) {
        this.httpAcceptPort = httpAcceptPort;
    }

    public int getThreadKeepAliveTime() {
        return threadKeepAliveTime;
    }

    public void setThreadKeepAliveTime(int threadKeepAliveTime) {
        this.threadKeepAliveTime = threadKeepAliveTime;
    }
}
