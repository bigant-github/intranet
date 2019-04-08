package priv.bigant.intrance.common.http;

import java.io.IOException;

public interface Container {

    void invoke(RequestProcessor httpProcessor) throws IOException;

    void responseInvoke(RequestProcessor httpProcessor, ResponseProcessor responseProcessor);
}
