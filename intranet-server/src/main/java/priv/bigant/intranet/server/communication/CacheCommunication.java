package priv.bigant.intranet.server.communication;

import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.communication.HttpCommunication;
import priv.bigant.intrance.common.log.LogUtil;
import priv.bigant.intranet.server.ServerConfig;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Stack;
import java.util.logging.Logger;

public class CacheCommunication extends HttpCommunication {

    private Logger LOG;
    private ServerConfig serverConfig;
    private Stack<SocketBean> socketStack;


    public CacheCommunication(SocketChannel socketChannel, ServerConfig config) throws IOException {
        super(socketChannel, config);
        this.serverConfig = config;
        socketStack = new Stack<>();
        this.LOG = LogUtil.getLog(config.getLogName(), CacheCommunication.class);

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
                        LOG.warning("http 连接器已关闭 。。。。。。。。。。。。。。。。。。。。。。。。");
                        continue;
                    }
                    LOG.fine("获取到http连接 :" + pop.getId() + "剩余" + socketStack.size());
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
        LOG.fine("归还http连接 :" + socketBean.getId());
        LOG.fine("before num :" + socketStack.size());
        socketStack.push(socketBean);
        LOG.fine("after num :" + socketStack.size());
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
