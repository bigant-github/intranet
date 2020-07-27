package priv.bigant.intrance.common.communication;

import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.HttpSocketManager;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.log.LogUtil;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.logging.Logger;

public abstract class HttpCommunication extends Communication {

    private Logger LOG;
    private String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public HttpCommunication(SocketChannel socketChannel, Config config) throws IOException {
        super(socketChannel, config);
    }

    public HttpCommunication(SocketChannel socketChannel, CommunicationDispose communicationDispose, Config config) throws IOException {
        super(socketChannel, communicationDispose, config);
        this.LOG = LogUtil.getLog(config.getLogName(), HttpCommunication.class);
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
            LOG.fine(host + "新建http连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return id;
    }
}
