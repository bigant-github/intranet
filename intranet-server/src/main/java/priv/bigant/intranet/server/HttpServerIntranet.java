package priv.bigant.intranet.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by GaoHan on 2018/5/22.
 * <p>
 * 接受申请穿透的socket线程 server端
 */
public class HttpServerIntranet extends Thread {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpServerIntranet.class);
    private ServerSocket serverSocket = null;

    private ServerConfig serverConfig;

    public HttpServerIntranet() {
        serverConfig = (ServerConfig) Config.getConfig();
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
                new HttpThroughThread(socket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
