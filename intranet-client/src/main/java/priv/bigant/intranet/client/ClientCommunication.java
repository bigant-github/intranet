package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.*;
import priv.bigant.intrance.common.communication.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author GaoLei 保持客户端与服务端通信
 */
public class ClientCommunication extends Communication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCommunication.class);
    private ClientConfig clientConfig;

    public ClientCommunication() {
        clientConfig = (ClientConfig) ClientConfig.getConfig();
    }


    public CommunicationResponse.CommunicationResponseP connect() throws Exception {
        try {
            if (socket != null) {
                close();
            }
            this.socket = new Socket();
            socket.connect(new InetSocketAddress(clientConfig.getHostName(), clientConfig.getPort()));

            inputStream = socket.getInputStream();

            outputStream = socket.getOutputStream();

            CommunicationRequest.CommunicationRequestHttpFirst communicationHttpFirst = new CommunicationRequest.CommunicationRequestHttpFirst(CommunicationEnum.HTTP);
            communicationHttpFirst.setHost(clientConfig.getDomainName());

            write(CommunicationRequest.createCommunicationRequest(communicationHttpFirst));
            return readResponse().toJavaObject(CommunicationResponse.CommunicationResponseP.class);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }

    }

    @Override
    public void run() {
        while (true) {//监控是否断开
            try {
                CommunicationResponse.CommunicationResponseP communicationResponseP = connect();
                if (communicationResponseP.isSuccess()) {
                    LOGGER.info("connect success:host=" + clientConfig.getDomainName());
                    while (true) {
                        CommunicationRequest communicationRequest = readRequest();
                        CommunicationEnum type = communicationRequest.getType();
                        if (type.equals(CommunicationEnum.HTTP_ADD)) {
                            add(communicationRequest);
                        }
                    }
                }
                CodeEnum code = communicationResponseP.getCode();
                if (CodeEnum.HOST_ALREADY_EXIST.equals(code)) {
                    LOGGER.error("connect error:" + code.getMsg());
                    return;
                }
            } catch (IOException e) {
                super.close();
                LOGGER.error("connect error: host =" + clientConfig.getDomainName() + "     try connect    " + e.getMessage());
                try {
                    sleep(10000);
                } catch (InterruptedException e1) {
                    ;
                }
            } catch (Exception e) {
                super.close();
                LOGGER.error("连接失败", e);
                return;
            }
        }
    }

    public void add(CommunicationRequest communicationRequest) {
        SocketBean socketBean = null;
        CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd = communicationRequest.toJavaObject(CommunicationRequest.CommunicationRequestHttpAdd.class);
        String id = communicationRequestHttpAdd.getId();
        try {
            Socket socket = new Socket(clientConfig.getHostName(), clientConfig.getHttpAcceptPort());
            socketBean = new SocketBean(socket);
            CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd1 = new CommunicationRequest.CommunicationRequestHttpAdd();
            communicationRequestHttpAdd1.setId(id);
            CommunicationRequest type = CommunicationRequest.createCommunicationRequest(communicationRequestHttpAdd1);
            socketBean.getOs().write(type.toByte());
            new ClientServe(socketBean).start();
            CommunicationResponse communicationResponse = CommunicationResponse.createCommunicationResponse(new CommunicationResponse.CommunicationResponseHttpAdd(id));
            write(communicationResponse);
        } catch (Exception e) {
            LOGGER.error("add http socket error", e);
            if (socketBean != null)
                socketBean.close();
            //write(new CommunicationResponse(CodeEnum.ERROR));
        }

        /*try {//成功返回
            CommunicationResponse communicationResponse = CommunicationResponse.createCommunicationResponse(new CommunicationResponse.CommunicationResponseHttpAdd(id));
            write(communicationResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}
