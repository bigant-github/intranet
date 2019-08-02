package priv.bigant.intranet.server;


import priv.bigant.intrance.common.ServerConnector;
import priv.bigant.intrance.common.LifecycleException;

public class Start {
    public static void main(String[] args) throws LifecycleException {
        ServerConfig config = (ServerConfig) ServerConfig.getConfig();
        HttpIntranetConnectorProcess httpIntranetConnectorProcess = new HttpIntranetConnectorProcess();
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
