package priv.bigant.intranet.client;

import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.http.HttpProcessor;

import java.io.IOException;
import java.net.Socket;

public class ClientHttpProcessor extends HttpProcessor {

    public ClientHttpProcessor(SocketBean socketBean) {
        super(socketBean);
    }

    protected SocketBean getSocketBean() throws IOException {
        int localPort = ((ClientConfig) config).getLocalPort();
        String localHost = ((ClientConfig) config).getLocalHost();
        return new SocketBean(new Socket(localHost, localPort));
    }

    protected void close() {
        receiver.close();
    }
}
