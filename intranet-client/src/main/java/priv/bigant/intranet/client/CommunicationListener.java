package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunicationListener extends Thread {
    private ClientCommunication clientCommunication;
    private final static Logger LOGGER = LoggerFactory.getLogger(CommunicationListener.class);
    private int mill;

    public CommunicationListener(ClientCommunication clientCommunication, int mill) {
        this.clientCommunication = clientCommunication;
        this.mill = mill;
    }


    @Override
    public void run() {
        while (true) {
            Boolean aBoolean = clientCommunication.isClose();
            if (aBoolean) {
                LOGGER.info("CommunicationListener 连接已断开");
                try {
                    clientCommunication.connect();
                } catch (Exception e) {
                    LOGGER.error("连接失败", e);
                }
            } else {
                LOGGER.info("CommunicationListener 连接正常");
            }
            try {
                sleep(mill);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
