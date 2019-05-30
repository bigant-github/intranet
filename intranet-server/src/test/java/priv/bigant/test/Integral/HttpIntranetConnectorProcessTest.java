package priv.bigant.test.Integral;

import priv.bigant.intrance.common.LifecycleException;
import priv.bigant.intranet.server.*;

public class HttpIntranetConnectorProcessTest {

    public static void main(String[] args) throws LifecycleException {
        ServerConfig config = (ServerConfig) ServerConfig.getConfig();
        HttpIntranetConnectorProcess httpIntranetConnectorProcess = new HttpIntranetConnectorProcess();
        Connector testHttpIntranetConnectorProcess = new Connector("testHttpIntranetConnectorProcess", httpIntranetConnectorProcess, config.getIntranetPort());
        testHttpIntranetConnectorProcess.start();
        HttpIntranetAcceptProcess httpIntranetAcceptProcess = new HttpIntranetAcceptProcess();
        Connector testHttpIntranetAcceptProcess = new Connector("testHttpIntranetAcceptProcess", httpIntranetAcceptProcess, config.getHttpAcceptPort());
        testHttpIntranetAcceptProcess.start();
        HttpIntranetServiceProcess httpIntranetServiceProcess = new HttpIntranetServiceProcess();
        Connector testHttpIntranetServiceProcess = new Connector("testHttpIntranetServiceProcess", httpIntranetServiceProcess, config.getHttpPort());
        testHttpIntranetServiceProcess.start();
    }
}
