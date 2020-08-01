package priv.bigant.intranet.server;

import priv.bigant.intrance.common.Config;

public class ServerConfig extends Config {
    private int socketTimeOut = 5000;
    private int httpPort = 8087;

    private int corePoolSize = 5;
    private int maximumPoolSize = 30;
    private int keepAliveTime = 1000;
    private int waitSocketTime = 200000;

    private ServerConfig() {

    }

    public int getWaitSocketTime() {
        return waitSocketTime;
    }


    public int getHttpPort() {
        return httpPort;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public static ServerConfig getSeverConfig() {
        if (!(config instanceof ServerConfig)) {
            synchronized (Config.class) {
                if (config == null) {
                    config = new ServerConfig();
                }
            }
        }
        return (ServerConfig) config;
    }

}
