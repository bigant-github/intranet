package priv.bigant.intrance.common.communication;

public abstract class CommunicationDispose {
    public void invoke(CommunicationRequest communicationRequest, Communication communication) {
        switch (communicationRequest.getType()) {
            case TEST:
                test(communicationRequest, communication);
                break;
            case HTTP:
                http(communicationRequest, communication);
                break;
            case HTTP_ADD:
                httpAdd(communicationRequest, communication);
                break;
            case HTTP_RETURN:
                httpReturn(communicationRequest, communication);
                break;
            default:
                ;
        }


    }

    protected abstract void httpReturn(CommunicationRequest communicationRequest, Communication communication);

    /**
     * 发送测试数据
     *
     * @param communicationRequest
     * @param communication
     */
    protected abstract void test(CommunicationRequest communicationRequest, Communication communication);

    /**
     * http类型
     *
     * @param communicationRequest
     * @param communication
     */
    protected abstract void http(CommunicationRequest communicationRequest, Communication communication);

    /**
     * 请求添加http接受嵌套字
     *
     * @param communicationRequest
     * @param communication
     */
    protected abstract void httpAdd(CommunicationRequest communicationRequest, Communication communication);
}
