package priv.bigant.intrance.common.util.net;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;

public class NioSocketWrapper extends SocketWrapperBase<NioChannel> {
    private final NioSelectorPool pool;

    public volatile boolean closed = false;

    public NioSocketWrapper(NioChannel channel, NioSelectorPool pool) {
        super(channel);
        this.pool = pool;
        socketBufferHandler = channel.getBufHandler();
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
        } else {
            // Fill the read buffer as best we can.
            nRead = fillReadBuffer(block);
            /*if (LOG.isDebugEnabled()) {
                LOG.debug("Socket: [" + this + "], Read into buffer: [" + nRead + "]");
            }*/

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
    public void setAppReadBufHandler(ApplicationBufferHandler handler) {
        getSocket().setAppReadBufHandler(handler);
    }
}
