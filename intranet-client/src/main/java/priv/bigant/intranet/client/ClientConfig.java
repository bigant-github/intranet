package priv.bigant.intranet.client;

import priv.bigant.intrance.common.Config;

public class ClientConfig extends Config {

    private ClientConfig() {
    }

    /**
     * 服务器端口
     */
    private int port = 45678;


    /**
     * 服务器地址
     */
    private String hostName;

    /**
     * 本地服务地址
     */
    private int localPort;

    private String localHost;

    private int listenerTime;

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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    public static Config getConfig() {
        if (config == null) {
            synchronized (Config.class) {
                if (config == null) {
                    config = new ClientConfig();
                }
            }
        }
        return config;
    }
}
