package priv.bigant.intranet.server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by GaoHan on 2018/5/22.
 * <p>
 * 接受申请穿透的socket线程 server端
 */
public class IntranetServer extends Thread {


    private int port;

    public IntranetServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        java.net.ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("IntranetServer start error");
            e.printStackTrace();
        }
        System.out.println("IntranetServer start port:" + port);
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("接受到申请穿透");
                new ThroughThread(socket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
