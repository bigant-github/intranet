package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HttpIntranetServer implements Server {

    private Map<String, Service> services = new HashMap<>();
    private static final String name = "HttpIntranetServer";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpIntranetServer.class);


    @Override
    public String getName() {
        return name;
    }

    public void start() {
        services.values().forEach(Service::start);
    }
}
