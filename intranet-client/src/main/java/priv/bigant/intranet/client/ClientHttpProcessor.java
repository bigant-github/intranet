package priv.bigant.intranet.client;

import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.exception.ServletException;
import priv.bigant.intrance.common.http.HttpProcessor;

import java.io.IOException;
import java.net.Socket;

public class ClientHttpProcessor extends HttpProcessor {


    public ClientHttpProcessor(SocketBean socketBean) {
        super(socketBean);
    }

    @Override
    protected void process() throws IOException, ServletException {
        super.process();
    }

    protected SocketBean getSocketBean() throws IOException {
        int localPort = ((ClientConfig) config).getLocalPort();
        String localHost = ((ClientConfig) config).getLocalHost();
        return new SocketBean(new Socket(localHost, localPort));
    }

    protected void close() throws IOException {

        socketBean.skip();
        socketBean.close();
        receiver.close();
        receiver.close();
    }
}