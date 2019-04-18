package priv.bigant.intranet.server;

import priv.bigant.intrance.common.SocketBeanss;
import priv.bigant.intrance.common.http.HttpProcessor;
import priv.bigant.intrance.common.thread.Config;

import java.io.IOException;
import java.net.Socket;

public class ServerHttpProcessor extends HttpProcessor {

    SocketBeanss socketBeanss;

    public ServerHttpProcessor(Socket socket, Config config) {
        super(socket, config);
    }

    public ServerHttpProcessor(Socket socket) {
        super(socket);
    }

    @Override
    protected Socket getSocketBean() {
        String host = super.requestProcessor.getHost();
        ServerCommunication serverCommunication = HttpSocketManager.get(host);
        this.socketBeanss = serverCommunication.getSocketBean();
        if (socketBeanss == null)
            return null;
        return socketBeanss.getSocket();
    }

    @Override
    protected void close() throws IOException {
        String host = super.requestProcessor.getHost();
        HttpSocketManager.get(host).putSocketBean(socketBeanss);
        super.socket.close();
    }
}
