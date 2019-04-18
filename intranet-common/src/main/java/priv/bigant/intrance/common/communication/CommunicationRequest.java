package priv.bigant.intrance.common.communication;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;


public class CommunicationRequest extends CommunicationReturn {


    public CommunicationRequest(CommunicationEnum type) {
        jsonObject.put("type", type);
    }

    private CommunicationRequest() {
    }

    public CommunicationEnum getType() {
        Object type = jsonObject.get("type");
        return CommunicationEnum.valueOf(type.toString());
    }

    private CommunicationRequest(JSONObject jsonObject) {
        super.jsonObject = jsonObject;
    }

    public static CommunicationRequest createCommunicationRequest(CommunicationP communicationP) throws Exception {
        CommunicationRequest communicationRequest = new CommunicationRequest();
        communicationRequest.add(communicationP);
        return communicationRequest;
    }

    public static CommunicationRequest createCommunicationRequest(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);
        JSONObject jsonObject = JSON.parseObject(s);
        return new CommunicationRequest(jsonObject);
    }

    public static class CommunicationRequestP implements CommunicationP {
        private CommunicationEnum type;

        public CommunicationRequestP(CommunicationEnum type) {
            this.type = type;
        }

        public CommunicationEnum getType() {
            return type;
        }

        public void setType(CommunicationEnum type) {
            this.type = type;
        }
    }

    public static class CommunicationRequestHttpFirst extends CommunicationRequestP {

        public CommunicationRequestHttpFirst(CommunicationEnum type) {
            super(type);
        }

        public CommunicationRequestHttpFirst() {
            super(CommunicationEnum.HTTP);
        }

        private String host;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }

    public static class CommunicationRequestHttpAdd extends CommunicationRequestP {

        public CommunicationRequestHttpAdd() {
            super(CommunicationEnum.HTTP_ADD);
        }

        public CommunicationRequestHttpAdd(String id) {
            super(CommunicationEnum.HTTP_ADD);
            this.id = id;
        }

        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
