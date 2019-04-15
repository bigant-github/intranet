package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientCommunication extends Communication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCommunication.class);

    private ClientConfig clientConfig;

    public Socket getSocket() {
        return socket;
    }

    public ClientCommunication() {
        ClientConfig config = (ClientConfig) ClientConfig.getConfig();
    }


    @Override
    public void connect() {
        try {
            if (socket != null) {
                close();
            }
            this.socket = new Socket();
            socket.connect(new InetSocketAddress(clientConfig.getHostName(), clientConfig.getPort()));
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            CommunicationRequest.CommunicationRequestHttpFirst communicationHttpFirst = new CommunicationRequest.CommunicationRequestHttpFirst(CommunicationReturnEnum.HTTP);
            communicationHttpFirst.setHost(clientConfig.getDomainName());

            write(CommunicationRequest.createCommunicationRequest(communicationHttpFirst));

            CommunicationResponse.CommunicationResponseP communicationResponseP = readResponse().toJavaObject(CommunicationResponse.CommunicationResponseP.class);

            if (!communicationResponseP.isSuccess()) {
                LOGGER.error("connect error ");
            }

            LOGGER.info("connect success:host=" + clientConfig.getDomainName());
        } catch (IOException e) {
            LOGGER.error("connect error: host =" + clientConfig.getDomainName(), e);
        } catch (Exception e) {
            LOGGER.error("connect error: host =" + clientConfig.getDomainName(), e);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

    }


}
