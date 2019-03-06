package priv.bigant.test;

import priv.bigant.intranet.server.HttpResponseSocket;
import priv.bigant.intranet.server.IntranetHttpServer;
import priv.bigant.intranet.server.IntranetServer;

public class ServerTest {

    public static void main(String[] args) {
        new IntranetHttpServer(80).start();
        new IntranetServer(45678).start();
        new HttpResponseSocket(45556).start();
    }

}