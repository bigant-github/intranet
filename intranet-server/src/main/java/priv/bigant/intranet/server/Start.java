package priv.bigant.intranet.server;


import org.apache.commons.lang3.StringUtils;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.ServerConnector;
import priv.bigant.intranet.server.process.CommunicationProcess;
import priv.bigant.intranet.server.process.HttpIntranetAcceptProcess;
import priv.bigant.intranet.server.process.HttpIntranetServiceProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
