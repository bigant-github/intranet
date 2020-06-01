package priv.bigant.intranet.server;


import priv.bigant.intrance.common.ServerConnector;
import priv.bigant.intranet.server.process.CommunicationProcess;
import priv.bigant.intranet.server.process.HttpIntranetAcceptProcess;
import priv.bigant.intranet.server.process.HttpIntranetServiceProcess;

public class Start {
    public static void main(String[] args) {
        ServerConfig config = (ServerConfig) ServerConfig.getConfig();

        CommunicationProcess httpIntranetConnectorProcess = new CommunicationProcess();
        ServerConnector testHttpIntranetConnectorProcess = new ServerConnector("testHttpIntranetConnectorProcess", httpIntranetConnectorProcess, config.getIntranetPort());
        testHttpIntranetConnectorProcess.start();

        HttpIntranetAcceptProcess httpIntranetAcceptProcess = new HttpIntranetAcceptProcess();
        ServerConnector testHttpIntranetAcceptProcess = new ServerConnector("testHttpIntranetAcceptProcess", httpIntranetAcceptProcess, config.getHttpAcceptPort());
        testHttpIntranetAcceptProcess.start();

        HttpIntranetServiceProcess httpIntranetServiceProcess = new HttpIntranetServiceProcess();
        ServerConnector testHttpIntranetServiceProcess = new ServerConnector("testHttpIntranetServiceProcess", httpIntranetServiceProcess, config.getHttpPort());
        testHttpIntranetServiceProcess.start();
    }
}
