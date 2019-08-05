package priv.bigant.intrance.common.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.HttpSocketManager;
import priv.bigant.intrance.common.SocketBean;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public abstract class HttpCommunication extends Communication {

    private static final Logger LOG = LoggerFactory.getLogger(HttpCommunication.class);
    private String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public HttpCommunication(SocketChannel socketChannel) throws IOException {
        super(socketChannel);
    }

    /**
     * 获取socketBean 超时则返回Null
     *
     * @throws InterruptedException
     */
    public abstract SocketBean getSocketBean();

    /**
     * 获取socketBean 还回socketBean
     */
    public abstract void putSocketBean(SocketBean socketBean);

    @Override
    public synchronized void close() {
        super.close();
    }

    public String createSocketBean() {
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
            super.writeN(communicationRequest);
            LOG.debug(host + "新建http连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return id;
    }
}
