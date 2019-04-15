package priv.bigant.intranet.server;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.CommunicationRequest;
import priv.bigant.intrance.common.CommunicationResponse;
import priv.bigant.intrance.common.SocketBeanss;
import priv.bigant.intrance.common.thread.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerHttpAccept extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHttpAccept.class);
    private ServerConfig serverConfig;
    private ServerSocket serverSocket;

    byte[] bytes = new byte[1024];

    public ServerHttpAccept() {
        serverConfig = (ServerConfig) Config.getConfig();
    }

    @Override
    public void run() {
        int httpAcceptPort = serverConfig.getHttpAcceptPort();
        try {
            serverSocket = new ServerSocket(httpAcceptPort);
        } catch (IOException e) {
            LOGGER.error("start http accept error", e);
        }
        while (true) {
            SocketBeanss socketBeanss;
            try {
                Socket accept = serverSocket.accept();
                socketBeanss = new SocketBeanss(accept);
                int read = socketBeanss.getIs().read(bytes);
                CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd = CommunicationResponse.createCommunicationResponse(ArrayUtils.subarray(bytes, 0, read)).toJavaObject(CommunicationRequest.CommunicationRequestHttpAdd.class);
                String id = communicationRequestHttpAdd.getId();
                socketBeanss.setId(id);
                ServerCommunication serverCommunication = HttpSocketManager.get(id);
                serverCommunication.putSocketBean(socketBeanss);
                LOGGER.info(serverCommunication.getHost() + "create new http accept socket");
            } catch (IOException e) {
                LOGGER.error("http accept error", e);
                try {
                    serverSocket.close();
                } catch (IOException ignored) {
                }
            }
        }

    }
}
