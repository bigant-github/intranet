package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HttpIntranetServer extends LifecycleMBeanBase implements Server {

    private Map<String, Service> services = new HashMap<>();
    private static final String name = "HttpIntranetServer";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpIntranetServer.class);

    @Override
    public void addService(Service service) {
        services.put(service.getName(), service);
    }

    @Override
    public void removeService(String name) {
        services.remove(name);
    }

    @Override
    protected String getDomainInternal() {
        return null;
    }

    @Override
    protected String getObjectNameKeyProperties() {
        return "type=server";
    }

    @Override
    protected void startInternal() throws LifecycleException {
        services.values().forEach(service -> {
            try {
                service.start();
            } catch (LifecycleException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void stopInternal() throws LifecycleException {

    }

    @Override
    public String getName() {
        return name;
    }
}
