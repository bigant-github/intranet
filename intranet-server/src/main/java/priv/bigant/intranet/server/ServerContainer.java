package priv.bigant.intranet.server;

import priv.bigant.intrance.common.http.*;
import priv.bigant.intrance.common.thread.SocketBean;
import priv.bigant.intrance.common.thread.ThroughManager;

import java.io.IOException;

@Deprecated
public class ServerContainer implements Container {
    @Override
    public void invoke(RequestProcessor httpProcessor) throws IOException {
        SocketBean socketBean = ThroughManager.get(httpProcessor.getHost());
    }

    @Override
    public void responseInvoke(RequestProcessor httpProcessor, ResponseProcessor responseProcessor) {
    }
}
