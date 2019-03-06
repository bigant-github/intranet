package priv.bigant.intranet.client;


public class IntranetClient {
    public static void start(String host, int serverPort, String domainName, Integer localPort) {
        //inputStream = new FileInputStream(new File(userPath + "/conf.properties"));
        System.out.println("请求穿透 服务端地址" + host + " 服务端口" + serverPort + " 使用域名" + domainName + "本地端口" + localPort);
        new ThroughThread(host, serverPort, domainName, localPort).start();
    }
}
