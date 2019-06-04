package priv.bigant.intranet.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.*;
import priv.bigant.intrance.common.communication.CodeEnum;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intrance.common.communication.CommunicationResponse;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by GaoHan on 2018/5/22.
 */
public class HttpThroughThread extends Thread {
    // 和本线程相关的Socket
    private SocketBean socketBean;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpThroughThread.class);
    private ServerCommunication serverCommunication;

    public HttpThroughThread(Socket socket) throws IOException {
        this.socketBean = new SocketBean(socket);
    }

    @Override
    public void run() {
        try {
            //创建server与客户端通信连接
            serverCommunication = new ServerCommunication(socketBean.getSocket());

            //读取客户端配置信息
            CommunicationRequest.CommunicationRequestHttpFirst communicationRequestHttpFirst = serverCommunication.readRequest().toJavaObject(CommunicationRequest.CommunicationRequestHttpFirst.class);

            String host = communicationRequestHttpFirst.getHost();
            serverCommunication.setHost(host);
            socketBean.setDomainName(host);
            boolean exist = HttpSocketManager.isExist(host);
            if (exist) {
                boolean b = HttpSocketManager.get(host).isClose();
                if (b) {
                    HttpSocketManager.get(host).close();
                    HttpSocketManager.remove(host);
                } else {
                    serverCommunication.write(CommunicationResponse.create(CodeEnum.HOST_ALREADY_EXIST));
                    serverCommunication.close();
                    LOGGER.info(host + CodeEnum.HOST_ALREADY_EXIST.getMsg());
                    return;
                }
            }
            HttpSocketManager.add(host, serverCommunication);
            serverCommunication.write(CommunicationResponse.createSuccess());
            LOGGER.info(host + " 连接成功");
        } catch (IOException e) {
            LOGGER.error("connection io error", e);
            serverCommunication.close();
        } catch (Exception e) {
            LOGGER.error("connection error", e);
            serverCommunication.close();
        }

        createdSocketBean();
    }

    public void createdSocketBean() {
        for (int x = 0; x < 10; x++) {
            serverCommunication.createSocketBean();
        }
    }

}
