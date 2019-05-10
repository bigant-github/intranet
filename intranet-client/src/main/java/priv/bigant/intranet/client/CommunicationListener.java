package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.http.HttpProcessor;

public class CommunicationListener extends Thread {
    private ClientCommunication clientCommunication;
    private final static Logger LOGGER = LoggerFactory.getLogger(CommunicationListener.class);

    public CommunicationListener(ClientCommunication clientCommunication) {
        this.clientCommunication = clientCommunication;
    }


    @Override
    public void run() {
        while (true) {
            Boolean aBoolean = clientCommunication.sendUrgentData();
            if (aBoolean) {
                LOGGER.info("CommunicationListener 连接已断开");
            } else {
                LOGGER.info("CommunicationListener 连接正常");
            }
            try {
                sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
