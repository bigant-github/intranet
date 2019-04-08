package priv.bigant.intranet.server;

import priv.bigant.intrance.common.thread.Config;

public class ServerConfig extends Config {
    private int socketTimeOut = 60000;
    private int httpPort = 80;
    private int corePoolSize = 5;
    private int maximumPoolSize = 30;
    private int keepAliveTime = 1000;

    private int intranetPort = 2270;

    public int getIntranetPort() {
        return intranetPort;
    }

    public void setIntranetPort(int intranetPort) {
        this.intranetPort = intranetPort;
    }

    public int getSocketTimeOut() {
        return socketTimeOut;
    }

    public void setSocketTimeOut(int socketTimeOut) {
        this.socketTimeOut = socketTimeOut;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }
}
