package priv.bigant.intranet.server;


import priv.bigant.intrance.common.ServerConnector;
import priv.bigant.intranet.server.process.CommunicationProcessor;
import priv.bigant.intranet.server.process.HttpProcessor;
import priv.bigant.intranet.server.process.IntranetProcessor;



public class Start {
    public static void main(String[] args) {
        ServerConfig config = ServerConfig.getSeverConfig();
        CommunicationProcessor httpIntranetConnectorProcess = new CommunicationProcessor(config);
        ServerConnector testHttpIntranetConnectorProcess = new ServerConnector("CommunicationProcessor", httpIntranetConnectorProcess, config.getIntranetPort(), config);
        testHttpIntranetConnectorProcess.start();

        IntranetProcessor intranetProcessor = new IntranetProcessor(config);
        ServerConnector testHttpIntranetAcceptProcess = new ServerConnector("IntranetProcessor", intranetProcessor, config.getHttpAcceptPort(), config);
        testHttpIntranetAcceptProcess.start();

        HttpProcessor httpProcessor = new HttpProcessor(config);
        ServerConnector testHttpIntranetServiceProcess = new ServerConnector("HttpProcessor", httpProcessor, config.getHttpPort(), config);
        testHttpIntranetServiceProcess.start();
    }
}
