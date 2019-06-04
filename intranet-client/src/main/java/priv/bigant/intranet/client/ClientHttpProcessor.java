package priv.bigant.intranet.client;

import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.exception.ServletException;
import priv.bigant.intrance.common.http.HttpProcessorAbs;

import java.io.IOException;
import java.net.Socket;

public class ClientHttpProcessor extends HttpProcessorAbs {


    public ClientHttpProcessor(SocketBean socketBean) {
        super(socketBean);
    }

    @Override
    protected void process() throws IOException, ServletException {
        super.process();
    }

    public SocketBean getSocketBean() throws IOException {
        int localPort = ((ClientConfig) config).getLocalPort();
        String localHost = ((ClientConfig) config).getLocalHost();
        return new SocketBean(new Socket(localHost, localPort));
    }

    public void close() throws IOException {
        socketBean.skip();
        socketBean.close();
        if (receiver != null) {
            receiver.skip();
            receiver.close();
        }

    }
}