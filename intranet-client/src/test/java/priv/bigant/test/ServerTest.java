package priv.bigant.test;

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
        Socket socket = new Socket("127.0.0.1", 45678);
        System.out.println(socket);
    }


}