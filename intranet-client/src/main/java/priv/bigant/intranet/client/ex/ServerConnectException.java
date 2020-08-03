package priv.bigant.intranet.client.ex;

public class ServerConnectException extends RuntimeException {
    public ServerConnectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerConnectException(String message) {
        super(message);
    }
}
