package priv.bigant.intrance.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.coyote.Processor;
import priv.bigant.intrance.common.coyote.http11.Http11Processor;
import priv.bigant.intrance.common.util.collections.SynchronizedStack;
import priv.bigant.intrance.common.util.net.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class HttpIntranetServiceProcessAbs extends ProcessBase {

    public static final Logger LOG = LoggerFactory.getLogger(HttpIntranetServiceProcessAbs.class);
    private ThreadPoolExecutor executor;
    protected SynchronizedStack<SocketProcessorBase> processorCache;
    private RecycledProcessors recycledProcessors = new RecycledProcessors();
    private NioSelectorPool nioSelectorPool = new NioSelectorPool();

    public HttpIntranetServiceProcessAbs() {
        this.processorCache = new SynchronizedStack<>();
        this.executor = new ThreadPoolExecutor(1, 20, Config.getConfig().getThreadKeepAliveTime(), TimeUnit.MILLISECONDS, new SynchronousQueue<>());
    }

    public abstract Http11Processor createHttp11Processor();

    @Override
    public void start() {

    }

    @Override
    public void read(Connector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        selectionKey.interestOps(selectionKey.interestOps() & (~selectionKey.readyOps()));
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        LOG.debug("HttpIntranetServiceProcessAbs read " + socketChannel + "      " + socketChannel.socket().getInputStream().available());
        executor.execute(new ReadProcessThread(socketChannel));
    }

    @Override
    public void accept(Connector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
        LOG.debug("HttpIntranetServiceProcessAbs accept " + socketChannel + "      " + socketChannel.socket().getInputStream().available());
        socketChannel.configureBlocking(false);
        connectorThread.register(socketChannel, SelectionKey.OP_READ);
        //executor.execute(new ReadProcessThread(socketChannel));
    }

    class ReadProcessThread implements Runnable {

        private SocketChannel socketChannel;
        private SocketBean socketBean;

        private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        public ReadProcessThread(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            this.socketBean = new SocketBean(socketChannel);
        }

        @Override
        public void run() {
            try {
                Processor pop = recycledProcessors.pop();
                if (pop == null) {
                    pop = createHttp11Processor();
                }
                NioChannel nioChannel = new NioChannel(socketChannel, new SocketBufferHandler(2048, 2048, true));
                NioSocketWrapper nioSocketWrapper = new NioSocketWrapper(nioChannel, nioSelectorPool);
                AbstractEndpoint.Handler.SocketState service = pop.process(nioSocketWrapper, SocketEvent.OPEN_READ);
                pop.recycle();
                //recycledProcessors.push(pop);
            } catch (Exception e) {
                LOG.error("service error", e);
            }
        }
    }

    public static class RecycledProcessors extends SynchronizedStack<Processor> {

        private static final Logger LOG = LoggerFactory.getLogger(RecycledProcessors.class);
        protected final AtomicInteger size = new AtomicInteger(0);

        private int processorCacheSize = 200;

        public RecycledProcessors() {
        }

        @SuppressWarnings("sync-override") // Size may exceed cache size a bit
        @Override
        public boolean push(Processor processor) {
            int cacheSize = processorCacheSize;
            boolean offer = cacheSize == -1 || size.get() < cacheSize;
            //avoid over growing our cache or add after we have stopped
            boolean result = false;
            if (offer) {
                result = super.push(processor);
                if (result) {
                    size.incrementAndGet();
                }
            }
            LOG.debug("回收 process 当前数量：" + size);
            return result;
        }

        @SuppressWarnings("sync-override") // OK if size is too big briefly
        @Override
        public Processor pop() {
            Processor result = super.pop();
            if (result != null) {
                size.decrementAndGet();
            }
            LOG.debug("从回收站中获取 process 当前数量：" + size);
            return result;
        }

        @Override
        public synchronized void clear() {
            Processor next = pop();
            while (next != null) {
                next = pop();
            }
            super.clear();
            size.set(0);
            LOG.debug("清空 process 当前数量");

        }
    }

}
