package priv.bigant.intrance.common.http;

import priv.bigant.intrance.common.SocketBean;

import java.io.IOException;

/**
 * http 通信核心处理
 */
public interface HttpProcessor {


    SocketBean getSocketBean() throws IOException;

    void close() throws IOException;

}
