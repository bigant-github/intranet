package priv.bigant.intrance.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

public class ServerConnector extends LifecycleMBeanBase implements BigAnt, Connector {

    private String name;
    private Process process;
    private int port;
    private ServerSocketChannel server;
    private ConnectorThread connectorThread;

    public ServerConnector(String name, Process process, int port) {
        this.name = name;
        this.process = process;
        this.port = port;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected String getDomainInternal() {
        return null;
    }

    @Override
    protected String getObjectNameKeyProperties() {
        return "type=" + name;
    }

    @Override
    protected void startInternal() throws LifecycleException {
        setState(LifecycleState.STARTING);
        try {
            build();
            connectorThread = new ConnectorThread(process);
            connectorThread.register(server, SelectionKey.OP_ACCEPT);
            connectorThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void build() throws IOException {
        this.server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(port));
    }

    @Override
    protected void stopInternal() {
        connectorThread.showdown();
    }

    @Override
    public void showdown() {
        stopInternal();
    }

    /**
     * nio process 监控线程
     */
    public static class ConnectorThread extends Thread implements Connector {
        private final Logger LOG = LoggerFactory.getLogger(ConnectorThread.class);
        private Selector selector;
        private Process process;
        private Boolean stopStatus = false;

        public ConnectorThread(Process process) throws IOException {
            this.process = process;
            this.selector = Selector.open();
        }

        public void register(SelectableChannel selectableChannel, int ops, Object attn) throws ClosedChannelException {
            selectableChannel.register(selector, ops, attn);
        }

        public void register(SelectableChannel selectableChannel, int ops) throws ClosedChannelException {
            selectableChannel.register(selector, ops);
        }

        public void showdown() {
            stopStatus = true;
        }

        private boolean isShowDown() {
            return stopStatus;
        }

        @Override
        public void run() {
            while (!isShowDown()) {
                int i;
                try {
                    i = selector.selectNow();
                } catch (IOException e) {
                    i = 0;
                    LOG.error(process.getName() + " process 监控线程 select error", e);
                }
                if (i < 1)
                    continue;
                Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                while (selectionKeys.hasNext()) {

                    SelectionKey selectionKey = selectionKeys.next();
                    selectionKeys.remove();
                    try {
                        if (selectionKey.isAcceptable()) {
                            process.accept(this, selectionKey);
                        } else if (selectionKey.isReadable()) {
                            process.read(this, selectionKey);
                        }
                    } catch (Exception e) {
                        selectionKey.cancel();
                        LOG.error(process.getName() + " process 监控线程 处理器处理事件失败", e);
                    }
                }
            }
        }

    }
}

