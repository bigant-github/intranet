package priv.bigant.intranet.server;


public class Start {
    public static void main(String[] args) {
        ServerConfig.getConfig();
        new HttpServerIntranet().start();
        new ServerHttpAccept().start();
        new ServerHttpConnector().start();
    }
}
