package priv.bigant.intranet.client;

import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.coyote.http11.Http11Processor;
import priv.bigant.intrance.common.log.LogUtil;
import priv.bigant.intrance.common.util.net.NioSelectorPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class Http11ProcessorServer extends Http11Processor {
    private static final Logger LOG = LogUtil.getLog();
    private final NioSelectorPool nioSelectorPool = new NioSelectorPool();
    private static final ClientConfig clientConfig = ClientConfig.getClientConfig();

    public Http11ProcessorServer(int maxHttpHeaderSize, String relaxedPathChars, String relaxedQueryChars) {
        super(maxHttpHeaderSize, relaxedPathChars, relaxedQueryChars);
    }

    @Override
    public SocketBean getSocketBean() throws IOException {
        int localPort = clientConfig.getLocalPort();
        String localHost = clientConfig.getLocalHost();
        return new SocketBean(SocketChannel.open(new InetSocketAddress(localHost, localPort)));
    }

    @Override
    public int getMaxHeaderCount() {
        return 50;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public NioSelectorPool getNioSelectorPool() {
        return nioSelectorPool;
    }

    @Override
    public void close() throws IOException {
        LOG.fine("server close..............");
        socketWrapper.close();
    }
}
