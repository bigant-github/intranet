package priv.bigant.intranet.client;

import priv.bigant.intrance.common.http.HttpProcessor;

import java.io.IOException;
import java.net.Socket;

public class ClientHttpProcessor extends HttpProcessor {

    public ClientHttpProcessor(Socket socket, ClientConfig config) {
        super(socket, config);
    }

    protected Socket getSocketBean() throws IOException {
        int localPort = ((ClientConfig) config).getLocalPort();
        String localHost = ((ClientConfig) config).getLocalHost();
        return new Socket(localHost, localPort);
    }

    protected void close() {

    }
}
