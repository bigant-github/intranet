package priv.bigant.intranet.server;

import priv.bigant.intrance.common.http.HttpProcessor;
import priv.bigant.intrance.common.thread.Config;
import priv.bigant.intrance.common.thread.SocketBean;
import priv.bigant.intrance.common.thread.ThroughManager;

import java.io.IOException;
import java.net.Socket;

public class ServerHttpProcessor extends HttpProcessor {

    public ServerHttpProcessor(Socket socket, Config config) {
        super(socket, config);
    }

    public ServerHttpProcessor(Socket socket) {
        super(socket);
    }

    @Override
    protected Socket getSocketBean() {
        String host = super.requestProcessor.getHost();
        SocketBean socketBean = ThroughManager.get(host);
        return socketBean.getSocket();
    }

    @Override
    protected void close() throws IOException {
        super.socket.close();
    }
}
