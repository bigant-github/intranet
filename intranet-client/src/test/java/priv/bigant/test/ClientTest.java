package priv.bigant.test;


import priv.bigant.intranet.client.IntranetClient;

public class ClientTest {

    public static void main(String[] args) {
        IntranetClient.start("localhost", 45678, "www.www.com", 8081);
    }
}
