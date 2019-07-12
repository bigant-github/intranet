package priv.bigant.intrance.common.util.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.util.net.NioEndpoint.Poller;
import priv.bigant.intrance.common.util.net.jsse.JSSESupport;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NioSocketWrapper extends SocketWrapperBase<NioChannel> {
    public static final Logger LOG = LoggerFactory.getLogger(NioSocketWrapper.class);
    private final NioSelectorPool pool;

    private Poller poller = null;
    private int interestOps = 0;
    private CountDownLatch readLatch = null;
    private CountDownLatch writeLatch = null;
    private volatile NioEndpoint.SendfileData sendfileData = null;
    private volatile long lastRead = System.currentTimeMillis();
    private volatile long lastWrite = lastRead;
    public volatile boolean closed = false;

    public NioSocketWrapper(NioChannel channel, NioSelectorPool pool) {
        super(channel);
        this.pool = pool;
        socketBufferHandler = channel.getBufHandler();
    }

    public Poller getPoller() {
        return poller;
    }

    public void setPoller(Poller poller) {
        this.poller = poller;
    }

    public int interestOps() {
        return interestOps;
    }

    public int interestOps(int ops) {
        this.interestOps = ops;
        return ops;
    }

    public CountDownLatch getReadLatch() {
        return readLatch;
    }

    public CountDownLatch getWriteLatch() {
        return writeLatch;
    }

    protected CountDownLatch resetLatch(CountDownLatch latch) {
        if (latch == null || latch.getCount() == 0) return null;
        else throw new IllegalStateException("Latch must be at count 0");
    }

    public void resetReadLatch() {
        readLatch = resetLatch(readLatch);
    }

    public void resetWriteLatch() {
        writeLatch = resetLatch(writeLatch);
    }

    protected CountDownLatch startLatch(CountDownLatch latch, int cnt) {
        if (latch == null || latch.getCount() == 0) {
            return new CountDownLatch(cnt);
        } else throw new IllegalStateException("Latch must be at count 0 or null.");
    }

    public void startReadLatch(int cnt) {
        readLatch = startLatch(readLatch, cnt);
    }

    public void startWriteLatch(int cnt) {
        writeLatch = startLatch(writeLatch, cnt);
    }

    protected void awaitLatch(CountDownLatch latch, long timeout, TimeUnit unit) throws InterruptedException {
        if (latch == null) throw new IllegalStateException("Latch cannot be null");
        // Note: While the return value is ignored if the latch does time
        //       out, logic further up the call stack will trigger a
        //       SocketTimeoutException
        latch.await(timeout, unit);
    }

    public void awaitReadLatch(long timeout, TimeUnit unit) throws InterruptedException {
        awaitLatch(readLatch, timeout, unit);
    }

    public void awaitWriteLatch(long timeout, TimeUnit unit) throws InterruptedException {
        awaitLatch(writeLatch, timeout, unit);
    }

    public void setSendfileData(NioEndpoint.SendfileData sf) {
        this.sendfileData = sf;
    }

    public NioEndpoint.SendfileData getSendfileData() {
        return this.sendfileData;
    }

    public void updateLastWrite() {
        lastWrite = System.currentTimeMillis();
    }

    public long getLastWrite() {
        return lastWrite;
    }

    public void updateLastRead() {
        lastRead = System.currentTimeMillis();
    }

    public long getLastRead() {
        return lastRead;
    }


    @Override
    public boolean isReadyForRead() throws IOException {
        socketBufferHandler.configureReadBufferForRead();

        if (socketBufferHandler.getReadBuffer().remaining() > 0) {
            return true;
        }

        fillReadBuffer(false);

        boolean isReady = socketBufferHandler.getReadBuffer().position() > 0;
        return isReady;
    }


    @Override
    public int read(boolean block, byte[] b, int off, int len) throws IOException {
        int nRead = populateReadBuffer(b, off, len);
        if (nRead > 0) {
            return nRead;
            /*
             * Since more bytes may have arrived since the buffer was last
             * filled, it is an option at this point to perform a
             * non-blocking read. However correctly handling the case if
             * that read returns end of stream adds complexity. Therefore,
             * at the moment, the preference is for simplicity.
             */
        }

        // Fill the read buffer as best we can.
        nRead = fillReadBuffer(block);
        updateLastRead();

        // Fill as much of the remaining byte array as possible with the
        // data that was just read
        if (nRead > 0) {
            socketBufferHandler.configureReadBufferForRead();
            nRead = Math.min(nRead, len);
            socketBufferHandler.getReadBuffer().get(b, off, nRead);
        }
        return nRead;
    }


    @Override
    public int read(boolean block, ByteBuffer to) throws IOException {
        int nRead = populateReadBuffer(to);
        if (nRead > 0) {
            return nRead;
            /*
             * Since more bytes may have arrived since the buffer was last
             * filled, it is an option at this point to perform a
             * non-blocking read. However correctly handling the case if
             * that read returns end of stream adds complexity. Therefore,
             * at the moment, the preference is for simplicity.
             */
        }

        // The socket read buffer capacity is socket.appReadBufSize
        int limit = socketBufferHandler.getReadBuffer().capacity();
        if (to.remaining() >= limit) {
            to.limit(to.position() + limit);
            nRead = fillReadBuffer(block, to);
            /*if (LOG.isDebugEnabled()) {
                LOG.debug("Socket: [" + this + "], Read direct from socket: [" + nRead + "]");
            }*/
            updateLastRead();
        } else {
            // Fill the read buffer as best we can.
            nRead = fillReadBuffer(block);
            /*if (LOG.isDebugEnabled()) {
                LOG.debug("Socket: [" + this + "], Read into buffer: [" + nRead + "]");
            }*/
            updateLastRead();

            // Fill as much of the remaining byte array as possible with the
            // data that was just read
            if (nRead > 0) {
                nRead = populateReadBuffer(to);
            }
        }
        return nRead;
    }


    @Override
    public void close() throws IOException {
        getSocket().close();
    }


    @Override
    public boolean isClosed() {
        return closed;
    }


    private int fillReadBuffer(boolean block) throws IOException {
        socketBufferHandler.configureReadBufferForWrite();
        return fillReadBuffer(block, socketBufferHandler.getReadBuffer());
    }


    private int fillReadBuffer(boolean block, ByteBuffer to) throws IOException {
        int nRead;
        NioChannel channel = getSocket();
        if (block) {
            Selector selector = null;
            try {
                selector = pool.get();
            } catch (IOException x) {
                // Ignore
            }
            try {
                NioSocketWrapper att = this;//TODO (NioSocketWrapper) channel.getAttachment();
                nRead = pool.read(to, channel, selector, att.getReadTimeout());
            } finally {
                if (selector != null) {
                    pool.put(selector);
                }
            }
        } else {
            nRead = channel.read(to);
            if (nRead == -1) {
                throw new EOFException();
            }
        }
        return nRead;
    }


    @Override
    protected void doWrite(boolean block, ByteBuffer from) throws IOException {
        long writeTimeout = getWriteTimeout();
        Selector selector = null;
        try {
            selector = pool.get();
        } catch (IOException x) {
            // Ignore
        }
        try {
            pool.write(from, getSocket(), selector, writeTimeout, block);
            if (block) {
                // Make sure we are flushed
                do {
                    if (getSocket().flush(true, selector, writeTimeout)) {
                        break;
                    }
                } while (true);
            }
            updateLastWrite();
        } finally {
            if (selector != null) {
                pool.put(selector);
            }
        }
        // If there is data left in the buffer the socket will be registered for
        // write further up the stack. This is to ensure the socket is only
        // registered for write once as both container and user code can trigger
        // write registration.
    }


    @Override
    public void registerReadInterest() {
        getPoller().add(getSocket(), SelectionKey.OP_READ);
    }


    @Override
    public void registerWriteInterest() {
        getPoller().add(getSocket(), SelectionKey.OP_WRITE);
    }


    @Override
    public SendfileDataBase createSendfileData(String filename, long pos, long length) {
        return new NioEndpoint.SendfileData(filename, pos, length);
    }


    @Override
    public SendfileState processSendfile(SendfileDataBase sendfileData) {
        setSendfileData((NioEndpoint.SendfileData) sendfileData);
        SelectionKey key = getSocket().getIOChannel().keyFor(getSocket().getPoller().getSelector());
        // Might as well do the first write on this thread
        return getSocket().getPoller().processSendfile(key, this, true);
    }


    @Override
    protected void populateRemoteAddr() {
        InetAddress inetAddr = getSocket().getIOChannel().socket().getInetAddress();
        if (inetAddr != null) {
            remoteAddr = inetAddr.getHostAddress();
        }
    }


    @Override
    protected void populateRemoteHost() {
        InetAddress inetAddr = getSocket().getIOChannel().socket().getInetAddress();
        if (inetAddr != null) {
            remoteHost = inetAddr.getHostName();
            if (remoteAddr == null) {
                remoteAddr = inetAddr.getHostAddress();
            }
        }
    }


    @Override
    protected void populateRemotePort() {
        remotePort = getSocket().getIOChannel().socket().getPort();
    }


    @Override
    protected void populateLocalName() {
        InetAddress inetAddr = getSocket().getIOChannel().socket().getLocalAddress();
        if (inetAddr != null) {
            localName = inetAddr.getHostName();
        }
    }


    @Override
    protected void populateLocalAddr() {
        InetAddress inetAddr = getSocket().getIOChannel().socket().getLocalAddress();
        if (inetAddr != null) {
            localAddr = inetAddr.getHostAddress();
        }
    }


    @Override
    protected void populateLocalPort() {
        localPort = getSocket().getIOChannel().socket().getLocalPort();
    }


    /**
     * {@inheritDoc}
     *
     * @param clientCertProvider Ignored for this implementation
     */
    @Override
    public SSLSupport getSslSupport(String clientCertProvider) {
        /*TODO
        if (getSocket() instanceof SecureNioChannel) {
            SecureNioChannel ch = (SecureNioChannel) getSocket();
            SSLSession session = ch.getSslEngine().getSession();
            return ((NioEndpoint) getEndpoint()).getSslImplementation().getSSLSupport(session);
        } else {
            return null;
        }*/
        return null;
    }


    @Override
    public void doClientAuth(SSLSupport sslSupport) throws IOException {
        SecureNioChannel sslChannel = (SecureNioChannel) getSocket();
        SSLEngine engine = sslChannel.getSslEngine();
        if (!engine.getNeedClientAuth()) {
            // Need to re-negotiate SSL connection
            engine.setNeedClientAuth(true);
            //TODO sslChannel.rehandshake(getEndpoint().getConnectionTimeout());
            ((JSSESupport) sslSupport).setSession(engine.getSession());
        }
    }


    @Override
    public void setAppReadBufHandler(ApplicationBufferHandler handler) {
        getSocket().setAppReadBufHandler(handler);
    }
}
