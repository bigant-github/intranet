package priv.bigant.intrance.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;


public class CommunicationRequest extends CommunicationReturn {


    public CommunicationRequest(CommunicationReturnEnum type) {
        jsonObject.put("type", type);
    }

    private CommunicationRequest() {
    }

    private CommunicationRequest(JSONObject jsonObject) {
        super.jsonObject = jsonObject;
    }

    /*public static CommunicationRequest toCommunicationRequest(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8).replaceAll("\r", "").replace("\n", "");
        String[] split = s.split(CodeEnum.SPLIT);
        return new CommunicationRequest(split[0], split[1]);
    }

    public static CommunicationRequest getCommunicationRequest(String type, String value) {
        return new CommunicationRequest(type, value);
    }*/

    public static CommunicationRequest createCommunicationRequest(CommunicationP communicationP) throws Exception {
        CommunicationRequest communicationRequest = new CommunicationRequest();
        communicationRequest.add(communicationP);
        return communicationRequest;
    }

    public static CommunicationRequest createCommunicationRequest(byte[] bytes) throws Exception {
        String s = new String(bytes, StandardCharsets.UTF_8);
        JSONObject jsonObject = JSON.parseObject(s);
        return new CommunicationRequest(jsonObject);
    }

    public static class CommunicationRequestP implements CommunicationP {
        private CommunicationReturnEnum type;

        public CommunicationRequestP(CommunicationReturnEnum type) {
            this.type = type;
        }

        public CommunicationReturnEnum getType() {
            return type;
        }

        public void setType(CommunicationReturnEnum type) {
            this.type = type;
        }
    }

    public static class CommunicationRequestHttpFirst extends CommunicationRequestP {

        public CommunicationRequestHttpFirst(CommunicationReturnEnum type) {
            super(type);
        }

        private String host;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }

}
