package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.communication.HttpCommunication;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Stack;

public class CacheCommunication extends HttpCommunication {

    private static final Logger LOG = LoggerFactory.getLogger(CacheCommunication.class);
    private ServerConfig serverConfig;
    private Stack<SocketBean> socketStack;


    public CacheCommunication(SocketChannel socketChannel) throws IOException {
        super(socketChannel);
        serverConfig = (ServerConfig) Config.getConfig();
        socketStack = new Stack<>();
    }

    /**
     * 获取socketBean 超时则返回Null
     *
     * @throws InterruptedException
     */
    public synchronized SocketBean getSocketBean() {
        long time = System.currentTimeMillis();
        while ((time + serverConfig.getWaitSocketTime()) > System.currentTimeMillis()) {
            if (!socketStack.empty()) {
                SocketBean pop = socketStack.pop();
                if (pop != null) {
                    boolean b = pop.sendUrgentData();
                    if (!b) {
                        LOG.warn("http 连接器已关闭 。。。。。。。。。。。。。。。。。。。。。。。。");
                        continue;
                    }
                    LOG.debug("获取到http连接 :" + pop.getId() + "剩余" + socketStack.size());
                    return pop;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 获取socketBean 还回socketBean
     */
    public synchronized void putSocketBean(SocketBean socketBean) {
        LOG.debug("归还http连接 :" + socketBean.getId());
        LOG.debug("before num :" + socketStack.size());
        socketStack.push(socketBean);
        LOG.debug("after num :" + socketStack.size());
    }

    @Override
    public void close() {
        super.close();
        Iterator<SocketBean> iterator = socketStack.iterator();
        while (iterator.hasNext()) {
            SocketBean next = iterator.next();
            next.close();
        }
    }

}
