package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerBootstrap {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerBootstrap.class);

    public static void main(String[] args) {
        ServerConfig.getConfig();
        new HttpServerIntranet().start();
        new ServerHttpAccept().start();
        new ServerHttpConnector().start();
    }

}