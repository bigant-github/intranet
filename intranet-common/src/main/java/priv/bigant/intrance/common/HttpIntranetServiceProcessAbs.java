package priv.bigant.intrance.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.coyote.Processor;
import priv.bigant.intrance.common.coyote.http11.Http11Processor;
import priv.bigant.intrance.common.util.collections.SynchronizedStack;
import priv.bigant.intrance.common.util.net.*;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * http Nio 处理中心
 */
public abstract class HttpIntranetServiceProcessAbs extends ProcessBase {

    public static final Logger LOG = LoggerFactory.getLogger(HttpIntranetServiceProcessAbs.class);
    /**
     * http 线程池
     */
    private ThreadPoolExecutor executor;
    /**
     * http client 与 server 段交互缓存堆
     */
    protected SynchronizedStack<SocketProcessorBase> processorCache;

    private RecycledProcessors recycledProcessors = new RecycledProcessors();
    private NioSelectorPool nioSelectorPool = new NioSelectorPool();
    private Config config;

    public HttpIntranetServiceProcessAbs() {
        this.config = Config.getConfig();
        this.processorCache = new SynchronizedStack<>();
        this.executor = new ThreadPoolExecutor(config.getHttpProcessCoreSize(), config.getHttpProcessMaxSize(), Config.getConfig().getHttpProcessWaitTime(), TimeUnit.MILLISECONDS, new SynchronousQueue<>());
    }

    public abstract Http11Processor createHttp11Processor();

    @Override
    public void read(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        //selectionKey.interestOps(selectionKey.interestOps() & (~selectionKey.readyOps()));
        selectionKey.cancel();
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        LOG.debug("HttpIntranetServiceProcessAbs read " + socketChannel + "      " + socketChannel.socket().getInputStream().available());
        executor.execute(new ReadProcessThread(socketChannel));
    }

    @Override
    public void accept(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
        LOG.debug("HttpIntranetServiceProcessAbs accept " + socketChannel + "      " + socketChannel.socket().getInputStream().available());
        socketChannel.configureBlocking(false);
        connectorThread.register(socketChannel, SelectionKey.OP_READ);
        //executor.execute(new ReadProcessThread(socketChannel));
    }

    class ReadProcessThread implements Runnable {

        private SocketChannel socketChannel;

        public ReadProcessThread(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            try {
                Processor pop = recycledProcessors.pop();
                if (pop == null) {
                    pop = createHttp11Processor();
                }
                NioChannel nioChannel = new NioChannel(socketChannel, new SocketBufferHandler(config.getHttpProcessReadBufferSize(), config.getHttpProcessWriteBufferSize(), true));
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
