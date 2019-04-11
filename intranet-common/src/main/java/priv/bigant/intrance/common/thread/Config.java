package priv.bigant.intrance.common.thread;

public abstract class Config {
    protected static Config config;

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

}
