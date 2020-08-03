package priv.bigant.intranet.client;

import priv.bigant.intrance.common.Config;

public class ClientConfig extends Config {

    /**
     * 请求穿透域名
     */
    private String hostName;

    /**
     * 本地服务地址
     */
    private int localPort;

    private String localHost;

    private int listenerTime = 5000;

    /**
     * 二级域名 自定义部署时有用
     */
    private String defaultHost = ".bigant.club";

    public String getDefaultHost() {
        return defaultHost;
    }

    public void setDefaultHost(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    private ClientConfig() {

    }

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

    public static ClientConfig getClientConfig() {
        if (!(config instanceof ClientConfig)) {
            synchronized (Config.class) {
                if (config == null) {
                    config = new ClientConfig();
                }
            }
        }
        return (ClientConfig) config;
    }

}
