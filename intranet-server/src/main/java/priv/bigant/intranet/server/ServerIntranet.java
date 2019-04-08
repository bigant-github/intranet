package priv.bigant.intranet.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by GaoHan on 2018/5/22.
 * <p>
 * 接受申请穿透的socket线程 server端
 */
public class ServerIntranet extends Thread {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerIntranet.class);
    private ServerSocket serverSocket = null;


    private ServerConfig serverConfig;

    public ServerIntranet(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(serverConfig.getIntranetPort());
        } catch (IOException e) {
            LOGGER.error("ServerIntranet start error", e);
        }
        LOGGER.info("IntranetServer start port:" + serverConfig.getIntranetPort());
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                LOGGER.info("接受到申请穿透");
                new ThroughThread(socket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
