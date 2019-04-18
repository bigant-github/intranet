package priv.bigant.intranet.client;

import priv.bigant.intrance.common.communication.Communication;
import priv.bigant.intrance.common.communication.CommunicationDispose;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intrance.common.Config;

public class CommunicationDisposeClient extends CommunicationDispose {

    private ClientConfig clientConfig;

    public CommunicationDisposeClient() {
        clientConfig = (ClientConfig) Config.getConfig();
    }

    @Override
    public void http() {

    }

    @Override
    public void httpAdd(CommunicationRequest communicationRequest, Communication communication) {
        //clientConfig.get
    }
}
