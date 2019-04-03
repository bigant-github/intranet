package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerBootstrap {

    private final static Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);

    public static void main(String[] args) {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(7081);
        serverConfig.setCorePoolSize(1);
        serverConfig.setKeepAliveTime(1000);
        serverConfig.setMaximumPoolSize(2);
        serverConfig.setSocketTimeOut(3000);
        new ServerHttpConnector(serverConfig).start();
    }

}
