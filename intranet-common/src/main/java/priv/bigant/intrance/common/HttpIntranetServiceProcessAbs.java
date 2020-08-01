package priv.bigant.intrance.common;

import priv.bigant.intrance.common.coyote.AbstractProcessor;
import priv.bigant.intrance.common.coyote.http11.Http11Processor;
import priv.bigant.intrance.common.log.LogUtil;
import priv.bigant.intrance.common.util.collections.SynchronizedStack;
import priv.bigant.intrance.common.util.net.NioChannel;
import priv.bigant.intrance.common.util.net.NioSelectorPool;
import priv.bigant.intrance.common.util.net.NioSocketWrapper;
import priv.bigant.intrance.common.util.net.SocketBufferHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * http Nio 处理中心
 */
public abstract class HttpIntranetServiceProcessAbs extends ProcessBase {

    public static final Logger LOG = LogUtil.getLog();
    ;
    /**
     * http 线程池
     */
    private ThreadPoolExecutor executor;

    private RecycledProcessors recycledProcessors = new RecycledProcessors();
    private NioSelectorPool nioSelectorPool = new NioSelectorPool();
    private static final Config config = Config.getConfig();

    public HttpIntranetServiceProcessAbs() {
        this.executor = new ThreadPoolExecutor(config.getHttpProcessCoreSize(), config.getHttpProcessMaxSize(), config.getHttpProcessWaitTime(), TimeUnit.MILLISECONDS, new SynchronousQueue<>());
    }

    @Override
    public void showdown() {
        executor.shutdown();
    }

    public abstract Http11Processor createHttp11Processor();

    @Override
    public void read(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        //selectionKey.interestOps(selectionKey.interestOps() & (~selectionKey.readyOps()));
        selectionKey.cancel();
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        LOG.fine("HttpIntranetServiceProcessAbs read " + socketChannel + "      " + socketChannel.socket().getInputStream().available());
        executor.execute(new ReadProcessThread(socketChannel));
    }

    @Override
    public void accept(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
        LOG.fine("HttpIntranetServiceProcessAbs accept " + socketChannel + "      " + socketChannel.socket().getInputStream().available());
        socketChannel.configureBlocking(false);
        connectorThread.register(socketChannel, SelectionKey.OP_READ);
        //executor.execute(new ReadProcessThread(socketChannel));
    }

    class ReadProcessThread implements Runnable {

        private SocketChannel socketChannel;

        public ReadProcessThread(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            try {
                AbstractProcessor pop = recycledProcessors.pop();
                if (pop == null) {
                    pop = createHttp11Processor();
                }

                //TODO
                NioChannel nioChannel = new NioChannel(socketChannel, new SocketBufferHandler(config.getHttpProcessReadBufferSize(), config.getHttpProcessWriteBufferSize(), true));
                NioSocketWrapper nioSocketWrapper = new NioSocketWrapper(nioChannel, nioSelectorPool);
                pop.service(nioSocketWrapper);

            } catch (Exception e) {
                LOG.severe("service error" + e);
                e.printStackTrace();
            }
        }
    }

    public static class RecycledProcessors extends SynchronizedStack<AbstractProcessor> {

        private static final Logger LOG = Logger.getLogger(RecycledProcessors.class.getName());
        protected final AtomicInteger size = new AtomicInteger(0);

        public RecycledProcessors() {
        }

        @SuppressWarnings("sync-override") // Size may exceed cache size a bit
        @Override
        public boolean push(AbstractProcessor processor) {
            int processorCacheSize = 200;
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
            LOG.fine("回收 process 当前数量：" + size);
            return result;
        }

        @SuppressWarnings("sync-override") // OK if size is too big briefly
        @Override
        public AbstractProcessor pop() {
            AbstractProcessor result = super.pop();
            if (result != null) {
                size.decrementAndGet();
            }
            LOG.fine("从回收站中获取 process 当前数量：" + size);
            return result;
        }

        @Override
        public synchronized void clear() {
            AbstractProcessor next = pop();
            while (next != null) {
                next = pop();
            }
            super.clear();
            size.set(0);
            LOG.fine("清空 process 当前数量");

        }
    }

}
