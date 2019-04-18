package priv.bigant.intranet.server;

import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.http.HttpProcessor;

import java.io.IOException;

/**
 * 负责与客户端HTTP交互
 */
public class ServerHttpProcessor extends HttpProcessor {


    public ServerHttpProcessor(SocketBean socketBean) {
        super(socketBean);
    }

    @Override
    protected SocketBean getSocketBean() {
        String host = super.requestProcessor.getHost();
        ServerCommunication serverCommunication = HttpSocketManager.get(host);
        return serverCommunication.getSocketBean();
    }

    @Override
    protected void close() throws IOException {
        String host = super.requestProcessor.getHost();
        HttpSocketManager.get(host).putSocketBean(receiver);
        super.socketBean.close();
    }
}
