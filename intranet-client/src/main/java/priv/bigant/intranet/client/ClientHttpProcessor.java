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
        return new Socket("127.0.0.1", localPort);
    }

    protected void close() {

    }
}
