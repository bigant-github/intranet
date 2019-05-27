package priv.bigant.intranet.server;

import priv.bigant.intrance.common.Config;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Stack;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpIntranetAcceptProcess extends ProcessBase {

    private static final String NAME = "HttpIntranetAcceptProcess";
    private Stack stack;
    private ThreadPoolExecutor executor;

    public HttpIntranetAcceptProcess() {
        stack = new Stack<>();
        ServerConfig serverConfig = (ServerConfig) Config.getConfig();
        this.executor = new ThreadPoolExecutor(serverConfig.getCorePoolSize(), serverConfig.getMaximumPoolSize(), serverConfig.getKeepAliveTime(), TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void read(Connector.ConnectorThread connectorThread, SocketChannel socketChannel) {

    }

    @Override
    public void accept(Connector.ConnectorThread connectorThread, SocketChannel channel) throws IOException {
        connectorThread.register(channel, SelectionKey.OP_READ);
    }

    class ReadProcessThread implements Runnable {
        SocketChannel socketChannel;

        public ReadProcessThread(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            
        }
    }

}
