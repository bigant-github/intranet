package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.communication.CodeEnum;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intrance.common.communication.CommunicationResponse;
import priv.bigant.intrance.common.coyote.http11.Http11Processor;
import priv.bigant.intrance.common.util.net.AbstractEndpoint;
import priv.bigant.intrance.common.util.net.NioChannel;
import priv.bigant.intrance.common.util.net.NioEndpoint;
import priv.bigant.intrance.common.util.net.SocketBufferHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpIntranetServiceProcess extends ProcessBase {

    public static final Logger LOG = LoggerFactory.getLogger(HttpIntranetServiceProcess.class);
    private static final String NAME = "HttpIntranetConnectorProcess";
    private Stack stack;
    private ThreadPoolExecutor executor;

    public HttpIntranetServiceProcess() {
        stack = new Stack<>();
        ServerConfig serverConfig = (ServerConfig) Config.getConfig();
        this.executor = new ThreadPoolExecutor(1, 10, serverConfig.getKeepAliveTime(), TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void read(Connector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        selectionKey.interestOps(selectionKey.interestOps() & (~selectionKey.readyOps()));
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        LOG.debug("HttpIntranetServiceProcess read " + socketChannel + "      " + socketChannel.socket().getInputStream().available());
        executor.execute(new ReadProcessThread(socketChannel));
    }

    @Override
    public void accept(Connector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();

        LOG.debug("HttpIntranetServiceProcess accept " + socketChannel + "      " + socketChannel.socket().getInputStream().available());
        socketChannel.configureBlocking(false);
        connectorThread.register(socketChannel, SelectionKey.OP_READ);
        //executor.execute(new ReadProcessThread(socketChannel));
    }

    class ReadProcessThread implements Runnable {

        private SocketChannel socketChannel;
        private ServerCommunication serverCommunication;
        private SocketBean socketBean;

        private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        public ReadProcessThread(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            this.socketBean = new SocketBean(socketChannel);
        }

        @Override
        public void run() {
            try {
                NioEndpoint nioEndpoint = new NioEndpoint();
                Http11Processor http11Processor = new Http11ProcessorServer(8 * 1024,
                        true, false, nioEndpoint, 8192,
                        Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()), 8192, 2 * 1024 * 1024, new HashMap<>(), true, null, null);
                NioChannel nioChannel = new NioChannel(socketChannel, new SocketBufferHandler(2048, 2048, true));
                NioEndpoint.NioSocketWrapper nioSocketWrapper = new NioEndpoint.NioSocketWrapper(nioChannel, nioEndpoint);
                AbstractEndpoint.Handler.SocketState service = http11Processor.service(nioSocketWrapper);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
