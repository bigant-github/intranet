package priv.bigant.intranet.server;

import priv.bigant.intrance.common.BigAnt;
import priv.bigant.intrance.common.ServerConnector;
import priv.bigant.intrance.common.Lifecycle;

public interface Service extends Lifecycle, BigAnt {
    // ------------------------------------------------------------- Properties

    /**
     * @return the <code>Engine</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     */
    /*public Engine getContainer();

     *//**
     * Set the <code>Engine</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     *
     * @param engine The new Engine
     *//*
    public void setContainer(Engine engine);*/

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


    /**
     * @return the domain under which this container will be / has been registered.
     */
    /*public String getDomain();*/


    // --------------------------------------------------------- Public Methods

    /**
     * Add a new ServerConnector to the set of defined Connectors, and associate it with this Service's Container.
     *
     * @param connector The ServerConnector to be added
     */
    public void addConnector(ServerConnector connector);

    /**
     * Find and return the set of Connectors associated with this Service.
     *
     * @return the set of associated Connectors
     */
/*
    public ServerConnector[] findConnectors();
*/

    /**
     * Remove the specified ServerConnector from the set associated from this Service.  The removed ServerConnector will also be
     * disassociated from our Container.
     *
     * @param connector The ServerConnector to be removed
     */
    public void removeConnector(ServerConnector connector);

}
