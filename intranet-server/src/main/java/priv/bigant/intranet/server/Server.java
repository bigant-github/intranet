package priv.bigant.intranet.server;

import java.util.Set;

public interface Server extends Lifecycle, BigAnt {

    void addService(Service service);

    void removeService(String name);
}
