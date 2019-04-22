package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.communication.Communication;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intrance.common.communication.CommunicationResponse;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Stack;
import java.util.UUID;

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
                    LOGGER.debug("获取到http连接 :" + pop.getId() + "剩余" + socketStack.size());
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

    public void connect() {
        LOGGER.warn("服务端不能连接");
    }

    public void createSocketBean() {
        String id = UUID.randomUUID().toString();
        CommunicationRequest communicationRequest = null;
        CommunicationRequest.CommunicationRequestP communicationRequestHttpAdd = new CommunicationRequest.CommunicationRequestHttpAdd(id);
        try {
            communicationRequest = CommunicationRequest.createCommunicationRequest(communicationRequestHttpAdd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpSocketManager.addKey(id, host);
        try {
            super.write(communicationRequest);
            CommunicationResponse.CommunicationResponseHttpAdd communicationResponseHttpAdd = super.readResponse().toJavaObject(CommunicationResponse.CommunicationResponseHttpAdd.class);
            if (communicationResponseHttpAdd.isSuccess()) {
                LOGGER.info(host + "新建http连接");
            } else
                LOGGER.info(host + "新建http失败");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
