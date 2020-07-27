package priv.bigant.intranet.server;


import java.util.HashMap;
import java.util.Map;

public class HttpIntranetServer implements Server {

    private Map<String, Service> services = new HashMap<>();
    private static final String name = "HttpIntranetServer";


    @Override
    public String getName() {
        return name;
    }

    public void start() {
        services.values().forEach(Service::start);
    }
}
