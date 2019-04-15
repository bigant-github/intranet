package priv.bigant.intranet.client;

import priv.bigant.intrance.common.Communication;
import priv.bigant.intrance.common.CommunicationDispose;
import priv.bigant.intrance.common.CommunicationRequest;
import priv.bigant.intrance.common.thread.Config;

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
