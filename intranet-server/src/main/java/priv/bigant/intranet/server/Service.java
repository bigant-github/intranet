package priv.bigant.intranet.server;

import priv.bigant.intrance.common.BigAnt;
import priv.bigant.intrance.common.ServerConnector;
import priv.bigant.intrance.common.Lifecycle;

public interface Service extends Lifecycle, BigAnt {

    /**
     * @return the <code>Server</code> with which we are associated (if any).
     */
    public Server getServer();

    /**
     * Set the <code>Server</code> with which we are associated (if any).
     *
     * @param server The server that owns this Service
     */
    public void setServer(Server server);


}
