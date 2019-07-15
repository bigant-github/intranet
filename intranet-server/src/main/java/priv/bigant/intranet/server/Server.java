package priv.bigant.intranet.server;

import priv.bigant.intrance.common.BigAnt;
import priv.bigant.intrance.common.Lifecycle;

public interface Server extends Lifecycle, BigAnt {

    void addService(Service service);

    void removeService(String name);
}
