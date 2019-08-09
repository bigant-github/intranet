package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.communication.HttpCommunication;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class ServerCommunication extends HttpCommunication {

    private static final Logger LOG = LoggerFactory.getLogger(ServerCommunication.class);
    public static final Map<String, SocketBean> MAP = new HashMap<>();
    private ServerConfig serverConfig;

    public ServerCommunication(SocketChannel socketChannel) throws IOException {
        super(socketChannel);
        serverConfig = (ServerConfig) Config.getConfig();
    }

    @Override
    public SocketBean getSocketBean() {
        long time = System.currentTimeMillis();
        String id = super.createSocketBean();
        while ((time + serverConfig.getWaitSocketTime()) > System.currentTimeMillis()) {
            SocketBean socketBean = MAP.get(id);
            if (socketBean != null)
                return socketBean;
        }
        LOG.debug("getSocketBean TIMEOUT: createTime=" + time + "    endTime=" + System.currentTimeMillis());
        return null;
    }

    @Override
    public void putSocketBean(SocketBean socketBean) {
        String id = socketBean.getId();
        MAP.put(id, socketBean);
        LOG.debug("put socket id:" + id);
    }

    @Override
    public String createSocketBean() {
        return null;
    }
}
