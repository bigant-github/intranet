package priv.bigant.intranet.client;


import priv.bigant.intrance.common.http.*;

import java.io.IOException;
import java.net.Socket;

public class ClientContainer implements Container {

    ClientConfig clientConfig;

    public ClientContainer(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public void invoke(RequestProcessor httpProcessor) throws IOException {
        int localPort = clientConfig.getLocalPort();
        Socket socket = new Socket("127.0.0.1", localPort);
        new SocketMutual(httpProcessor.getInput(), new SocketBeanss(socket), httpProcessor.getContentLength()).mutual();
    }

    public void responseInvoke(RequestProcessor httpProcessor, ResponseProcessor responseProcessor) {

    }
}
