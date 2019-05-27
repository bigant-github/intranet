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
        if (serverCommunication != null) {
            boolean b = serverCommunication.sendUrgentData();
            if (!b) {
                LOGGER.info("客户端已关闭。。。。。。。。。。。。。。。。。。。。。。。。");
                serverCommunication.close();
            }
            return null;
        }
        return checkSocketBean();
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
