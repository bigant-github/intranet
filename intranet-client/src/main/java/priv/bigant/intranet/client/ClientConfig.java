package priv.bigant.intranet.client;

import priv.bigant.intrance.common.Config;

public class ClientConfig extends Config {

    /**
     * 服务器地址
     */
    private String hostName;

    /**
     * 本地服务地址
     */
    private int localPort;

    private String localHost;

    private int listenerTime = 5000;

    public int getListenerTime() {
        return listenerTime;
    }

    public void setListenerTime(int listenerTime) {
        this.listenerTime = listenerTime;
    }

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

}
