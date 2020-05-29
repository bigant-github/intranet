package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.HttpSocketManager;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.communication.HttpCommunication;
import priv.bigant.intrance.common.coyote.http11.Http11Processor;
import priv.bigant.intrance.common.util.net.*;

import java.io.IOException;

public class Http11ProcessorServer extends Http11Processor {
    private HttpCommunication httpCommunication;
    private SocketBean receiver;
    private static final Logger LOG = LoggerFactory.getLogger(Http11ProcessorServer.class);
    private NioSelectorPool nioSelectorPool = new NioSelectorPool();

    public Http11ProcessorServer(int maxHttpHeaderSize, boolean allowHostHeaderMismatch, boolean rejectIllegalHeaderName, String relaxedPathChars, String relaxedQueryChars) {
        super(maxHttpHeaderSize, rejectIllegalHeaderName, relaxedPathChars, relaxedQueryChars);
    }

    @Override
    public SocketBean getSocketBean() {

        String host = super.request.getHost();

        LOG.debug("获取socketBean host=" + host);

        httpCommunication = HttpSocketManager.get(host);
        if (httpCommunication == null)
            return null;
        if (httpCommunication.isClose()) {
            LOG.info("客户端已关闭。。。。。。。。。。。。。。。。。。。。。。。。");
            httpCommunication.close();
            return null;
        }
        receiver = httpCommunication.getSocketBean();
        return receiver;
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
        LOG.debug("server close.............." + httpCommunication);
        socketWrapper.close();

        if (receiver != null) {
            receiver.skip();
            receiver.close();
            if (httpCommunication != null) {
                LOG.debug("server close add client socket..............");
                httpCommunication.createSocketBean();
            }
        }
    }
}
