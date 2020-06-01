package priv.bigant.intranet.server;

import priv.bigant.intrance.common.ServerConnector;

import java.util.HashMap;
import java.util.Map;

public class HttpIntranetService implements Service {

    private static final String NAME = "HttpIntranetService";
    private Server server;
    private Map<String, ServerConnector> connectors = new HashMap();


    public String getName() {
        return NAME;
    }

    @Override
    public void start() {
        connectors.values().forEach(ServerConnector::start);
    }
}
