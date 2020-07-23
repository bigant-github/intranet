package priv.bigant.intranet.server;


import priv.bigant.intrance.common.ServerConnector;
import priv.bigant.intranet.server.process.CommunicationProcessor;
import priv.bigant.intranet.server.process.IntranetProcessor;
import priv.bigant.intranet.server.process.HttpProcessor;

public class Start {
    public static void main(String[] args) {
        ServerConfig config = (ServerConfig) ServerConfig.getConfig();

        CommunicationProcessor httpIntranetConnectorProcess = new CommunicationProcessor();
        ServerConnector testHttpIntranetConnectorProcess = new ServerConnector("testHttpIntranetConnectorProcess", httpIntranetConnectorProcess, config.getIntranetPort());
        testHttpIntranetConnectorProcess.start();

        IntranetProcessor intranetProcessor = new IntranetProcessor();
        ServerConnector testHttpIntranetAcceptProcess = new ServerConnector("testHttpIntranetAcceptProcess", intranetProcessor, config.getHttpAcceptPort());
        testHttpIntranetAcceptProcess.start();

        HttpProcessor httpProcessor = new HttpProcessor();
        ServerConnector testHttpIntranetServiceProcess = new ServerConnector("testHttpIntranetServiceProcess", httpProcessor, config.getHttpPort());
        testHttpIntranetServiceProcess.start();
    }
}
