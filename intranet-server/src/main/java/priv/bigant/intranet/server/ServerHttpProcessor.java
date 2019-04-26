package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.http.HttpProcessor;

import java.io.IOException;

/**
 * 负责与客户端HTTP交互
 */
public class ServerHttpProcessor extends HttpProcessor {
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerHttpProcessor.class);

    private ServerCommunication serverCommunication;

    public ServerHttpProcessor(SocketBean socketBean) {
        super(socketBean);
    }

    @Override
    protected SocketBean getSocketBean() {
        String host = super.requestProcessor.getHost();
        serverCommunication = HttpSocketManager.get(host);
        SocketBean socketBean = serverCommunication.getSocketBean();
        return socketBean;
    }

    @Override
    protected void close() throws IOException {
        LOGGER.debug("server close.............." + serverCommunication);
        if (receiver != null) {
            receiver.skip();
            receiver.close();
            if (serverCommunication != null) {
                LOGGER.debug("server close add client socket..............");
                serverCommunication.createSocketBean();
            }
        }

        if (socketBean != null) {
            socketBean.skip();
            super.socketBean.close();
        }

    }
}
