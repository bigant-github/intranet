package priv.bigant.intranet.server;

import java.net.Socket;

public class ServerHttpProcessor implements Runnable {

    private Socket httpSocket;

    public ServerHttpProcessor(Socket httpSocket) {
        this.httpSocket = httpSocket;
    }

    public Socket getHttpSocket() {
        return httpSocket;
    }

    public void setHttpSocket(Socket httpSocket) {
        this.httpSocket = httpSocket;
    }

    public void run() {
        
    }
}
