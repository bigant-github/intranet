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
        SocketBean socketBean = serverCommunication.getSocketBean();
        return socketBean;
    }

    @Override
    protected void close() throws IOException {
        if (receiver != null) {
            receiver.skip();
            receiver.close();
            String host = super.requestProcessor.getHost();
            HttpSocketManager.get(host).createSocketBean();
        }

        if (socketBean != null) {
            socketBean.skip();
            super.socketBean.close();
        }

    }
}
