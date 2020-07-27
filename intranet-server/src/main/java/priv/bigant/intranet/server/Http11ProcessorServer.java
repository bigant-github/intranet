package priv.bigant.intranet.server;

import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.HttpSocketManager;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.communication.HttpCommunication;
import priv.bigant.intrance.common.coyote.http11.Http11Processor;
import priv.bigant.intrance.common.log.LogUtil;
import priv.bigant.intrance.common.util.net.NioSelectorPool;

import java.io.IOException;
import java.util.logging.Logger;

public class Http11ProcessorServer extends Http11Processor {
    private HttpCommunication httpCommunication;
    private SocketBean receiver;
    private Logger LOG;
    private NioSelectorPool nioSelectorPool = new NioSelectorPool();

    public Http11ProcessorServer(int maxHttpHeaderSize, String relaxedPathChars, String relaxedQueryChars, Config config) {
        super(maxHttpHeaderSize, relaxedPathChars, relaxedQueryChars, config);
        this.LOG = LogUtil.getLog(config.getLogName(), Http11ProcessorServer.class);
    }

    @Override
    public SocketBean getSocketBean() {

        String host = super.request.getHost();

        LOG.fine("获取socketBean host=" + host);

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
        LOG.fine("server close.............." + httpCommunication);
        socketWrapper.close();

        if (receiver != null) {
            receiver.skip();
            receiver.close();
            if (httpCommunication != null) {
                LOG.fine("server close add client socket..............");
                httpCommunication.createSocketBean();
            }
        }
    }
}
