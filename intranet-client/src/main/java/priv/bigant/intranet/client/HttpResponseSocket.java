package priv.bigant.intranet.client;

import priv.bigant.intrance.common.thread.HttpResponseThread;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by GaoHan on 2018/5/25.
 */
public class HttpResponseSocket extends Thread {

    private int port;

    public HttpResponseSocket(int port) {
        this.port = port;
    }


    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("response 链接已经启动");
            //noinspection InfiniteLoopStatement
            while (true) {
                new HttpResponseThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("response 启动异常");
        } finally {
            if (serverSocket != null)
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }
}
