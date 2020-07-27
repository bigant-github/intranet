package priv.bigant.intranet.server.process;

import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.HttpIntranetServiceProcessAbs;
import priv.bigant.intrance.common.coyote.http11.Http11Processor;
import priv.bigant.intranet.server.Http11ProcessorServer;

/**
 * 用户创建新的http交互通道处理器
 */
public class HttpProcessor extends HttpIntranetServiceProcessAbs {
    public HttpProcessor(Config config) {
        super(config);
    }

    @Override
    public Http11Processor createHttp11Processor(Config config) {
        return new Http11ProcessorServer(8 * 1024, null, null, config);
    }

    @Override
    public String getName() {
        return null;
    }
}
