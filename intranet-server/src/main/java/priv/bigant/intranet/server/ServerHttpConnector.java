package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerHttpConnector extends Thread {

    private int socketTimeOut;
    private int port;
    private int corePoolSize;
    private int maximumPoolSize;
    private int keepAliveTime;
    private ThreadPoolExecutor executor;
    private ServerSocket serverSocket;
    private boolean stopped = false;
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerHttpConnector.class);

    @Override
    public void run() {
        LOGGER.info("ServerHttpConnector start port:" + port);
        open();
        while (!stopped) {
            try {
                Socket accept = serverSocket.accept();
                if (socketTimeOut > 0)
                    accept.setSoTimeout(socketTimeOut);
                ServerHttpProcessor serverHttpProcessor = new ServerHttpProcessor(accept);
                executor.execute(serverHttpProcessor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        stopped = true;
    }

    public void open() {
        this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
