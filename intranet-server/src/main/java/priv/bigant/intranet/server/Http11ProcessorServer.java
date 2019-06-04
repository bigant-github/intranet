package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.coyote.UpgradeProtocol;
import priv.bigant.intrance.common.coyote.http11.Http11Processor;
import priv.bigant.intrance.common.util.net.*;

import java.io.IOException;
import java.util.*;

public class Http11ProcessorServer extends Http11Processor {
    private ServerCommunication serverCommunication;
    private SocketBean receiver;
    Logger LOGGER = LoggerFactory.getLogger(Http11ProcessorServer.class);

    public Http11ProcessorServer(int maxHttpHeaderSize, boolean allowHostHeaderMismatch, boolean rejectIllegalHeaderName, AbstractEndpoint<?> endpoint, int maxTrailerSize, Set<String> allowedTrailerHeaders, int maxExtensionSize, int maxSwallowSize, Map<String, UpgradeProtocol> httpUpgradeProtocols, boolean sendReasonPhrase, String relaxedPathChars, String relaxedQueryChars) {
        super(maxHttpHeaderSize, allowHostHeaderMismatch, rejectIllegalHeaderName, endpoint, maxTrailerSize, allowedTrailerHeaders, maxExtensionSize, maxSwallowSize, httpUpgradeProtocols, sendReasonPhrase, relaxedPathChars, relaxedQueryChars);
    }

    @Override
    public SocketBean getSocketBean() {
        String host = super.request.getHost();
        serverCommunication = HttpSocketManager.get(host);
        if (serverCommunication == null)
            return null;
        if (serverCommunication.isClose()) {
            LOGGER.info("客户端已关闭。。。。。。。。。。。。。。。。。。。。。。。。");
            serverCommunication.close();
            return null;
        }
        receiver = checkSocketBean();
        return receiver;
    }

    protected SocketBean checkSocketBean() {
        while (true) {
            SocketBean socketBean = serverCommunication.getSocketBean();
            if (socketBean != null) {
                boolean b = socketBean.sendUrgentData();
                if (!b) {
                    LOGGER.warn("客户端已关闭。。。。。。。。。。。。。。。。。。。。。。。。");
                    serverCommunication.close();
                    serverCommunication.createSocketBean();
                    continue;
                }
                return socketBean;
            } else {
                return null;
            }
        }
    }

    @Override
    public void close() throws IOException {
        LOGGER.debug("server close.............." + serverCommunication);
        if (receiver != null) {
            receiver.skip();
            receiver.close();
            if (serverCommunication != null) {
                LOGGER.debug("server close add client socket..............");
                serverCommunication.createSocketBean();
            }
        }

        /*if (socketBean != null) {
            socketBean.skip();
            super.socketBean.close();
        }*/
    }
}
