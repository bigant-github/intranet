package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.http.HttpProcessorAbs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerHttpConnector extends Thread {

    private ServerConfig serverConfig;
    private ThreadPoolExecutor executor;
    private ServerSocket serverSocket;
    private boolean stopped = false;
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerHttpConnector.class);

    public ServerHttpConnector() {
        this.serverConfig = (ServerConfig) ServerConfig.getConfig();
    }

    @Override
    public void run() {
        LOGGER.info("ServerHttpConnector start port:" + serverConfig.getHttpPort());
        open();
        while (!stopped) {
            synchronized (LOGGER) {
                try {
                    Socket accept = serverSocket.accept();
                    LOGGER.debug("接受到http请求");
                    if (serverConfig.getSocketTimeOut() > 0)
                        accept.setSoTimeout(serverConfig.getSocketTimeOut());
                    HttpProcessorAbs serverHttpProcessor = new ServerHttpProcessor(new SocketBean(accept));
                    //serverHttpProcessor.run();
                    executor.execute(serverHttpProcessor);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void close() {
        stopped = true;
    }

    public void open() {
        this.executor = new ThreadPoolExecutor(serverConfig.getCorePoolSize(), serverConfig.getMaximumPoolSize(), serverConfig.getKeepAliveTime(), TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
        try {
            serverSocket = new ServerSocket(serverConfig.getHttpPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
