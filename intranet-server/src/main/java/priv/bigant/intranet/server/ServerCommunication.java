package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.communication.Communication;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.Config;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Stack;

public class ServerCommunication extends Communication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerCommunication.class);

    private ServerConfig serverConfig;
    private String host;
    private Stack<SocketBean> socketStack;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public ServerCommunication(Socket socket) throws IOException {
        super(socket);
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
        do {
            if (!socketStack.empty()) {
                SocketBean pop = socketStack.pop();
                if (pop != null) {
                    LOGGER.debug("获取到http连接 :" + pop.getId());
                    return pop;
                }
            }
            try {
                LOGGER.debug("未获取到客户端http连接 等待。。。。");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.error("get socket bean sleep error");
            }
        } while ((time + serverConfig.getWaitSocketTime()) < System.currentTimeMillis());
        return null;
    }

    /**
     * 获取socketBean 还回socketBean
     */
    public synchronized void putSocketBean(SocketBean socketBeanss) {
        LOGGER.debug("归还http连接 :" + socketBeanss.getId());
        socketStack.push(socketBeanss);
    }

    @Override
    public synchronized void close() {
        super.close();
        Iterator<SocketBean> iterator = socketStack.iterator();
        while (iterator.hasNext()) {
            SocketBean next = iterator.next();
            next.close();
        }
    }

    @Override
    public void connect() {
        LOGGER.warn("服务端不能连接");
    }
}
