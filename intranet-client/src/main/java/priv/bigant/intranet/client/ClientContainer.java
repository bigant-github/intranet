package priv.bigant.intranet.client;


import priv.bigant.intrance.common.http.*;

import java.io.IOException;
import java.net.Socket;

@Deprecated
public class ClientContainer implements Container {

    ClientConfig clientConfig;

    public ClientContainer(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public void invoke(RequestProcessor httpProcessor) throws IOException {
        int localPort = clientConfig.getLocalPort();
        Socket socket = new Socket("127.0.0.1", localPort);
    }

    public void responseInvoke(RequestProcessor httpProcessor, ResponseProcessor responseProcessor) {

    }
}
