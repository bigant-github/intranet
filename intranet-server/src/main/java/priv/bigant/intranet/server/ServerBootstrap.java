package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerBootstrap {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerBootstrap.class);

    public static void main(String[] args) {
        ServerConfig serverConfig = (ServerConfig) ServerConfig.getConfig();
        serverConfig.setHttpPort(7082);
        serverConfig.setCorePoolSize(1);
        serverConfig.setKeepAliveTime(1000);
        serverConfig.setMaximumPoolSize(10);
        serverConfig.setSocketTimeOut(3000);
        new ServerHttpConnector(serverConfig).start();
        new ServerIntranet(serverConfig).start();
    }

}