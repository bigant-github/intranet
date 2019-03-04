package priv.bigant.intranet.server;


import priv.bigant.intrance.common.thread.HttpThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by GaoHan on 2018/5/23.
 * <p>
 * 处理http请求的线程
 */
public class IntranetHttpServer extends Thread {


    private int port;

    public IntranetHttpServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("IntranetHttpServer 启动异常");
            e.printStackTrace();
        }
        System.out.println("IntranetHttpServer start");
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                assert serverSocket != null;
                Socket accept = serverSocket.accept();
                new HttpThread(accept).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
