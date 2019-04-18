package priv.bigant.intrance.common;

public abstract class Config {
    protected static Config config;
    /**
     * 服务器端口
     */
    private int httpAcceptPort = 45679;
    private int bufferSize = 16;

    protected Config() {

    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public static Config getConfig() {
        return config;
    }

    public int getHttpAcceptPort() {
        return httpAcceptPort;
    }

    public void setHttpAcceptPort(int httpAcceptPort) {
        this.httpAcceptPort = httpAcceptPort;
    }

}
