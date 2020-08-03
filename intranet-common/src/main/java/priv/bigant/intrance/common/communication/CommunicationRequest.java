package priv.bigant.intrance.common.communication;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;


/**
 * 交换器 请求发送
 */
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

    public static CommunicationRequest createCommunicationRequest(CommunicationP communicationP) {
        CommunicationRequest communicationRequest = new CommunicationRequest();
        communicationRequest.add(communicationP);
        return communicationRequest;
    }

    public static CommunicationRequest createCommunicationRequest(byte[] bytes) {
        return createCommunicationRequest(new String(bytes, StandardCharsets.UTF_8));
    }

    public static CommunicationRequest createCommunicationRequest(String s) {
        if (StringUtils.isEmpty(s))
            return null;
        JSONObject jsonObject = JSON.parseObject(s);
        return new CommunicationRequest(jsonObject);
    }


    @Override
    public String toString() {
        return jsonObject.toString();
    }


    public static class CommunicationRequestP implements CommunicationP {

        private CommunicationEnum type;

        //请求或相应 1请求类型 2相应类型
        //private Integer fromType;

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

    public static class CommunicationRequestTest extends CommunicationRequestP {

        public CommunicationRequestTest() {
            super(CommunicationEnum.TEST);
        }

    }

    public static class CommunicationRequestHttpReturn extends CommunicationRequestP {

        public static enum Status {
            SUCCESS,//成功
            DOMAIN_OCCUPIED//域名已被占用
        }

        private Status status;

        public CommunicationRequestHttpReturn() {
            super(CommunicationEnum.HTTP_RETURN);
        }

        public CommunicationRequestHttpReturn(Status status) {
            super(CommunicationEnum.HTTP_RETURN);
            this.status = status;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }
    }
}
