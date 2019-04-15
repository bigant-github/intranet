package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Communication;
import priv.bigant.intrance.common.SocketBeanss;
import priv.bigant.intrance.common.thread.Config;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Stack;

public class ServerCommunication extends Communication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerCommunication.class);

    private ServerConfig serverConfig;
    private String host;
    private Stack<SocketBeanss> socketStack;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public ServerCommunication(String host) {
        serverConfig = (ServerConfig) Config.getConfig();
        socketStack = new Stack<>();
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
    public synchronized SocketBeanss getSocketBean() {
        long time = System.currentTimeMillis();
        do {
            if (!socketStack.empty()) {
                SocketBeanss pop = socketStack.pop();
                if (pop != null)
                    return pop;
            }
            try {
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
    public synchronized void putSocketBean(SocketBeanss socketBeanss) {
        socketStack.push(socketBeanss);
    }

    @Override
    public synchronized void close() {
        super.close();
        Iterator<SocketBeanss> iterator = socketStack.iterator();
        while (iterator.hasNext()) {
            SocketBeanss next = iterator.next();
            next.close();
        }
    }

    @Override
    public void connect() {
        LOGGER.warn("服务端不能连接");
    }
}
