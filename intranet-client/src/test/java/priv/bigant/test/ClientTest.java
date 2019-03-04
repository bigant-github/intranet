package priv.bigant.test;

import priv.bigant.intranet.client.HttpResponseSocket;

public class ClientTest {
    public static void main(String[] args) {
        new HttpResponseSocket(9999).run();
    }
}
