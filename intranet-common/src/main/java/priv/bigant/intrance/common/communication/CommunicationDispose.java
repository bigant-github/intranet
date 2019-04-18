package priv.bigant.intrance.common.communication;

public abstract class CommunicationDispose {
    /**
     * http类型
     */
    protected abstract void http();

    protected abstract void httpAdd(CommunicationRequest communicationRequest, Communication communication);

    public void invoke(CommunicationRequest communicationRequest, Communication communication) {
        switch (communicationRequest.getType()) {
            case HTTP:
                http();
            case HTTP_ADD:
                httpAdd(communicationRequest, communication);
            default:
                ;
        }


    }
}
