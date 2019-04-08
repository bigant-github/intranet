package priv.bigant.intranet.server;

import priv.bigant.intrance.common.http.*;
import priv.bigant.intrance.common.thread.SocketBean;
import priv.bigant.intrance.common.thread.ThroughManager;

import java.io.IOException;

public class ServerContainer implements Container {
    @Override
    public void invoke(RequestProcessor httpProcessor) throws IOException {
        SocketBean socketBean = ThroughManager.get(httpProcessor.getHost());
        new SocketMutual(httpProcessor.getInput(), new SocketBeanss(socketBean.getSocket()), httpProcessor.getContentLength()).mutual();
    }

    @Override
    public void responseInvoke(RequestProcessor httpProcessor, ResponseProcessor responseProcessor) {
        new SocketMutual(responseProcessor.getInput(), new SocketBeanss(socketBean.getSocket()), httpProcessor.getContentLength()).mutual();
    }
}
