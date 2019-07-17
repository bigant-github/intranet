package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.coyote.UpgradeProtocol;
import priv.bigant.intrance.common.coyote.http11.Http11Processor;
import priv.bigant.intrance.common.util.net.NioSelectorPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class Http11ProcessorServer extends Http11Processor {
    private SocketBean receiver;
    private static final Logger LOG = LoggerFactory.getLogger(Http11ProcessorServer.class);
    private NioSelectorPool nioSelectorPool = new NioSelectorPool();
    private ClientConfig clientConfig;

    public Http11ProcessorServer(int maxHttpHeaderSize, boolean allowHostHeaderMismatch, boolean rejectIllegalHeaderName, Map<String, UpgradeProtocol> httpUpgradeProtocols, boolean sendReasonPhrase, String relaxedPathChars, String relaxedQueryChars) {
        super(maxHttpHeaderSize, allowHostHeaderMismatch, rejectIllegalHeaderName, httpUpgradeProtocols, sendReasonPhrase, relaxedPathChars, relaxedQueryChars);
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
        if (receiver != null) {
            receiver.skip();
            receiver.close();
        }
    }
}
