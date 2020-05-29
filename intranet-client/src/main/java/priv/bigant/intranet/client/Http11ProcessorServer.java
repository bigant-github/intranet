package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.coyote.http11.Http11Processor;
import priv.bigant.intrance.common.util.net.NioSelectorPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Http11ProcessorServer extends Http11Processor {
    private static final Logger LOG = LoggerFactory.getLogger(Http11ProcessorServer.class);
    private NioSelectorPool nioSelectorPool = new NioSelectorPool();
    private ClientConfig clientConfig;

    public Http11ProcessorServer(int maxHttpHeaderSize, boolean allowHostHeaderMismatch, boolean rejectIllegalHeaderName, String relaxedPathChars, String relaxedQueryChars) {
        super(maxHttpHeaderSize, rejectIllegalHeaderName, relaxedPathChars, relaxedQueryChars);
        clientConfig = (ClientConfig) ClientConfig.getConfig();
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
        LOG.debug("server close..............");
        socketWrapper.close();
    }
}
