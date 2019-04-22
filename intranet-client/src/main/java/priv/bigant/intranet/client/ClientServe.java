package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.SocketBean;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by GaoHan on 2018/5/23.
 */
public class ClientServe extends Thread {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientServe.class);
    private ClientConfig clientConfig;
    private SocketBean socketBean;

    public ClientServe(SocketBean socketBean) {
        this.socketBean = socketBean;
        clientConfig = (ClientConfig) ClientConfig.getConfig();
    }


    @Override
    public void run() {
        threadDispose();
    }


    private void close() {
        socketBean.close();
    }

    /**
     * 执行客户端与服务端HTTP交互
     */
    private void threadDispose() {
        try {
            //while (true) {
            ClientHttpProcessor httpProcessor = new ClientHttpProcessor(socketBean);
            httpProcessor.run();
            //}
        } catch (Exception e) {
            LOGGER.error("error", e);
        } finally {
            close();
        }
    }
}
