package priv.bigant.test;

import priv.bigant.intranet.server.HttpResponseSocket;
import priv.bigant.intranet.server.IntranetHttpServer;
import priv.bigant.intranet.server.IntranetServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTest {

    /*public static void main(String[] args) {
        new IntranetHttpServer(80).start();
        new IntranetServer(45678).start();
        new HttpResponseSocket(45556).start();
    }*/


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        Socket accept = serverSocket.accept();
        InputStream inputStream = accept.getInputStream();
        while (true) {
            System.out.print(inputStream.read());
        }
    }


}