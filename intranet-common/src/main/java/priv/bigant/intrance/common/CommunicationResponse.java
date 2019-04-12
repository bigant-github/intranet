package priv.bigant.intrance.common;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class CommunicationResponse extends CommunicationReturn {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationResponse.class);

    public CommunicationResponse(CodeEnum code) {
        jsonObject.put("code", code);
    }

    private CommunicationResponse(JSONObject jsonObject) {
        super.jsonObject = jsonObject;
    }

    public boolean isSuccess() {
        CommunicationResponseP communicationResponseP = jsonObject.toJavaObject(CommunicationResponseP.class);
        return communicationResponseP.code.equals(CodeEnum.SUCCESS);
    }

    public static CommunicationResponse createCommunicationResponse(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);
        JSONObject jsonObject = JSON.parseObject(s);
        return new CommunicationResponse(jsonObject);
    }

    public <T> T toJavaObject(Class<T> clazz) {
        return jsonObject.toJavaObject(clazz);
    }


    public static class CommunicationResponseP implements CommunicationP {
        private CodeEnum code;

        public CommunicationResponseP(CodeEnum code) {
            this.code = code;
        }

        public boolean isSuccess() {
            return code.equals(CodeEnum.SUCCESS);
        }

        public CodeEnum getCode() {
            return code;
        }

        public void setCode(CodeEnum code) {
            this.code = code;
        }
    }

    public static class CommunicationResponseHttpFirst extends CommunicationResponseP {

        private String msg;

        public CommunicationResponseHttpFirst(CodeEnum code) {
            super(code);
        }


    }
}