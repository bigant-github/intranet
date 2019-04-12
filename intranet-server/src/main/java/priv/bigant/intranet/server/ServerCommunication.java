package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Communication;
import priv.bigant.intrance.common.thread.Config;

import java.io.IOException;
import java.net.Socket;

public class ServerCommunication extends Communication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerCommunication.class);

    private ServerConfig serverConfig;

    public ServerCommunication() {
        serverConfig = (ServerConfig) Config.getConfig();
    }

    public ServerCommunication(Socket socket) throws IOException {
        super(socket);
        serverConfig = (ServerConfig) Config.getConfig();
    }

    @Override
    public void connect() {
        LOGGER.warn("服务端不能连接");
    }
}
